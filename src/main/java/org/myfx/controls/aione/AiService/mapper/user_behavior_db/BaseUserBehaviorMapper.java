package org.myfx.controls.aione.AiService.mapper.user_behavior_db;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.myfx.controls.aione.AiService.common.user_behavior_db.BehaviorEnum;
import org.myfx.controls.aione.AiService.entity.user_behavior_db.BaseUserBehavior;

import java.util.List;

/**
 * 用户行为字典表Mapper（纯行为定义，无分值）
 */
@Mapper
public interface BaseUserBehaviorMapper {

    /**
     * 1. 新增用户行为（behavior_code + behavior_name）
     * @param baseUserBehavior 待新增的行为记录
     * @return 影响行数
     */
    int insert(BaseUserBehavior baseUserBehavior);

    /**
     * 2. 根据行为编码更新行为名称
     * @param baseUserBehavior 入参（含behaviorCode、behaviorName）
     * @return 影响行数
     */
    int updateNameByCode(BaseUserBehavior baseUserBehavior);

    /**
     * 3. 根据行为ID动态更新（有值则更新）
     * @param baseUserBehavior 入参（含behaviorId，可选behaviorCode/behaviorName）
     * @return 影响行数
     */
    int updateById(BaseUserBehavior baseUserBehavior);

    /**
     * 4. 根据行为编码查询记录
     * @param behaviorCode 行为编码枚举
     * @return 匹配的行为记录（无则返回null）
     */
    BaseUserBehavior selectByCode(@Param("behaviorCode") BehaviorEnum behaviorCode);

    /**
     * 5. 查询所有行为记录
     * @return 所有行为记录列表
     */
    List<BaseUserBehavior> selectAll();

    /**
     * 6. 根据行为编码删除记录
     * @param behaviorCode 行为编码枚举
     * @return 影响行数
     */
    int deleteByCode(@Param("behaviorCode") BehaviorEnum behaviorCode);

    /**
     * 7. 根据行为ID删除记录
     * @param behaviorId 行为ID
     * @return 影响行数
     */
    int deleteById(Integer behaviorId);

    /**
     * 8. 根据行为ID查询记录
     * @param behaviorId 行为ID
     * @return 匹配的行为记录（无则返回null）
     */
    BaseUserBehavior selectById(@Param("behaviorId") Integer behaviorId);
}