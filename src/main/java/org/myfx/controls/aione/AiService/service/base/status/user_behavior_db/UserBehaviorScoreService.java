package org.myfx.controls.aione.AiService.service.base.status.user_behavior_db;

import org.myfx.controls.aione.AiService.common.user_behavior_db.BehaviorEnum;
import org.myfx.controls.aione.AiService.common.user_behavior_db.FeatureTypeEnum;
import org.myfx.controls.aione.AiService.entity.user_behavior_db.UserBehaviorScore;

import java.util.List;

/**
 * 用户行为分值配置业务接口（多维度分值：喜爱值/活跃度/熟悉度）
 */
public interface UserBehaviorScoreService {

    // ===================== 分值配置核心接口 =====================
    /**
     * 新增/保存行为分值配置（唯一约束：code + type）
     * @param behaviorCode 行为编码
     * @param scoreType 分值类型：LIKE/ACTIVITY/FAMILIARITY
     * @param scoreVal 变动分值（-100~100，可null）
     * @return 是否操作成功
     */
    boolean saveScoreConfig(BehaviorEnum behaviorCode, FeatureTypeEnum scoreType, Integer scoreVal);

    /**
     * 根据【行为编码+分值类型】查询唯一分值配置
     * @param behaviorCode 行为编码
     * @param scoreType 分值类型
     * @return 分值配置实体
     */
    UserBehaviorScore getScoreByCodeAndType(BehaviorEnum behaviorCode, FeatureTypeEnum scoreType);

    /**
     * 根据行为编码查询【所有维度】的分值配置
     * @param behaviorCode 行为编码
     * @return 分值配置列表
     */
    List<UserBehaviorScore> listScoresByBehaviorCode(BehaviorEnum behaviorCode);

    /**
     * 根据【行为编码+分值类型】更新分值
     * @param behaviorCode 行为编码
     * @param scoreType 分值类型
     * @param scoreVal 新分值
     * @return 是否更新成功
     */
    boolean updateScoreValue(BehaviorEnum behaviorCode, FeatureTypeEnum scoreType, Integer scoreVal);

    /**
     * 根据ID删除分值配置
     * @param id 主键ID
     * @return 是否删除成功
     */
    boolean deleteScoreById(Integer id);

    /**
     * 根据【行为编码+分值类型】删除配置
     * @param behaviorCode 行为编码
     * @param scoreType 分值类型
     * @return 是否删除成功
     */
    boolean deleteScoreByCodeAndType(BehaviorEnum behaviorCode, FeatureTypeEnum scoreType);
}