package org.myfx.controls.aione.AiService.dto.redis;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 专用于Redis存储的AI特征分值实体（精简版）
 * 核心：仅保留核心业务字段 + 时间戳（避免LocalDateTime转换麻烦）
 */
@Data // Lombok自动生成get/set/toString等，适配Redis序列化
public class AiActivityScoreRedisDTO {

    @Schema(description = "用户ID", example = "1001")
    private Integer userId; // 保留：用户ID

    @Schema(description = "ai活跃度分值（0-100，默认0）", example = "50")
    private Integer activityScore; // 保留：活跃度分值

    @Schema(description = "初始化时间戳（毫秒）", example = "1735689500000")
    private Long initializationTimestamp; // 新增：初始化时间戳（仅初始化时填写，其他场景无需填写）

    @Schema(description = "上次更新时间戳（毫秒）", example = "1735689600000")
    private Long lastUpdateTimestamp; // 新增：上次更新时间戳（long类型，直接存数字，无需转换）

    @Schema(description = "上次收到用户消息时间戳（毫秒）", example = "1735689800000")
    private Long lastReceiveUserMsgTimestamp; // 上次收到用户消息时间戳（毫秒）
}