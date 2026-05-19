package org.myfx.controls.aione.AiService.service.base.ai_chat_db;

import org.myfx.controls.aione.AiService.entity.ai_chat_db.AiChatSessionSystemPrompt;

import java.util.List;

/**
 * AI对话会话-系统提示词业务层接口（业务语义化封装）
 */
public interface AiChatSessionSystemPromptService {

    /**
     * 新增会话系统提示词（自动处理雪花ID+序列号，无需关注时间字段）
     * @param prompt 系统提示词实体（需包含id/sessionId/userId/serialNumber/systemPrompt）
     */
    void addSessionSystemPrompt(AiChatSessionSystemPrompt prompt);

    /**
     * 重载：新增会话系统提示词（自动生成序列号，简化参数）
     * @param userId 用户ID
     * @param sessionId 会话ID
     * @param systemPrompt 系统提示词文本
     */
    void addSessionSystemPrompt(Integer userId, Long sessionId, String systemPrompt);

    /**
     * 初始化默认系统提示词（序列号固定为0，幂等：仅当无默认提示词时执行）
     * @param userId 用户ID
     * @param sessionId 会话ID
     * @param defaultSystemPrompt 自定义的默认提示词文本
     * @return 初始化后的默认系统提示词实体（已存在则返回原有记录）
     */
    AiChatSessionSystemPrompt initDefaultSystemPrompt(Integer userId, Long sessionId, String defaultSystemPrompt);

    /**
     * 重载：初始化默认系统提示词（使用通用默认提示词，序列号固定为0，幂等）
     * @param userId 用户ID
     * @param sessionId 会话ID
     * @return 初始化后的默认系统提示词实体（已存在则返回原有记录）
     */
    AiChatSessionSystemPrompt initDefaultSystemPrompt(Integer userId, Long sessionId);

    /**
     * 获得指定用户+会话的默认系统提示词（对应序列号0的记录）
     * @param userId 用户ID
     * @param sessionId 会话ID
     * @return 默认系统提示词（序列号0），无则返回null
     */
    AiChatSessionSystemPrompt getDefaultSystemPrompt(Integer userId, Long sessionId);

    /**
     * 查找指定用户+会话的最新历史摘要提示词（排除序列号0的默认提示词）
     * @param userId 用户ID
     * @param sessionId 会话ID
     * @return 最新的历史摘要提示词（无则返回null）
     */
    AiChatSessionSystemPrompt getLatestHistorySummarySystemPrompt(Integer userId, Long sessionId);

    /**
     * 从最新历史摘要提示词中提取序列号
     * @param userId 用户ID
     * @param sessionId 会话ID
     * @return 序列号（null返回0，非null则必须>0）
     */
    Integer getSerialNumberFromLatestSummaryPrompt(Integer userId, Long sessionId);

    /**
     * 获取指定用户+会话的所有系统提示词（按序列号升序排列）
     * @param userId 用户ID
     * @param sessionId 会话ID
     * @return 系统提示词列表，无数据返回空列表
     */
    List<AiChatSessionSystemPrompt> listAllSessionSystemPrompt(Integer userId, Long sessionId);

    /**
     * 删除指定用户+会话的所有系统提示词
     * @param userId 用户ID
     * @param sessionId 会话ID
     */
    void removeAllBySession(Integer userId, Long sessionId);

    /**
     * 删除指定用户的所有系统提示词
     * @param userId 用户ID
     */
    void removeAllByUser(Integer userId);
}