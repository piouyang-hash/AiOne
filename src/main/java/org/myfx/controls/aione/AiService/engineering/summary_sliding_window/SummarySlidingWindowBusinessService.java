package org.myfx.controls.aione.AiService.engineering.summary_sliding_window;

/**
 * 总结滑动窗口业务核心接口
 * 核心能力：基于滑动窗口逻辑自动判断是否需要总结，并完成对话历史的总结
 */
public interface SummarySlidingWindowBusinessService {

    /**
     * 滑动窗口式对话总结（自带“是否需要总结”判断）
     * <p>核心逻辑：
     * 1. 自动判断当前会话的历史对话是否达到总结条件（如消息数量/长度阈值）；
     * 2. 若需要总结：查询目标历史对话 → 构建请求 → 调用AI总结；
     * 3. 若不需要总结：返回空/提示语（可根据业务调整）。
     * </p>
     * @param userId 用户ID
     * @param sessionId 会话ID
     * @return 总结后的对话摘要文本（无需总结时返回空字符串或指定提示语）
     */
    String summarizeConversationWithSlidingWindow(Integer userId, Long sessionId);

}