package org.myfx.controls.aione.AiService.engineering.summary_sliding_window.impl;

import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.myfx.controls.aione.AiService.engineering.summary_sliding_window.SummaryConversationHistoryQueryService;
import org.myfx.controls.aione.AiService.engineering.summary_sliding_window.SummarySlidingWindowAopEnhanceService;
import org.myfx.controls.aione.AiService.engineering.summary_sliding_window.SummarySlidingWindowBusinessService;
import org.myfx.controls.aione.AiService.entity.ai_chat_db.AiChatMessage;
import org.myfx.controls.aione.AiService.entity.ai_chat_db.AiChatSessionSystemPrompt;
import org.myfx.controls.aione.AiService.service.base.ai_chat_db.AiChatSessionSystemPromptService;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;

/**
 * 总结滑动窗口AOP增强服务实现类
 * 核心：为AOP通知提供前置/后置处理能力，依赖构造器注入
 */
@Service("summarySlidingWindowAopEnhanceService") // 指定Bean名称，便于AOP注入
@RequiredArgsConstructor // 构造器注入（替代@Autowired，符合最佳实践）
public class SummarySlidingWindowAopEnhanceServiceImpl implements SummarySlidingWindowAopEnhanceService {

    // ========== 构造器注入依赖（按你要求的三个服务） ==========
    private final SummaryConversationHistoryQueryService summaryConversationHistoryQueryService;
    @Resource(name = "sparkSummarySlidingWindowBusinessService")
    private SummarySlidingWindowBusinessService summarySlidingWindowBusinessService;
    private final AiChatSessionSystemPromptService aiChatSessionSystemPromptService;

    // ========== 实现方法1：AOP前置 - 预加载会话全部历史对话 ==========
    @Override
    public List<AiChatMessage> preLoadConversationHistory(Integer userId, Long sessionId) {
        // 1. 参数校验（避免空值调用）
        Assert.notNull(userId, "用户ID（userId）不能为空");
        Assert.notNull(sessionId, "会话ID（sessionId）不能为空");

        // 2. 调用查询服务，返回全部历史对话
        return summaryConversationHistoryQueryService.queryAllConversationHistory(userId, sessionId);
    }

    // ========== 实现方法2：AOP后置 - 总结会话历史上下文并存储摘要 ==========
    @Override
    public void postSummaryConversationContext(Integer userId, Long sessionId) {
        // 1. 参数校验
        Assert.notNull(userId, "用户ID（userId）不能为空");
        Assert.notNull(sessionId, "会话ID（sessionId）不能为空");

        // 2. 调用滑动窗口业务服务，总结历史上下文（返回null表示无需总结）
        String summaryResult = summarySlidingWindowBusinessService.summarizeConversationWithSlidingWindow(userId, sessionId);

        // 3. 仅当有总结结果时，调用提示词服务存储摘要（避免空摘要入库）
        if (summaryResult != null) {
            aiChatSessionSystemPromptService.addSessionSystemPrompt(userId, sessionId, summaryResult);
        }
    }

    // ========== 实现方法3：AOP前置 - 预获取最新的历史对话摘要 ==========
    @Override
    public String preGetLatestHistorySummary(Integer userId, Long sessionId) {
        // 1. 参数校验
        Assert.notNull(userId, "用户ID（userId）不能为空");
        Assert.notNull(sessionId, "会话ID（sessionId）不能为空");

        // 2. 调用提示词服务，获取最新历史摘要实体
        AiChatSessionSystemPrompt promptEntity = aiChatSessionSystemPromptService.getLatestHistorySummarySystemPrompt(userId, sessionId);

        // 3. 处理实体/字段为空的情况，返回null
        if (promptEntity == null || promptEntity.getSystemPrompt() == null || promptEntity.getSystemPrompt().isBlank()) {
            return null;
        }

        // 4. 提取内容，格式化为 <聊天历史摘要>：（内容）（换行）
        String historySummaryContent = promptEntity.getSystemPrompt();
        return String.format("<聊天历史摘要>：%s\n", historySummaryContent);
    }

}