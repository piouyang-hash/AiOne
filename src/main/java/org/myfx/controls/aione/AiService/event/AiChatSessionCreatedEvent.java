package org.myfx.controls.aione.AiService.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 会话创建成功事件（携带userId和sessionId）
 */
@Getter
public  class AiChatSessionCreatedEvent extends ApplicationEvent {
    // getter
    private final Integer userId; // 用户ID（Integer类型）
    private final Long sessionId; // 会话ID

    public AiChatSessionCreatedEvent(Object source, Integer userId, Long sessionId) {
        super(source);
        this.userId = userId;
        this.sessionId = sessionId;
    }

}