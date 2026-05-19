package org.myfx.controls.aione.AiService.service.upper.impl;

import lombok.RequiredArgsConstructor;
import org.myfx.controls.aione.AiService.service.upper.*;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * 上层提示词构建服务实现类
 * 具体实现提示词的拼接逻辑
 */
@Service
@RequiredArgsConstructor
public class UpperPromptBuildingServiceImpl implements UpperPromptBuildingService {

    // 注入依赖的服务（推荐使用@Resource，也可使用@Autowired）
    private final BaseMemoryService baseMemoryService;
    private final BaseAiMoodService baseAiMoodService;
    private final BaseAiPersonalityService baseAiPersonalityService;
    private final AiChatBaseSystemPromptService aiChatBaseSystemPromptService;

    /**
     * 核心方法：拼接生成完整的提示词（基于用户ID和会话ID）
     * 调整后步骤：默认提示词 → 历史摘要 → 用户画像 → AI性格 → AI心情 → 拼接要求
     * @param userId 用户ID
     * @param sessionId 会话ID
     * @return 完整提示词
     */
    @Override
    public String buildFullSystemPrompt(Integer userId, Long sessionId) {
        // 1. 获取默认系统提示词（无则报错，必选）
        String defaultSystemPrompt = aiChatBaseSystemPromptService.getBaseDefaultSystemPromptText(userId, sessionId);
        // 校验默认提示词：非null、非空、非空白字符串
        Assert.hasText(defaultSystemPrompt, "默认系统提示词不能为空（userId：" + userId + "，sessionId：" + sessionId + "）");

        // 2. 生成用户基础画像提示词（基于用户ID）
        String userProfilePrompt = baseMemoryService.getUserProfilePrompt(userId);
        // 校验用户基础画像提示词
        Assert.hasText(userProfilePrompt, "用户基础画像提示词不能为空（userId：" + userId + "）");

        // 3. 生成用户爱好画像提示词（基于用户ID，确保末尾加回车）
        String userHobbyPrompt = baseMemoryService.getUserHobbyPrompt(userId);
        // 校验用户爱好画像提示词
        Assert.hasText(userHobbyPrompt, "用户爱好画像提示词不能为空（userId：" + userId + "）");

        // 4. 调用AI性格服务，获取AI自身性格提示词（基于用户ID和会话ID）
        String aiPersonalityPrompt = baseAiPersonalityService.getAiCurrentStrongestPersonalityPrompt(userId);
        // 校验AI性格提示词
        Assert.hasText(aiPersonalityPrompt, "AI性格提示词不能为空（userId：" + userId + "，sessionId：" + sessionId + "）");

        // 5. 调用AI心情服务，获取AI自身心情提示词（基于用户ID和会话ID）
        String aiMoodPrompt = baseAiMoodService.getAiCurrentStrongestMoodPrompt(userId);
        // 校验AI心情提示词
        Assert.hasText(aiMoodPrompt, "AI心情提示词不能为空（userId：" + userId + "，sessionId：" + sessionId + "）");

        // 6. 拼接核心提示词：默认提示词 + 用户画像 + AI性格 + AI心情
        // 先加默认提示词（必选）
        return defaultSystemPrompt +
                // 拼接用户画像、AI性格、AI心情
                userProfilePrompt +
                userHobbyPrompt +
                aiPersonalityPrompt +
                aiMoodPrompt;
    }
}