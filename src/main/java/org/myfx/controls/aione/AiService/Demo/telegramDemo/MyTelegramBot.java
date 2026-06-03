package org.myfx.controls.aione.AiService.Demo.telegramDemo;

import jakarta.annotation.Resource;
import org.myfx.controls.aione.AiService.service.facade.ChatTaskService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MyTelegramBot extends TelegramLongPollingBot {

    private final String botToken;
    private final String botUsername;

    // 线程安全的chatId存储（支持多用户）
    private final Map<Long, String> userChatIds = new ConcurrentHashMap<>();

    // 仅注入 Spring 事件发布器（无任何业务依赖，无循环）
    @Resource
    private ApplicationEventPublisher eventPublisher;

    /**
     * 构造时注入所有必要配置，并创建已配置代理的 DefaultBotOptions
     */
    public MyTelegramBot(
            @Value("${telegram.bot.token}") String botToken,
            @Value("${telegram.bot.username}") String botUsername,
            @Value("${telegram.bot.proxy.host:127.0.0.1}") String proxyHost,
            @Value("${telegram.bot.proxy.port:10808}") int proxyPort,
            @Value("${telegram.bot.proxy.type:SOCKS5}") String proxyType) {
        super(createProxyOptions(proxyHost, proxyPort, proxyType));
        this.botToken = botToken;
        this.botUsername = botUsername;
    }

    /**
     * 创建并配置代理选项
     */
    private static DefaultBotOptions createProxyOptions(String host, int port, String type) {
        DefaultBotOptions options = new DefaultBotOptions();
        options.setProxyHost(host);
        options.setProxyPort(port);

        // 根据配置字符串设置代理类型
        if ("HTTP".equalsIgnoreCase(type)) {
            options.setProxyType(DefaultBotOptions.ProxyType.HTTP);
        } else {
            // 默认使用 SOCKS5
            options.setProxyType(DefaultBotOptions.ProxyType.SOCKS5);
        }

        // 如果 v2rayN 开启了代理认证，可在此设置（通常不需要）
        // options.setProxyUser("username");
        // options.setProxyPassword("password");

        return options;
    }

    @Override
    public String getBotToken() {
        return this.botToken;
    }

    @Override
    public String getBotUsername() {
        return this.botUsername;
    }

    @Override
    public void onUpdateReceived(Update update) {
        // 只处理文本消息
        if (update.hasMessage() && update.getMessage().hasText()) {
            // 获取用户信息
            String userMessage = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            String userName = update.getMessage().getFrom().getFirstName();

            // ====================== 打印日志 ======================
            System.out.println("======= 收到用户消息 =======");
            System.out.println("用户ChatID：" + chatId);
            System.out.println("用户名称：" + userName);
            System.out.println("发送内容：" + userMessage);
            System.out.println("===========================");

            // ====================== 🔥 核心：发布事件 ======================
            eventPublisher.publishEvent(new TelegramMessageReceivedEvent(this, userMessage));
        }
    }

    // 主动发送消息的方法（不变）
    public void sendMessageToUser(long chatId, String message) {
        SendMessage sendMessage = SendMessage.builder()
                .chatId(String.valueOf(chatId))
                .text(message)
                .build();
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            System.err.println("主动发送消息失败：" + e.getMessage());
        }
    }
}