package org.myfx.controls.aione.UserService.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.ServiceCommon.serviceEnum.AppTypeEnum;
import org.myfx.controls.aione.UserService.event.UserLoginSuccessEvent;
import org.myfx.controls.aione.UserService.model.entity.UserProfile;
import org.myfx.controls.aione.UserService.service.UserProfileService;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class UserLoginEventListener {

    private final UserProfileService userProfileService;

    /**
     * 处理用户登录成功事件
     * 用于执行登录后的异步业务逻辑
     */
    @EventListener
    @Async
    public void handleUserLoginSuccess(UserLoginSuccessEvent event) {
        try {
            Integer userId = event.getUserId();
            AppTypeEnum appType = event.getAppType();

            log.info("用户登录成功: userId={}, appType={}", userId, appType);

            // 1. 记录登录日志
            // loginLogService.recordLogin(userId, appType);

            // 2. 更新用户最后登录时间
            // userService.updateLastLoginTime(userId);

            // 3. 【核心】按需初始化用户在该应用的资料
            // 实现"按需初始化"的逻辑
            UserProfile userProfile = userProfileService.getUserProfile(userId, appType);
            if (userProfile == null) {
                // 用户在该应用下没有资料，执行初始化
                userProfileService.addUserProfile(userId, appType);
                log.info("已为用户初始化应用资料: userId={}, appType={}", userId, appType);
            } else {
                log.info("用户已有应用资料，无需初始化: userId={}, appType={}", userId, appType);
            }

        } catch (Exception e) {
            log.error("处理用户登录事件异常", e);
        }
    }
}