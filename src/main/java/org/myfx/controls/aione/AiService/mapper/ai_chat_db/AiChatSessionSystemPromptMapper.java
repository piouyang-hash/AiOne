package org.myfx.controls.aione.AiService.mapper.ai_chat_db;

import org.apache.ibatis.annotations.Param;
import org.myfx.controls.aione.AiService.entity.ai_chat_db.AiChatSessionSystemPrompt;

import java.util.List;

/**
 * AI对话会话-系统提示词Mapper接口
 * 包含CURD：新增（无时间）、查询（最新/所有）、删除（按用户+会话/按用户）
 */
public interface AiChatSessionSystemPromptMapper {

    /**
     * 新增系统提示词（数据库自动生成create_time/update_time）
     * @param prompt 系统提示词实体（无需设置createTime/updateTime）
     * @return 新增行数
     */
    int insert(AiChatSessionSystemPrompt prompt);

    /**
     * 查询指定用户+会话下的最大序列号
     * @param userId  用户ID
     * @param sessionId 会话ID
     * @return 大于0的最大序列号（无符合条件数据返回0）
     */
    Integer selectMaxSerialNumber(@Param("userId") Integer userId, @Param("sessionId") Long sessionId);

    /**
     * 查询指定用户+会话下序列号为0的系统提示词记录
     * @param userId  用户ID
     * @param sessionId 会话ID
     * @return 序列号为0的记录（无则返回null）
     */
    AiChatSessionSystemPrompt selectSerialNumberZero(@Param("userId") Integer userId, @Param("sessionId") Long sessionId);

    /**
     * 根据用户ID+会话ID查询最新的系统提示词（按序列号降序取1条，排除序列号0）
     * @param userId 用户ID
     * @param sessionId 会话ID
     * @return 最新的系统提示词（仅序列号>0）
     */
    AiChatSessionSystemPrompt selectLatestByUserIdAndSessionId(
            @Param("userId") Integer userId,
            @Param("sessionId") Long sessionId
    );

    /**
     * 根据用户ID+会话ID查询所有系统提示词（按序列号升序）
     * @param userId 用户ID
     * @param sessionId 会话ID
     * @return 系统提示词列表
     */
    List<AiChatSessionSystemPrompt> selectAllByUserIdAndSessionId(
            @Param("userId") Integer userId,
            @Param("sessionId") Long sessionId
    );

    /**
     * 根据用户ID+会话ID删除所有系统提示词
     * @param userId 用户ID
     * @param sessionId 会话ID
     * @return 删除行数
     */
    int deleteByUserIdAndSessionId(
            @Param("userId") Integer userId,
            @Param("sessionId") Long sessionId
    );

    /**
     * 根据用户ID删除所有系统提示词
     * @param userId 用户ID
     * @return 删除行数
     */
    int deleteByUserId(@Param("userId") Integer userId);
}