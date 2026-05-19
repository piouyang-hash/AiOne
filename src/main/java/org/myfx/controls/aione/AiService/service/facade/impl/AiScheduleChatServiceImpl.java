package org.myfx.controls.aione.AiService.service.facade.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.AiService.common.ai_chat_db.ChatRoleEnum;
import org.myfx.controls.aione.AiService.dto.AiInternalChatDTO;
import org.myfx.controls.aione.AiService.dto.ChatMessageCombineDTO;
import org.myfx.controls.aione.AiService.service.base.ai_chat_db.AiChatMessageService;
import org.myfx.controls.aione.AiService.service.facade.AiScheduleChatService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 内部AI对话业务实现类（调度/定时任务专属）
 * 包含：AI回复生成、事务管控、消息入库、幂等性校验等核心逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiScheduleChatServiceImpl implements AiScheduleChatService {

    //private final AiService aiService;
    private final AiChatMessageService aiChatMessageService;

    /**
     * 核心逻辑：处理调度/定时任务触发的AI主动对话（事务管控+幂等性）
     */
    @Override
    @Transactional // 事务管控：要么都成功要么回滚
    public String triggerAiActiveMessage(AiInternalChatDTO aiInternalChatDTO) {
        // 1. 参数校验（核心参数非空，替代控制器的@Valid）
        validateParams(aiInternalChatDTO);

//        // 2. 调用AI服务生成回复（核心逻辑：结合用户记忆生成AI主动消息）
//        log.info("开始处理调度触发的AI主动对话，目标用户ID：{}，会话ID：{}",
//                aiInternalChatDTO.getUserId(), aiInternalChatDTO.getSessionId());
//        String aiReplyContent = aiService.triggerSystemInitiatedAiChat(
//                aiInternalChatDTO.getUserId(),
//                aiInternalChatDTO.getSessionId(),
//                aiInternalChatDTO.getMessage());
//
//        // 3. 构建消息合并DTO，处理角色逻辑（伪装为用户侧消息）
//        ChatMessageCombineDTO combineDTO = buildChatMessageCombineDTO(aiInternalChatDTO, aiReplyContent);
//
//        // 4. 处理聊天消息入库（事务内执行，包含幂等性校验）
//        aiChatMessageService.handleChatMessage(combineDTO, aiInternalChatDTO.getUserId());
//
//        log.info("AI主动消息处理完成，目标用户ID：{}",
//                aiInternalChatDTO.getUserId());
//        return aiReplyContent;
        return "你好";
    }

    /**
     * 参数校验（内部调用也要做，避免空指针）
     */
    private void validateParams(AiInternalChatDTO aiInternalChatDTO) {
        if (aiInternalChatDTO.getUserId() == null) {
            throw new IllegalArgumentException("内部调用AI对话：用户ID（userId）不能为空");
        }
        if (aiInternalChatDTO.getSessionId() == null) {
            throw new IllegalArgumentException("内部调用AI对话：会话ID（sessionId）不能为空");
        }
        if (aiInternalChatDTO.getMessage() == null || aiInternalChatDTO.getMessage().isBlank()) {
            throw new IllegalArgumentException("内部调用AI对话：系统预设消息（message）不能为空");
        }
    }

    /**
     * 构建聊天消息合并DTO（封装角色逻辑）
     */
    private ChatMessageCombineDTO buildChatMessageCombineDTO(AiInternalChatDTO aiInternalChatDTO, String aiReplyContent) {
        ChatMessageCombineDTO combineDTO = new ChatMessageCombineDTO();
        combineDTO.setSessionId(aiInternalChatDTO.getSessionId());
        combineDTO.setUserMessage(aiInternalChatDTO.getMessage());
        combineDTO.setRole(ChatRoleEnum.SPRINGBOOT); // 伪装角色，营造AI主动发消息的错觉
        combineDTO.setAiReplyContent(aiReplyContent);
        return combineDTO;
    }
}