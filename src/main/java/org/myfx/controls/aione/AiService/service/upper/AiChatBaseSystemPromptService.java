package org.myfx.controls.aione.AiService.service.upper;

/**
 * AI对话-基础系统提示词业务接口（Base：基础/通用能力封装）
 * 核心：封装“默认提示词、历史摘要提示词”的文本获取能力，返回纯文本（String）
 */
public interface AiChatBaseSystemPromptService {

    /**
     * 获取基础默认系统提示词文本（对应序列号0的默认提示词）
     * @param userId 用户ID
     * @param sessionId 会话ID
     * @return 默认提示词文本（无则返回空字符串）
     */
    String getBaseDefaultSystemPromptText(Integer userId, Long sessionId);

}