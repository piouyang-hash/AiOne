package org.myfx.controls.aione.AiService.service.base.status;

import java.util.List;

/**
 * 状态阈值业务接口
 * Status Threshold Service
 */
public interface StatusThresholdService {

    /**
     * 查询因活跃度触发「事件结束型主动消息」的用户ID列表
     * （核心逻辑：取高活跃度和低活跃度用户ID的交集）
     * @return 触发消息的用户ID列表（无交集返回空列表）
     */
    List<Integer> listTriggerEventEndActiveMsgUserIdsByActivity();

    /**
     * 【喜爱度维度】查询触发主动消息事件的用户ID列表
     * 规则：喜爱度较高(like_value > 5) 的用户
     * @return 用户ID列表
     */
    List<Integer> listTriggerEventEndActiveMsgUserIdsByLike();

    /**
     * 【统一入口】查询可触发主动消息的用户ID列表
     * 规则：满足 高活跃度 / 高喜爱度 任 一 条件即可
     * @return 符合条件的用户ID并集（去重）
     */
    List<Integer> listTriggerActiveMsgUserIds();

}