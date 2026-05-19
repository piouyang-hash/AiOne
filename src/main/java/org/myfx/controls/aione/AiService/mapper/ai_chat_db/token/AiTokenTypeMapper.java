package org.myfx.controls.aione.AiService.mapper.ai_chat_db.token;

import org.apache.ibatis.annotations.Param;
import org.myfx.controls.aione.AiService.entity.ai_chat_db.token.AiTokenType;

/**
 * AI Token类型配置 Mapper接口
 */
public interface AiTokenTypeMapper {

    /**
     * 新增Token类型
     * 主键/创建时间 数据库自动生成，无需传入
     * @param tokenType 实体
     * @return 影响行数
     */
    int insert(AiTokenType tokenType);

    /**
     * 根据类型ID修改Token类型
     * @param tokenType 实体
     * @return 影响行数
     */
    int updateByTypeId(AiTokenType tokenType);

    /**
     * 根据类型唯一编码查询（命中唯一索引 uk_type_code）
     * @param typeCode 类型编码
     * @return 实体
     */
    AiTokenType selectByTypeCode(@Param("typeCode") String typeCode);

    /**
     * 根据类型ID查询
     * @param typeId 类型ID
     * @return 实体
     */
    AiTokenType selectByTypeId(@Param("typeId") Long typeId);

    /**
     * 根据类型ID删除
     * @param typeId 类型ID
     * @return 影响行数
     */
    int deleteByTypeId(@Param("typeId") Long typeId);
}