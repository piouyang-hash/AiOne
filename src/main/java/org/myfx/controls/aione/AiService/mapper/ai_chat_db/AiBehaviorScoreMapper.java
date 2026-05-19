package org.myfx.controls.aione.AiService.mapper.ai_chat_db;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Mapper;
import org.myfx.controls.aione.AiService.common.ai_chat_db.AiBehaviorEnum;
import org.myfx.controls.aione.AiService.common.ai_chat_db.EmotionTypeEnum;
import org.myfx.controls.aione.AiService.entity.ai_chat_db.AiBehaviorScore;

import java.util.List;

@Mapper
public interface AiBehaviorScoreMapper {

    /**
     * 1. 新增AI行为分值配置
     * @param score AI行为分值配置实体
     * @return 受影响的行数
     */
    int insert(AiBehaviorScore score);

    /**
     * 2. 根据主键ID更新AI行为分值配置
     * @param score 包含ID和更新字段的AI行为分值配置实体
     * @return 受影响的行数
     */
    int updateById(AiBehaviorScore score);

    /**
     * 3. 根据行为编码查询所有相关的分值配置
     * @param behaviorCode AI行为编码（如：WAIT/SLEEP）
     * @return 匹配的AI行为分值配置列表
     */
    List<AiBehaviorScore> selectListByBehaviorCode(AiBehaviorEnum behaviorCode);

    /**
     * 4. 根据主键ID查询AI行为分值配置
     * @param id 主键ID
     * @return 匹配的AI行为分值配置（无则返回null）
     */
    AiBehaviorScore selectById(Integer id);

    /**
     * 5. 根据行为编码 + 分值类型枚举 精准查询AI行为分值配置
     * @param behaviorCode AI行为编码（如：WAIT/SLEEP）
     * @param scoreType 分值类型枚举
     * @return 匹配的AI行为分值配置（无则返回null）
     */
    AiBehaviorScore selectByCodeAndType(@Param("behaviorCode") AiBehaviorEnum behaviorCode,
                                        @Param("scoreType") EmotionTypeEnum scoreType);

    /**
     * 6. 根据行为编码删除所有相关的分值配置
     * @param behaviorCode AI行为编码
     * @return 受影响的行数
     */
    int deleteByBehaviorCode(AiBehaviorEnum behaviorCode);

    /**
     * 7. 根据主键ID删除单条分值配置
     * @param id 主键ID
     * @return 受影响的行数
     */
    int deleteById(Integer id);
}