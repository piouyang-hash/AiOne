package org.myfx.controls.aione.AiService.service.facade;

import org.myfx.controls.aione.AiService.dto.AiInternalChatDTO;

/**
 * 内部AI对话业务服务（调度服务/定时任务专属）
 * 核心场景：非用户手动触发，由调度/定时任务调用，实现AI主动给用户发消息
 */
public interface AiScheduleChatService {

    /**
     * 处理调度/定时任务触发的AI主动对话
     * @param aiInternalChatDTO 内部调用参数（用户ID+会话ID+系统预设消息）
     * @return AI生成的回复内容
     */
    String triggerAiActiveMessage(AiInternalChatDTO aiInternalChatDTO);
}