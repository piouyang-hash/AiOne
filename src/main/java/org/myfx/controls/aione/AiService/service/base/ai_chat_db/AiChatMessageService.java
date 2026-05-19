package org.myfx.controls.aione.AiService.service.base.ai_chat_db;

import org.myfx.controls.aione.AiService.aiClient.advisor.dto.ChatInformationDTO;
import org.myfx.controls.aione.AiService.dto.AiReplyMessageDTO;
import org.myfx.controls.aione.AiService.dto.ChatMessageCombineDTO;
import org.myfx.controls.aione.AiService.entity.ai_chat_db.AiChatMessage;

import java.util.List;

/**
 * AI对话消息业务层接口
 */
public interface AiChatMessageService {

    /**
     * 通过对话ID查询该对话下的所有消息（按创建时间正序排列）
     * @param sessionId 对话ID（雪花ID）
     * @return 该对话下的所有消息列表
     */
    List<AiChatMessage> listChatMessagesBySessionId(Long sessionId);

    /**
     * 通过用户ID+对话ID查询该对话下的所有消息（按创建时间正序排列）
     * 重载方法：新增userId参数，精准限定用户维度的会话查询
     * @param userId 用户ID
     * @param sessionId 对话ID（雪花ID）
     * @return 该用户+该对话下的所有消息列表
     */
    List<AiChatMessage> listChatMessagesBySessionId(Integer userId, Long sessionId);

    /**
     * 统计指定会话+用户的消息总数
     * @param userId 用户ID
     * @param sessionId 对话ID（雪花ID）
     * @return 该会话下当前用户的消息总数（无数据返回0）
     */
    Integer countChatMessagesBySessionIdAndUserId(Integer userId, Long sessionId);

    /**
     * 按用户ID、会话ID，查询指定数量的最新聊天历史（按时间倒序）
     * @param userId 用户ID
     * @param sessionId 会话ID
     * @param limitNum 指定查询的数量
     * @return 最新的聊天消息列表
     */
    List<AiChatMessage> listLatestChatMessages(Integer userId, Long sessionId, Integer limitNum);

    /**
     * 按会话ID，查询指定数量的最新聊天历史（重载：自动获取当前用户ID）
     * @param sessionId 会话ID
     * @param limitNum 指定查询的数量
     * @return 最新的聊天消息列表
     */
    List<AiChatMessage> listLatestChatMessages(Long sessionId, Integer limitNum);

    /**
     * 上拉加载更早的历史消息（游标分页）
     * @param userId      用户ID
     * @param sessionId   会话ID
     * @param minMessageId 当前页面最小消息ID
     * @param limitNum    每页加载数量
     * @return 历史消息列表（按时间正序，用于前端展示）
     */
    List<AiChatMessage> listMoreHistoryMessages(
            Integer userId,
            Long sessionId,
            Long minMessageId,
            Integer limitNum
    );

    /**
     * 上拉加载更早的历史消息（重载：自动获取当前用户ID）
     * @param sessionId 会话ID
     * @param minMessageId 当前页面最小消息ID
     * @param limitNum 每页加载数量
     * @return 历史消息列表
     */
    List<AiChatMessage> listMoreHistoryMessages(Long sessionId, Long minMessageId, Integer limitNum);

    /**
     * 按用户ID、会话ID，查询最新两条聊天记录，返回第二条记录的创建时间戳（毫秒级）
     * @param userId 用户ID
     * @param sessionId 会话ID
     * @return 第二条最新消息的创建时间戳（毫秒级，无足够数据时返回null）
     */
    Long getLastChatTimestamp(Integer userId, Long sessionId);

    /**
     * 插入用户发送的聊天消息（使用DTO统一传参）
     * 自动生成父消息ID，幂等插入：存在则忽略
     */
    void addUserChatMessage(ChatInformationDTO dto);

    /**
     * 插入AI回复的消息（角色固定为3，自动生成消息ID）
     * 幂等：存在则忽略，不会重复插入
     *
     * @param dto AI回复消息DTO
     * @return 影响行数：1=插入成功，0=已存在无需插入
     */
    int addAIReplyMessage(AiReplyMessageDTO dto);


    /**
     * 预创建AI流式回复【占位消息】（DTO传参，无返回值，内部赋值DTO）
     */
    void createAiStreamPlaceholder(ChatInformationDTO dto);

    /**
     * 更新AI流式消息的最终内容（流式结束后调用）
     * 三重定位防越权，content必更新，splitContentJson可选更新
     */
    void updateAiStreamFinalContent(ChatInformationDTO dto);

    // 新增1：删除消息
    void deleteMessageByParentMsgId(Long parentMsgId, Long sessionId);

    // 新增2：查询最近一条消息
    AiChatMessage getLatestUserMessageBySessionId(Long sessionId);

    /**
     * 【带userId】根据父消息ID+会话ID+用户ID查询关联消息（语义化：找唯一关联的parent消息）
     * @param parentMsgId 父消息ID（关联唯一消息）
     * @param sessionId 会话ID（防越权）
     * @param userId 用户ID（Integer类型，外部传入）
     * @return 关联的单条消息（无则返回null）
     */
    AiChatMessage getUniqueParentAssociatedMessage(Long parentMsgId, Long sessionId, Integer userId);

    /**
     * 【不带userId】根据父消息ID+会话ID查询关联消息（自动从上下文取当前登录用户ID）
     * @param parentMsgId 父消息ID（关联唯一消息）
     * @param sessionId 会话ID（防越权）
     * @return 关联的单条消息（无则返回null）
     */
    AiChatMessage getUniqueParentAssociatedMessage(Long parentMsgId, Long sessionId);

    /**
     * 【事务版】合并插入用户消息+AI回复消息
     * 两个操作在同一个事务中，要么都成功，要么都回滚
     *
     * @param dto 合并版请求参数（内部包含userId字段）
     * @return 生成的父消息ID（关联用户-AI）
     */
    Long handleChatMessage(ChatMessageCombineDTO dto);

}