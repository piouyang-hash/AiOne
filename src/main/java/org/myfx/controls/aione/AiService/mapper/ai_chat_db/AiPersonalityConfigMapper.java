package org.myfx.controls.aione.AiService.mapper.ai_chat_db;

import org.apache.ibatis.annotations.Param;
import org.myfx.controls.aione.AiService.entity.ai_chat_db.AiPersonalityConfig;

import java.util.List;

/**
 * AI基本性格配置Mapper接口（单用户维度，移除会话ID依赖）
 */
public interface AiPersonalityConfigMapper {

    /**
     * 查询指定用户下最强烈的AI性格配置
     * @param userId  用户ID（BIGINT类型，适配数据库字段）
     * @return 性格配置
     */
    AiPersonalityConfig selectPriorityConfig(@Param("userId") Integer userId);

    /**
     * 新增AI性格配置
     * @param config 性格配置（已移除sessionId字段）
     * @return 影响行数
     */
    int insertAiPersonalityConfig(AiPersonalityConfig config);

    /**
     * 按id+userId动态更新（非空字段才更新）
     * @param config 性格配置（需包含id、userId及要更新的字段）
     * @return 影响行数
     */
    int updateByIdAndUserId(AiPersonalityConfig config);

    /**
     * 按userId切换is_valid状态
     * @param userId  用户ID
     * @param isValid 是否有效（1=有效，0=无效）
     * @return 影响行数
     */
    int switchValidByUserId(@Param("userId") Integer userId, @Param("isValid") Integer isValid);

    /**
     * 按id精准切换is_valid状态
     * @param id 主键ID
     * @param isValid 是否有效（1=有效，0=无效）
     * @return 影响行数
     */
    int switchValidById(@Param("id") Long id, @Param("isValid") Integer isValid);

    /**
     * 按userId查询所有AI性格配置（过滤已逻辑删除的数据）
     * @param userId  用户ID
     * @return 性格配置列表
     */
    List<AiPersonalityConfig> selectAiPersonalityConfigByUserId(@Param("userId") Integer userId);
}