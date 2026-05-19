package org.myfx.controls.aione.AiService.service.base.ai_chat_db.impl;

import cn.hutool.core.collection.CollUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.AiService.aiClient.advisor.dto.ChatInformationDTO;
import org.myfx.controls.aione.AiService.common.ai_chat_db.ChatRoleEnum;
import org.myfx.controls.aione.AiService.dto.AiReplyMessageDTO;
import org.myfx.controls.aione.AiService.dto.ChatMessageCombineDTO;
import org.myfx.controls.aione.AiService.entity.ai_chat_db.AiChatMessage;
import org.myfx.controls.aione.AiService.mapper.ai_chat_db.AiChatMessageMapper;
import org.myfx.controls.aione.AiService.service.base.ai_chat_db.AiChatMessageService;
import org.myfx.controls.aione.ServiceCommon.context.UserContext;
import org.myfx.controls.aione.ServiceCommon.utils.SnowflakeGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;

/**
 * AI对话消息业务层实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AiChatMessageServiceImpl implements AiChatMessageService {

    private final AiChatMessageMapper aiChatMessageMapper;

    @Override
    public List<AiChatMessage> listChatMessagesBySessionId(Long sessionId) {
        Integer userId = UserContext.getUserId();
        // 调用Mapper层按会话ID查询所有消息方法
        return aiChatMessageMapper.selectBySessionId(userId, sessionId);
    }

    @Override
    public List<AiChatMessage> listChatMessagesBySessionId(Integer userId, Long sessionId) {
        // 直接使用传入的userId，无需从上下文获取，精准匹配用户+会话维度
        return aiChatMessageMapper.selectBySessionId(userId, sessionId);
    }

    @Override
    public Integer countChatMessagesBySessionIdAndUserId(Integer userId, Long sessionId) {
        // 基础参数校验（与现有风格对齐）
        Assert.notNull(sessionId, "对话ID不能为空");
        Assert.notNull(userId, "用户ID不能为空");

        // 调用Mapper层统计方法（无数据时COUNT(*)返回0，无需额外处理）
        Integer count = aiChatMessageMapper.countBySessionIdAndUserId(userId, sessionId);

        // 兜底：防止Mapper返回null（理论上COUNT(*)不会返回null，仅做容错）
        return count == null ? 0 : count;
    }

    /**
     * 实现查询最新对话历史的方法
     */
    @Override
    public List<AiChatMessage> listLatestChatMessages(Integer userId, Long sessionId, Integer limitNum) {
        // 1. 基础参数校验（调整逻辑：limitNum≤0时返回空列表，而非报错）
        if (userId == null || sessionId == null) {
            throw new IllegalArgumentException("参数异常：用户ID/会话ID不能为空");
        }
        // 2. limitNum为空/≤0时，直接返回空列表（无需查询）
        if (limitNum == null || limitNum <= 0) {
            return Collections.emptyList(); // 返回不可变空列表，比null更安全
            // 如果你更想返回null，把上面一行换成：return null;
        }
        // 3. 校验limitNum必须是偶数（保留原有逻辑，非法值仍报错）
        if (limitNum % 2 != 0) {
            throw new IllegalArgumentException("参数异常：查询数量（limitNum）必须为偶数，当前值为：" + limitNum);
        }
        // 4. 调用修正后的Mapper方法，返回正序的最新N条
        return aiChatMessageMapper.selectLatestMessagesBySessionId(userId, sessionId, limitNum);
    }

    /**
     * 重载方法：自动获取当前用户ID → 查询最新消息
     */
    @Override
    public List<AiChatMessage> listLatestChatMessages(Long sessionId, Integer limitNum) {
        // 从上下文获取当前登录用户ID
        Integer userId = UserContext.getUserId();
        return this.listLatestChatMessages(userId, sessionId, limitNum);
    }

    /**
     * 上拉加载历史消息 实现
     */
    @Override
    public List<AiChatMessage> listMoreHistoryMessages(Integer userId, Long sessionId, Long minMessageId, Integer limitNum) {
        // 1. 断言校验参数（和你现有代码一致）
        Assert.notNull(userId, "用户ID不能为空");
        Assert.notNull(sessionId, "会话ID不能为空");
        Assert.notNull(minMessageId, "分页游标minMessageId不能为空");
        Assert.isTrue(limitNum > 0, "每页加载数量必须大于0");

        // 2. 调用Mapper查询历史消息（返回：消息ID倒序 → 新→旧）
        List<AiChatMessage> historyList = aiChatMessageMapper.selectMoreHistoryMessages(
                userId,
                sessionId,
                minMessageId,
                limitNum
        );

        // 3. 🔥 关键：反转列表 → 旧→新（和前端消息顺序保持一致）
        if (CollUtil.isNotEmpty(historyList)) {
            Collections.reverse(historyList);
        }

        return historyList;
    }

    /**
     * 重载方法：自动获取当前用户ID → 加载历史消息
     */
    @Override
    public List<AiChatMessage> listMoreHistoryMessages(Long sessionId, Long minMessageId, Integer limitNum) {
        // 从上下文获取当前登录用户ID
        Integer userId = UserContext.getUserId();
        return this.listMoreHistoryMessages(userId, sessionId, minMessageId, limitNum);
    }

    /**
     * 实现“查询最新两条消息，返回第二条的创建时间戳（毫秒级）”
     */
    @Override
    public Long getLastChatTimestamp(Integer userId, Long sessionId) {
        // 1. 调用现有方法，查询最新的2条（limitNum固定传2，符合“偶数”要求）
        List<AiChatMessage> latestTwoList = listLatestChatMessages(userId, sessionId, 2);

        // 2. 空列表/不足2条时返回null（无第二条记录）
        if (CollectionUtils.isEmpty(latestTwoList) || latestTwoList.size() < 2) {
            return null;
        }

        // 3. 取第二条消息（列表索引为1）
        AiChatMessage secondLatestMsg = latestTwoList.get(1);

        // 4. LocalDateTime转毫秒级时间戳（核心转换逻辑）
        LocalDateTime createTime = secondLatestMsg.getCreateTime();
        if (createTime == null) {
            return null; // 消息创建时间为空时返回null
        }

        // 关键修改：先绑定UTC(0时区)，再转时间戳（国内服务器用这个会自动+8小时）
        return createTime.atZone(ZoneId.of("UTC")).toInstant().toEpochMilli();
    }

    @Override
    public void addUserChatMessage(ChatInformationDTO dto) {
        // 1. 初始化消息实体
        AiChatMessage message = new AiChatMessage();

        // 2. 直接使用传入的 userMessageId 作为主键
        message.setMessageId(dto.getUserMessageId());

        // 3. 生成父消息ID（关联AI回复）
        Long parentMsgId = SnowflakeGenerator.generateId();
        message.setParentMsgId(parentMsgId);

        // 4. 从 DTO 绑定基础参数
        message.setUserId(dto.getUserId());
        message.setSessionId(dto.getSessionId());
        message.setContent(dto.getUserMessage());
        message.setTaskId(dto.getTaskId());
        message.setSortTimestamp(dto.getUserSendTimestamp());

        // ===================== 核心修改：根据主动消息标识动态设置角色 =====================
        Boolean isActiveMessage = dto.getIsActiveMessage();
        // 主动消息 = 系统(SPRINGBOOT) | 非主动消息 = 用户(USER)
        if (Boolean.TRUE.equals(isActiveMessage)) {
            message.setRole(ChatRoleEnum.SPRINGBOOT);
        } else {
            message.setRole(ChatRoleEnum.USER);
        }

        // 5. 幂等插入
        aiChatMessageMapper.insertIgnore(message);

        // 6. 给DTO赋值父消息ID
        dto.setParentMsgId(parentMsgId);
    }

    @Override
    public int addAIReplyMessage(AiReplyMessageDTO dto) {
        // 1. 转换DTO为实体类
        AiChatMessage message = new AiChatMessage();
        // 2. 自动生成消息ID（雪花ID）
        message.setMessageId(SnowflakeGenerator.generateId());
        // 3. 设置父消息ID（DTO传入）
        message.setParentMsgId(dto.getParentMsgId());
        // 4. 设置会话ID（DTO传入）
        message.setSessionId(dto.getSessionId());
        // 5. 设置用户ID（DTO传入，Integer转Long）
        message.setUserId(UserContext.getUserId());
        // 6. 固定设置角色为（AI回复）
        message.setRole(ChatRoleEnum.ASSISTANT);
        // 7. 设置AI回复内容（DTO传入）
        message.setContent(dto.getContent());
        // 8. create_time由数据库默认生成，无需设置

        // 9. 直接调用Mapper层幂等插入
        return aiChatMessageMapper.insertIgnore(message);
    }

    @Override
    public void createAiStreamPlaceholder(ChatInformationDTO dto) {
        // 1. 初始化消息实体
        AiChatMessage message = new AiChatMessage();

        // 2. 生成【真实雪花ID】(AI消息ID)
        long aiMessageId = SnowflakeGenerator.generateId();
        message.setMessageId(aiMessageId);

        // 3. 从DTO绑定业务参数
        message.setUserId(dto.getUserId());
        message.setSessionId(dto.getSessionId());
        message.setParentMsgId(dto.getParentMsgId());
        message.setTaskId(dto.getTaskId());

        // 4. 固定角色：AI助手
        message.setRole(ChatRoleEnum.ASSISTANT);

        // 5. 固定占位符
        message.setContent("[AI_STREAM_PLACEHOLDER]");

        // 6. 插入占位消息
        aiChatMessageMapper.insertAiStreamPlaceholder(message);

        // ===================== 核心：直接给DTO赋值AI消息ID =====================
        dto.setAiMessageId(aiMessageId);
    }

    @Override
    public void updateAiStreamFinalContent(ChatInformationDTO dto) {
        // ====================== 从DTO获取参数 ======================
        Integer userId = dto.getUserId();
        Long sessionId = dto.getSessionId();
        Long messageId = dto.getAiMessageId();
        String content = dto.getAiReplyContent();
        String splitContentJson = dto.getSplitContentJson();
        Long aiReplyStartTimestamp = dto.getAiReplyStartTimestamp();

        // ====================== 必填参数断言校验（核心不变） ======================
        Assert.notNull(userId, "用户ID(userId)不能为空");
        Assert.notNull(sessionId, "会话ID(sessionId)不能为空");
        Assert.notNull(messageId, "消息ID(messageId)不能为空");
        Assert.hasText(content, "消息内容(content)不能为空或空白字符");
        Assert.notNull(aiReplyStartTimestamp, "AI开始回复时间戳不能为空");

        // ====================== 封装实体类（不变） ======================
        AiChatMessage message = new AiChatMessage();
        message.setUserId(userId);
        message.setSessionId(sessionId);
        message.setMessageId(messageId);
        message.setContent(content);
        message.setSplitContentJson(splitContentJson);
        message.setSortTimestamp(aiReplyStartTimestamp);

        // 调用Mapper不变
        aiChatMessageMapper.updateAiStreamContentById(message);
    }


    @Override
    @Transactional // 保留事务注解，任意异常回滚
    public Long handleChatMessage(ChatMessageCombineDTO dto) {
        // 补充DTO中userId的校验
        if (dto.getUserId() == null || dto.getUserId() < 0) {
            throw new IllegalArgumentException("用户ID不能为空（0=匿名，>0=登录用户）");
        }

        // -------------------- 第一步：插入用户消息 --------------------
        AiChatMessage userMessage = new AiChatMessage();
        // 1. 生成用户消息ID+父消息ID（父消息ID用于关联AI回复）
        Long userMsgId = SnowflakeGenerator.generateId();
        Long parentMsgId = SnowflakeGenerator.generateId();
        userMessage.setMessageId(userMsgId);
        userMessage.setParentMsgId(parentMsgId);
        // 2. 固定参数：会话ID、用户ID（改用DTO内置字段）
        userMessage.setSessionId(dto.getSessionId());
        userMessage.setUserId(dto.getUserId()); // 关键修改：使用DTO的userId
        userMessage.setRole(dto.getRole());
        userMessage.setContent(dto.getUserMessage());
        // 3. 插入用户消息
        aiChatMessageMapper.insertIgnore(userMessage);

        // -------------------- 第二步：插入AI回复消息 --------------------
        AiChatMessage aiReplyMessage = new AiChatMessage();
        // 1. 生成AI回复消息ID
        aiReplyMessage.setMessageId(SnowflakeGenerator.generateId());
        // 2. 关联用户消息的父消息ID
        aiReplyMessage.setParentMsgId(parentMsgId);
        // 3. 固定参数：会话ID、用户ID（改用DTO内置字段）
        aiReplyMessage.setSessionId(dto.getSessionId());
        aiReplyMessage.setUserId(dto.getUserId()); // 关键修改：使用DTO的userId
        aiReplyMessage.setRole(ChatRoleEnum.ASSISTANT);
        aiReplyMessage.setContent(dto.getAiReplyContent());
        aiReplyMessage.setSplitContentJson(dto.getSplitContentJson()); // 赋值切分内容
        // 4. 插入AI回复消息
        aiChatMessageMapper.insertIgnore(aiReplyMessage);

        // -------------------- 第三步：返回父消息ID（关联标识） --------------------
        return parentMsgId;
    }

    @Override
    public void deleteMessageByParentMsgId(Long parentMsgId, Long sessionId) {
        // 1. 参数校验
        if (parentMsgId == null || sessionId == null) {
            throw new IllegalArgumentException("参数不合法：父消息ID/会话ID不能为空");
        }
        // 2. 获取当前登录用户ID
        Integer userId = UserContext.getUserId();
        // 3. 执行删除（校验sessionId+userId防越权）
        int deleteCount = aiChatMessageMapper.deleteByParentMsgIdAndSessionIdAndUserId(parentMsgId, sessionId, userId);
        if (deleteCount == 0) {
            log.warn("用户{}删除消息失败：父消息ID={}，会话ID={}（消息不存在或无权限）", userId, parentMsgId, sessionId);
            throw new RuntimeException("消息删除失败（消息不存在或无操作权限）");
        }
        log.info("用户{}成功删除消息：父消息ID={}，会话ID={}", userId, parentMsgId, sessionId);
    }

    // 新增2：查询最近一条消息
    @Override
    public AiChatMessage getLatestUserMessageBySessionId(Long sessionId) {
        // 1. 参数校验
        if (sessionId == null) {
            throw new IllegalArgumentException("参数不合法：会话ID不能为空");
        }
        // 2. 获取当前登录用户ID
        Integer userId = UserContext.getUserId();
        // 3. 查询最近一条（0条时返回null，无需额外处理）
        return aiChatMessageMapper.selectLatestUserMessageBySessionIdAndUserId(sessionId, userId);
    }

    @Override
    public AiChatMessage getUniqueParentAssociatedMessage(Long parentMsgId, Long sessionId, Integer userId) {
        // 1. 参数校验（语义化提示）
        if (parentMsgId == null) {
            throw new IllegalArgumentException("参数不合法：父消息ID（关联ID）不能为空");
        }
        if (sessionId == null) {
            throw new IllegalArgumentException("参数不合法：会话ID不能为空");
        }
        if (userId == null) {
            throw new IllegalArgumentException("参数不合法：用户ID不能为空");
        }
        // 2. 调用Mapper查询（Mapper已适配Integer userId）
        AiChatMessage message = aiChatMessageMapper.selectByParentMsgIdAndSessionIdAndUserId(
                parentMsgId, sessionId, userId
        );
        // 3. 日志提示（语义化）
        if (message == null) {
            log.warn("未找到关联的父消息：parentMsgId={}, sessionId={}, userId={}", parentMsgId, sessionId, userId);
        } else {
            log.info("成功找到关联的父消息：parentMsgId={}, sessionId={}, userId={}", parentMsgId, sessionId, userId);
        }
        return message;
    }

    // ========== 语义化实现：简化方法（不带userId，从上下文取） ==========
    @Override
    public AiChatMessage getUniqueParentAssociatedMessage(Long parentMsgId, Long sessionId) {
        // 1. 从上下文获取当前登录用户ID（Integer类型，无需转换）
        Integer userId = UserContext.getUserId();
        if (userId == null) {
            throw new IllegalArgumentException("参数不合法：用户未登录，无法获取用户ID");
        }
        // 2. 复用核心方法（语义化+代码复用）
        return this.getUniqueParentAssociatedMessage(parentMsgId, sessionId, userId);
    }
}