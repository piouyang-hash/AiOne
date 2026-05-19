package org.myfx.controls.aione.AiService.service.base.ai_chat_db.impl;

import jakarta.annotation.Resource;
import org.myfx.controls.aione.AiService.entity.ai_chat_db.AiChatSessionSystemPrompt;
import org.myfx.controls.aione.AiService.event.AiChatSessionCreatedEvent;
import org.myfx.controls.aione.AiService.mapper.ai_chat_db.AiChatSessionSystemPromptMapper;
import org.myfx.controls.aione.AiService.service.base.ai_chat_db.AiChatSessionSystemPromptService;
import org.myfx.controls.aione.ServiceCommon.utils.SnowflakeGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.List;

/**
 * AI对话会话-系统提示词业务层实现类
 * 核心：简单封装Mapper调用，仅加基础参数校验，逻辑可自行扩展
 */
@Service
public class AiChatSessionSystemPromptServiceImpl implements AiChatSessionSystemPromptService {

    @Resource
    private AiChatSessionSystemPromptMapper systemPromptMapper;

    // 默认通用系统提示词（可配置化，这里先写死，你后续可抽成配置）
    private static final String DEFAULT_PROMPT = "你是一个智能AI助手，需根据上下文友好、准确地回答用户问题";

    // ========== 核心方法：统一处理雪花ID生成 + 参数校验 + 新增 ==========
    @Override
    public void addSessionSystemPrompt(AiChatSessionSystemPrompt prompt) {
        // 基础参数校验：实体不能为空
        Assert.notNull(prompt, "系统提示词实体不能为空");

        // ========== 核心改动：统一生成雪花ID（若未传入则自动生成） ==========
        if (prompt.getId() == null) {
            prompt.setId(SnowflakeGenerator.generateId()); // 自动生成雪花ID并赋值
        }

        // 其他参数校验（保留原有逻辑，雪花ID已确保非空）
        Assert.notNull(prompt.getId(), "主键ID（雪花ID）不能为空");
        Assert.notNull(prompt.getSessionId(), "会话ID不能为空");
        Assert.notNull(prompt.getUserId(), "用户ID不能为空");
        Assert.notNull(prompt.getSerialNumber(), "序列号不能为空");
        Assert.hasText(prompt.getSystemPrompt(), "系统提示词文本不能为空");

        // 调用Mapper新增
        systemPromptMapper.insert(prompt);
    }

    // ========== 重载方法：自动生成序列号，简化参数（无需处理雪花ID） ==========
    @Override
    public void addSessionSystemPrompt(Integer userId, Long sessionId, String systemPrompt) {
        // 1. 参数校验（避免空值导致业务异常）
        Assert.notNull(userId, "用户ID（userId）不能为空");
        Assert.notNull(sessionId, "会话ID（sessionId）不能为空");
        Assert.hasText(systemPrompt, "系统提示词文本（systemPrompt）不能为空");

        // 2. 获取该会话下最新的最大序列号
        Integer latestSerialNumber = getSerialNumberFromLatestSummaryPrompt(userId, sessionId);

        // 3. 计算新序列号：无历史数据则从1开始，有则+1
        Integer newSerialNumber = latestSerialNumber == null ? 1 : latestSerialNumber + 1;

        // 4. 构建系统提示词实体（无需设置雪花ID，核心方法会自动生成）
        AiChatSessionSystemPrompt prompt = new AiChatSessionSystemPrompt();
        prompt.setUserId(userId);                // 设置用户ID
        prompt.setSessionId(sessionId);          // 设置会话ID
        prompt.setSerialNumber(newSerialNumber); // 设置自动生成的新序列号
        prompt.setSystemPrompt(systemPrompt);    // 设置传入的系统提示词文本
        // 雪花ID：无需处理，核心方法会自动生成

        // 5. 调用核心方法完成新增（自动生成雪花ID + 校验 + 插入）
        this.addSessionSystemPrompt(prompt);
    }

    // ========== 初始化默认系统提示词核心实现（幂等版，无需处理雪花ID） ==========
    @Override
    public AiChatSessionSystemPrompt initDefaultSystemPrompt(Integer userId, Long sessionId, String defaultSystemPrompt) {
        // 1. 入参校验（与AI情绪初始化对齐，保留原有逻辑）
        if (userId == null || sessionId == null) {
            throw new IllegalArgumentException("用户ID和会话ID不能为空");
        }
        if (defaultSystemPrompt == null || defaultSystemPrompt.isBlank()) {
            throw new IllegalArgumentException("初始化默认系统提示词失败：默认提示词文本不能为空");
        }

        // ========== 核心幂等逻辑：先查是否已初始化默认提示词，已存在则直接返回 ==========
        AiChatSessionSystemPrompt existPrompt = getDefaultSystemPrompt(userId, sessionId);
        if (existPrompt != null) {
            // 已初始化过默认提示词，直接返回已有记录，不重复执行插入
            return existPrompt;
        }

        // ========== 仅当无默认提示词时，执行初始化（序列号固定为0，无需设置雪花ID） ==========
        AiChatSessionSystemPrompt prompt = new AiChatSessionSystemPrompt();
        // 移除：手动生成雪花ID的逻辑（核心方法会自动生成）
        prompt.setUserId(userId);                      // 设置用户ID
        prompt.setSessionId(sessionId);                // 设置会话ID
        prompt.setSerialNumber(0);                     // 默认提示词序列号固定为0
        prompt.setSystemPrompt(defaultSystemPrompt);   // 设置自定义默认提示词文本

        // 调用核心方法插入数据库（自动生成雪花ID + 复用参数校验逻辑）
        addSessionSystemPrompt(prompt);

        return prompt;
    }

    // ========== 重载：初始化默认系统提示词（使用通用默认提示词） ==========
    @Override
    public AiChatSessionSystemPrompt initDefaultSystemPrompt(Integer userId, Long sessionId) {
        // 复用核心方法，传入通用默认提示词
        return initDefaultSystemPrompt(userId, sessionId, DEFAULT_PROMPT);
    }

    // ========== 新增：事务监听器（与AI情绪监听器对齐） ==========
    /**
     * 监听会话创建事件，初始化系统提示词（并入主事务，提交前执行）
     * 与AI情绪监听器保持一致：TransactionPhase.BEFORE_COMMIT，绑定主事务
     */
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleSessionSystemPromptInitEvent(AiChatSessionCreatedEvent event) {
        // 1. 从事件中获取参数（与AI情绪监听器对齐）
        Integer userId = event.getUserId();
        Long sessionId = event.getSessionId();

        // 2. 参数校验（增强健壮性）
        if (userId == null || sessionId == null) {
            throw new IllegalArgumentException("初始化系统提示词失败：用户ID和会话ID不能为空");
        }

        // 3. 调用核心初始化方法（使用默认通用提示词）
        initDefaultSystemPrompt(userId, sessionId);
    }

    // ========== 新增：获得默认系统提示词（序列号0） ==========
    @Override
    public AiChatSessionSystemPrompt getDefaultSystemPrompt(Integer userId, Long sessionId) {
        // 基础参数校验（与示例保持一致）
        Assert.notNull(userId, "用户ID不能为空");
        Assert.notNull(sessionId, "会话ID不能为空");

        // 调用Mapper查询序列号0的默认提示词
        return systemPromptMapper.selectSerialNumberZero(userId, sessionId);
    }

    @Override
    public AiChatSessionSystemPrompt getLatestHistorySummarySystemPrompt(Integer userId, Long sessionId) {
        // 基础参数校验
        Assert.notNull(userId, "用户ID不能为空");
        Assert.notNull(sessionId, "会话ID不能为空");

        // 调用Mapper查询最新的历史摘要提示词（排除序列号0）
        return systemPromptMapper.selectLatestByUserIdAndSessionId(userId, sessionId);
    }

    @Override
    public Integer getSerialNumberFromLatestSummaryPrompt(Integer userId, Long sessionId) {
        // 1. 调用本地方法获取最新提示词
        AiChatSessionSystemPrompt prompt = this.getLatestHistorySummarySystemPrompt(userId, sessionId);

        // 2. 提取序列号
        Integer serialNumber = (prompt != null) ? prompt.getSerialNumber() : null;

        // 3. 校验规则：null返回0，非null则必须>0
        if (serialNumber == null) {
            return 0;
        }
        // 校验序列号大于0，不满足则抛异常（理论上不会发生，便于排查问题）
        if (serialNumber <= 0) {
            throw new IllegalArgumentException("历史摘要提示词的序列号必须大于0，当前值：" + serialNumber);
        }

        // 4. 返回合法的序列号
        return serialNumber;
    }

    @Override
    public List<AiChatSessionSystemPrompt> listAllSessionSystemPrompt(Integer userId, Long sessionId) {
        // 基础参数校验
        Assert.notNull(userId, "用户ID不能为空");
        Assert.notNull(sessionId, "会话ID不能为空");

        // 调用Mapper查询所有，无数据返回空列表（避免NPE）
        List<AiChatSessionSystemPrompt> promptList = systemPromptMapper.selectAllByUserIdAndSessionId(userId, sessionId);
        return promptList == null ? Collections.emptyList() : promptList;
    }

    @Override
    public void removeAllBySession(Integer userId, Long sessionId) {
        // 基础参数校验
        Assert.notNull(userId, "用户ID不能为空");
        Assert.notNull(sessionId, "会话ID不能为空");

        // 调用Mapper删除
        systemPromptMapper.deleteByUserIdAndSessionId(userId, sessionId);
    }

    @Override
    public void removeAllByUser(Integer userId) {
        // 基础参数校验
        Assert.notNull(userId, "用户ID不能为空");

        // 调用Mapper删除
        systemPromptMapper.deleteByUserId(userId);
    }
}