package org.myfx.controls.aione.AiService.aiClient.advisor;

import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.AiService.aiClient.SpringAiPromptConvertUtil;
import org.myfx.controls.aione.AiService.entity.ai_chat_db.AiChatMessage;
import org.myfx.controls.aione.AiService.service.base.ai_chat_db.AiChatMessageService;
import org.myfx.controls.aione.AiService.service.llm.SmallLlmBusinessService;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.List;

/**
 * 获得对话背景Advisor
 * 核心能力：提取用户ID/会话ID，获取指定轮数的历史对话，处理空值后存入上下文/打印日志
 * order（越小执行最早）：1
 */
@Component
@Slf4j
public class GetConversationContextAdvisor implements BaseAdvisor {

    // ========== 固定配置：选取3轮对话 ==========
    private final Integer CONVERSATION_ROUNDS = 3;

    // ========== 注入依赖 ==========
    @Autowired
    private AiChatMessageService aiChatMessageService;

    // 新增：注入小模型业务服务
    @Autowired
    private SmallLlmBusinessService smallLlmBusinessService;


    /**
     * 前置逻辑：获取对话背景（历史消息）+ 生成历史摘要 + 处理空值并打印
     */
    @Override
    public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
        // 1. 提取上下文、用户ID、会话ID
        var adviseContext = chatClientRequest.context();
        Integer userId = (Integer) adviseContext.get("userId");
        Long sessionId = (Long) adviseContext.get("sessionId");

        // 2. 基础参数校验（空值则跳过逻辑）
        if (userId == null) {
            log.warn("【获得对话背景Advisor】上下文未获取到userId，跳过历史对话查询");
            return chatClientRequest;
        }
        if (sessionId == null) {
            log.warn("【获得对话背景Advisor】用户[{}]上下文未获取到sessionId，跳过历史对话查询", userId);
            return chatClientRequest;
        }

        // 3. 计算查询条数：3轮对话 × 2（每轮包含用户+助手消息）
        Integer limitNum = CONVERSATION_ROUNDS * 2;
        log.info("【获得对话背景Advisor】用户[{}]会话[{}]开始查询历史对话 | 配置轮数：{}轮 | 查询条数：{}条",
                userId, sessionId, CONVERSATION_ROUNDS, limitNum);

        // 4. 调用消息服务获取最新历史对话
        List<AiChatMessage> aiChatMessageList = aiChatMessageService.listLatestChatMessages(userId, sessionId, limitNum);

        // 5. 处理历史对话空值：空列表/Null则填空字符串，否则转换为Message列表
        Object historyMessageValue;
        if (aiChatMessageList == null || aiChatMessageList.isEmpty()) {
            historyMessageValue = ""; // 空列表/Null时填充空字符串
            log.info("【获得对话背景Advisor】用户[{}]会话[{}]无历史对话，填充空字符串", userId, sessionId);
        } else {
            historyMessageValue = SpringAiPromptConvertUtil.convertToMessageList(aiChatMessageList);
            log.info("【获得对话背景Advisor】用户[{}]会话[{}]查询到{}条历史对话，已转换为Message列表",
                    userId, sessionId, aiChatMessageList.size());
        }

        // 6. 从上下文获取threeLayerMemoryPromptTemplate并打印（便于调试）
        PromptTemplate threeLayerMemoryPromptTemplate = (PromptTemplate) adviseContext.get("promptTemplate");
        if (threeLayerMemoryPromptTemplate == null) {
            log.warn("【获得对话背景Advisor】用户[{}]会话[{}]上下文未找到threeLayerMemoryPromptTemplate", userId, sessionId);
        } else {
            log.info("【获得对话背景Advisor】用户[{}]会话[{}]获取到threeLayerMemoryPromptTemplate | 模板内容预览：{}",
                    userId, sessionId, threeLayerMemoryPromptTemplate.getTemplate().substring(0, Math.min(100, threeLayerMemoryPromptTemplate.getTemplate().length())) + "...");
        }

        // ========== 新增：第7步 - 调用小模型生成历史对话摘要 ==========
        String conversationSummary = "";
        // 仅当历史消息是Message列表时，才调用摘要方法（空值时跳过）
        if (historyMessageValue instanceof List<?>) {
            try {
                List<Message> messageList = (List<Message>) historyMessageValue;
                if (!messageList.isEmpty()) {
                    // 调用小模型服务生成摘要
                    conversationSummary = smallLlmBusinessService.summarizeMessages(messageList);
                    log.info("【获得对话背景Advisor】用户[{}]会话[{}]历史对话摘要：{}", userId, sessionId, conversationSummary);
                }
            } catch (Exception e) {
                // 异常兜底：摘要生成失败不影响主流程
                log.error("【获得对话背景Advisor】用户[{}]会话[{}]生成历史对话摘要失败", userId, sessionId, e);
                conversationSummary = "";
            }
        }

        // ========== 核心修正：第8/9步 - 向PromptTemplate注入参数（而非直接存上下文） ==========
        // 8. 处理历史对话摘要注入
        if (threeLayerMemoryPromptTemplate != null) {
            threeLayerMemoryPromptTemplate.add("historyConversationSummary", conversationSummary);
            log.debug("【获得对话背景Advisor】用户[{}]会话[{}]已向模板注入历史对话摘要", userId, sessionId);
        } else {
            // 兜底：模板为空时仍存入上下文
            adviseContext.put("historyConversationSummary", conversationSummary);
            log.warn("【获得对话背景Advisor】用户[{}]会话[{}]无PromptTemplate，摘要存入上下文备用", userId, sessionId);
        }

        // 9. 处理原始历史消息注入（先转换为字符串，避免模板渲染异常）
        String historyMessageStr = historyMessageValue instanceof String ?
                (String) historyMessageValue : historyMessageValue.toString();
        if (threeLayerMemoryPromptTemplate != null) {
            threeLayerMemoryPromptTemplate.add("historyConversationContext", historyMessageStr);
            log.debug("【获得对话背景Advisor】用户[{}]会话[{}]已向模板注入原始历史消息", userId, sessionId);
        } else {
            // 兜底：模板为空时仍存入上下文
            adviseContext.put("historyConversationContext", historyMessageValue);
            log.warn("【获得对话背景Advisor】用户[{}]会话[{}]无PromptTemplate，原始消息存入上下文备用", userId, sessionId);
        }

        // 10. 返回原请求（上下文/模板已更新）
        return chatClientRequest;
    }

    /**
     * 后置逻辑：暂不实现
     */
    @Override
    public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
        return chatClientResponse;
    }

    /**
     * 自定义Advisor名称（英文，便于日志/调试识别）
     */
    @Override
    public String getName() {
        return "GetConversationContextAdvisor";
    }

    /**
     * 执行顺序：优先级低于「获取最新对话Advisor」（order=2）
     * 保证先获取最新会话ID，再查询该会话的历史消息
     */
    @Override
    public int getOrder() {
        return 1;
    }
}