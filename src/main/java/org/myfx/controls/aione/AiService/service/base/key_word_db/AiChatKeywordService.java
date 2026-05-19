package org.myfx.controls.aione.AiService.service.base.key_word_db;

import org.myfx.controls.aione.AiService.entity.key_word_db.AiChatKeyword;

import java.util.List;

/**
 * AI对话关键词业务服务接口
 * 负责关键词的新增、查询、删除等核心业务逻辑
 */
public interface AiChatKeywordService {

    /**
     * 保存单条对话关键词
     * @param keyword 关键词实体（含雪花ID、会话/消息关联信息、关键词内容等）
     * @return 保存成功返回true，失败返回false
     */
    boolean saveChatKeyword(AiChatKeyword keyword);

    /**
     * 批量保存对话关键词（核心场景：单条消息提取多个关键词）
     * @param keywordList 关键词列表
     * @return 保存成功的条数
     */
    int batchSaveChatKeywords(List<AiChatKeyword> keywordList);

    /**
     * 查询指定用户+指定会话下的所有关键词
     * @param userId 用户ID（0=匿名用户）
     * @param sessionId 会话ID
     * @return 关键词列表（按优先级排序）
     */
    List<AiChatKeyword> listChatKeywordsByUserAndSession(Integer userId, Long sessionId);

    /**
     * 删除指定用户+指定会话下的所有关键词（删除单个对话的关键词）
     * @param userId 用户ID
     * @param sessionId 会话ID
     * @return 删除成功返回true，失败返回false
     */
    boolean removeChatKeywordsByUserAndSession(Integer userId, Long sessionId);

    /**
     * 删除指定用户的所有关键词（用户级批量清理）
     * @param userId 用户ID
     * @return 删除成功的条数
     */
    int removeAllChatKeywordsByUser(Integer userId);
}