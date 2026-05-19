package org.myfx.controls.aione.AiService.service.facade;

/**
 * 聊天服务核心接口
 * 包含多种类型的AI聊天交互方法，底层统一调用API完成交互
 */
public interface SyncChatService {

    /**
     * 核心接口聊天（Main Interface Chat）
     * @param msg 用户输入的对话消息
     * @param sessionId 对话会话ID（用于日志追踪/会话上下文关联）
     * @return AI返回的内容
     */
    String mainInterfaceChat(String msg, Long sessionId);

    /**
     * 滑动窗口聊天（Sliding Window Chat）
     * @param msg 用户输入的对话消息
     * @param sessionId 对话会话ID（用于日志追踪/会话上下文关联）
     * @return AI返回的内容
     */
    String slidingWindowChat(String msg, Long sessionId);

    /**
     * 总结型滑动窗口聊天（Summary Sliding Window Chat）
     * @param msg 用户输入的对话消息
     * @param sessionId 对话会话ID（用于日志追踪/会话上下文关联）
     * @return AI返回的内容
     */
    String summarySlidingWindowChat(String msg, Long sessionId);

    /**
     * 近追加式聊天（Append Only Chat）
     * @param msg 用户输入的对话消息
     * @param sessionId 对话会话ID（用于日志追踪/会话上下文关联）
     * @return AI返回的内容
     */
    String appendOnlyChat(String msg, Long sessionId);

    /**
     * 事件驱动型核心接口聊天（Event Driven Main Interface Chat）
     * @param msg 用户输入的对话消息
     * @param sessionId 对话会话ID（用于日志追踪/会话上下文关联）
     * @return AI返回的内容
     */
    String eventDrivenMainInterfaceChat(String msg, Long sessionId);

    // ==================== 调用API的核心方法（唯一有重载的接口） ====================
    /**
     * 调用AI API（带会话ID，用于上下文/日志追踪）
     * @param msg 用户输入的对话消息
     * @param sessionId 对话会话ID
     * @return AI返回的内容
     */
    String callApi(String msg, Long sessionId);

    /**
     * 调用AI API（重载：无会话ID，适用于无需上下文追踪的场景）
     * @param msg 用户输入的对话消息
     * @return AI返回的内容
     */
    String callApi(String msg);
}
