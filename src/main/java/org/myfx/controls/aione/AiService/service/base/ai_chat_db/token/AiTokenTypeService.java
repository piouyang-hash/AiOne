package org.myfx.controls.aione.AiService.service.base.ai_chat_db.token;

import org.myfx.controls.aione.AiService.entity.ai_chat_db.token.AiTokenType;

import java.util.List;

/**
 * AI Token类型配置 业务接口
 */
public interface AiTokenTypeService {

    /**
     * 新增AI Token类型配置
     * @param tokenType Token类型实体
     * @return 影响行数
     */
    int addAiTokenType(AiTokenType tokenType);

    /**
     * 根据类型ID查询AI Token类型
     * @param typeId 类型ID
     * @return 实体对象
     */
    AiTokenType getAiTokenTypeById(Long typeId);

    /**
     * 根据类型编码查询AI Token类型
     * @param typeCode 类型唯一编码
     * @return 实体对象
     */
    AiTokenType getAiTokenTypeByTypeCode(String typeCode);

    /**
     * 获取系统默认Token类型（固定：DeepSeek输入、输出）
     * @return 默认Token类型集合
     */
    List<AiTokenType> getDefaultAiTokenTypeList();

    /**
     * 修改AI Token类型配置
     * @param tokenType Token类型实体
     * @return 影响行数
     */
    int updateAiTokenType(AiTokenType tokenType);

    /**
     * 根据类型ID删除AI Token类型配置
     * @param typeId 类型ID
     * @return 影响行数
     */
    int removeAiTokenTypeById(Long typeId);
}