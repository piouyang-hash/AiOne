package org.myfx.controls.aione.AiService.mapper.key_word_db;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.myfx.controls.aione.AiService.entity.key_word_db.AiChatKeyword;

import java.util.List;

/**
 * AI对话关键词表Mapper接口
 */
@Mapper
public interface AiChatKeywordMapper {

    /**
     * 新增单条关键词（创建时间由数据库自动生成，无需传入）
     * @param aiChatKeyword 关键词实体
     * @return 影响行数
     */
    int insertKeyword(AiChatKeyword aiChatKeyword);

    /**
     * 批量新增关键词（适配多关键词批量插入场景）
     * @param keywordList 关键词列表
     * @return 影响行数
     */
    int batchInsertKeywords(@Param("keywordList") List<AiChatKeyword> keywordList);

    /**
     * 根据用户ID和会话ID查询所有关键词记录
     * @param userId 用户ID
     * @param sessionId 会话ID
     * @return 关键词列表
     */
    List<AiChatKeyword> selectByUserIdAndSessionId(@Param("userId") Integer userId, @Param("sessionId") Long sessionId);

    /**
     * 根据用户ID和会话ID删除一个对话中的所有关键词记录
     * @param userId 用户ID
     * @param sessionId 会话ID
     * @return 影响行数
     */
    int deleteByUserIdAndSessionId(@Param("userId") Integer userId, @Param("sessionId") Long sessionId);

    /**
     * 根据用户ID删除该用户的所有关键词记录
     * @param userId 用户ID
     * @return 影响行数
     */
    int deleteByUserId(@Param("userId") Integer userId);
}