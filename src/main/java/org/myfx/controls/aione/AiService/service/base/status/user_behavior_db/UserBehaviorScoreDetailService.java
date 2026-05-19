package org.myfx.controls.aione.AiService.service.base.status.user_behavior_db;

import org.myfx.controls.aione.AiService.common.user_behavior_db.FeatureTypeEnum;
import org.myfx.controls.aione.AiService.dto.UserFeatureScoreOperateDTO;
import org.myfx.controls.aione.AiService.entity.user_behavior_db.UserBehaviorScoreDetail;

import java.util.List;

/**
 * 用户行为积分明细业务服务接口
 * 封装用户行为积分明细的新增、查询、删除等核心业务操作
 */
public interface UserBehaviorScoreDetailService {

    /**
     * 新增用户行为积分明细记录
     *
     * @param operateDTO 用户特征分值操作DTO（含userId/behaviorId/featureType/addScore/scoreBefore）
     * @return 新增成功的行数（固定返回1）
     * @throws IllegalArgumentException 入参不合法时抛出
     */
    int addUserBehaviorScoreDetail(UserFeatureScoreOperateDTO operateDTO);

    /**
     * 查询指定用户下的所有行为积分明细
     * （业务场景：查看某用户的积分变动记录）
     * @param userId 用户ID
     * @return 积分明细列表（无匹配记录则返回空列表，不会返回null）
     */
    List<UserBehaviorScoreDetail> queryUserBehaviorScoreDetailsByUserId(Integer userId);

    /**
     * 查询指定用户+指定特征类型下的所有行为积分明细
     * （业务场景：查看某用户某类特征（如活跃度）的积分变动记录）
     * @param userId 用户ID
     * @param featureType 特征类型枚举（ACTIVITY-活跃度/FAVOR-喜爱度/FAMILIAR-熟悉度）
     * @return 积分明细列表（无匹配记录则返回空列表，不会返回null）
     */
    List<UserBehaviorScoreDetail> queryUserBehaviorScoreDetailsByUserIdAndFeatureType(
            Integer userId,
            FeatureTypeEnum featureType
    );

    /**
     * 查询指定用户+指定特征类型下的最新一条行为积分明细
     * （业务场景：获取某用户某类特征（如活跃度）的最新积分变动）
     * @param userId 用户ID
     * @param featureType 特征类型枚举（ACTIVITY-活跃度/FAVOR-喜爱度/FAMILIAR-熟悉度）
     * @return 最新积分明细记录（无匹配记录则返回null）
     */
    UserBehaviorScoreDetail queryLatestUserBehaviorScoreDetailByUserIdAndFeatureType(
            Integer userId,
            FeatureTypeEnum featureType
    );

    /**
     * 查询指定用户下活跃度特征的最新一条行为积分明细（快捷方法）
     * @param userId 用户ID
     * @return 最新活跃度明细记录（无匹配则返回null）
     */
    UserBehaviorScoreDetail queryLatestActivityScoreDetail(Integer userId);

    /**
     * 查询指定用户下喜爱度特征的最新一条行为积分明细（快捷方法）
     * @param userId 用户ID
     * @return 最新喜爱度明细记录（无匹配则返回null）
     */
    UserBehaviorScoreDetail queryLatestFavorScoreDetail(Integer userId);

    /**
     * 查询指定用户下熟悉度特征的最新一条行为积分明细（快捷方法）
     * @param userId 用户ID
     * @return 最新熟悉度明细记录（无匹配则返回null）
     */
    UserBehaviorScoreDetail queryLatestFamiliarScoreDetail(Integer userId);

    /**
     * 删除指定用户的所有行为积分明细
     * （业务场景：用户注销/数据清理时，删除该用户所有积分变动记录）
     * @param userId 用户ID
     * @return 删除成功的行数（0=无匹配记录，>0=成功删除的条数）
     */
    int removeAllUserBehaviorScoreDetailsByUserId(Integer userId);
}