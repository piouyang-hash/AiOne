package org.myfx.controls.aione.AiService.mapper.user_behavior_db;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.myfx.controls.aione.AiService.common.user_behavior_db.BehaviorEnum;
import org.myfx.controls.aione.AiService.common.user_behavior_db.FeatureTypeEnum;
import org.myfx.controls.aione.AiService.entity.user_behavior_db.UserBehaviorScore;

import java.util.List;

/**
 * 用户行为分值配置表Mapper（多维度分值，与行为解耦）
 */
@Mapper
public interface UserBehaviorScoreMapper {

    /**
     * 1. 新增行为分值配置
     * @param behaviorScore 分值配置实体
     * @return 影响行数
     */
    int insert(UserBehaviorScore behaviorScore);

    /**
     * 2. 根据主键ID查询分值配置
     * @param id 主键
     * @return 配置实体
     */
    UserBehaviorScore selectById(@Param("id") Integer id);

    /**
     * 3. 根据【行为编码+分值类型】唯一查询（核心方法）
     * @param behaviorCode 行为编码
     * @param scoreType 分值类型(LIKE/ACTIVITY/FAMILIARITY)
     * @return 分值配置
     */
    UserBehaviorScore selectByCodeAndType(@Param("behaviorCode") BehaviorEnum behaviorCode,
                                          @Param("scoreType") FeatureTypeEnum scoreType);

    /**
     * 4. 根据行为编码查询所有维度的分值配置
     * @param behaviorCode 行为编码
     * @return 分值配置列表
     */
    List<UserBehaviorScore> selectListByCode(@Param("behaviorCode") BehaviorEnum behaviorCode);

    /**
     * 5. 根据ID动态更新分值配置
     * @param behaviorScore 实体
     * @return 影响行数
     */
    int updateById(UserBehaviorScore behaviorScore);

    /**
     * 6. 根据【行为编码+分值类型】更新分值（核心方法）
     * @param behaviorCode 行为编码
     * @param scoreType 分值类型
     * @param scoreVal 变动分值
     * @return 影响行数
     */
    int updateScoreByCodeAndType(@Param("behaviorCode") BehaviorEnum behaviorCode,
                                 @Param("scoreType") FeatureTypeEnum scoreType,
                                 @Param("scoreVal") Integer scoreVal);

    /**
     * 7. 根据ID删除分值配置
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(@Param("id") Integer id);

    /**
     * 8. 根据【行为编码+分值类型】删除配置
     * @param behaviorCode 行为编码
     * @param scoreType 分值类型
     * @return 影响行数
     */
    int deleteByCodeAndType(@Param("behaviorCode") BehaviorEnum behaviorCode,
                            @Param("scoreType") FeatureTypeEnum scoreType);
}