package org.myfx.controls.aione.AiService.service.base.ai_chat_db.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.AiService.common.ai_chat_db.AiChatSessionStatusEnum;
import org.myfx.controls.aione.AiService.common.ai_chat_db.AiChatSystemSessionEnum;
import org.myfx.controls.aione.AiService.common.ai_chat_db.RecycleStatusEnum;
import org.myfx.controls.aione.AiService.entity.ai_chat_db.AiChatSession;
import org.myfx.controls.aione.AiService.event.AiChatSessionPhysicalDeleteEvent;
import org.myfx.controls.aione.AiService.mapper.ai_chat_db.AiChatSessionMapper;
import org.myfx.controls.aione.AiService.service.base.ai_chat_db.AiChatSessionService;
import org.myfx.controls.aione.AiService.utils.UUIDUtils;
import org.myfx.controls.aione.ServiceCommon.context.UserContext;
import org.myfx.controls.aione.ServiceCommon.serviceEnum.LogicalDeleteEnum;
import org.myfx.controls.aione.ServiceCommon.utils.SnowflakeGenerator;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.List;

/**
 * AI对话会话业务层实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AiChatSessionServiceImpl implements AiChatSessionService {

    private final AiChatSessionMapper aiChatSessionMapper;
    private final ApplicationContext applicationContext;

    @Override
    public AiChatSession getChatSessionBySessionId(Long sessionId) {
        Integer userId = UserContext.getUserId();
        // 调用Mapper层按会话ID查询方法
        return aiChatSessionMapper.selectBySessionIdAndUserId(sessionId, userId);
    }

    /**
     * 根据会话UUID+用户ID查询对应的会话ID（雪花ID）
     * 核心逻辑：调用Mapper查询有效会话 → 提取会话ID返回
     */
    @Override
    public Long getSessionIdByUUID(String sessionUuid, Integer userId) {
        // 1. 入参非空校验（保障业务逻辑健壮性）
        if (sessionUuid == null || sessionUuid.isBlank()) {
            throw new IllegalArgumentException("根据UUID查询会话ID失败：会话UUID（sessionUuid）不能为空");
        }
        if (userId == null) {
            throw new IllegalArgumentException("根据UUID查询会话ID失败：用户ID（userId）不能为空");
        }

        // 2. 调用Mapper查询有效会话（未回收、未逻辑删除）
        AiChatSession chatSession = aiChatSessionMapper.selectBySessionUuidAndUserId(sessionUuid, userId);

        // 3. 日志打印（便于调试和问题排查）
        Long sessionId = chatSession != null ? chatSession.getSessionId() : null;
        if (sessionId == null) {
            log.info("[会话查询] 用户{}根据UUID{}未查询到有效会话", userId, sessionUuid);
        } else {
            log.info("[会话查询] 用户{}根据UUID{}查询到会话ID：{}",
                    userId, sessionUuid, sessionId);
        }

        // 4. 返回会话ID（无数据则返回null）
        return sessionId;
    }

    /**
     * 重载方法：仅传入UUID，自动从UserContext获取当前登录用户ID
     */
    @Override
    public Long getSessionIdByUUID(String sessionUuid) {
        // 1. 从UserContext获取当前登录用户ID，并做非空校验
        Integer userId = UserContext.getUserId();
        if (userId == null) {
            throw new IllegalArgumentException("根据UUID查询会话ID失败：从上下文获取的用户ID（userId）为空，请确认用户已登录");
        }

        // 2. 调用已有的双参数方法，复用核心逻辑
        return getSessionIdByUUID(sessionUuid, userId);
    }

    @Override
    public void batchRecycleSessions(Integer userId, List<Long> sessionIds) {
        // 1. 入参非空/非空集合校验
        Assert.notNull(userId, "用户ID不能为空");
        Assert.notEmpty(sessionIds, "会话ID集合不能为空");

        // 2. 调用Mapper执行批量回收
        int count = aiChatSessionMapper.batchRecycleSessionsByUserIdAndSessionIds(userId, sessionIds);

        // 3. 日志打印
        log.info("[会话批量回收] 用户{}成功回收{}个会话，会话ID列表：{}", userId, count, sessionIds);
    }

    @Override
    public List<Long> batchGetSessionIdsByUuids(Integer userId, List<String> sessionUuids) {
        // 1. 入参非空/非空集合校验
        Assert.notNull(userId, "用户ID不能为空");
        Assert.notEmpty(sessionUuids, "会话UUID集合不能为空");

        // 2. 调用Mapper批量查询会话
        List<AiChatSession> sessionList = aiChatSessionMapper.selectBySessionUuidsAndUserId(sessionUuids, userId);

        // 3. 提取会话ID列表
        List<Long> sessionIds = sessionList.stream()
                .map(AiChatSession::getSessionId)
                .toList();

        // 4. 日志打印
        log.info("[会话批量查询] 用户{}根据UUID数组查询到{}个有效会话，UUID列表：{}",
                userId, sessionIds.size(), sessionUuids);

        // 5. 返回结果（无数据返回空集合）
        return sessionIds;
    }

    @Override
    public AiChatSession getUserCurrentActiveSession(Integer userId) {
        // 1. 入参非空校验（保障业务逻辑健壮性）
        if (userId == null) {
            throw new IllegalArgumentException("获取用户当前激活会话失败：用户ID（userId）不能为空");
        }

        // 2. 调用Mapper查询激活会话（SQL已限制LIMIT 1）
        AiChatSession activeSession = aiChatSessionMapper.selectActiveSessionByUserId(userId);

        // 3. 日志打印（便于调试和问题排查）
        if (activeSession == null) {
            log.info("[会话查询] 用户{}暂无激活的会话", userId);
        } else {
            log.info("[会话查询] 用户{}当前激活会话：会话ID={}，会话标题={}",
                    userId, activeSession.getSessionId(), activeSession.getChatTitle());
        }

        return activeSession;
    }

    @Override
    public List<AiChatSession> batchGetUserCurrentActiveSession(List<Integer> userIds) {
        // 1. 参数校验
        if (userIds == null || userIds.isEmpty()) {
            throw new IllegalArgumentException("批量查询激活会话失败：用户ID列表不能为空");
        }

        // 2. 批量查询（1次SQL）
        List<AiChatSession> sessionList = aiChatSessionMapper.selectActiveSessionByUserIds(userIds);

        // 3. 日志（对齐原有格式）
        log.info("[批量会话查询] 用户ID列表：{}，查询到激活会话数量：{}", userIds, sessionList.size());

        return sessionList;
    }

    @Override
    public AiChatSession getUserCurrentActiveSession() {
        // 1. 从UserContext获取当前登录用户ID（适配上下文场景，简化调用）
        Integer userId = UserContext.getUserId();

        // 2. 校验上下文获取的userId有效性（避免空指针，明确异常语义）
        if (userId == null) {
            throw new IllegalArgumentException("获取用户当前激活会话失败：从UserContext中未获取到有效用户ID，请确认用户已登录");
        }

        // 3. 复用原有方法逻辑，保证业务一致性（无需重复写查询逻辑）
        return getUserCurrentActiveSession(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createUserChatSession(Integer userId, String sessionUuid, Integer roleId) {
        // 1. 入参校验
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("用户ID不能为空且需为正整数");
        }
        if (sessionUuid == null || sessionUuid.isBlank()) {
            throw new IllegalArgumentException("会话UUID不能为空（必须为标准UUIDv4格式）");
        }

        // 🔥 核心修改：roleId为空则默认赋值为1（硬编码）
        roleId = (roleId == null) ? 1 : roleId;

        // 2. 关闭该用户所有会话的激活状态
        closeAllSessionsByUserId(userId);

        // 3. 构建会话对象
        AiChatSession session = buildDefaultChatSession(userId, null);
        session.setSessionUuid(sessionUuid);
        session.setRoleId(roleId); // 赋值：要么传参，要么默认1

        Long sessionId = session.getSessionId();
        session.setIsSystemSession(AiChatSystemSessionEnum.NON_SYSTEM);

        // 4. 插入数据库
        aiChatSessionMapper.insertIgnore(session);

        // 5. 日志
        log.info("用户[{}]创建新会话 | 角色ID：{} | 会话UUID：{} | 会话ID：{}",
                userId, roleId, sessionUuid, sessionId);

        return sessionId;
    }

    @Override
    public boolean checkSessionIsActive(Integer userId, Long sessionId) {
        // 1. 参数校验
        Assert.notNull(userId, "用户ID不能为空");
        Assert.notNull(sessionId, "会话ID不能为空");

        // 2. 调用Mapper检查
        int activeCount = aiChatSessionMapper.checkSessionIsActive(userId, sessionId);
        boolean isActive = activeCount > 0;

        log.info("检查用户{}的会话{}活跃状态：{}", userId, sessionId, isActive ? "活跃" : "非活跃/不存在");
        return isActive;
    }

    @Override
    public boolean checkSessionIsActive(Long sessionId) {
        // 1. 从上下文获取userId
        Integer userId = UserContext.getUserId();
        Assert.notNull(userId, "上下文用户ID不能为空（未登录）");

        // 2. 复用手动传参方法
        return checkSessionIsActive(userId, sessionId);
    }

    /**
     * 按指定用户ID初始化聊天会话
     * 对齐登录态创建逻辑：先检查初始化状态，已初始化则直接返回现有会话ID
     * 未初始化则物理删除旧数据后重新初始化
     *
     * @param userId 目标用户ID（必填，禁止为null）
     * @return 新创建的会话ID
     * @throws IllegalArgumentException 用户ID为空时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long initChatSessionForUserId(Integer userId) {
        // 1. 严格参数校验
        if (userId == null) {
            throw new IllegalArgumentException("按用户ID创建会话失败：用户ID（userId）不能为空");
        }

        // 2. 【核心修改】幂等性检查：获取已初始化的会话ID
        Long initializedSessionId = getInitializedSessionId(userId);

        if (initializedSessionId != null) {
            // 用户已初始化，直接返回现有会话ID
            log.info("用户AI聊天已初始化，跳过重新创建，直接返回现有会话，用户ID: {}, 会话ID: {}",
                    userId, initializedSessionId);
            return initializedSessionId;
        }

        // 3. 用户未初始化，物理删除该用户下所有旧对话数据
        int deletedCount = aiChatSessionMapper.physicalDeleteAllByUserId(userId);
        log.info("用户未初始化，清理旧会话数据，物理删除条数: {}, 用户ID: {}", deletedCount, userId);

        // 4. 构建默认系统会话
        AiChatSession session = buildDefaultChatSession(userId, "萌芽");
        Long sessionId = session.getSessionId();
        // 设置为系统级会话（用户不可以删除）
        session.setIsSystemSession(AiChatSystemSessionEnum.SYSTEM);
        // 设置为“手机版对话”
        session.setLastMessageContent("手机版对话");

        // ========== 新增：生成并设置 SessionUUID ==========
        // 生成标准 UUIDv4 字符串（与前端格式一致）
        String sessionUuid = UUIDUtils.generateUUID();
        // 通过 setter 方法设置到会话对象中
        session.setSessionUuid(sessionUuid);
        log.info("为新会话生成UUID，用户ID: {}, 会话ID: {}, SessionUUID: {}",
                userId, sessionId, sessionUuid);

        // 5. 直接调用Mapper层插入
        aiChatSessionMapper.insertIgnore(session);

        log.info("用户AI聊天初始化完成，用户ID: {}, 新会话ID: {}", userId, sessionId);
        return sessionId;
    }

    @Override
    public Long getInitializedSessionId(Integer userId) {
        log.debug("获取用户系统级别AI聊天会话ID，用户ID: {}", userId);

        // 1. 调用Mapper获取用户的系统级别会话（已过滤status=1、is_system_session=1等条件，且仅返回1条）
        AiChatSession systemSession = aiChatSessionMapper.selectSystemSessionByUser(userId);

        // 2. 基础校验：系统会话是否存在
        if (systemSession == null) {
            log.debug("用户无有效系统级别会话，用户ID: {}", userId);
            return null;
        }

        // 3. 兜底校验（可选，因Mapper已筛选系统会话，仅做双重保障）
        if (systemSession.getIsSystemSession() != AiChatSystemSessionEnum.SYSTEM) {
            log.warn("异常：获取到非系统级别会话，用户ID: {}，会话ID: {}",
                    userId, systemSession.getSessionId());
            return null;
        }

        // 4. 返回系统会话ID
        Long sessionId = systemSession.getSessionId();
        log.debug("成功获取用户系统级别会话ID，用户ID: {}，会话ID: {}", userId, sessionId);
        return sessionId;
    }

    /**
     * 构建默认的对话会话对象（抽取公共逻辑，避免重复代码）
     *
     * @param userId    用户ID
     * @param chatTitle 对话标题（可为null，null时默认"新对话"）
     * @return 默认配置的会话对象
     */
    private AiChatSession buildDefaultChatSession(Integer userId, String chatTitle) {
        AiChatSession session = new AiChatSession();
        // 1. 生成雪花ID作为会话ID
        session.setSessionId(SnowflakeGenerator.generateId());
        // 2. 设置用户ID（Integer转Long，适配实体类字段类型）
        session.setUserId(userId);
        // 3. 设置对话标题：传入null则用默认"新对话"，否则用传入值
        session.setChatTitle(chatTitle == null ? "新对话" : chatTitle);
        // 4. 设置默认会话状态
        session.setStatus(AiChatSessionStatusEnum.ACTIVE);
        // 5. create_time由数据库默认生成，无需设置
        return session;
    }

    /**
     * 获取当前登录用户的所有未回收对话
     */
    @Override
    public List<AiChatSession> listUserAllNormalSessions() {
        // 从上下文获取用户ID（无需校验，确保有值）
        Integer userId = UserContext.getUserId();
        // 查询未删除（is_recycle=0）的会话列表
        return aiChatSessionMapper.selectListByUserIdAndIsRecycle(userId, RecycleStatusEnum.NOT_RECYCLE);
    }

    /**
     * 在回收站获取当前登录用户的所有已回收对话
     */
    @Override
    public List<AiChatSession> listUserAllDeletedSessions() {
        // 从上下文获取用户ID（无需校验，确保有值）
        Integer userId = UserContext.getUserId();
        // 查询已回收（is_recycle=1）的会话列表
        return aiChatSessionMapper.selectListByUserIdAndIsRecycle(userId, RecycleStatusEnum.RECYCLED);
    }

    @Override
    public void topSession(Long sessionId) {
        // 从上下文获取当前登录用户ID
        Integer userId = UserContext.getUserId();
        // 调用带userId的置顶会话方法
        topSessionByUserId(sessionId, userId);
    }

    @Override
    public void untopSession(Long sessionId) {
        // 从上下文获取当前登录用户ID
        Integer userId = UserContext.getUserId();
        // 调用带userId的取消置顶会话方法
        untopSessionByUserId(sessionId, userId);
    }

    @Override
    public void increaseSessionUnreadCount(Long sessionId, Integer normalUnreadCount, Integer splitUnreadCount) {
        // 从上下文获取当前登录用户ID
        Integer userId = UserContext.getUserId();
        // 调用带userId的4参更新方法
        increaseSessionUnreadCount(sessionId, userId, normalUnreadCount, splitUnreadCount);
    }

    /**
     * 【sessionId+userId】置顶会话（切换is_top=1）
     */
    @Override
    public void topSessionByUserId(Long sessionId, Integer userId) {
        // 1. 校验参数
        if (sessionId == null || userId == null) {
            throw new IllegalArgumentException("参数不合法：会话ID/用户ID不能为空");
        }

        // 2. 查询会话（确认会话存在且有权限）
        AiChatSession session = aiChatSessionMapper.selectBySessionIdAndUserId(sessionId, userId);
        if (session == null) {
            throw new RuntimeException("会话不存在或无操作权限");
        }

        // 3. 仅当当前未置顶时，执行置顶操作（0=未置顶）
        if (session.getIsTop() == 0) {
            // 传递当前状态：0（未置顶）
            // 置顶：传入 当前时间戳(毫秒)
            int updateCount = aiChatSessionMapper.toggleTopStatusBySessionIdAndUserId(
                    sessionId, userId, 0, System.currentTimeMillis()
            );
            if (updateCount == 0) {
                throw new RuntimeException("会话置顶失败，请重试");
            }
            log.info("用户{}将会话{}置顶成功", userId, sessionId);
        } else {
            log.warn("用户{}尝试置顶已置顶的会话{}，无需操作", userId, sessionId);
        }
    }

    /**
     * 【sessionId+userId】取消置顶会话（切换is_top=0）
     */
    @Override
    public void untopSessionByUserId(Long sessionId, Integer userId) {
        // 1. 校验参数
        if (sessionId == null || userId == null) {
            throw new IllegalArgumentException("参数不合法：会话ID/用户ID不能为空");
        }

        // 2. 查询会话（确认会话存在且有权限）
        AiChatSession session = aiChatSessionMapper.selectBySessionIdAndUserId(sessionId, userId);
        if (session == null) {
            throw new RuntimeException("会话不存在或无操作权限");
        }

        // 3. 仅当当前已置顶时，执行取消置顶操作（1=已置顶）
        if (session.getIsTop() == 1) {
            // 传递当前状态：1（已置顶）
            int updateCount = aiChatSessionMapper.toggleTopStatusBySessionIdAndUserId(
                    sessionId, userId, 1, null
            );
            if (updateCount == 0) {
                throw new RuntimeException("会话取消置顶失败，请重试");
            }
            log.info("用户{}将会话{}取消置顶成功", userId, sessionId);
        } else {
            log.warn("用户{}尝试取消未置顶的会话{}，无需操作", userId, sessionId);
        }
    }

    /**
     * 【sessionId+userId】累加会话未读消息数（动态累加：传值则累加，null不修改该字段）
     */
    @Override
    public void increaseSessionUnreadCount(Long sessionId, Integer userId, Integer normalUnreadCount, Integer splitUnreadCount) {
        // 1. 校验核心参数
        if (sessionId == null || userId == null) {
            throw new IllegalArgumentException("参数不合法：会话ID/用户ID不能为空");
        }

        // 2. 查询会话（获取原有未读消息数，校验会话存在）
        AiChatSession session = aiChatSessionMapper.selectBySessionIdAndUserId(sessionId, userId);
        if (session == null) {
            throw new RuntimeException("会话不存在或无操作权限");
        }

        // 3. 计算累加后的值（核心逻辑：传值则累加，null保持原值）
        Integer finalNormal = normalUnreadCount != null ?
                // 原有值 + 累加值（兼容原值为null的情况，默认0）
                (session.getNormalUnreadCount() == null ? 0 : session.getNormalUnreadCount()) + normalUnreadCount
                : null;

        Integer finalSplit = splitUnreadCount != null ?
                (session.getSplitUnreadCount() == null ? 0 : session.getSplitUnreadCount()) + splitUnreadCount
                : null;

        // 4. 执行更新（传入累加后的最终值，mapper按动态字段更新）
        int updateCount = aiChatSessionMapper.updateSessionUnreadCount(
                sessionId, userId, finalNormal, finalSplit
        );
        if (updateCount == 0) {
            throw new RuntimeException("会话未读消息数累加失败，请重试");
        }
        log.info("用户{}累加会话{}未读消息数成功，累加normal：{}，累加split：{}，最终normal：{}，最终split：{}",
                userId, sessionId, normalUnreadCount, splitUnreadCount, finalNormal, finalSplit);
    }

    // ====================== 任务1：基础清零方法（全参） ======================
    @Override
    public void clearSessionUnreadCount(Long sessionId, Integer userId) {
        // 1. Assert 统一参数校验
        Assert.notNull(sessionId, "会话ID不能为空");
        Assert.notNull(userId, "用户ID不能为空");

        // 2. 调用Mapper执行清零
        int updateCount = aiChatSessionMapper.clearSessionUnreadCount(sessionId, userId);

        // 3. 校验执行结果
        if (updateCount == 0) {
            throw new RuntimeException("会话未读消息数清零失败，请检查会话是否存在");
        }

        log.info("用户{}清零会话{}未读消息数成功（normal、split均置0）", userId, sessionId);
    }

    // ====================== 任务2：重载方法（自动获取userId） ======================
    @Override
    public void clearSessionUnreadCount(Long sessionId) {
        // 从上下文获取当前登录用户ID
        Integer userId = UserContext.getUserId();
        // 调用全参方法
        this.clearSessionUnreadCount(sessionId, userId);
    }

    /**
     * 实现业务接口方法：调用mapper查询所有会话
     */
    @Override
    public List<AiChatSession> listUserAllSessions(Integer userId) {
        // 1. 参数校验（避免空指针，提升健壮性）
        if (userId == null || userId <= 0) {
            return Collections.emptyList(); // 返回空列表，不返回null
        }

        // 2. 调用mapper接口方法查询所有会话
        List<AiChatSession> sessionList = aiChatSessionMapper.SelectUserAllSessions(userId);

        // 3. 兜底处理：如果mapper返回null，转为空列表（避免业务层处理null）
        return sessionList == null ? Collections.emptyList() : sessionList;
    }

    // ====================== 手动传userId的方法实现 ======================
    @Override
    public int closeAllSessionsByUserId(Integer userId) {
        // 1. 参数校验
        Assert.notNull(userId, "用户ID不能为空");

        // 2. 调用Mapper批量关闭
        int affectedRows = aiChatSessionMapper.updateStatusToClosedByUserId(userId);
        log.info("手动传userId：{}，批量关闭所有会话完成，影响行数：{}", userId, affectedRows);
        return affectedRows;
    }

    @Override
    public boolean activateSessionByUserAndSession(Integer userId, Long sessionId) {
        // 1. 参数校验
        Assert.notNull(userId, "用户ID不能为空");
        Assert.notNull(sessionId, "会话ID不能为空");

        // 2. 核心校验：检查该用户是否有任意活跃会话（语义更清晰）
        int activeCount = aiChatSessionMapper.checkUserHasActiveSession(userId);
        if (activeCount > 0) {
            log.warn("用户{}已有活跃会话，禁止激活新会话：{}", userId, sessionId);
            return false; // 已有活跃会话，返回失败
        }

        // 3. 补充校验：当前会话是否已激活（可选，防止重复激活同一会话）
        int sessionActive = aiChatSessionMapper.checkSessionIsActive(userId, sessionId);
        if (sessionActive > 0) {
            log.warn("用户{}的会话{}已处于激活状态，无需重复激活", userId, sessionId);
            return false;
        }

        // 4. 校验通过：激活指定会话
        int affectedRows = aiChatSessionMapper.updateStatusToActiveByUserAndSession(userId, sessionId);
        if (affectedRows > 0) {
            log.info("用户{}的会话{}激活成功", userId, sessionId);
            return true;
        } else {
            log.warn("用户{}的会话{}激活失败（会话不存在/已删除）", userId, sessionId);
            return false;
        }
    }

    // ====================== 从上下文获取userId的方法实现 ======================
    @Override
    public int closeAllSessions() {
        // 1. 从上下文获取userId
        Integer userId = UserContext.getUserId();
        Assert.notNull(userId, "上下文用户ID不能为空（未登录）");

        // 2. 复用手动传参的方法
        return closeAllSessionsByUserId(userId);
    }

    @Override
    public boolean activateSession(Long sessionId) {
        // 1. 从上下文获取userId
        Integer userId = UserContext.getUserId();
        Assert.notNull(userId, "上下文用户ID不能为空（未登录）");
        Assert.notNull(sessionId, "会话ID不能为空");

        // 2. 复用手动传参的方法
        return activateSessionByUserAndSession(userId, sessionId);
    }

    @Override
    public boolean updateLastMessageByUserIdAndSessionId(Integer userId, Long sessionId, String lastMessageContent) {
        // 参数校验：避免空值导致更新失败
        Assert.notNull(userId, "用户ID不能为空");
        Assert.notNull(sessionId, "会话ID不能为空");
        Assert.hasText(lastMessageContent, "最后一条消息内容不能为空");

        // 调用Mapper更新，返回影响行数>0则代表成功
        int affectRows = aiChatSessionMapper.updateSessionByUserIdAndSessionId(userId, sessionId, lastMessageContent);
        return affectRows > 0;
    }

    /**
     * 用户手动删除回收站中的会话
     */
    @Override
    public boolean manualDeleteRecycleSession(Long sessionId) {
        // 1. 参数校验：sessionId不能为空
        if (sessionId == null || sessionId <= 0) {
            return false;
        }

        // 2. 从上下文获取当前登录用户ID
        Integer userId = UserContext.getUserId();
        if (userId == null || userId <= 0) {
            return false; // 未获取到用户ID，无权限删除
        }

        // 3. 调用mapper执行回收站物理删除
        int affectRows = aiChatSessionMapper.physicalDeleteByUserIdAndSessionIdInRecycle(userId, sessionId);

        // 4. 影响行数=1则删除成功，否则失败
        return affectRows == 1;
    }

    /**
     * 【仅sessionId】放入回收站（自动从上下文取userId）
     * 语义修正：原“删除会话”改为“放入回收站”，贴合isRecycle字段语义
     */
    @Override
    public void deleteSession(Long sessionId) {
        // 从上下文获取当前登录用户ID
        Integer userId = UserContext.getUserId();

        // 调用带userId的放入回收站方法
        deleteSessionByUserId(sessionId, userId);
    }

    /**
     * 【仅sessionId】复原会话（自动从上下文取userId）
     * 语义保留：recover语义正确，无需修改
     */
    @Override
    public void recoverSession(Long sessionId) {
        Integer userId = UserContext.getUserId();

        // 调用带userId的复原方法
        recoverSessionByUserId(sessionId, userId);
    }

    /**
     * 【sessionId+userId】放入回收站（切换is_recycle=1）
     * 语义修正：原“删除会话”改为“放入回收站”，操作的是isRecycle而非isDeleted
     */
    @Override
    public void deleteSessionByUserId(Long sessionId, Integer userId) {
        // 1. 校验参数：抛出Java原生IllegalArgumentException（参数不合法）
        if (sessionId == null || userId == null) {
            throw new IllegalArgumentException("参数不合法：会话ID/用户ID不能为空");
        }

        // 2. 查询会话（确认会话存在且有权限）
        AiChatSession session = aiChatSessionMapper.selectFullBySessionIdAndUserId(sessionId, userId);
        if (session == null) {
            throw new RuntimeException("会话不存在或无操作权限");
        }

        // 3. 仅当当前未放入回收站时，才切换为“已放入回收站”
        // 关键修正1：判断isRecycle（回收站状态），而非isDeleted（系统删除）
        if (session.getIsRecycle() == RecycleStatusEnum.NOT_RECYCLE) {
            // 关键修正2：传递当前回收站状态（NOT_RECYCLE），确保SQL只在状态匹配时切换
            int updateCount = aiChatSessionMapper.toggleRecycleStatusBySessionIdAndUserId(
                    sessionId, userId, RecycleStatusEnum.NOT_RECYCLE
            );
            if (updateCount == 0) {
                throw new RuntimeException("会话放入回收站失败，请重试");
            }
            log.info("用户{}将会话{}放入回收站成功", userId, sessionId);
        } else {
            log.warn("用户{}尝试将已放入回收站的会话{}再次放入，无需操作", userId, sessionId);
        }
    }

    /**
     * 【sessionId+userId】复原会话（从回收站移出，切换is_recycle=0）
     * 语义保留：recover语义正确，仅修正内部判断逻辑
     */
    @Override
    public void recoverSessionByUserId(Long sessionId, Integer userId) {
        // 1. 校验参数：抛出Java原生IllegalArgumentException（参数不合法）
        if (sessionId == null || userId == null) {
            throw new IllegalArgumentException("参数不合法：会话ID/用户ID不能为空");
        }

        // 2. 查询会话（确认会话存在且有权限）
        AiChatSession session = aiChatSessionMapper.selectFullBySessionIdAndUserId(sessionId, userId);
        if (session == null) {
            throw new RuntimeException("会话不存在或无操作权限");
        }

        // 3. 仅当当前已放入回收站时，才切换为“未放入回收站”
        // 关键修正1：判断isRecycle（回收站状态），而非isDeleted（系统删除）
        if (session.getIsRecycle() == RecycleStatusEnum.RECYCLED) {
            // 关键修正2：传递当前回收站状态（RECYCLED），确保SQL只在状态匹配时切换
            int updateCount = aiChatSessionMapper.toggleRecycleStatusBySessionIdAndUserId(
                    sessionId, userId, RecycleStatusEnum.RECYCLED
            );
            if (updateCount == 0) {
                throw new RuntimeException("会话从回收站复原失败，请重试");
            }
            log.info("用户{}将会话{}从回收站复原成功", userId, sessionId);
        } else {
            log.warn("用户{}尝试复原未放入回收站的会话{}，无需操作", userId, sessionId);
        }
    }

    /**
     * 物理删除（注册失败补偿）：校验仅1条数据才删除（sessionId取自查询结果，时间校验交给SQL）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean physicalDeleteForRegisterFail(Integer userId) {
        // 1. 参数校验：避免空指针
        if (userId == null || userId <= 0) {
            return false;
        }

        // 2. 本地调用查询用户所有会话
        List<AiChatSession> allSessions = listUserAllSessions(userId);

        // 3. 校验：仅当会话数量=1时继续
        if (allSessions == null || allSessions.size() != 1) {
            return false;
        }

        // 4. 获取查询结果中唯一的sessionId（无需传入，直接用查到的）
        AiChatSession targetSession = allSessions.get(0);
        Long sessionId = targetSession.getSessionId();

        boolean isSystemSession = AiChatSystemSessionEnum.SYSTEM.equals(targetSession.getIsSystemSession());

        // 4.2 非系统会话直接返回false，不执行删除
        if (!isSystemSession) {
            log.warn("物理删除失败：用户{}的会话{}非系统级会话，禁止删除", userId, sessionId);
            return false;
        }

        // 5. 调用mapper执行物理删除（时间校验交给SQL，代码层不处理）
        int affectRows = aiChatSessionMapper.physicalDeleteByUserIdAndCreateTimeLess5Min(userId, sessionId);

        // 6. 仅当删除成功（影响行数=1）时，才发布事件
        if (affectRows == 1) {
            // 发布物理删除会话事件（使用查询到的sessionId）
            applicationContext.publishEvent(new AiChatSessionPhysicalDeleteEvent(this,
                    userId, sessionId));
        }

        // 7. 仅当删除成功返回true，否则false
        return affectRows == 1;
    }

    /**
     * 逻辑删除（账号注销）：校验有数据才执行
     */
    @Override
    public boolean logicDeleteAllForAccountCancel(Integer userId) {
        // 1. 参数校验
        if (userId == null || userId <= 0) {
            return false;
        }

        // 2. 校验用户有会话数据
        List<AiChatSession> allSessions = listUserAllSessions(userId);
        if (allSessions == null || allSessions.isEmpty()) {
            return false; // 无数据，退出SAGA
        }

        // 3. 调用mapper执行逻辑删除
        int affectRows = aiChatSessionMapper.logicDeleteAllByUserId(userId);

        // 4. 影响行数>0则返回true（继续SAGA），否则false
        return affectRows > 0;
    }

    /**
     * 逻辑复原（注销失败补偿）：校验有逻辑删除数据才执行
     */
    @Override
    public boolean logicRecoverForCancelFail(Integer userId) {
        // 1. 参数校验
        if (userId == null || userId <= 0) {
            return false;
        }

        // 2. 校验用户有逻辑删除的会话
        List<AiChatSession> allSessions = listUserAllSessions(userId);
        if (allSessions == null || allSessions.isEmpty()) {
            return false;
        }
        // 筛选is_deleted=1的会话
        long deletedCount = allSessions.stream()
                .filter(session -> LogicalDeleteEnum.DELETED == session.getIsDeleted())
                .count();
        if (deletedCount == 0) {
            return false; // 无逻辑删除数据，退出SAGA
        }

        // 3. 调用mapper执行逻辑复原
        int affectRows = aiChatSessionMapper.logicRecoverAllByUserId(userId);

        // 4. 影响行数>0则返回true（继续SAGA），否则false
        return affectRows > 0;
    }
}