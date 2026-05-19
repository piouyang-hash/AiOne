package org.myfx.controls.aione.AiService.service.upper;

import org.myfx.controls.aione.AiService.entity.ai_chat_db.AiPersonalityConfig;

import java.util.List;

/**
 * 基础AI性格服务接口
 * 极简逻辑：上下文取userId + 打印关键信息 + 调用底层AiPersonalityConfigService
 * 无复杂业务逻辑，仅做参数透传和简化
 */
public interface BaseAiPersonalityService {

    // ====================== 手动传userId ======================
    /**
     * 【手动传userId】获取AI当前最强性格的提示词（无任何兜底逻辑）
     * @param userId 用户ID（Integer类型，0=匿名）
     * @return AI性格提示词
     */
    String getAiCurrentStrongestPersonalityPrompt(Integer userId);

    /**
     * 【手动传userId】获取指定用户+会话下的全部性格配置
     * @param userId 用户ID（Integer类型，0=匿名）
     * @return 性格配置列表
     */
    List<AiPersonalityConfig> listAllAiPersonalityConfig(Integer userId);

}