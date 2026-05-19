package org.myfx.controls.aione.AiService.utils;

import org.myfx.controls.aione.AiService.dto.ConversationIdDTO;
import org.springframework.util.Assert;

/**
 * 会话ID（conversationId）工具类
 * 提供：1.生成唯一conversationId 2.解析conversationId为DTO
 */
public class ConversationIdUtil {

    // 固定前缀，统一管理，避免硬编码
    private static final String CONVERSATION_ID_PREFIX = "conversationId_";

    /**
     * 生成唯一的会话ID（conversationId）
     * @param userId 目标用户ID
     * @param sessionId 会话ID
     * @return 格式：conversationId_用户ID_会话ID（例：conversationId_1001_123456789）
     */
    public static String generateConversationId(Integer userId, Long sessionId) {
        // 参数校验：避免空值/无效值导致生成异常
        Assert.notNull(userId, "userId cannot be null");
        Assert.isTrue(userId > 0, "userId must be greater than 0");
        Assert.isTrue(sessionId > 0, "sessionId must be greater than 0");

        // 生成指定格式的conversationId
        return String.format("%s%d_%d", CONVERSATION_ID_PREFIX, userId, sessionId);
    }

    /**
     * 解析conversationId字符串，转换为ConversationIdDTO
     * @param conversationId 待解析的会话ID字符串（格式：conversationId_用户ID_会话ID）
     * @return 包含userId和sessionId的DTO对象
     * @throws IllegalArgumentException 解析失败时抛出异常（格式错误/数字转换失败）
     */
    public static ConversationIdDTO parseConversationId(String conversationId) {
        // 参数校验
        Assert.hasText(conversationId, "conversationId cannot be null or empty");
        Assert.isTrue(conversationId.startsWith(CONVERSATION_ID_PREFIX),
                "conversationId format error: must start with '" + CONVERSATION_ID_PREFIX + "'");

        try {
            // 1. 去掉前缀，拆分剩余部分（例：1001_123456789）
            String[] parts = conversationId.replace(CONVERSATION_ID_PREFIX, "").split("_");
            Assert.isTrue(parts.length == 2, "conversationId format error: must be 'conversationId_${userId}_${sessionId}'");

            // 2. 转换为对应类型
            Integer userId = Integer.parseInt(parts[0]);
            long sessionId = Long.parseLong(parts[1]);

            // 3. 封装DTO并返回
            ConversationIdDTO dto = new ConversationIdDTO();
            dto.setUserId(userId);
            dto.setSessionId(sessionId);
            return dto;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("conversationId parse failed: userId or sessionId is not a number", e);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("conversationId parse failed: " + e.getMessage(), e);
        }
    }
}