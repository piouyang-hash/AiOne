package org.myfx.controls.aione.AiService.service.upper.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.AiService.common.ai_chat_db.AiPersonalityEnum;
import org.myfx.controls.aione.AiService.common.ai_chat_db.AiPersonalityStrengthEnum;
import org.myfx.controls.aione.AiService.entity.ai_chat_db.AiPersonalityConfig;
import org.myfx.controls.aione.AiService.service.base.ai_chat_db.AiPersonalityConfigService;
import org.myfx.controls.aione.AiService.service.upper.BaseAiPersonalityService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 基础AI性格服务实现类
 * 极简逻辑：上下文取userId + 打印关键信息 + 调用底层AiPersonalityConfigService
 * 无复杂业务逻辑，仅做参数透传和简化
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BaseAiPersonalityServiceImpl implements BaseAiPersonalityService {

    // 注入底层性格配置服务
    private final AiPersonalityConfigService aiPersonalityConfigService;

    // ====================== 手动传userId的方法（核心实现） ======================
    @Override
    public String getAiCurrentStrongestPersonalityPrompt(Integer userId) {
        // 1. 参数校验（手动传参必须校验userId非空）
        if (userId == null) {
            throw new IllegalArgumentException("手动传入的用户ID不能为空");
        }

        // 2. 调用底层服务获取AI性格配置（无兜底）
        AiPersonalityConfig aiPersonalityConfig = aiPersonalityConfigService.getCurrentStrongestPersonality(userId);

        // 3. 校验AI性格配置：为空/无效直接抛严重错误（无兜底，禁止默认性格）
        if (aiPersonalityConfig == null) {
            String errorMsg = String.format("严重错误：AI性格配置为空，userId：%s，禁止继续对话", userId);
            throw new RuntimeException(errorMsg);
        }
        if (aiPersonalityConfig.getIsValid() == null || aiPersonalityConfig.getIsValid() != 1) {
            String errorMsg = String.format("严重错误：AI性格配置无效（isValid=%s），userId：%s，禁止继续对话",
                    aiPersonalityConfig.getIsValid(), userId);
            throw new RuntimeException(errorMsg);
        }

        // 4. 解析AI性格枚举：为null直接抛严重错误（无兜底，杜绝性格突变）
        AiPersonalityEnum aiPersonality = aiPersonalityConfig.getAiPersonalityCode();
        if (aiPersonality == null) {
            String errorMsg = String.format("严重错误：AI性格枚举为空，userId：%s，禁止继续对话", userId);
            throw new RuntimeException(errorMsg);
        }
        // 直接赋值（依赖前置null校验，无任何兜底）
        String personalityName = aiPersonality.getName();
        String personalityTalkRule = aiPersonality.getPersonalitySpeakRule();

        // 5. 解析AI强度枚举：为null直接抛严重错误（无兜底，杜绝强度突变）
        AiPersonalityStrengthEnum aiStrength = aiPersonalityConfig.getPersonalityStrengthCode();
        if (aiStrength == null) {
            String errorMsg = String.format("严重错误：AI性格强度枚举为空，userId：%s，禁止继续对话", userId);
            throw new RuntimeException(errorMsg);
        }
        // 直接赋值（依赖前置null校验，无任何兜底）
        String strengthName = aiStrength.getName();
        String strengthStyle = aiStrength.getStrengthRule();

        // 6. 拼接AI性格提示词（无任何兜底，所有字段均为有效值）
        return String.format(
                "AI性格规则：当前性格为【%s】，强度为【%s】，AI话术：%s（%s）。\n",
                personalityName, strengthName, personalityTalkRule, strengthStyle
        );
    }

    @Override
    public List<AiPersonalityConfig> listAllAiPersonalityConfig(Integer userId) {
        // 1. 参数校验（手动传参必须校验userId非空）
        if (userId == null) {
            throw new IllegalArgumentException("手动传入的用户ID不能为空");
        }

        // 2. 调用底层服务，透传参数（兜底返回空列表，避免空指针）
        List<AiPersonalityConfig> personalityConfigList = aiPersonalityConfigService.listAiPersonalityConfigByUserId(userId);
        return personalityConfigList == null ? List.of() : personalityConfigList;
    }

}