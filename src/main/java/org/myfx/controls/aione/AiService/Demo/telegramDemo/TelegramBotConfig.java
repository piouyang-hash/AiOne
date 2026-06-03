package org.myfx.controls.aione.AiService.Demo.telegramDemo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
public class TelegramBotConfig {
    private static final Logger log = LoggerFactory.getLogger(TelegramBotConfig.class);

    @Bean
    public TelegramBotsApi telegramBotsApi(MyTelegramBot myTelegramBot) {
        log.info("初始化 TelegramBotsApi...");
        log.info("代理配置: {}:{} 类型 {}",
                myTelegramBot.getOptions().getProxyHost(),
                myTelegramBot.getOptions().getProxyPort(),
                myTelegramBot.getOptions().getProxyType());

        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            log.info("正在注册机器人: {} ...", myTelegramBot.getBotUsername());
            botsApi.registerBot(myTelegramBot);
            log.info("机器人注册成功！");
            return botsApi;
        } catch (TelegramApiException e) {
            log.error("机器人注册失败！", e);
            // 打印详细错误，帮助诊断
            if (e.getMessage().contains("Connection timed out")) {
                log.error("网络超时，请确认：");
                log.error("1. v2rayN 是否已启动？");
                log.error("2. 代理端口 {} 是否正确？", myTelegramBot.getOptions().getProxyPort());
                log.error("3. 代理类型是否设置为 {}？", myTelegramBot.getOptions().getProxyType());
                log.error("4. 防火墙是否允许 Java 进程通过代理访问外网？");
            }
            return null;
        }
    }
}