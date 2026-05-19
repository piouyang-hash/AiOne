package org.myfx.controls.aione.AiService.engineering.summary_sliding_window.impl;

import lombok.RequiredArgsConstructor;
import org.myfx.controls.aione.AiService.engineering.summary_sliding_window.SummaryConversationHistoryQueryService;
import org.myfx.controls.aione.AiService.engineering.summary_sliding_window.SummarySlidingWindowBusinessService;
import org.myfx.controls.aione.AiService.entity.AIChatRequest;
import org.myfx.controls.aione.AiService.entity.ai_chat_db.AiChatMessage;
import org.springframework.stereotype.Service;

import java.util.List;
/**
 * 讯飞星火模型-总结滑动窗口业务实现类
 * 核心：注入总结业务服务+历史对话查询服务，完成滑动窗口式对话总结
 * 关键逻辑：依赖querySummaryTargetConversationHistory返回值判断是否总结（null=无需总结）
 */
@Service("sparkSummarySlidingWindowBusinessService") // 指定Bean名称，便于注入
@RequiredArgsConstructor // 构造器注入（替代@Autowired，更符合最佳实践）
public class SparkSummarySlidingWindowBusinessService implements SummarySlidingWindowBusinessService {

    // 注入Spark总结业务服务（已有的核心总结能力）
//    private final SparkAISummaryBusinessService sparkAISummaryBusinessService;

    // 注入修改后的历史对话查询服务（获取待总结的对话历史，自带是否需要总结的判断逻辑）
    private final SummaryConversationHistoryQueryService summaryConversationHistoryQueryService;

    @Override
    public String summarizeConversationWithSlidingWindow(Integer userId, Long sessionId) {
        // ========== 步骤1：调用查询方法（自带是否需要总结的判断） ==========
        // querySummaryTargetConversationHistory返回null → 无需总结；非null → 待总结的对话列表
        List<AiChatMessage> targetHistory = summaryConversationHistoryQueryService.querySummaryTargetConversationHistory(userId, sessionId);

        // 无需总结：直接返回null
        if (targetHistory == null) {
            return null;
        }

        // ========== 步骤2：构建AIChatRequest（使用简易双参数构造方法，替换原Builder） ==========
        // 1. 调用简易构造方法，传入userId和sessionId（自动生成conversationId）
        AIChatRequest request = new AIChatRequest(userId, sessionId);

        // 2. 调用已有的setHistoryMessages方法，设置待总结的对话历史（自动完成类型转换）
        request.setHistoryMessages(targetHistory);

        // ========== 步骤3：调用Spark总结业务，返回总结结果 ==========
//        return sparkAISummaryBusinessService.summarizeConversationContext(request);
        return "总结完毕";
    }

}