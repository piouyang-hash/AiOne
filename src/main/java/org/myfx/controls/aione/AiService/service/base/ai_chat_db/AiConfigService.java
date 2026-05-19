package org.myfx.controls.aione.AiService.service.base.ai_chat_db;

import org.myfx.controls.aione.AiService.entity.ai_chat_db.AiConfig;

/**
 * AI配置业务接口
 */
public interface AiConfigService {

    /**
     * 初始化AI配置（无则添加）
     * @param userId 用户ID
     */
    void initConfig(Integer userId);

    /**
     * 添加AI配置
     * @param userId 用户ID
     */
    void addConfig(Integer userId);

    /**
     * 切换主动聊天模式（翻转0/1）
     * @param userId 用户ID
     * @return 最新配置
     */
    AiConfig toggleActiveChat(Integer userId);

    /**
     * 切换AI消息切分模式（翻转0/1）
     * @param userId 用户ID
     * @return 最新配置
     */
    AiConfig toggleSplitAiMessage(Integer userId);

    /**
     * 根据用户ID查询AI配置
     * @param userId 用户ID
     * @return AI配置
     */
    AiConfig getConfig(Integer userId);
}