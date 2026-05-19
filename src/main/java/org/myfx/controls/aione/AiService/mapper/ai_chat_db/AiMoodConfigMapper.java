package org.myfx.controls.aione.AiService.mapper.ai_chat_db;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.myfx.controls.aione.AiService.entity.ai_chat_db.AiMoodConfig;

import java.util.List;

/**
 * AI心情配置Mapper接口
 */
@Mapper
public interface AiMoodConfigMapper {

    /**
     * 查询指定用户下最强烈的AI心情配置
     * 情绪优先级：选心情code最大的（最强烈），code相同则选创建时间最早的；
     * @param userId 用户ID（可为null，null代表全局用户）
     * @return 匹配的唯一AI心情配置，无匹配则返回null
     */
    AiMoodConfig selectPriorityConfig(
            @Param("userId") Integer userId
    );

    /**
     * 新增AI心情配置
     * @param aiMoodConfig 配置实体
     * @return 影响行数
     */
    int insertAiMoodConfig(AiMoodConfig aiMoodConfig);

    /**
     * 按id+userId更新（非空字段才更新）
     * @param config 入参需包含：id、userId（Integer），以及要更新的非空字段
     * @return 影响行数
     */
    int updateByIdAndUser(AiMoodConfig config);

    /**
     * 按userId切换is_valid状态
     * @param userId 用户ID（Integer，非空）
     * @param isValid 状态值（仅支持1/0）
     * @return 影响行数
     */
    int switchValidByUser(
            @Param("userId") Integer userId,
            @Param("isValid") Integer isValid
    );

    /**
     * 按id精准切换is_valid状态（精确关闭/启用）
     * @param id 主键ID（非空）
     * @param isValid 状态值（仅支持1/0）
     * @return 影响行数
     */
    int switchValidById(
            @Param("id") Long id,
            @Param("isValid") Integer isValid
    );

    /**
     * 按用户ID查询所有AI心情配置（多个结果，忽略is_valid）
     * @param userId 用户ID（Integer，代码层保证非空）
     * @return 匹配的所有心情配置（按创建时间倒序）
     */
    List<AiMoodConfig> selectAiMoodConfigByUserId(
            @Param("userId") Integer userId
    );
}