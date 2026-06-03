package org.myfx.controls.aione.AiService.Demo.telegramDemo;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Telegram 用户消息接收事件
 * 严格对齐：UserOfflineTooLongEvent 格式
 */
@Getter
public class TelegramMessageReceivedEvent extends ApplicationEvent {

    // 唯一参数：用户消息
    private final String userMessage;

    /**
     * 构造方法（和你的事件完全一致）
     * @param source 事件源（固定传 this）
     * @param userMessage 用户发送的消息
     */
    public TelegramMessageReceivedEvent(Object source, String userMessage) {
        super(source);
        this.userMessage = userMessage;
    }

}