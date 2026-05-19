package org.myfx.controls.aione.AiService.aiClient.advisor;

import lombok.RequiredArgsConstructor;
import org.myfx.controls.aione.AiService.aiClient.SpringAiPromptConvertUtil;
import org.myfx.controls.aione.AiService.engineering.summary_append_only.ConversationHistoryQueryService;
import org.myfx.controls.aione.AiService.entity.ai_chat_db.AiChatMessage;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * Append-Only策略Advisor
 * 核心能力：全量保留历史对话（不丢弃、无滑动窗口），稳定触发缓存命中，控制token持续增长但不重复计费
 * order（越小执行最早）：1
 */
@Component
@RequiredArgsConstructor
public class AppendOnlyAdvisor implements BaseAdvisor {

    // 注入会话历史查询服务（构造器注入，通过@RequiredArgsConstructor实现）
    private final ConversationHistoryQueryService conversationHistoryQueryService;

    /**
     * 前置逻辑：加载全量历史对话，填充到提示词模板中（无摘要、无截断）
     */
    @Override
    public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
        // 1. 从上下文获取基础参数（userId、sessionId）
        var adviseContext = chatClientRequest.context();
        Integer userId = (Integer) adviseContext.get("userId");
        Long sessionId = (Long) adviseContext.get("sessionId");

        // 2. 提取三层记忆提示词模板（复用原模板结构，仅移除摘要逻辑）
        PromptTemplate threeLayerMemoryPromptTemplate = (PromptTemplate) adviseContext.get("promptTemplate");

        // 3. 核心业务逻辑：加载全量历史对话，填充模板变量（无截断、无总结）
        if (threeLayerMemoryPromptTemplate != null && userId != null && sessionId != null) {
            // 3.1 调用全量历史查询方法（append-only核心：查询全部历史，不丢弃任何对话）
            List<AiChatMessage> aiChatMessageList = conversationHistoryQueryService.queryAllConversationHistory(userId, sessionId);

            // 3.2 处理历史对话空值：空列表/Null则填空字符串，否则转换为Message列表
            Object historyMessageValue;
            if (aiChatMessageList == null || aiChatMessageList.isEmpty()) {
                historyMessageValue = ""; // 空列表/Null时填充空字符串，避免模板渲染异常
            } else {
                // 会直接过滤到springboot消息
                historyMessageValue = SpringAiPromptConvertUtil.convertToMessageList(aiChatMessageList);
            }

            // 3.3 填充历史对话变量（仅填充historyMessagesFormatted，无historySummary）
            threeLayerMemoryPromptTemplate.add("historyMessagesFormatted", historyMessageValue);
        }

        // 4. 返回原请求（仅做变量赋值，不修改请求核心内容）
        return chatClientRequest;
    }

    /**
     * 后置逻辑：append-only策略无需后置总结/清理操作，直接返回原响应
     */
    @Override
    public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
        // append-only核心：历史对话全量保留，无需总结、无需清理过期对话，直接返回原响应
        return chatClientResponse;
    }

    /**
     * 流式场景：手动调用before逻辑，先进行全量检索
     */
    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest request, StreamAdvisorChain chain) {
        // 核心：手动调用before方法，执行人设填充逻辑
        ChatClientRequest processedRequest = before(request, chain);

        // 继续流式调用链，返回处理后的请求
        return chain.nextStream(processedRequest);
    }

    /**
     * 自定义Advisor名称（方便日志/调试识别，贴合append-only策略）
     */
    @Override
    public String getName() {
        return "AppendOnlyAdvisor";
    }

    /**
     * 执行顺序（与SummarySlidingWindowAdvisor保持一致，数值=1）
     */
    @Override
    public int getOrder() {
        return 1;
    }
}