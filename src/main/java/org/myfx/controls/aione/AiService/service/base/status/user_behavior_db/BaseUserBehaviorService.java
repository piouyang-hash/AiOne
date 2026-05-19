package org.myfx.controls.aione.AiService.service.base.status.user_behavior_db;

import org.myfx.controls.aione.AiService.common.user_behavior_db.BehaviorEnum;
import org.myfx.controls.aione.AiService.entity.user_behavior_db.BaseUserBehavior;

import java.util.List;

/**
 * 基础用户行为字典业务接口（纯行为定义，与分值完全解耦）
 */
public interface BaseUserBehaviorService {

    // ===================== 核心 CRUD 接口 =====================
    /**
     * 新增用户行为（行为编码 + 行为名称）
     * @param behaviorCode 行为编码枚举
     * @param behaviorName 行为中文名称
     * @return 是否新增成功
     */
    boolean addBehavior(BehaviorEnum behaviorCode, String behaviorName);

    /**
     * 根据行为ID查询
     * @param behaviorId 行为ID
     * @return 行为实体
     */
    BaseUserBehavior getBehaviorById(Integer behaviorId);

    /**
     * 根据行为编码查询
     * @param behaviorCode 行为编码枚举
     * @return 行为实体
     */
    BaseUserBehavior getBehaviorByCode(BehaviorEnum behaviorCode);

    /**
     * 查询所有行为列表
     * @return 行为列表
     */
    List<BaseUserBehavior> listAllBehaviors();

    /**
     * 根据ID动态更新行为信息（编码/名称）
     * @param behavior 行为实体（必须带ID）
     * @return 是否更新成功
     */
    boolean updateBehaviorById(BaseUserBehavior behavior);

    /**
     * 根据行为编码更新名称
     * @param behaviorCode 行为编码
     * @param behaviorName 新的行为名称
     * @return 是否更新成功
     */
    boolean updateBehaviorNameByCode(BehaviorEnum behaviorCode, String behaviorName);

    /**
     * 根据行为编码删除
     * @param behaviorCode 行为编码
     * @return 是否删除成功
     */
    boolean deleteBehaviorByCode(BehaviorEnum behaviorCode);

    /**
     * 根据行为ID删除
     * @param behaviorId 行为ID
     * @return 是否删除成功
     */
    boolean deleteBehaviorById(Integer behaviorId);
}