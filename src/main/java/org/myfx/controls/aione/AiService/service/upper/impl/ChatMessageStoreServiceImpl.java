package org.myfx.controls.aione.AiService.service.upper.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.AiService.aiClient.advisor.dto.ChatInformationDTO;
import org.myfx.controls.aione.AiService.common.ai_chat_db.ChatRoleEnum;
import org.myfx.controls.aione.AiService.dto.ChatMessageCombineDTO;
import org.myfx.controls.aione.AiService.service.base.ai_chat_db.AiChatMessageService;
import org.myfx.controls.aione.AiService.service.base.ai_chat_db.AiChatSessionService;
import org.myfx.controls.aione.AiService.service.upper.ChatMessageStoreService;
import org.myfx.controls.aione.AiService.utils.SplitContentUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;


/**
 * 上层聊天后置处理服务实现类
 * 封装聊天后的消息入库、会话最后消息更新等核心业务逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMessageStoreServiceImpl implements ChatMessageStoreService {

    // 注入聊天消息服务
    private final AiChatMessageService aiChatMessageService;

    // 注入聊天会话服务
    private final AiChatSessionService aiChatSessionService;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long chatPostProcess(ChatMessageCombineDTO combineDTO) {
        // 1. 核心参数校验（基于DTO字段，保持原校验规则）
        Assert.notNull(combineDTO, "聊天后置处理时，合并DTO（combineDTO）不能为空");
        Assert.notNull(combineDTO.getUserId(), "聊天后置处理时，用户ID（userId）不能为空");
        Assert.isTrue(combineDTO.getUserId() >= 0, "用户ID需为非负整数（0=匿名，>0=登录用户）");
        Assert.notNull(combineDTO.getSessionId(), "聊天后置处理时，会话ID（sessionId）不能为空");
        Assert.notNull(combineDTO.getRole(), "聊天后置处理时，发起对话的角色（initiateRole）不能为空");

        // 2. 直接使用入参DTO调用消息服务（无需重新构建DTO）
        Long parentId = aiChatMessageService.handleChatMessage(combineDTO);

        // 3. 调用会话服务更新最后一条消息（核心修改：优先使用lastSegment）
        String lastSegment = combineDTO.getLastSegment(); // 获取切分后的最后一段
        String aiResponse = combineDTO.getAiReplyContent();
        String userMsg = combineDTO.getUserMessage();

        // 优先级：lastSegment（切分最后一段）→ aiResponse（AI原始回复）→ userMsg（用户消息）→ 空字符串
        String lastMessageContent = lastSegment != null ? lastSegment :
                (aiResponse != null ? aiResponse :
                        (userMsg != null ? userMsg : ""));

        boolean updateSuccess = aiChatSessionService.updateLastMessageByUserIdAndSessionId(
                combineDTO.getUserId(),
                combineDTO.getSessionId(),
                lastMessageContent
        );

        // 处理日志打印的内容：最多显示12个字（仅打印截断，插入的消息内容不变）
        String showContent = lastMessageContent;
        if (showContent.length() > 12) {
            showContent = showContent.substring(0, 12) + "...";
        }

        // 打印日志（从DTO取值，补充发起角色）
        ChatRoleEnum initiateRole = combineDTO.getRole();
        Integer userId = combineDTO.getUserId();
        Long sessionId = combineDTO.getSessionId();
        if (updateSuccess) {
            log.info("用户[{}]会话{}（发起角色：{}）的最后一条消息更新成功，内容：{}",
                    userId, sessionId, initiateRole.name(), showContent);
        } else {
            log.warn("用户[{}]会话{}（发起角色：{}）的最后一条消息更新失败，内容：{}",
                    userId, sessionId, initiateRole.name(), showContent);
        }

        // 5. 打印指定日志：补充发起角色
        log.info("用户[{}]消息插入完毕（发起角色：{}），关联parentId是{}",
                userId, initiateRole.name(), parentId);

        // 6. 返回handleChatMessage的返回值（parentId）
        return parentId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChatInformationDTO saveUserMessageAndCreateAiPlaceholder(ChatInformationDTO dto) {
        // 1. 保存用户消息（内部自动赋值 parentMsgId）
        aiChatMessageService.addUserChatMessage(dto);

        // 2. 更新会话最后一条消息
        aiChatSessionService.updateLastMessageByUserIdAndSessionId(
                dto.getUserId(),
                dto.getSessionId(),
                dto.getUserMessage()
        );

        // 3. 创建AI流式占位符（内部自动赋值 aiMessageId）
        aiChatMessageService.createAiStreamPlaceholder(dto);

        // 4. 直接返回填充完ID的DTO
        return dto;
    }

    /**
     * 后置逻辑（事务）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChatInformationDTO updateAiMessageAndSession(ChatInformationDTO dto) {
        // 1. 更新AI流式最终内容 → 直接传DTO，清爽！
        aiChatMessageService.updateAiStreamFinalContent(dto);

        // 2. 更新会话最后一条消息（不变）
        aiChatSessionService.updateLastMessageByUserIdAndSessionId(
                dto.getUserId(),
                dto.getSessionId(),
                dto.getLastSegment()
        );

        // 3. 更新会话未读消息数（不变）
        Integer splitUnreadCount = SplitContentUtils.getSplitTotalCount(dto.getSplitContentJson());
        aiChatSessionService.increaseSessionUnreadCount(
                dto.getSessionId(),
                dto.getUserId(),
                1,
                splitUnreadCount
        );

        return dto;
    }

}
