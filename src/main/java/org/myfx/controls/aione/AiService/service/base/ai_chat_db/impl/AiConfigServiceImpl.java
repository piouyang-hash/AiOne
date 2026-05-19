package org.myfx.controls.aione.AiService.service.base.ai_chat_db.impl;

import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.AiService.entity.ai_chat_db.AiConfig;
import org.myfx.controls.aione.AiService.mapper.ai_chat_db.AiConfigMapper;
import org.myfx.controls.aione.AiService.service.base.ai_chat_db.AiConfigService;
import org.springframework.stereotype.Service;

/**
 * AI配置业务层实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AiConfigServiceImpl implements AiConfigService {

    // 注入Mapper（和你AiRoleMapper注入方式完全一致）
    @Resource
    private AiConfigMapper aiConfigMapper;

    /**
     * 初始化配置：查询无配置则添加
     * @param userId 用户ID
     */
    @Override
    public void initConfig(Integer userId) {
       addConfig(userId);
    }

    /**
     * 添加配置：默认关闭主动聊天、默认关闭消息切分
     * @param userId 用户ID
     */
    @Override
    public void addConfig(Integer userId) {
        AiConfig aiConfig = new AiConfig();
        aiConfig.setUserId(userId);
        // 默认0：关闭主动聊天
        aiConfig.setActiveChatMode(0);
        // 🔥 新增：默认0：不切分AI消息
        aiConfig.setSplitAiMessage(0);
        aiConfigMapper.insertAiConfig(aiConfig);
        log.info("AI配置添加成功，用户ID：{}", userId);
    }

    /**
     * 翻转主动聊天模式
     * @param userId 用户ID
     * @return 最新配置
     */
    @Override
    public AiConfig toggleActiveChat(Integer userId) {
        // 执行翻转
        aiConfigMapper.updateActiveChatModeByUserId(userId);
        log.info("用户ID：{}，主动聊天模式已翻转", userId);
        // 返回最新配置
        return getConfig(userId);
    }

    /**
     * 🔥 新增：翻转AI消息切分模式
     * @param userId 用户ID
     * @return 最新配置
     */
    @Override
    public AiConfig toggleSplitAiMessage(Integer userId) {
        // 执行翻转
        aiConfigMapper.updateSplitAiMessageByUserId(userId);
        log.info("用户ID：{}，AI消息切分模式已翻转", userId);
        // 返回最新配置
        return getConfig(userId);
    }

    /**
     * 查询用户配置
     * @param userId 用户ID
     * @return AI配置
     */
    @Override
    public AiConfig getConfig(Integer userId) {
        return aiConfigMapper.selectByUserId(userId);
    }
}