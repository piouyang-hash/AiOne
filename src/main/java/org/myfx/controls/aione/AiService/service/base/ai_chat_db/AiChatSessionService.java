package org.myfx.controls.aione.AiService.service.base.ai_chat_db;

import org.myfx.controls.aione.AiService.entity.ai_chat_db.AiChatSession;

import java.util.List;

/**
 * AI对话会话业务层接口
 */
public interface AiChatSessionService {

    /**
     * 通过对话ID查询对话会话信息
     *
     * @param sessionId 对话ID（雪花ID）
     * @return 对话会话信息
     */
    AiChatSession getChatSessionBySessionId(Long sessionId);

    /**
     * 根据会话UUID+用户ID查询对应的会话ID（雪花ID）
     * @param sessionUuid 会话UUID（标准UUIDv4格式）
     * @param userId 用户ID
     * @return 会话ID（雪花ID），null=无匹配的有效会话
     */
    Long getSessionIdByUUID(String sessionUuid, Integer userId);

    /**
     * 重载方法：仅传入会话UUID，自动从UserContext获取当前登录用户ID
     * @param sessionUuid 会话UUID（标准UUIDv4格式）
     * @return 会话ID（雪花ID），null=无匹配的有效会话
     */
    Long getSessionIdByUUID(String sessionUuid);

    /**
     * 批量将会话移入回收站（用户端删除操作）
     * @param userId 用户ID
     * @param sessionIds 会话ID集合
     */
    void batchRecycleSessions(Integer userId, List<Long> sessionIds);

    /**
     * 根据会话UUID数组 + 用户ID 批量查询会话ID列表
     * @param userId 用户ID
     * @param sessionUuids 会话UUID集合
     * @return 会话ID列表（无数据返回空集合）
     */
    List<Long> batchGetSessionIdsByUuids(Integer userId, List<String> sessionUuids);

    /**
     * 获取用户当前激活的会话（业务语义：用户正在使用的会话）
     * @param userId 用户ID
     * @return 激活的会话实体（无激活会话则返回null）
     */
    AiChatSession getUserCurrentActiveSession(Integer userId);

    /**
     * 批量获取多个用户的当前激活会话（每个用户仅1条）
     * @param userIds 用户ID列表
     * @return 激活会话列表
     */
    List<AiChatSession> batchGetUserCurrentActiveSession(List<Integer> userIds);

    /**
     * 获取用户当前激活的会话（重载：从UserContext自动获取当前登录用户ID，简化调用）
     * @return 激活的会话实体（无激活会话则返回null）
     * @throws IllegalArgumentException 若UserContext中未获取到有效用户ID
     */
    AiChatSession getUserCurrentActiveSession();

    /**
     * 新建用户的对话会话（传入用户ID+会话UUID+角色ID，不再自动获取上下文）
     * 幂等：存在则忽略，不会重复创建
     *
     * @param userId 用户ID（非空，正整数）
     * @param sessionUuid 会话UUID（标准UUIDv4格式，非空，作为会话的UUID标识）
     * @param roleId 角色ID（非空，正整数）
     * @return 生成的对话ID（雪花ID）
     */
    Long createUserChatSession(Integer userId, String sessionUuid, Integer roleId);

    /**
     * 检查指定用户+指定会话是否为活跃状态（status=1，未删除）
     * @param userId 用户ID
     * @param sessionId 会话ID
     * @return true=活跃，false=非活跃/不存在/已删除
     */
    boolean checkSessionIsActive(Integer userId, Long sessionId);

    /**
     * 【上下文获取userId】检查当前登录用户的指定会话是否为活跃状态
     * @param sessionId 会话ID
     * @return true=活跃，false=非活跃/不存在/已删除
     */
    boolean checkSessionIsActive(Long sessionId);

    /**
     * 新建指定用户ID的对话会话（传入用户ID）
     * 幂等：存在则忽略，不会重复创建
     *
     * @param userId 指定的用户ID（Integer类型）
     * @return 生成的对话ID（雪花ID）
     */
    Long initChatSessionForUserId(Integer userId);

    /**
     * 获取已初始化的AI聊天会话ID
     * 如果用户已正确初始化，返回会话ID；否则返回null
     *
     * @param userId 用户ID
     * @return 初始化会话的ID，如果未初始化则返回null
     */
    Long getInitializedSessionId(Integer userId);

    /**
     * 获取当前登录用户的所有未删除对话（正常会话列表）
     * @return 未删除的会话列表
     */
    List<AiChatSession> listUserAllNormalSessions();

    /**
     * 在回收站获取当前登录用户的所有已删除对话
     * @return 已删除的会话列表（回收站数据）
     */
    List<AiChatSession> listUserAllDeletedSessions();

    /**
     * 【仅sessionId】置顶会话
     * @param sessionId 会话ID
     */
    void topSession(Long sessionId);

    /**
     * 【仅sessionId】取消置顶会话
     * @param sessionId 会话ID
     */
    void untopSession(Long sessionId);

    /**
     * 【sessionId】更新会话未读消息数（动态更新：传值则更新，null不修改）
     * @param sessionId 会话ID
     * @param normalUnreadCount 非切分模式未读消息数
     * @param splitUnreadCount 切分模式未读消息数
     */
    void increaseSessionUnreadCount(Long sessionId, Integer normalUnreadCount, Integer splitUnreadCount);

    /**
     * 【sessionId+userId】置顶会话（切换is_top=1）
     * @param sessionId 会话ID
     * @param userId 用户ID
     */
    void topSessionByUserId(Long sessionId, Integer userId);

    /**
     * 【sessionId+userId】取消置顶会话（切换is_top=0）
     * @param sessionId 会话ID
     * @param userId 用户ID
     */
    void untopSessionByUserId(Long sessionId, Integer userId);

    /**
     * 【sessionId+userId】累加会话未读消息数（动态累加：传值则累加，null不修改该字段）
     * @param sessionId 会话ID
     * @param userId 用户ID
     * @param normalUnreadCount 非切分模式【需要累加的数量】（传null则不累加）
     * @param splitUnreadCount 切分模式【需要累加的数量】（传null则不累加）
     */
    void increaseSessionUnreadCount(Long sessionId, Integer userId, Integer normalUnreadCount, Integer splitUnreadCount);

    /**
     * 强制清零会话未读消息数（normal + split 同时置0）
     * @param sessionId  会话ID
     * @param userId     用户ID
     */
    void clearSessionUnreadCount(Long sessionId, Integer userId);

    /**
     * 强制清零会话未读消息数（重载：自动获取当前登录用户ID）
     * @param sessionId  会话ID
     */
    void clearSessionUnreadCount(Long sessionId);

    /**
     * 按用户ID查询所有会话（包含逻辑删除、回收站状态的全部数据）
     * @param userId 用户ID（Integer类型）
     * @return 该用户的所有会话列表（无数据返回空列表）
     */
    List<AiChatSession> listUserAllSessions(Integer userId);

    // ====================== 手动传userId的方法 ======================
    /**
     * 【手动传userId】批量关闭指定用户的所有会话（置为status=0）
     * @param userId 用户ID（0=匿名）
     * @return 影响行数
     */
    int closeAllSessionsByUserId(Integer userId);

    /**
     * 【手动传userId】激活指定用户的指定会话（前置校验：无已激活会话才执行）
     * @param userId 用户ID（0=匿名）
     * @param sessionId 会话ID
     * @return true=激活成功，false=已有激活会话/激活失败
     */
    boolean activateSessionByUserAndSession(Integer userId, Long sessionId);

    // ====================== 从上下文获取userId的方法 ======================
    /**
     * 【上下文获取userId】批量关闭当前登录用户的所有会话（置为status=0）
     * @return 影响行数
     */
    int closeAllSessions();

    /**
     * 【仅sessionId】删除会话（切换is_deleted为1）
     * @param sessionId 会话ID
     */
    void deleteSession(Long sessionId);

    /**
     * 【仅sessionId】复原会话（切换is_deleted为0）
     * @param sessionId 会话ID
     */
    void recoverSession(Long sessionId);

    /**
     * 【sessionId+userId】删除会话（切换is_deleted为1）
     * @param sessionId 会话ID
     * @param userId 用户ID
     */
    void deleteSessionByUserId(Long sessionId, Integer userId);

    /**
     * 【sessionId+userId】复原会话（切换is_deleted为0）
     * @param sessionId 会话ID
     * @param userId 用户ID
     */
    void recoverSessionByUserId(Long sessionId, Integer userId);

    /**
     * 【上下文获取userId】激活当前登录用户的指定会话（前置校验：无已激活会话才执行）
     * @param sessionId 会话ID
     * @return true=激活成功，false=已有激活会话/激活失败
     */
    boolean activateSession(Long sessionId);

    /**
     * 【手动传用户ID】更新会话最后一条消息内容
     * @param userId 用户ID（手动传入）
     * @param sessionId 会话ID
     * @param lastMessageContent 最后一条消息内容（AI回复）
     * @return 是否更新成功
     */
    boolean updateLastMessageByUserIdAndSessionId(Integer userId, Long sessionId, String lastMessageContent);

    /**
     * 用户手动删除回收站中的会话（仅删除is_recycle=1的会话）
     * @param sessionId 会话ID（前端传入）
     * @return true=删除成功，false=删除失败（无权限/会话不在回收站/参数无效）
     */
    boolean manualDeleteRecycleSession(Long sessionId);

    /**
     * 物理删除（注册失败补偿）：仅当用户会话仅1条且匹配条件时删除
     * @param userId 用户ID（Integer类型）
     * @return true=删除成功/继续SAGA；false=不满足条件/退出SAGA
     */
    boolean physicalDeleteForRegisterFail(Integer userId);

    /**
     * 逻辑删除（账号注销）：标记用户所有会话为删除
     * @param userId 用户ID（Integer类型）
     * @return true=执行成功/继续SAGA；false=无数据/退出SAGA
     */
    boolean logicDeleteAllForAccountCancel(Integer userId);

    /**
     * 逻辑复原（注销失败补偿）：复原用户所有逻辑删除的会话
     * @param userId 用户ID（Integer类型）
     * @return true=复原成功/继续SAGA；false=无数据/退出SAGA
     */
    boolean logicRecoverForCancelFail(Integer userId);
}