package org.myfx.controls.aione.AiService.mapper.ai_chat_db;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.myfx.controls.aione.AiService.entity.ai_chat_db.AiChatMessage;

import java.util.List;

/**
 * AI对话消息明细表Mapper接口
 * @author xxx
 */
@Mapper
public interface AiChatMessageMapper {

    /**
     * 查询数据库中所有AI聊天消息
     * @return 全部消息列表（按主键正序）
     */
    List<AiChatMessage> selectAll();

    /**
     * 按用户ID+会话ID查询所有消息（按创建时间排序）
     * @param userId 用户ID（Integer类型）
     * @param sessionId 对话ID
     * @return 消息列表
     */
    List<AiChatMessage> selectBySessionId(
            @Param("userId") Integer userId,
            @Param("sessionId") Long sessionId
    );

    /**
     * 按用户ID+会话ID统计消息数量
     * @param userId 用户ID（Integer类型）
     * @param sessionId 对话ID（雪花ID）
     * @return 该会话下当前用户的消息总数（无数据返回0）
     */
    Integer countBySessionIdAndUserId(
            @Param("userId") Integer userId,
            @Param("sessionId") Long sessionId
    );

    /**
     * 按用户ID、会话ID，查询指定数量的最新聊天历史（按时间倒序）
     * @param userId 用户ID
     * @param sessionId 会话ID
     * @param limitNum 指定查询的数量（翻译：specified number）
     * @return 最新的聊天消息列表（按时间从新到旧）
     */
    List<AiChatMessage> selectLatestMessagesBySessionId(
            @Param("userId") Integer userId,
            @Param("sessionId") Long sessionId,
            @Param("limitNum") Integer limitNum
    );

    /**
     * 上拉加载更早的历史消息（游标分页：根据最小消息ID查询更早数据）
     * @param userId      用户ID
     * @param sessionId   会话ID
     * @param minMessageId 当前页面最小的消息ID（雪花ID）
     * @param limitNum    每页加载数量
     * @return 历史消息列表（按消息ID倒序）
     */
    List<AiChatMessage> selectMoreHistoryMessages(
            @Param("userId") Integer userId,
            @Param("sessionId") Long sessionId,
            @Param("minMessageId") Long minMessageId,
            @Param("limitNum") Integer limitNum
    );

    /**
     * 插入消息（幂等：存在则忽略）
     */
    int insertIgnore(AiChatMessage message);

    /**
     * 插入AI流式回复【占位消息】（幂等）
     * 用途：AI开始流式回复前，预创建数据库占位记录，生成真实messageId
     * 固定填充content特殊占位符，其余字段由业务层传入
     */
    int insertAiStreamPlaceholder(AiChatMessage message);

    /**
     * 动态更新AI流式消息最终内容
     */
    int updateAiStreamContentById(AiChatMessage message);

    // 新增1：删除消息（物理删除）
    int deleteByParentMsgIdAndSessionIdAndUserId(
            @Param("parentMsgId") Long parentMsgId,
            @Param("sessionId") Long sessionId,
            @Param("userId") Integer userId
    );

    // 新增2：查询最近一条消息
    AiChatMessage selectLatestUserMessageBySessionIdAndUserId(
            @Param("sessionId") Long sessionId,
            @Param("userId") Integer userId
    );

    AiChatMessage selectByParentMsgIdAndSessionIdAndUserId(
            @Param("parentMsgId") Long parentMsgId,
            @Param("sessionId") Long sessionId,
            @Param("userId") Integer userId
    );
}