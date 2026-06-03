package org.myfx.controls.aione.AiService.Demo.telegramDemo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.AiService.aiClient.advisor.dto.ChatStreamContentMetaVO;
import org.myfx.controls.aione.AiService.aiClient.advisor.dto.ChatStreamMetaDTO;
import org.myfx.controls.aione.AiService.dto.ChatChunkDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.concurrent.CompletableFuture;

/**
 * Telegram AI 消息推送服务（单类实现，无接口，极简可用）
 * 兼容：首帧 ChatStreamMetaDTO + 普通帧 ChatStreamContentMetaVO
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramPushService {

    private final MyTelegramBot myTelegramBot;

    // 管理员固定 ChatID（配置文件读取）
    @Value("${telegram.bot.admin.chat-id}")
    private long adminChatId;

    // ===================== 最终版：同步方法，由上游统一做异步 =====================
    public void pushAiMessageToTelegram(ChatChunkDTO chunk) {
        // 过滤结束帧
        if (Boolean.TRUE.equals(chunk.getIsEnd()) || chunk.getContent() == null) {
            return;
        }

        try {
            // 统一解析：首帧(ChatStreamMetaDTO) + 普通帧(ChatStreamContentMetaVO)
            String aiText = extractRealAiContent(chunk);
            if (aiText != null && !aiText.isBlank()) {
                myTelegramBot.sendMessageToUser(adminChatId, aiText);
                log.info("✅ Telegram 推送成功：{}", aiText);
            }
        } catch (Exception e) {
            log.error("❌ Telegram 推送失败，已忽略", e);
        }
    }

    // ===================== 【核心修复】同时解析：首帧 + 普通帧 =====================
    private String extractRealAiContent(ChatChunkDTO chunk) {
        Object content = chunk.getContent();

        // 情况1：首帧 → ChatStreamMetaDTO
        if (content instanceof ChatStreamMetaDTO metaDTO) {
            return metaDTO.getAiReplyContent();
        }

        // 情况2：普通消息帧 → ChatStreamContentMetaVO
        if (content instanceof ChatStreamContentMetaVO metaVO) {
            return metaVO.getAiReplyContent();
        }

        // 其他类型（未知）→ 不推送
        return null;
    }
}