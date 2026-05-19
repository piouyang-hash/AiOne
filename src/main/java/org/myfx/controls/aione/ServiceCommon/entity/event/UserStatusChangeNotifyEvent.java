package org.myfx.controls.aione.ServiceCommon.entity.event;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import org.myfx.controls.aione.ServiceCommon.serviceEnum.AppTypeEnum;
import org.springframework.context.ApplicationEvent;
import java.time.LocalDateTime;

/**
 * 用户状态变更通知事件
 * 替代原Kafka消息，用于单体应用内状态变更通知
 */
@Getter
public class UserStatusChangeNotifyEvent extends ApplicationEvent {

    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "12345")
    private final Integer userId;

    @Schema(description = "应用类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "APP", allowableValues = {"APP", "PC", "H5"})
    private final AppTypeEnum appType;

    @Schema(description = "应用类型描述", example = "移动端APP")
    private final String appTypeDesc;

    @Schema(description = "状态（上线/离线）", requiredMode = Schema.RequiredMode.REQUIRED, example = "上线")
    private final String status;

    @Schema(description = "状态变更时间戳（毫秒）", example = "1742125800000")
    private final Long statusTime;

    @Schema(description = "消息发送时间（生产者本地时间）", example = "2026-03-04 10:30:00")
    private final LocalDateTime sendTime;

    /**
     * 构造方法（统一规范）
     */
    public UserStatusChangeNotifyEvent(Object source, Integer userId, AppTypeEnum appType, String appTypeDesc, String status, Long statusTime) {
        super(source);
        this.userId = userId;
        this.appType = appType;
        this.appTypeDesc = appTypeDesc;
        this.status = status;
        this.statusTime = statusTime;
        this.sendTime = LocalDateTime.now();
    }
}
