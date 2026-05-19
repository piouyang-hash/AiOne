package org.myfx.controls.aione.AiService.service.upper;

import org.myfx.controls.aione.AiService.entity.ai_chat_db.AiMoodConfig;

import java.util.List;

/**
 * 基础AI心情服务接口
 * 核心：封装心情配置的获取逻辑，屏蔽userId参数（从上下文自动获取），简化上层调用
 */
public interface BaseAiMoodService {

    // ====================== 手动传userId ======================
    /**
     * 【手动传userId】获取AI当前最强心情的提示词（AI自身的心情，非用户）
     * @param userId 用户ID（Integer类型，0=匿名）
     * @return AI心情提示词（适配AI对话的话术规则）
     */
    String getAiCurrentStrongestMoodPrompt(Integer userId);

    /**
     * 【手动传userId】获取指定用户+会话下的全部心情配置
     * @param userId 用户ID（Integer类型，0=匿名）
     * @return 心情配置列表（无数据返回空列表）
     */
    List<AiMoodConfig> listAllAiMoodConfig(Integer userId);
}