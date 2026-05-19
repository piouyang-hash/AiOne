package org.myfx.controls.aione.UserService.event;

import lombok.Getter;
import org.myfx.controls.aione.ServiceCommon.serviceEnum.AppTypeEnum;
import org.springframework.context.ApplicationEvent;

/**
 * 用户登录成功事件
 * 用于异步处理登录后的相关业务逻辑
 */
@Getter
public class UserLoginSuccessEvent extends ApplicationEvent {

    private final Integer userId;    // 登录成功的用户ID
    private final AppTypeEnum appType; // 登录应用标识

    /**
     * 构造函数：创建用户登录成功事件
     * @param source 事件源（通常是触发对象，如UserServiceImpl实例）
     * @param userId 登录成功的用户ID
     * @param appType 登录应用标识枚举
     */
    public UserLoginSuccessEvent(Object source, Integer userId, AppTypeEnum appType) {
        super(source);
        this.userId = userId;
        this.appType = appType;
    }
}