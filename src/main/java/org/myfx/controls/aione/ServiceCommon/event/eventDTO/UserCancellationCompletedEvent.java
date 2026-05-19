package org.myfx.controls.aione.ServiceCommon.event.eventDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import java.time.LocalDateTime;

/**
 * 用户注销完成事件
 */
@Getter
public class UserCancellationCompletedEvent extends ApplicationEvent {

    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "12345")
    private final Integer userId;

    // 标准构造方法
    public UserCancellationCompletedEvent(Object source, Integer userId) {
        super(source);
        this.userId = userId;
    }
}