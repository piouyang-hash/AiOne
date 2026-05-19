package org.myfx.controls.aione.AiService.service.base.status.user_behavior_db.upper;

import org.myfx.controls.aione.AiService.common.user_behavior_db.FeatureTypeEnum;
import org.myfx.controls.aione.AiService.dto.redis.UserActivityScoreRedisDTO;

/**
 * 用户特征分值「上一次变动」查询服务（核心：快速查询活跃度等特征的最新变动信息，关联Redis存储DTO）
 */
public interface IUserFeatureScoreUpperQueryService {

    /**
     * 查询指定用户下「上一次活跃度变动」的完整信息（封装为Redis适配DTO）
     * @param userId 用户ID
     * @return Redis适配的特征分值DTO（无变动记录时：字段为默认值，lastUpdateTimestamp=null）
     */
    UserActivityScoreRedisDTO queryLastActivityChangeInfo(Integer userId);


    /**
     * 根据特征类型枚举查询指定用户下的具体特征分值
     * 核心逻辑：查询用户的分值记录 → 提取对应特征类型的具体分值（null按0处理）
     * @param userId 用户ID（必传，非正整数视为无效）
     * @param featureType 特征类型（必传，指定要查询的分值类型）
     * @return 该特征类型的具体分值：
     *         - 有记录时返回对应特征的分值（null按0处理）；
     *         - 无记录/参数校验失败时返回null；
     */
    Integer queryUserFeatureScoreByType(Integer userId,
                                        FeatureTypeEnum featureType
    );


    /**
     * 简化接口：查询指定用户下的「活跃度分值」（固定特征类型为ACTIVITY）
     * 核心逻辑：直接调用queryUserFeatureScoreByType，固定传入FeatureTypeEnum.ACTIVITY
     * @param userId 用户ID（参数校验由queryUserFeatureScoreByType负责）
     * @return 活跃度分值：
     *         - 有记录时返回活跃度原始分值（null即返回null）；
     *         - 无记录时返回null；
     *         - 参数不合法时抛IllegalArgumentException（由底层方法抛出）
     */
    Integer queryUserActivityScore(Integer userId);

}
