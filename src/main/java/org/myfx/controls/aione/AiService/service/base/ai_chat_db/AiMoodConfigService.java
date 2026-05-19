package org.myfx.controls.aione.AiService.service.base.ai_chat_db;

import org.myfx.controls.aione.AiService.common.ai_chat_db.AiMoodEnum;
import org.myfx.controls.aione.AiService.common.ai_chat_db.AiMoodStrengthEnum;
import org.myfx.controls.aione.AiService.dto.AddAiMoodConfigDTO;
import org.myfx.controls.aione.AiService.entity.ai_chat_db.AiMoodConfig;

import java.util.List;

/**
 * AI心情配置业务层接口（单用户维度，移除会话ID依赖）
 */
public interface AiMoodConfigService {

    /**
     * 新增AI心情配置（业务层封装，自动填充默认值）
     * @param dto 新增配置DTO（已移除sessionId字段，仅保留userId）
     * @return 生成的配置主键ID（雪花ID）
     */
    Long addAiMoodConfig(AddAiMoodConfigDTO dto);

    /**
     * 获取用户当前最强烈的AI心情配置
     * 情绪强度优先级：选心情code最大的（最强烈），同code选最早创建的。
     * @param userId 用户ID（Integer类型，可为null，null时查全局配置）
     * @return 最终匹配的最强烈AI心情配置，无任何匹配则返回null
     */
    AiMoodConfig getCurrentStrongestMood(Integer userId);

    /**
     * 查询用户对应的所有AI心情配置（多个结果，过滤已逻辑删除数据）
     * @param userId 用户ID（Integer类型，非空）
     * @return 所有匹配的心情配置（无则返回空List）
     */
    List<AiMoodConfig> listAiMoodConfigByUserId(Integer userId);

    /**
     * 【批量】关闭/打开AI心情（按userId）
     * @param userId 用户ID（Integer类型，非空）
     * @param isValid 状态：1=打开，0=关闭（仅支持0/1）
     * @return 影响行数
     */
    int toggleAiMoodValidByUser(Integer userId, Integer isValid);

    /**
     * 【精准更新】更新某一个AI心情（按id+userId）
     * @param id 心情配置ID（非空）
     * @param userId 用户ID（Integer类型，非空）
     * @param aiMoodCode 新的心情编码（可选，传null则不更新）
     * @param aiStrengthCode 新的强度编码（可选，传null则不更新）
     * @return 影响行数
     */
    int updateAiMoodByIdAndUser(Long id, Integer userId, AiMoodEnum aiMoodCode, AiMoodStrengthEnum aiStrengthCode);

    /**
     * 【精准】关闭/打开某一个AI心情（按id）
     * @param id 心情配置ID（非空）
     * @param isValid 状态：1=打开，0=关闭（仅支持0/1）
     * @return 影响行数
     */
    int toggleAiMoodValidById(Long id, Integer isValid);
}