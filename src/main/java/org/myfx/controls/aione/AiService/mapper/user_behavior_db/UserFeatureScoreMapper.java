package org.myfx.controls.aione.AiService.mapper.user_behavior_db;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.myfx.controls.aione.AiService.entity.user_behavior_db.UserFeatureScore;

import java.util.List;

@Mapper
public interface UserFeatureScoreMapper {

    /**
     * 新增用户特征分值记录
     * @param score 特征分值记录（含userId、各类分值）
     * @return 影响行数
     */
    int insert(UserFeatureScore score);

    /**
     * 根据用户ID查询特征分值记录
     * @param userId 用户ID
     * @return 用户特征分值记录（无匹配返回null）
     */
    UserFeatureScore selectByUserId(@Param("userId") Integer userId);

    /**
     * 查询所有活跃度为0的用户ID列表
     * @return 活跃度为0的用户ID列表（无数据返回空列表，非null）
     */
    List<Integer> selectUserIdsByActivityScoreZero();

    /**
     * 根据用户ID更新特征分值
     * @param score 待更新的分值（含userId和需要更新的分值字段）
     * @return 影响行数
     */
    int updateByUserId(UserFeatureScore score);

    /**
     * 根据用户ID删除特征分值记录
     * @param userId 用户ID
     * @return 影响行数
     */
    int deleteByUserId(Integer userId);
}