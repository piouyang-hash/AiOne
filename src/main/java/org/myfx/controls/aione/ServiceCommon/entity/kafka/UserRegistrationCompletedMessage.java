package org.myfx.controls.aione.ServiceCommon.entity.kafka;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.myfx.controls.aione.ServiceCommon.serviceEnum.AppTypeEnum;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 用户注册完成消息DTO
 * 用于在用户注册核心流程完成后通知各个微服务进行后续处理
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户注册完成消息DTO")
public class UserRegistrationCompletedMessage {

    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "12345")
    private Integer userId;

    @Schema(description = "应用类型", requiredMode = Schema.RequiredMode.REQUIRED,
            example = "1")
    private AppTypeEnum appType;

    @Schema(description = "消息发送时间（生产者本地时间）",
            example = "2026-03-05 10:30:00")
    private LocalDateTime sendTime = LocalDateTime.now(); // 初始化自动填充当前时间
}