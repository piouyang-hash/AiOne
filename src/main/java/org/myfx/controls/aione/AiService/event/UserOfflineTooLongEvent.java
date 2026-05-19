package org.myfx.controls.aione.AiService.event;

import org.springframework.context.ApplicationEvent;
import lombok.Getter;

/**
 * 用户离线过久事件
 */
@Getter
public class UserOfflineTooLongEvent extends ApplicationEvent {

    private final Integer userId;

    // 构造方法（和你的事件格式完全一致）
    public UserOfflineTooLongEvent(Object source, Integer userId) {
        super(source);
        this.userId = userId;
    }

}