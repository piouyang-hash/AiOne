package org.myfx.controls.aione.AiService.service.base.status.user_behavior_db;

import org.myfx.controls.aione.AiService.common.user_behavior_db.FeatureTypeEnum;
import org.myfx.controls.aione.AiService.dto.UserFeatureScoreOperateDTO;
import org.myfx.controls.aione.AiService.entity.user_behavior_db.UserFeatureScore;

import java.util.List;

/**
 * 用户特征分值业务接口（初始化/更新/清空核心逻辑）
 */
public interface UserFeatureScoreService {

    /**
     * 初始化用户特征分值（注册时调用，默认分值为0）
     * @param userId 用户ID（唯一业务主键，非空正整数）
     * @throws RuntimeException 用户已存在分值记录时抛出异常
     * @throws IllegalArgumentException 用户ID不合法时抛出异常
     */
    void initUserFeatureScore(Integer userId);

    /**
     * 查询低活跃度（activity_score=0）的用户ID列表
     * @return 低活跃度用户ID列表（无数据返回空列表）
     */
    List<Integer> listLowActivityUserIds();

    /**
     * 更新用户特征分值（指定类型+加分值，自动计算新分值并兜底）
     *
     * @param operateDTO 用户特征分值操作DTO（含userId/featureType/addScore）
     * @return 修改前的分数：null=无旧记录/分值无变化，非null=更新成功
     * @throws IllegalArgumentException 入参不合法时抛出
     */
    Integer updateUserFeatureScore(UserFeatureScoreOperateDTO operateDTO);

    /**
     * 清空指定用户的某类特征分值（将该特征分值置为0）
     *
     * @param userId 用户ID（必传，正整数）
     * @param featureType 特征类型（必传，指定要清空的分值类型）
     * @return 包含清空操作信息的DTO：
     *         - scoreBefore：清空前的原始分值（无旧记录为null，分值无变化为0）
     *         - 其他字段：userId/featureType/addScore 均为本次操作的参数
     * @throws IllegalArgumentException 入参不合法时抛出
     */
    UserFeatureScoreOperateDTO clearUserFeatureScore(Integer userId, FeatureTypeEnum featureType);

    /**
     * 简化接口：清空指定用户的「活跃度分值」（固定特征类型为ACTIVITY）
     * 核心逻辑：直接调用clearUserFeatureScore，固定传入FeatureTypeEnum.ACTIVITY
     * @param userId 用户ID（必传，正整数，参数校验由底层clearUserFeatureScore方法负责）
     * @return 包含清空操作信息的DTO（规则同clearUserFeatureScore）
     * @throws IllegalArgumentException 入参不合法时由底层方法抛出
     */
    UserFeatureScoreOperateDTO clearUserActivityScore(Integer userId);

    /**
     * 根据用户ID查询特征分值
     * @param userId 用户ID（非空正整数）
     * @return 特征分值实体（无匹配则返回null）
     */
    UserFeatureScore getUserFeatureScoreByUserId(Integer userId);

    /**
     * 根据用户ID物理删除该用户的特征分值记录
     * @param userId 用户ID（非空正整数）
     * @return true=删除成功（至少删1条），false=无匹配记录
     */
    boolean deleteAllUserFeatureScoreByUserId(Integer userId);
}