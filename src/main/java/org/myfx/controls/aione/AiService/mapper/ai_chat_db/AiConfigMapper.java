package org.myfx.controls.aione.AiService.mapper.ai_chat_db;

import org.apache.ibatis.annotations.Mapper;
import org.myfx.controls.aione.AiService.entity.ai_chat_db.AiConfig;

/**
 * AI配置表Mapper接口
 */
@Mapper
public interface AiConfigMapper {

    /**
     * 新增AI配置
     * @param aiConfig AI配置实体
     * @return 影响行数
     */
    int insertAiConfig(AiConfig aiConfig);

    /**
     * 根据用户ID翻转主动聊天模式（0↔1）
     * @param userId 用户ID
     * @return 影响行数
     */
    int updateActiveChatModeByUserId(Integer userId);

    /**
     * 翻转AI消息切分模式
     */
    void updateSplitAiMessageByUserId(Integer userId);

    /**
     * 根据用户ID查询AI配置
     * @param userId 用户ID
     * @return AI配置实体
     */
    AiConfig selectByUserId(Integer userId);
}