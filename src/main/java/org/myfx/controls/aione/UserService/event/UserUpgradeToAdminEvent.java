package org.myfx.controls.aione.UserService.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 用户提升为超级管理员事件
 * 承载提升操作的核心参数：userId
 */
@Getter
public class UserUpgradeToAdminEvent extends ApplicationEvent {
    // 获取用户ID的getter
    // 核心参数：要提升的用户ID
    private final Integer userId;

    /**
     * 构造函数（Spring事件必须传source，这里传userId作为source，也可传当前服务类实例）
     * @param userId 要提升的用户ID
     */
    public UserUpgradeToAdminEvent(Integer userId) {
        super(userId); // source参数，Spring事件必需
        this.userId = userId;
    }

}