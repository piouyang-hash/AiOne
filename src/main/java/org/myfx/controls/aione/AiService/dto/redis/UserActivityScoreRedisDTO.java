package org.myfx.controls.aione.AiService.dto.redis;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 专用于Redis存储的用户特征分值实体（精简版）
 * 核心：仅保留核心业务字段 + 时间戳（避免LocalDateTime转换麻烦）
 */
@Data // Lombok自动生成get/set/toString等，适配Redis序列化
public class UserActivityScoreRedisDTO {

    @Schema(description = "用户ID", example = "1001")
    private Integer userId; // 保留：用户ID

    @Schema(description = "用户活跃度分值（0-100，默认0）", example = "50")
    private Integer activityScore; // 保留：活跃度分值

    @Schema(description = "上次更新时间戳（毫秒）", example = "1735689600000")
    private Long lastUpdateTimestamp; // 新增：上次更新时间戳（long类型，直接存数字，无需转换）

    @Schema(description = "用户下线时间戳（毫秒）", example = "1735689800000")
    private Long userOfflineTimestamp; // 用户下线时间戳

}