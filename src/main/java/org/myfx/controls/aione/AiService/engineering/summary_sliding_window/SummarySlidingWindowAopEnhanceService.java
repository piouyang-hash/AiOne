package org.myfx.controls.aione.AiService.engineering.summary_sliding_window;

import org.myfx.controls.aione.AiService.entity.ai_chat_db.AiChatMessage;

import java.util.List;

/**
 * 总结滑动窗口AOP增强服务接口
 * 核心能力：为AOP通知提供滑动窗口总结相关的前置/后置处理能力
 */
public interface SummarySlidingWindowAopEnhanceService {

    /**
     * AOP前置处理：预加载会话全部历史对话（用于后续AOP逻辑的前置校验/数据准备）
     * @param userId 用户ID
     * @param sessionId 会话ID
     * @return 该会话下的全部历史对话列表
     */
    List<AiChatMessage> preLoadConversationHistory(Integer userId, Long sessionId);

    /**
     * AOP后置处理：总结会话历史上下文（AOP返回后执行，自动完成总结+存储摘要）
     * @param userId 用户ID
     * @param sessionId 会话ID
     */
    void postSummaryConversationContext(Integer userId, Long sessionId);

    /**
     * AOP前置处理：获取最新的历史对话摘要（用于后续AOP逻辑的前置数据准备）
     * @param userId 用户ID
     * @param sessionId 会话ID
     * @return 格式化后的最新历史对话摘要字符串（<聊天历史摘要>：内容\n）
     */
    String preGetLatestHistorySummary(Integer userId, Long sessionId);

}