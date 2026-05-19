package org.myfx.controls.aione.ServiceCommon.event.eventDTO;

import org.springframework.context.ApplicationEvent;
import lombok.Getter;

@Getter
public class UserCanceledEvent extends ApplicationEvent {

    private final Integer userId; // 只传用户ID，和注册事件一致

    public UserCanceledEvent(Object source, Integer userId) {
        super(source);
        this.userId = userId;
    }
}

