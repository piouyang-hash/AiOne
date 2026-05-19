package org.myfx.controls.aione.AiService.engineering.summary_sliding_window;

import org.myfx.controls.aione.AiService.entity.ai_chat_db.AiChatMessage;

import java.util.List;

/**
 * 总结专用对话历史查询服务接口
 * 核心能力：查询会话的全部历史对话、查询需要总结的目标历史对话（滑动窗口预处理）
 */
public interface SummaryConversationHistoryQueryService {

    /**
     * 查询会话的全部历史对话
     * @param userId 用户ID
     * @param sessionId 会话ID
     * @return 该会话下的全部对话消息列表（正序：最早→最新）
     */
    List<AiChatMessage> queryAllConversationHistory(Integer userId, Long sessionId);

    /**
     * 查询需要总结的目标历史对话（滑动窗口预处理）
     * 预处理逻辑：获取指定数量消息并截取待总结的消息，返回符合总结条件的对话列表
     * @param userId 用户ID
     * @param sessionId 会话ID
     * @return 待总结的目标对话消息列表
     */
    List<AiChatMessage> querySummaryTargetConversationHistory(Integer userId, Long sessionId);

}