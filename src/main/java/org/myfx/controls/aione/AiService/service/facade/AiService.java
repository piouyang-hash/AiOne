package org.myfx.controls.aione.AiService.service.facade;

/**
 * AI业务服务接口
 * 【重要注释】：不要在其他类中注入该服务的实例，避免造成循环依赖！
 * 仅允许该服务单方面注入其他Service（如订单、用户、调度等业务Service）处理业务逻辑。
 */
public interface AiService {

    /**
     * 公共AI对话（无需登录，无用户关联）
     * @param msg 用户输入的对话消息
     * @return AI返回的内容
     */
    String handlePublicAiChat(String msg);

    /**
     * 触发系统发起的AI主动聊天（系统触发主动聊天机制专用）
     * @param userId 目标用户ID（系统主动聊天的接收用户）
     * @param sessionId 会话ID（关联用户记忆/聊天链路）
     * @param triggerContext 系统触发主动聊天的上下文
     * @return AI主动发起的聊天回复内容
     */
    String triggerSystemInitiatedAiChat(Integer userId, long sessionId, String triggerContext);

    /**
     * 用户AI对话（重载：关联会话ID，便于追踪对话链路）
     * @param msg 用户输入的对话消息
     * @param sessionId 对话会话ID（用于日志追踪/会话上下文关联）
     * @return AI返回的内容
     */
    String respondToUserPureChatWithMemory(String msg, Long sessionId);

    /**
     * 与主模型聊天（关联会话ID，便于追踪对话链路）
     * @param msg 用户输入的对话消息
     * @param sessionId 对话会话ID（用于日志追踪/会话上下文关联）
     * @return AI返回的内容
     */
    String chatWithMainModel(String msg, Long sessionId);

    /**
     * 与事件驱动模型聊天（关联会话ID，便于追踪对话链路）
     * @param msg 用户输入的对话消息
     * @param sessionId 对话会话ID（用于日志追踪/会话上下文关联）
     * @return AI返回的内容
     */
    String chatWithEDModel(String msg, Long sessionId);

    /**
     * 事件结束型主动消息（带三个参数，userId固定为1）
     * 核心逻辑：构建完整事件描述 → 填充提示词模板 → 调用AI模型生成主动消息 → 返回消息内容
     * @param userId 用户ID（不再固定，由调用方传入）
     * @param locationDesc  地点描述（如HOME/SCHOOL/PARK）
     * @param eventDesc     事件描述（如SLEEP/EAT_BREAKFAST/WALK）
     * @param eventDuration 事件持续时间（单位：秒，可为null）
     * @return AI生成的主动消息内容（String）
     */
    String eventEndActiveMessage(Integer userId, String locationDesc, String eventDesc, Integer eventDuration);

}