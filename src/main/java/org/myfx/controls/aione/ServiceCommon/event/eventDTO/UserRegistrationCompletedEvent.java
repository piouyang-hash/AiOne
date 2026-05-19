package org.myfx.controls.aione.ServiceCommon.event.eventDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import org.myfx.controls.aione.ServiceCommon.serviceEnum.AppTypeEnum;
import org.springframework.context.ApplicationEvent;

/**
 * 用户注册完成事件
 * 替代原Kafka消息，用于单体应用内注册流程后续通知
 */
@Getter
public class UserRegistrationCompletedEvent extends ApplicationEvent {

    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "12345")
    private final Integer userId;

    @Schema(description = "应用类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private final AppTypeEnum appType;

    /**
     * 构造方法（统一规范）
     */
    public UserRegistrationCompletedEvent(Object source, Integer userId, AppTypeEnum appType) {
        super(source);
        this.userId = userId;
        this.appType = appType;
    }
}