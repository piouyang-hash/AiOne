package org.myfx.controls.aione.AiService.engineering.summary_append_only.impl;

import lombok.RequiredArgsConstructor;
import org.myfx.controls.aione.AiService.engineering.summary_append_only.ConversationHistoryQueryService;
import org.myfx.controls.aione.AiService.entity.ai_chat_db.AiChatMessage;
import org.myfx.controls.aione.AiService.service.base.ai_chat_db.AiChatMessageService;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * 会话历史查询服务实现类
 * 适配append-only模式的会话历史查询（不丢弃历史、持续追加、触发总结）
 */
@Service // Spring注解，将类注册为服务Bean
@RequiredArgsConstructor
public class ConversationHistoryQueryServiceImpl implements ConversationHistoryQueryService {

    // 注入AiChatMessageService依赖（需确保该服务已被Spring管理）
    private final AiChatMessageService aiChatMessageService;

    /**
     * 实现：查询会话的全部历史对话
     * 核心逻辑：调用重载后的listChatMessagesBySessionId方法，保证参数精准性
     */
    @Override
    public List<AiChatMessage> queryAllConversationHistory(Integer userId, Long sessionId) {
        // 严格参数校验，避免空指针或无效查询
        if (userId == null) {
            throw new IllegalArgumentException("用户ID（userId）不能为空");
        }
        if (sessionId == null) {
            throw new IllegalArgumentException("会话ID（sessionId）不能为空");
        }
        // 调用重载方法，获取该用户+该会话的全部历史消息（正序）
        return aiChatMessageService.listChatMessagesBySessionId(userId, sessionId);
    }

}