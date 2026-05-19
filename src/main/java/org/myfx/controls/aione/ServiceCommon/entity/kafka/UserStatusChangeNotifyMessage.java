package org.myfx.controls.aione.ServiceCommon.entity.kafka;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.myfx.controls.aione.ServiceCommon.serviceEnum.AppTypeEnum;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 用户状态变更通知消息（Kafka消息体）
 */
@Data
@Schema(description = "用户状态变更Kafka消息")
public class UserStatusChangeNotifyMessage {

    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "12345")
    private Integer userId;

    @Schema(description = "应用类型", requiredMode = Schema.RequiredMode.REQUIRED,
            example = "APP", allowableValues = {"APP", "PC", "H5"}) // 示例值匹配AppTypeEnum枚举值
    private AppTypeEnum appType;

    @Schema(description = "应用类型描述", example = "移动端APP")
    private String appTypeDesc;

    @Schema(description = "状态（上线/离线）", requiredMode = Schema.RequiredMode.REQUIRED,
            example = "上线")
    private String status;

    @Schema(description = "状态变更时间戳（毫秒）", example = "1742125800000")
    private Long statusTime;

    @Schema(description = "消息发送时间（生产者本地时间）",
            example = "2026-03-04 10:30:00")
    private LocalDateTime sendTime = LocalDateTime.now(); // 初始化自动填充当前时间
}