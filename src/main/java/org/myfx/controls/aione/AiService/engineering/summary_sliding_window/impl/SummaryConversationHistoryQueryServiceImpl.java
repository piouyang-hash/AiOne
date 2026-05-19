package org.myfx.controls.aione.AiService.engineering.summary_sliding_window.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.AiService.engineering.summary_sliding_window.SummaryConversationHistoryQueryService;
import org.myfx.controls.aione.AiService.engineering.summary_sliding_window.SummarySlidingSpringAiPromptBuildUtil;
import org.myfx.controls.aione.AiService.engineering.summary_sliding_window.SummarySlidingWindowConfig;
import org.myfx.controls.aione.AiService.entity.ai_chat_db.AiChatMessage;
import org.myfx.controls.aione.AiService.service.base.ai_chat_db.AiChatMessageService;
import org.myfx.controls.aione.AiService.service.base.ai_chat_db.AiChatSessionSystemPromptService;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;

/**
 * 总结型滚动窗口构建提示词实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SummaryConversationHistoryQueryServiceImpl implements SummaryConversationHistoryQueryService {

    // 注入聊天消息服务
    private final AiChatMessageService aiChatMessageService;

    // 注入滑动窗口配置类
    private final SummarySlidingWindowConfig summarySlidingWindowConfig;

    // 新增注入：会话历史摘要系统提示词服务
    private final AiChatSessionSystemPromptService aiChatSessionSystemPromptService;

    @Override
    public List<AiChatMessage> queryAllConversationHistory(Integer userId, Long sessionId) {
        // 1. 参数非空校验
        Assert.notNull(userId, "用户ID不能为空");
        Assert.notNull(sessionId, "会话ID不能为空");

        // 2. 调用countChatMessagesBySessionIdAndUserId获取总消息条数
        Integer totalMessageNum = aiChatMessageService.countChatMessagesBySessionIdAndUserId(userId, sessionId);
        // 兜底：总消息数为null时视为0
        totalMessageNum = (totalMessageNum == null) ? 0 : totalMessageNum;

        // 3. 计算基础参数
        Integer summaryRound = summarySlidingWindowConfig.getSummaryRound();
        Integer windowMaxRound = summarySlidingWindowConfig.getWindowMaxRound();
        Integer summaryMessageNum = summaryRound * 2; // 总结的消息数
        Integer maxWindowLimit = windowMaxRound * 2; // 窗口最大限制数

        // 4. 调用工具类计算最终的limitNum
        Integer limitNum = SummarySlidingSpringAiPromptBuildUtil.calculateSlidingWindowLimitNum(
                totalMessageNum, summaryMessageNum, maxWindowLimit
        );

        // 5. 调用查询方法（limitNum为0时返回空列表，符合业务逻辑）
        return aiChatMessageService.listLatestChatMessages(userId, sessionId, limitNum);
    }


    @Override
    public List<AiChatMessage> querySummaryTargetConversationHistory(Integer userId, Long sessionId) {
        // 1. 参数非空校验
        Assert.notNull(userId, "用户ID不能为空");
        Assert.notNull(sessionId, "会话ID不能为空");

        // 2. 提取序列号（调用指定方法）
        Integer serialNumber = aiChatSessionSystemPromptService.getSerialNumberFromLatestSummaryPrompt(userId, sessionId);

        // 3. 调用countChatMessagesBySessionIdAndUserId获取总消息条数
        Integer totalMessageNum = aiChatMessageService.countChatMessagesBySessionIdAndUserId(userId, sessionId);
        // 兜底：总消息数为null时视为0
        totalMessageNum = (totalMessageNum == null) ? 0 : totalMessageNum;

        // 4. 计算基础参数
        Integer summaryRound = summarySlidingWindowConfig.getSummaryRound();
        Integer windowMaxRound = summarySlidingWindowConfig.getWindowMaxRound();
        Integer summaryMessageNum = summaryRound * 2;
        Integer maxWindowLimit = windowMaxRound * 2;

        // 5. 计算减法次数
        int subtractCount = SummarySlidingSpringAiPromptBuildUtil.calculateSlidingWindowSubtractCount(
                totalMessageNum, summaryMessageNum, maxWindowLimit
        );

        // 6. 核心条件判断
        if (subtractCount <= (serialNumber == null ? 0 : serialNumber)) {
            return null;
        }

        // 7. 调用工具类计算最终的查询limitNum
        Integer queryLimitNum = SummarySlidingSpringAiPromptBuildUtil.calculateSlidingWindowLimitNum(
                totalMessageNum, summaryMessageNum, maxWindowLimit
        );

        // 8. 调用方法获取指定数量的最新聊天消息
        List<AiChatMessage> allMessages = aiChatMessageService.listLatestChatMessages(userId, sessionId, queryLimitNum);

        // 9. 计算需要截取的待总结消息数
        int summaryLimitNum = summarySlidingWindowConfig.getSummaryRound() * 2;

        // 10. 截取消息（处理列表长度不足的情况）
        if (allMessages == null || allMessages.isEmpty()) {
            return List.of();
        }
        int endIndex = Math.min(summaryLimitNum, allMessages.size());

        // 11. 返回待总结的消息列表
        return allMessages.subList(0, endIndex);
    }

}