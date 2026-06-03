package org.myfx.controls.aione.AiService.Demo.telegramDemo;

import lombok.RequiredArgsConstructor;
import org.myfx.controls.aione.AiService.service.facade.ChatTaskService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Telegram 消息事件监听器
 * 解耦：Bot 不依赖业务，业务监听 Bot 事件
 */
@Component
@RequiredArgsConstructor
public class TelegramMessageListener {

    // 这里注入你的业务服务（无循环依赖）
    private final ChatTaskService chatTaskService;

    /**
     * 监听 Telegram 消息事件
     */
    @EventListener
    public void handleTelegramMessage(TelegramMessageReceivedEvent event) {
        // 获取消息
        String userMessage = event.getUserMessage();
        // 调用业务方法
        chatTaskService.createAndSubmitAiChatTask(userMessage);
    }
}