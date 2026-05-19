package org.myfx.controls.aione.ServiceCommon.event.eventDTO;

import lombok.Getter;
import org.myfx.controls.aione.ServiceCommon.serviceEnum.AppTypeEnum;
import org.springframework.context.ApplicationEvent;

/**
 * 用户注册成功事件
 */
@Getter
public class UserRegisteredEvent extends ApplicationEvent {

    private final Integer userId;    // 传递用户ID
    private final AppTypeEnum appType; // 新增：传递应用标识（多应用隔离核心）

    /**
     * 构造函数：触发注册事件时传递用户ID+应用标识
     * @param source 事件源（通常是当前类/触发对象，如UserServiceImpl实例）
     * @param userId 注册成功的用户ID
     * @param appType 应用标识枚举（用于创建对应应用的用户资料）
     */
    public UserRegisteredEvent(Object source, Integer userId, AppTypeEnum appType) {
        super(source);
        this.userId = userId;
        this.appType = appType; // 赋值新增的应用标识字段
    }
}