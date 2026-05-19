package org.myfx.controls.aione.AiService.service.base.status.user_behavior_db.upper;

import org.myfx.controls.aione.AiService.common.user_behavior_db.BehaviorEnum;

/**
 * 用户行为影响分值服务（上层业务封装）
 * 统一处理「用户行为触发特征分值更新 + 行为加分明细记录」的完整业务流程
 */
public interface UserBehaviorImpactScoreService {

    /**
     * 处理用户聊天行为触发的分值变动
     * @param userId 用户ID（必传，正整数）
     * @return 操作结果：true=成功，false=失败/无变动
     */
    boolean handleChatBehavior(Integer userId);

    /**
     * 处理用户离线行为触发的分值变动
     * @param userId 用户ID（明确指定离线用户，必传，正整数）
     * @param score 离线行为对应的分值（用户传入具体值，可null，范围-100~100）
     * @return 操作结果：true=成功，false=失败/无变动
     */
    boolean handleUserOffline(Integer userId, Integer score);

    /**
     * 处理用户离线过久行为（清空该用户的活跃度分值）
     * 注：当用户离线时长超出阈值时，直接清空其活跃度分值（置为0）
     * @param userId 用户ID（必传，正整数）
     * @return 操作结果：true=清空成功/无需要清空的分值，false=清空失败
     */
    boolean handleUserOfflineTooLong(Integer userId);

    /**
     * 处理用户点赞行为触发的分值变动
     * @return 操作结果：true=成功，false=失败/无变动
     */
    boolean handleLikeBehavior();

    /**
     * 处理用户拓展通用行为触发的分值变动
     * @param behaviorEnum 拓展行为枚举（必传）
     * @return 操作结果：true=成功，false=失败/无变动
     */
    boolean handleGeneralBehavior(BehaviorEnum behaviorEnum);

}