package org.myfx.controls.aione.AiService.service.upper;

/**
 * 基础记忆服务接口（组合式业务入口）
 * 核心：聚合用户信息、爱好等基础数据，提供统一的画像/记忆类能力
 */
public interface BaseMemoryService {

    // ====================== 手动传userId ======================
    /**
     * 【手动传userId】获取指定用户的基础画像提示词（适配AI对话场景）
     * @param userId 用户ID（Integer类型，0=匿名）
     * @return 用户基础画像提示词字符串（格式：用户基础画像：性别为【XX】，年龄为【XX】，身份为【XX】。）
     * @throws IllegalArgumentException 传入的用户ID为空时抛出
     */
    String getUserProfilePrompt(Integer userId);

    /**
     * 【手动传userId】获取指定用户的爱好画像提示词（适配AI对话场景）
     * @param userId 用户ID（Integer类型，0=匿名）
     * @return 用户爱好画像提示词字符串（格式：用户爱好画像：爱好为【XX、XX】。/用户爱好画像：暂无已知爱好。）
     * @throws IllegalArgumentException 传入的用户ID/用户InfoID为空时抛出
     */
    String getUserHobbyPrompt(Integer userId);
}