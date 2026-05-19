package org.myfx.controls.aione.AiService.mapper.user_behavior_db;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.myfx.controls.aione.AiService.common.user_behavior_db.FeatureTypeEnum;
import org.myfx.controls.aione.AiService.entity.user_behavior_db.UserBehaviorScoreDetail;

import java.util.List;

/**
 * 用户行为加分明细Mapper接口（核心CRD，无逻辑删除/更新，基于【用户】维度）
 */
@Mapper
public interface UserBehaviorScoreDetailMapper {

    /**
     * 1. 新增用户行为加分明细记录
     * @param detail 待新增的明细记录（含userId、行为ID等核心字段）
     * @return 影响行数
     */
    int insert(UserBehaviorScoreDetail detail);

    /**
     * 2. 根据userId查询该用户所有明细记录
     * @param userId 用户ID（独立参数）
     * @return 该用户下的所有明细记录列表（无匹配则返回空列表）
     */
    List<UserBehaviorScoreDetail> selectListByUserId(
            @Param("userId") Integer userId
    );

    /**
     * 3. 根据userId + 特征类型（feature_type）查询明细记录
     * @param userId 用户ID（独立参数）
     * @param featureType 特征类型枚举（ACTIVITY-活跃度/FAVOR-喜爱度/FAMILIAR-熟悉度）
     * @return 该用户+特征类型下的所有明细记录列表（无匹配则返回空列表）
     */
    List<UserBehaviorScoreDetail> selectListByUserIdAndFeatureType(
            @Param("userId") Integer userId,
            @Param("featureType") FeatureTypeEnum featureType
    );

    /**
     * 4. 根据userId + 特征类型查询最新的一条明细记录（性能优化：仅查1条）
     * @param userId 用户ID
     * @param featureType 特征类型枚举
     * @return 该条件下最新的明细记录（无匹配则返回null）
     */
    UserBehaviorScoreDetail selectLatestByUserIdAndFeatureType(
            @Param("userId") Integer userId,
            @Param("featureType") FeatureTypeEnum featureType
    );

    /**
     * 5. 根据userId删除该用户所有明细记录
     * @param userId 用户ID
     * @return 影响行数
     */
    int deleteByUserId(Integer userId);
}