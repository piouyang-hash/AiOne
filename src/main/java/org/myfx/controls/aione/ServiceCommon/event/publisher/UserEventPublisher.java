package org.myfx.controls.aione.ServiceCommon.event.publisher;

import lombok.RequiredArgsConstructor;
import org.myfx.controls.aione.ServiceCommon.event.eventDTO.UserCanceledEvent;
import org.myfx.controls.aione.ServiceCommon.event.eventDTO.UserCancellationCompletedEvent;
import org.myfx.controls.aione.ServiceCommon.event.eventDTO.UserRegisteredEvent;
import org.myfx.controls.aione.ServiceCommon.event.eventDTO.UserRegistrationCompletedEvent;
import org.myfx.controls.aione.ServiceCommon.serviceEnum.AppTypeEnum;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * 全局事件发布器
 * 统一管理用户相关事件的发布
 */
@Component
@RequiredArgsConstructor
public class UserEventPublisher {

    /**
     * Spring 内置事件发布器
     */
    private final ApplicationEventPublisher eventPublisher;

    // ====================== 发布【用户注册】事件（第一步） ======================
    public void publishUserRegisteredEvent(Integer userId, AppTypeEnum appType) {
        UserRegisteredEvent event = new UserRegisteredEvent(this, userId, appType);
        eventPublisher.publishEvent(event);
    }

    // ====================== 发布【用户注册完成】事件（第二步/Saga成功后） ======================
    public void publishUserRegistrationCompletedEvent(Integer userId, AppTypeEnum appType) {
        UserRegistrationCompletedEvent event = new UserRegistrationCompletedEvent(this, userId, appType);
        eventPublisher.publishEvent(event);
    }

    // ====================== 发布【用户注销】事件 ======================
    public void publishUserCanceledEvent(Integer userId) {
        UserCanceledEvent event = new UserCanceledEvent(this, userId);
        eventPublisher.publishEvent(event);
    }

    // ====================== 发布【用户注销完成】事件 ======================
    public void publishUserCancellationCompletedEvent(Integer userId) {
        UserCancellationCompletedEvent event = new UserCancellationCompletedEvent(this, userId);
        eventPublisher.publishEvent(event);
    }
}