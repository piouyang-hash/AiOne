package org.myfx.controls.aione.AiService.service.schedule.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.AiService.dto.AiInternalChatDTO;
import org.myfx.controls.aione.AiService.entity.ai_chat_db.AiChatSession;
import org.myfx.controls.aione.AiService.service.base.ai_chat_db.AiChatSessionService;
import org.myfx.controls.aione.AiService.service.facade.AiScheduleChatService;
import org.myfx.controls.aione.AiService.service.schedule.AiActiveChatAggregateService;
import org.myfx.controls.aione.AiService.service.upper.AiActiveMessageService;
import org.springframework.stereotype.Service;

/**
 * AI主动消息聚合调度服务实现类（落地定时任务调度的AI主动发消息完整流程）
 */
@Slf4j
@Service
@RequiredArgsConstructor // 构造器注入依赖，替代@Autowired，更优雅
public class AiActiveChatAggregateServiceImpl implements AiActiveChatAggregateService {

    // 注入依赖服务（按业务流程顺序）
    private final AiChatSessionService aiChatSessionService;
    private final AiActiveMessageService aiActiveMessageService;
    private final AiScheduleChatService aiScheduleChatService;

    @Override
    public String executeAiActiveMessageDispatch(Integer userId) {
        // 步骤1：入参非空校验（任务调度场景必须保证用户ID有效）
        if (userId == null) {
            throw new IllegalArgumentException("执行AI主动消息调度失败：目标用户ID（userId）不能为空");
        }

        // 步骤2：获取用户当前激活会话，无则创建新会话
        AiChatSession activeSession = aiChatSessionService.getUserCurrentActiveSession(userId);
        Long sessionId;
        if (activeSession == null) {
            // 仅保留核心逻辑，删除冗余日志
            sessionId = aiChatSessionService.initChatSessionForUserId(userId);
        } else {
            sessionId = activeSession.getSessionId();
        }

        // 步骤3：调用熟悉度检查，生成系统指令消息（保留该日志）
        String systemMessage = aiActiveMessageService.checkAiFamiliarity(userId);
        log.info("[AI主动消息调度] 用户{}会话{} - 系统指令消息：{}", userId, sessionId, systemMessage);

        // 步骤4：组装内部聊天DTO（传递核心参数）
        AiInternalChatDTO aiInternalChatDTO = new AiInternalChatDTO();
        aiInternalChatDTO.setUserId(userId);
        aiInternalChatDTO.setSessionId(sessionId);
        aiInternalChatDTO.setMessage(systemMessage);

        // 步骤5：触发AI主动消息，获取AI回复（已存入数据库）
        String aiReply = aiScheduleChatService.triggerAiActiveMessage(aiInternalChatDTO);

        boolean updateSuccess = aiChatSessionService.updateLastMessageByUserIdAndSessionId(
                userId,        // 用户ID（Integer类型，匹配方法参数）
                sessionId,     // 会话ID
                aiReply        // AI回复内容（存入预览字段）
        );

        // 步骤6：打印+记录最终回复日志（保留该输出）
        String finalReplyLog = String.format("【AI主动消息最终回复】用户ID：%s，会话ID：%s，AI回复：%s", userId, sessionId, aiReply);
        System.out.println(finalReplyLog);
        log.info(finalReplyLog);

        return aiReply;
    }
}