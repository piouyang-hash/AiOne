package org.myfx.controls.aione.ConnectService.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.myfx.controls.aione.ConnectService.common.DeviceTypeEnum;
import org.myfx.controls.aione.ServiceCommon.serviceEnum.AppTypeEnum;

/**
 * 用户登录状态DTO（独立存储，供Redis序列化/反序列化使用）
 * 微服务场景下：用户微服务登录后，可通过Feign调用本服务写入Redis
 */
@Data
@Schema(description = "用户登录状态信息")
public class UserStatus {

    @Schema(description = "用户ID", example = "1001")
    private Integer userId;

    @Schema(description = "登录时间戳（毫秒）", example = "1735689600000")
    private Long loginTime;

    @Schema(description = "最后心跳时间戳（毫秒）", example = "1735689660000")
    private Long lastHeartbeatTime;

    @Schema(description = "登录设备（如Android/PC/iOS）", example = "PC")
    private DeviceTypeEnum device;

    @Schema(description = "登录IP地址", example = "192.168.1.100")
    private String ip;

    @Schema(description = "长连接通道ID（Netty Channel唯一标识）", example = "netty-channel-1001")
    private String channelId;

    @Schema(description = "应用类型编码（区分不同端/应用）", example = "01") // 新增app类型字段
    private AppTypeEnum appType;
}