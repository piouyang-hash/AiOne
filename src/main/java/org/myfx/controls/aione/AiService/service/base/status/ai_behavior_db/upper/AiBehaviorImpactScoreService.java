package org.myfx.controls.aione.AiService.service.base.status.ai_behavior_db.upper;

/**
 * AI行为影响分数业务接口
 * 核心处理AI行为（如等待行为）触发的用户情绪分值变动逻辑
 */
public interface AiBehaviorImpactScoreService {

    /**
     * 处理AI等待行为（触发用户情绪分值调整）
     * 注：AI等待行为会根据传入的分值调整用户情绪分（可正可负）
     * @param userId 用户ID（明确指定受影响的用户）
     * @param score AI等待行为对应的变动分值（可正可负，范围-100~100）
     * @return 操作结果：true=成功（分值有变动/执行完成），false=失败/无变动
     */
    boolean handleAiWaitBehavior(Integer userId, Integer score);

    /**
     * 处理「AI接收用户发送聊天消息」行为（扣减AI活跃度分值）
     * 注：
     * 1. 仅需用户ID，分值固定取枚举/数据库中 USER_SEND_MSG 行为的默认分值（-20）；
     * 2. 扣减后AI活跃度不低于0；
     * @param userId 用户ID（关联对应的AI实例，扣减其活跃度）
     * @return 操作结果：true=成功扣减，false=失败（如用户不存在/分值无变动）
     */
    boolean handleAiUserSendMsgBehavior(Integer userId);

    /**
     * 处理「AI主动发送聊天消息」行为（修改AI活跃度 + 喜爱值分值）
     * 注：
     * 1. 仅需用户ID，分值固定取枚举/数据库中 AI_ACTIVE_SEND_MSG 行为的默认分值；
     * 2. 扣减/增加后AI活跃度、喜爱值不低于0；
     * @param userId 用户ID（关联对应的AI实例，修改其数值）
     * @return 操作结果：true=成功处理，false=失败（如用户不存在/分值无变动）
     */
    boolean handleAiActiveSendMsgBehavior(Integer userId);

    /**
     * 处理AI等待很久行为（补满该用户的AI活跃度分值）
     * 注：当AI等待用户交互时长超出阈值时，直接补满其活跃度分值（置为100）
     * @param userId 用户ID（必传）
     * @return 操作结果：true=处理成功/无需要补满的分值，false=补满失败
     */
    boolean handleAiWaitTooLong(Integer userId);

}