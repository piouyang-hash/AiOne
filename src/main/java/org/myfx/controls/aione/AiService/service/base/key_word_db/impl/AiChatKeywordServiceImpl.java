package org.myfx.controls.aione.AiService.service.base.key_word_db.impl;

import lombok.RequiredArgsConstructor;
import org.myfx.controls.aione.AiService.entity.key_word_db.AiChatKeyword;
import org.myfx.controls.aione.AiService.mapper.key_word_db.AiChatKeywordMapper;
import org.myfx.controls.aione.AiService.service.base.key_word_db.AiChatKeywordService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * AI对话关键词业务实现类
 * 实现关键词管理的核心业务逻辑，依赖Mapper操作数据库
 */
@Service
@RequiredArgsConstructor
public class AiChatKeywordServiceImpl implements AiChatKeywordService {

    // 注入Mapper接口（Spring自动装配）
    private final AiChatKeywordMapper aiChatKeywordMapper;

    /**
     * 保存单条关键词：调用Mapper的单条插入方法
     */
    @Override
    public boolean saveChatKeyword(AiChatKeyword keyword) {
        if (keyword == null || keyword.getKeywordId() == null) {
            return false; // 入参校验：雪花ID不能为空
        }
        int affectRows = aiChatKeywordMapper.insertKeyword(keyword);
        return affectRows > 0;
    }

    /**
     * 批量保存关键词：调用Mapper的批量插入方法
     */
    @Override
    @Transactional(rollbackFor = Exception.class) // 批量操作加事务，保证原子性
    public int batchSaveChatKeywords(List<AiChatKeyword> keywordList) {
        if (keywordList == null || keywordList.isEmpty()) {
            return 0; // 空列表直接返回0
        }
        // 校验列表中每个关键词的雪花ID是否存在
        for (AiChatKeyword keyword : keywordList) {
            if (keyword.getKeywordId() == null) {
                throw new IllegalArgumentException("批量保存关键词失败：存在未生成雪花ID的关键词");
            }
        }
        return aiChatKeywordMapper.batchInsertKeywords(keywordList);
    }

    /**
     * 查询用户+会话的关键词：调用Mapper的查询方法
     */
    @Override
    public List<AiChatKeyword> listChatKeywordsByUserAndSession(Integer userId, Long sessionId) {
        if (userId == null || sessionId == null) {
            return List.of(); // 入参为空返回空列表
        }
        return aiChatKeywordMapper.selectByUserIdAndSessionId(userId, sessionId);
    }

    /**
     * 删除用户+会话的关键词：调用Mapper的删除方法
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeChatKeywordsByUserAndSession(Integer userId, Long sessionId) {
        if (userId == null || sessionId == null) {
            return false;
        }
        int affectRows = aiChatKeywordMapper.deleteByUserIdAndSessionId(userId, sessionId);
        return affectRows > 0;
    }

    /**
     * 删除用户所有关键词：调用Mapper的删除方法
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int removeAllChatKeywordsByUser(Integer userId) {
        if (userId == null) {
            return 0;
        }
        return aiChatKeywordMapper.deleteByUserId(userId);
    }
}