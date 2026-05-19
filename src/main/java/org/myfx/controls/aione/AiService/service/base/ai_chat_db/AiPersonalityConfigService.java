package org.myfx.controls.aione.AiService.service.base.ai_chat_db;

import org.myfx.controls.aione.AiService.common.ai_chat_db.AiPersonalityEnum;
import org.myfx.controls.aione.AiService.common.ai_chat_db.AiPersonalityStrengthEnum;
import org.myfx.controls.aione.AiService.dto.AddAiPersonalityConfigDTO;
import org.myfx.controls.aione.AiService.entity.ai_chat_db.AiPersonalityConfig;

import java.util.List;

/**
 * AI基本性格配置业务层接口
 */
public interface AiPersonalityConfigService {

    /**
     * 新增AI性格配置
     * @param dto 新增性格配置入参
     * @return 生成的主键ID
     */
    Long addAiPersonalityConfig(AddAiPersonalityConfigDTO dto);

    /**
     * 查询指定用户+会话下当前最强的AI性格配置
     * @param userId 用户ID
     * @return 最强性格配置（无则返回null）
     */
    AiPersonalityConfig getCurrentStrongestPersonality(Integer userId);

    /**
     * 按用户ID+会话ID查询所有AI性格配置
     * @param userId 用户ID
     * @return 性格配置列表（无数据返回空List）
     */
    List<AiPersonalityConfig> listAiPersonalityConfigByUserId(Integer userId);

    /**
     * 批量：按userId+sessionId关闭/打开AI性格配置
     * @param userId  用户ID
     * @param isValid 是否有效（1=有效，0=无效）
     * @return 影响行数
     */
    int toggleAiPersonalityValidByUser(Integer userId, Integer isValid);

    /**
     * 精准更新：按id+userId+sessionId更新某一个AI性格配置
     * @param id                      配置ID
     * @param userId                  用户ID
     * @param aiPersonalityCode       性格编码
     * @param personalityStrengthCode 性格强度编码
     * @return 影响行数
     */
    int updateAiPersonalityByIdUser(Long id, Integer userId, AiPersonalityEnum aiPersonalityCode, AiPersonalityStrengthEnum personalityStrengthCode);

    /**
     * 精准：按id关闭/打开某一个AI性格配置
     * @param id 配置ID
     * @param isValid 是否有效（1=有效，0=无效）
     * @return 影响行数
     */
    int toggleAiPersonalityValidById(Long id, Integer isValid);
}