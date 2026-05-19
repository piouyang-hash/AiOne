package org.myfx.controls.aione.AiService.engineering.summary_append_only;

import org.myfx.controls.aione.AiService.entity.ai_chat_db.AiChatMessage;

import java.util.List;

/**
 * 会话历史查询服务接口
 * 核心能力：查询会话全部历史对话、按会话ID（+用户ID）查询消息列表
 */
public interface ConversationHistoryQueryService {

    /**
     * 查询会话的全部历史对话
     * @param userId 用户ID
     * @param sessionId 会话ID
     * @return 该会话下的全部对话消息列表（正序：最早→最新）
     */
    List<AiChatMessage> queryAllConversationHistory(Integer userId, Long sessionId);

}