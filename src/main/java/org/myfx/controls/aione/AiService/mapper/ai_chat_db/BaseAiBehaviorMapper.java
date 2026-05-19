package org.myfx.controls.aione.AiService.mapper.ai_chat_db;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.myfx.controls.aione.AiService.common.ai_chat_db.AiBehaviorEnum;
import org.myfx.controls.aione.AiService.entity.ai_chat_db.BaseAiBehavior;

import java.util.List;

/**
 * 基础AI行为字典表Mapper
 */
@Mapper
public interface BaseAiBehaviorMapper {

    /**
     * 1. 新增行为（含行为编码和行为名称）
     *
     * @param baseAiBehavior 待新增的行为记录
     * @return 影响行数
     */
    int insert(BaseAiBehavior baseAiBehavior);

    /**
     * 2. 根据行为编码更新行为名称
     *
     * @param baseAiBehavior 入参（含behaviorCode、behaviorName）
     * @return 影响行数
     */
    int updateNameByCode(BaseAiBehavior baseAiBehavior);

    /**
     * 3. 根据行为ID动态更新编码/名称（有值则更）
     *
     * @param baseAiBehavior 入参（含behaviorId，可选behaviorCode/behaviorName）
     * @return 影响行数
     */
    int updateById(BaseAiBehavior baseAiBehavior);

    /**
     * 4. 根据行为编码查询记录
     *
     * @param behaviorCode 行为编码（如WAIT/SLEEP）
     * @return 匹配的行为记录（无则返回null）
     */
    BaseAiBehavior selectByCode(@Param("behaviorCode") AiBehaviorEnum behaviorCode);

    /**
     * 5. 查询所有行为记录
     *
     * @return 所有行为记录列表（无则返回空列表）
     */
    List<BaseAiBehavior> selectAll();

    /**
     * 6. 根据行为编码删除记录
     *
     * @param behaviorCode 行为编码（如WAIT/SLEEP）
     * @return 影响行数
     */
    int deleteByCode(@Param("behaviorCode") AiBehaviorEnum behaviorCode);

    /**
     * 7. 根据行为ID删除记录
     *
     * @param behaviorId 行为ID
     * @return 影响行数
     */
    int deleteById(@Param("behaviorId") Integer behaviorId);

    /**
     * 8. 根据行为ID查询记录
     *
     * @param behaviorId 行为ID（正整数）
     * @return 匹配的行为记录（无则返回null）
     */
    BaseAiBehavior selectById(@Param("behaviorId") Integer behaviorId);
}