package org.myfx.controls.aione.AiService.mapper.ai_chat_db;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.myfx.controls.aione.AiService.common.ai_chat_db.RecycleStatusEnum;
import org.myfx.controls.aione.AiService.entity.ai_chat_db.AiChatSession;

import java.util.List;

/**
 * AI对话会话主表Mapper接口
 *
 * @author xxx
 */
@Mapper
public interface AiChatSessionMapper {

    /**
     * 按用户ID查询系统级别（is_system_session=1）的活跃会话（仅1条，无则返回null）
     * @param userId 用户ID
     * @return 系统级别活跃会话（null=无）
     */
    AiChatSession selectSystemSessionByUser(
            @Param("userId") Integer userId
    );

    /**
     * 按用户ID+会话ID查询status=1的活跃会话（仅1条，无则返回null）
     * @param userId 用户ID
     * @param sessionId 会话ID
     * @return 活跃会话（null=无）
     */
    AiChatSession selectActiveSessionByUserAndSession(
            @Param("userId") Integer userId,
            @Param("sessionId") Long sessionId
    );

    /**
     * 检查指定用户是否有任意活跃会话（status=1，未删除）
     * @param userId 用户ID
     * @return 符合条件的会话数（0=无，≥1=有）
     */
    int checkUserHasActiveSession(@Param("userId") Integer userId);

    /**
     * 检查指定用户+指定会话是否为活跃状态（status=1，未删除）
     * @param userId 用户ID
     * @param sessionId 会话ID
     * @return 1=活跃，0=非活跃/不存在
     */
    int checkSessionIsActive(
            @Param("userId") Integer userId,
            @Param("sessionId") Long sessionId
    );

    /**
     * 根据用户ID查询当前激活的会话（核心：仅返回1条激活且未删除的会话）
     * @param userId 用户ID
     * @return 激活的会话实体（无激活会话则返回null）
     */
    AiChatSession selectActiveSessionByUserId(@Param("userId") Integer userId);

    /**
     * 批量查询多个用户的激活会话
     * @param userIds 用户ID列表
     * @return 会话列表（每个用户最多1条）
     */
    List<AiChatSession> selectActiveSessionByUserIds(@Param("userIds") List<Integer> userIds);

    /**
     * 按用户ID批量修改会话状态为0（关闭）（仅修改未删除的会话）
     * @param userId 用户ID（Integer类型，0=匿名）
     * @return 影响行数
     */
    int updateStatusToClosedByUserId(@Param("userId") Integer userId);

    /**
     * 按用户ID+会话ID精准修改会话状态为1（活跃）（仅修改未删除的会话）
     * @param userId 用户ID（Integer类型，0=匿名）
     * @param sessionId 会话ID
     * @return 影响行数
     */
    int updateStatusToActiveByUserAndSession(
            @Param("userId") Integer userId,
            @Param("sessionId") Long sessionId
    );

    // 需要加一个参数
    /**
     * 根据userId和sessionId更新会话信息（主要更新lastMessageContent，可扩展其他字段）
     * @param userId 归属用户ID
     * @param sessionId 会话ID（主键）
     * @param lastMessageContent 最后一条消息内容（预览用）
     * @return 影响行数
     */
    int updateSessionByUserIdAndSessionId(
            @Param("userId") Integer userId,
            @Param("sessionId") Long sessionId,
            @Param("lastMessageContent") String lastMessageContent
    );

    /**
     * 插入会话（幂等：存在则忽略）
     */
    int insertIgnore(AiChatSession session);

    /**
     * 切换会话回收站状态（0↔1）
     * @param sessionId 会话ID（雪花ID，Long类型）
     * @param userId 用户ID（自增主键，Integer类型，严格匹配你的微服务设计）
     * @param currentIsRecycle 会话当前的回收站状态（用专属枚举）
     * @return 受影响行数（1=切换成功，0=无匹配数据/状态已一致）
     */
    int toggleRecycleStatusBySessionIdAndUserId(
            @Param("sessionId") Long sessionId,
            @Param("userId") Integer userId, // 固定Integer，不用改！
            @Param("currentIsRecycle") RecycleStatusEnum currentIsRecycle); // 新枚举

    /**
     * 切换会话置顶状态（0↔1）
     * @param sessionId    会话ID
     * @param userId       用户ID
     * @param currentIsTop 当前置顶状态
     * @param topAt        置顶时间戳（置顶传毫秒值，取消置顶传null）
     * @return 受影响行数
     */
    int toggleTopStatusBySessionIdAndUserId(
            @Param("sessionId") Long sessionId,
            @Param("userId") Integer userId,
            @Param("currentIsTop") Integer currentIsTop,
            @Param("topAt") Long topAt
    );

    /**
     * 更新会话未读消息数（动态更新：字段有值则更新，无值不修改）
     * @param sessionId         会话ID
     * @param userId            用户ID
     * @param normalUnreadCount 非切分模式未读消息数（传null则不更新该字段）
     * @param splitUnreadCount  切分模式未读消息数（传null则不更新该字段）
     * @return 受影响行数
     */
    int updateSessionUnreadCount(
            @Param("sessionId") Long sessionId,
            @Param("userId") Integer userId,
            @Param("normalUnreadCount") Integer normalUnreadCount,
            @Param("splitUnreadCount") Integer splitUnreadCount
    );

    /**
     * 🔥 强制清零会话未读消息数
     * 同时将 normal_unread_count 和 split_unread_count 置为 0
     * @param sessionId  会话ID
     * @param userId     用户ID
     * @return 受影响行数
     */
    int clearSessionUnreadCount(
            @Param("sessionId") Long sessionId,
            @Param("userId") Integer userId
    );

    /**
     * 查询未删除的会话（按sessionId+userId）
     * @param sessionId 会话ID
     * @param userId 用户ID
     * @return 会话信息（null=无匹配数据）
     */
    AiChatSession selectBySessionIdAndUserId(
            @Param("sessionId") Long sessionId,
            @Param("userId") Integer userId);

    /**
     * 根据sessionId+userId查询全量会话（不过滤回收站，仅排除系统删除）
     */
    AiChatSession selectFullBySessionIdAndUserId(
            @Param("sessionId") Long sessionId,
            @Param("userId") Integer userId
    );

    /**
     * 查询未回收、未逻辑删除的会话（按sessionUuid+userId，核心安全查询）
     * @param sessionUuid 会话UUID
     * @param userId 用户ID
     * @return 会话信息（null=无匹配数据）
     */
    AiChatSession selectBySessionUuidAndUserId(
            @Param("sessionUuid") String sessionUuid,
            @Param("userId") Integer userId);

    /**
     * 批量将会话移入回收站（用户端删除操作，更新 is_recycle=1）
     * @param userId 用户ID
     * @param sessionIds 会话ID集合
     * @return 影响行数
     */
    int batchRecycleSessionsByUserIdAndSessionIds(
            @Param("userId") Integer userId,
            @Param("sessionIds") List<Long> sessionIds
    );

    /**
     * 批量根据sessionUuid数组和userId查询会话（未回收、未逻辑删除）
     * @param sessionUuids 会话UUID集合
     * @param userId 用户ID
     * @return 会话列表
     */
    List<AiChatSession> selectBySessionUuidsAndUserId(
            @Param("sessionUuids") List<String> sessionUuids,
            @Param("userId") Integer userId
    );

    /**
     * 查询未回收、未逻辑删除的会话（仅按sessionUuid，匿名用户/异常场景兜底）
     * @param sessionUuid 会话UUID
     * @return 会话信息（null=无匹配数据）
     */
    AiChatSession selectBySessionUuid(
            @Param("sessionUuid") String sessionUuid);

    /**
     * 按用户ID+回收站状态查询会话列表
     * @param userId 用户ID（自增主键，Integer类型）
     * @param isRecycle 回收站状态（用专属枚举）
     * @return 会话列表（无数据返回空列表）
     */
    List<AiChatSession> selectListByUserIdAndIsRecycle(
            @Param("userId") Integer userId, // 固定Integer，不用改！
            @Param("isRecycle") RecycleStatusEnum isRecycle); // 新枚举

    /**
     * 按用户ID查询所有会话（包含逻辑删除、回收站状态的全部数据）
     * @param userId 用户ID（Integer类型）
     * @return 该用户的所有会话列表（无数据返回空列表）
     */
    List<AiChatSession> SelectUserAllSessions(@Param("userId") Integer userId);

    /**
     * 物理删除1：按userId+sessionId删除创建时间<5分钟的会话（有约束确保仅1条）
     * @param userId 用户ID（Integer类型）
     * @param sessionId 会话ID
     * @return 影响行数（0=无数据，1=删除成功）
     */
    int physicalDeleteByUserIdAndCreateTimeLess5Min(
            @Param("userId") Integer userId,
            @Param("sessionId") Long sessionId
    );

    /**
     * 物理删除2：按userId+sessionId删除回收站中的数据（is_recycle=1）
     * @param userId 用户ID（Integer类型）
     * @param sessionId 会话ID
     * @return 影响行数（0=无符合条件数据，1=删除成功）
     */
    int physicalDeleteByUserIdAndSessionIdInRecycle(
            @Param("userId") Integer userId,
            @Param("sessionId") Long sessionId
    );

    /**
     * 逻辑删除：按userId标记所有对话为删除（is_deleted=1）
     * @param userId 用户ID（Integer类型）
     * @return 影响行数（0=无数据，>0=逻辑删除的条数）
     */
    int logicDeleteAllByUserId(@Param("userId") Integer userId);

    /**
     * 物理删除：按userId删除该用户下所有对话（高风险！谨慎使用）
     * 适配logicDeleteAllByUserId的物理删除版本，直接永久删除全量数据，无回收站/逻辑删除限制
     * @param userId 用户ID（Integer类型）
     * @return 影响行数（0=无数据，>0=物理删除的条数）
     */
    int physicalDeleteAllByUserId(@Param("userId") Integer userId);

    /**
     * 逻辑复原：按userId复原所有逻辑删除的对话（SAGA补偿专用）
     * @param userId 用户ID（Integer类型）
     * @return 影响行数（0=无数据，>0=复原的条数）
     */
    int logicRecoverAllByUserId(@Param("userId") Integer userId);
}