package org.myfx.controls.aione.ServiceCommon.entity.event;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import org.myfx.controls.aione.ServiceCommon.serviceEnum.AppTypeEnum;
import org.springframework.context.ApplicationEvent;
import java.time.LocalDateTime;

/**
 * 用户登录事件
 */
@Getter
public class UserLoginEvent extends ApplicationEvent {

    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "12345")
    private final Integer userId;

    @Schema(description = "应用类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private final AppTypeEnum appType;

    @Schema(description = "消息发送时间", example = "2026-03-04 10:30:00")
    private final LocalDateTime sendTime;

    // 标准构造方法
    public UserLoginEvent(Object source, Integer userId, AppTypeEnum appType) {
        super(source);
        this.userId = userId;
        this.appType = appType;
        this.sendTime = LocalDateTime.now();
    }
}