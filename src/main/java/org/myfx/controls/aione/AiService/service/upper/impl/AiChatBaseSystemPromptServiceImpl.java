package org.myfx.controls.aione.AiService.service.upper.impl;

import lombok.RequiredArgsConstructor;
import org.myfx.controls.aione.AiService.entity.ai_chat_db.AiChatSessionSystemPrompt;
import org.myfx.controls.aione.AiService.service.base.ai_chat_db.AiChatSessionSystemPromptService;
import org.myfx.controls.aione.AiService.service.upper.AiChatBaseSystemPromptService;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * AI对话-基础系统提示词业务实现类（Base：基础能力落地）
 * 核心：调用底层Service，提取systemPrompt文本，对外返回纯String（简化上层调用）
 */
@Service
@RequiredArgsConstructor
public class AiChatBaseSystemPromptServiceImpl implements AiChatBaseSystemPromptService {

    // 注入原有系统提示词Service
    private final AiChatSessionSystemPromptService systemPromptService;

    @Override
    public String getBaseDefaultSystemPromptText(Integer userId, Long sessionId) {
        // 1. 基础参数校验（与原有风格对齐）
        Assert.notNull(userId, "用户ID不能为空");
        Assert.notNull(sessionId, "会话ID不能为空");

        // 2. 调用底层Service获取默认提示词实体
        AiChatSessionSystemPrompt defaultPrompt = systemPromptService.getDefaultSystemPrompt(userId, sessionId);

        // 3. 无默认提示词则报错，有则提取文本并格式化
        if (defaultPrompt == null || defaultPrompt.getSystemPrompt() == null || defaultPrompt.getSystemPrompt().isBlank()) {
            throw new IllegalArgumentException("用户ID：" + userId + " 会话ID：" + sessionId + " 未配置默认系统提示词");
        }

        // 4. 提取纯文本，拼接<基本要求>键值对格式，保留换行（贴合参考风格，无私有字段）
        String promptContent = defaultPrompt.getSystemPrompt();
        return String.format("<基本要求>：【%s】\n", promptContent);
    }

}