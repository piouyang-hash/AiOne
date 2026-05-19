package org.myfx.controls.aione.AiService.aiClient.advisor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.AiService.aiClient.SpringAiPromptConvertUtil;
import org.myfx.controls.aione.AiService.engineering.sliding_window.SlidingWindowService;
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
 * 滑动窗口Advisor（仅前置逻辑）
 * 核心能力：基于滑动窗口机制加载最新历史对话，填充到提示词模板的{historyMessagesFormatted}变量
 * order：20（与原总结型滑动窗口Advisor一致）
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SlidingWindowAdvisor implements BaseAdvisor {

    // 注入滑动窗口业务服务（替代原总结型服务）
    private final SlidingWindowService slidingWindowService;

    // --- 1. 同步逻辑：仅保留前置加载历史对话 ---
    @Override
    public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
        return handleBeforeLogic(chatClientRequest); // 复用前置核心逻辑
    }

    // 后置逻辑：空实现（无任何操作）
    @Override
    public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
        return chatClientResponse; // 直接返回，不做任何处理
    }

    // --- 2. 流式逻辑：仅处理前置，无后置总结 ---
    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest request, StreamAdvisorChain chain) {
        // 1. Before 逻辑：加载历史对话并填充模板
        ChatClientRequest augmentedRequest = handleBeforeLogic(request);

        // 2. 仅执行链式调用，无任何后置逻辑（移除原总结相关的doOnTerminate/doOnCancel）
        return chain.nextStream(augmentedRequest);
    }

    // --- 3. 抽离的核心前置逻辑（仅加载历史对话，填充变量） ---
    private ChatClientRequest handleBeforeLogic(ChatClientRequest request) {
        // 1. 从上下文获取基础参数
        var adviseContext = request.context();
        Integer userId = (Integer) adviseContext.get("userId");
        Long sessionId = (Long) adviseContext.get("sessionId");

        // 2. 提取提示词模板
        PromptTemplate promptTemplate = (PromptTemplate) adviseContext.get("promptTemplate");

        // 3. 核心逻辑：加载滑动窗口内的历史对话，填充historyMessagesFormatted变量
        if (promptTemplate != null && userId != null && sessionId != null) {
            // 3.1 调用滑动窗口服务获取最新历史对话
            List<AiChatMessage> historyMessages = slidingWindowService.getSlidingWindowChatMessages(userId, sessionId);

            // 3.2 处理空值：空列表则填空字符串，否则转换为格式化的消息列表
            Object historyMessageValue = (historyMessages == null || historyMessages.isEmpty())
                    ? ""
                    : SpringAiPromptConvertUtil.convertToMessageList(historyMessages);

            // 3.3 填充到提示词模板的{historyMessagesFormatted}变量
            promptTemplate.add("historyMessagesFormatted", historyMessageValue);
            log.info("[SlidingWindowAdvisor] 滑动窗口历史对话填充完成 | 用户ID={}, 会话ID={}, 历史消息条数={}",
                    userId, sessionId, historyMessages == null ? 0 : historyMessages.size());
        } else {
            log.warn("[SlidingWindowAdvisor] 跳过历史对话填充：模板/用户ID/会话ID为空（userId={}, sessionId={}, 模板={}",
                    userId, sessionId, promptTemplate);
        }

        // 4. 返回处理后的请求（仅填充变量，不修改其他内容）
        return request;
    }

    /**
     * 自定义Advisor名称（方便日志/调试识别）
     */
    @Override
    public String getName() {
        return "SlidingWindowAdvisor";
    }

    /**
     * 执行顺序（与原总结型一致，确保加载历史的时机不变）
     */
    @Override
    public int getOrder() {
        return 20;
    }
}