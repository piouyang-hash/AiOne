package org.myfx.controls.aione.AiService.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 物理删除会话事件（注册失败补偿专用）
 * 包含userId和sessionId两个核心参数
 */
@Getter
public class AiChatSessionPhysicalDeleteEvent extends ApplicationEvent {

    // Getter方法（供事件监听者获取参数）
    // 事件携带的两个参数
    private final Integer userId;
    private final Long sessionId;

    /**
     * 构造函数
     * @param source 事件源（通常传当前业务类实例）
     * @param userId 用户ID
     * @param sessionId 会话ID
     */
    public AiChatSessionPhysicalDeleteEvent(Object source, Integer userId, Long sessionId) {
        super(source);
        this.userId = userId;
        this.sessionId = sessionId;
    }

}