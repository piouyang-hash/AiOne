package org.myfx.controls.aione.AiService.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import lombok.Data;

/**
 * AI等待过久事件
 */
@Getter
public class AiWaitTooLongEvent extends ApplicationEvent {

    private final Integer userId;

    // 构造方法（和你的事件格式完全一致）
    public AiWaitTooLongEvent(Object source, Integer userId) {
        super(source);
        this.userId = userId;
    }

}