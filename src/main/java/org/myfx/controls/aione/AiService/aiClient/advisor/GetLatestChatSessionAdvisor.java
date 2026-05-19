package org.myfx.controls.aione.AiService.aiClient.advisor;

import lombok.RequiredArgsConstructor;
import org.myfx.controls.aione.AiService.entity.ai_chat_db.AiChatSession;
import org.myfx.controls.aione.AiService.service.base.ai_chat_db.AiChatSessionService;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.stereotype.Component;

/**
 * 获取最新对话Advisor（中文名称）
 * 核心能力：提取用户ID，获取其最新活跃会话ID，存入上下文供后续逻辑使用
 * order（越小执行最早）：0
 */
@Component
@RequiredArgsConstructor
public class GetLatestChatSessionAdvisor implements BaseAdvisor {

    // 1. 注入会话服务（构造器注入）
    private final AiChatSessionService aiChatSessionService;

    /**
     * 前置逻辑：提取用户ID，获取最新活跃会话ID，存入上下文
     */
    @Override
    public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
        // 1. 提取上下文和用户ID
        var adviseContext = chatClientRequest.context();
        Integer userId = (Integer) adviseContext.get("userId");

        // 2. 参数校验：userId为空则直接返回（避免空指针）
        if (userId == null) {
           return chatClientRequest;
        }

        // 3. 调用方法获取用户最新活跃会话
        AiChatSession activeSession = aiChatSessionService.getUserCurrentActiveSession(userId);
        if (activeSession == null) {
            return chatClientRequest;
        }

        // 4. 将最新会话ID存入上下文（键：sessionId）
        Long latestSessionId = activeSession.getSessionId();
        adviseContext.put("sessionId", latestSessionId);

        // 5. 新增：存入主动消息标记（键isActiveMessage，值为true）
        adviseContext.put("isActiveMessage", true);

        // 6. 返回原请求（上下文已更新为最新会话ID + 主动消息标记）
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
        return "GetLatestChatSessionAdvisor";
    }

    /**
     * 切面第一个执行
     */
    @Override
    public int getOrder() {
        return 0;
    }
}