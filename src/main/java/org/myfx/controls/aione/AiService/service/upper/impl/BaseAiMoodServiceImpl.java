package org.myfx.controls.aione.AiService.service.upper.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.AiService.common.ai_chat_db.AiMoodEnum;
import org.myfx.controls.aione.AiService.common.ai_chat_db.AiMoodStrengthEnum;
import org.myfx.controls.aione.AiService.entity.ai_chat_db.AiMoodConfig;
import org.myfx.controls.aione.AiService.service.base.ai_chat_db.AiMoodConfigService;
import org.myfx.controls.aione.AiService.service.upper.BaseAiMoodService;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * 基础AI心情服务实现类
 * 极简逻辑：上下文取userId + 打印关键信息 + 调用底层AiMoodConfigService
 * 无复杂业务逻辑，仅做参数透传和简化
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BaseAiMoodServiceImpl implements BaseAiMoodService {

    // 注入底层心情配置服务
    private final AiMoodConfigService aiMoodConfigService;

    // ====================== 手动传userId的方法（核心实现） ======================
    @Override
    public String getAiCurrentStrongestMoodPrompt(Integer userId) {
        // 1. 参数校验（手动传参必须校验userId非空）
        if (userId == null) {
            throw new IllegalArgumentException("手动传入的用户ID不能为空");
        }

        // 2. 调用底层服务获取AI心情配置（无兜底）
        AiMoodConfig aiMoodConfig = aiMoodConfigService.getCurrentStrongestMood(userId);

        // 3. 校验AI心情配置：为空/无效直接抛严重错误（无兜底，禁止默认心情）
        if (aiMoodConfig == null) {
            String errorMsg = String.format("严重错误：AI心情配置为空，userId：%s，禁止继续对话", userId);
            throw new RuntimeException(errorMsg);
        }
        if (aiMoodConfig.getIsValid() == null || aiMoodConfig.getIsValid() != 1) {
            String errorMsg = String.format("严重错误：AI心情配置无效（isValid=%s），userId：%s，禁止继续对话",
                    aiMoodConfig.getIsValid(), userId);
            throw new RuntimeException(errorMsg);
        }

        // 4. 解析AI心情枚举：为null直接抛严重错误（无兜底，杜绝心情突变）
        AiMoodEnum aiMood = aiMoodConfig.getAiMoodCode();
        if (aiMood == null) {
            String errorMsg = String.format("严重错误：AI心情枚举为空，userId：%s，禁止继续对话", userId);
            throw new RuntimeException(errorMsg);
        }
        // 直接赋值（依赖前置null校验，无任何兜底）
        String moodName = aiMood.getName();
        String moodTalkRule = aiMood.getBaseSpeakRule();

        // 5. 解析AI强度枚举：为null直接抛严重错误（无兜底，杜绝强度突变）
        AiMoodStrengthEnum aiStrength = aiMoodConfig.getAiStrengthCode();
        if (aiStrength == null) {
            String errorMsg = String.format("严重错误：AI心情强度枚举为空，userId：%s，禁止继续对话", userId);
            throw new RuntimeException(errorMsg);
        }
        // 直接赋值（依赖前置null校验，无任何兜底）
        String strengthName = aiStrength.getName();
        String strengthStyle = aiStrength.getStrengthSpeakRule();

        // 6. 拼接AI心情提示词（无任何兜底，所有字段均为有效值）
        return String.format(
                "AI心情规则：当前心情为【%s】，强度为【%s】，AI话术：%s（%s）。\n",
                moodName, strengthName, moodTalkRule, strengthStyle
        );
    }

    @Override
    public List<AiMoodConfig> listAllAiMoodConfig(Integer userId) {
        // 1. 参数校验（手动传参必须校验userId非空）
        if (userId == null) {
            throw new IllegalArgumentException("手动传入的用户ID不能为空");
        }

        // 2. 调用底层服务，透传参数（兜底返回空列表，避免空指针）
        List<AiMoodConfig> moodConfigList = aiMoodConfigService.listAiMoodConfigByUserId(userId);
        return moodConfigList == null ? List.of() : moodConfigList;
    }
}