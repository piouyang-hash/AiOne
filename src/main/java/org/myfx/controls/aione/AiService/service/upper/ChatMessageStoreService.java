package org.myfx.controls.aione.AiService.service.upper;

import jakarta.validation.constraints.NotNull;
import org.myfx.controls.aione.AiService.aiClient.advisor.dto.ChatInformationDTO;
import org.myfx.controls.aione.AiService.dto.ChatMessageCombineDTO;

/**
 * 上层聊天后置处理服务接口
 * 封装AI聊天完成后的各类后置业务逻辑（如会话日志记录、用户画像更新、消息推送等）
 */
public interface ChatMessageStoreService {

    /**
     * 聊天后置处理核心方法（DTO精简版）
     * @param combineDTO 聊天消息合并DTO（核心入参，包含所有必要字段）
     *                   必填字段：userId（0=匿名，>0=登录用户）、sessionId、initiateRole
     *                   可选字段：userMsg、aiResponse、splitContentJson
     * @return Long handleChatMessage的返回值（关联的parentId）
     */
    Long chatPostProcess(@NotNull ChatMessageCombineDTO combineDTO);

    /**
     * 前置存储：保存用户消息 + 创建AI流式占位符
     * @param dto 入参DTO
     * @return 赋值后的DTO
     */
    ChatInformationDTO saveUserMessageAndCreateAiPlaceholder(ChatInformationDTO dto);

    /**
     * 后置存储：更新AI最终消息 + 更新会话最后消息
     * @param dto 入参DTO
     * @return 处理后的DTO
     */
    ChatInformationDTO updateAiMessageAndSession(ChatInformationDTO dto);
}
