package org.myfx.controls.aione.AiService.engineering.sliding_window;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.AiService.entity.ai_chat_db.AiChatMessage;
import org.myfx.controls.aione.AiService.service.base.ai_chat_db.AiChatMessageService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 滑动窗口业务处理类
 * 核心逻辑：基于配置的最大轮数，查询最新的对话历史（适配“轮数→条数”转换）
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SlidingWindowService {

    // 注入对话消息Service（提供历史查询能力）
    private final AiChatMessageService aiChatMessageService;
    // 注入滑动窗口配置（动态获取最大轮数）
    private final SlidingWindowConfig slidingWindowConfig;

    /**
     * 核心方法：获取滑动窗口内的最新对话历史
     * @param userId 用户ID（必填）
     * @param sessionId 会话ID（必填）
     * @return 窗口内的最新对话列表（按时间正序），无数据返回空列表
     */
    public List<AiChatMessage> getSlidingWindowChatMessages(Integer userId, Long sessionId) {
        // 1. 基础参数校验（复用原有逻辑）
        if (userId == null || sessionId == null) {
            log.warn("滑动窗口查询参数异常：用户ID/会话ID不能为空 | userId={}, sessionId={}", userId, sessionId);
            throw new IllegalArgumentException("参数异常：用户ID/会话ID不能为空");
        }

        // 2. 轮数转条数（核心：1轮=用户+AI两条记录，所以条数=轮数×2）
        Integer maxRound = slidingWindowConfig.getWindowMaxRound();
        Integer limitNum = maxRound * 2; // 最大轮数 → 最大查询条数
        log.debug("滑动窗口参数转换：最大轮数={} → 查询条数={}", maxRound, limitNum);

        // 3. 调用历史查询方法，返回窗口内的最新对话
        try {
            List<AiChatMessage> chatMessages = aiChatMessageService.listLatestChatMessages(userId, sessionId, limitNum);
            log.info("滑动窗口查询完成 | 用户ID={}, 会话ID={}, 查询轮数={}, 实际返回条数={}",
                    userId, sessionId, maxRound, chatMessages.size());
            return chatMessages;
        } catch (IllegalArgumentException e) {
            log.error("滑动窗口查询失败 | 用户ID={}, 会话ID={}, 异常原因={}", userId, sessionId, e.getMessage());
            throw e; // 抛出异常，由上层处理
        }
    }
}