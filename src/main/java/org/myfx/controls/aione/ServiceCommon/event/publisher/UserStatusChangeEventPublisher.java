package org.myfx.controls.aione.ServiceCommon.event.publisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.ServiceCommon.entity.event.UserStatusChangeNotifyEvent;
import org.myfx.controls.aione.ServiceCommon.serviceEnum.AppTypeEnum;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * 用户状态变更事件发布器
 * 替代原Kafka生产者，用于单体应用内发布用户上线/离线事件
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserStatusChangeEventPublisher {

    // 注入Spring事件发布器（核心替换KafkaTemplate）
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 发送用户离线通知（对外方法完全兼容原有调用）
     * @param userIdStr 字符串类型的用户ID
     * @param appTypeStr 字符串类型的应用类型
     */
    public void sendUserOfflineNotify(String userIdStr, String appTypeStr) {
        // 调用公共事件发布方法
        publishUserStatusChangeEvent(userIdStr, appTypeStr, "离线");
    }

    /**
     * 发送用户上线通知（对外方法完全兼容原有调用）
     * @param userId Integer类型的用户ID
     * @param appType AppTypeEnum枚举类型的应用类型
     */
    public void sendUserOnlineNotify(Integer userId, AppTypeEnum appType) {
        // 前置空值校验（原逻辑不变）
        if (userId == null) {
            log.error("用户上线事件发布失败：用户ID为空！");
            return;
        }
        if (appType == null) {
            log.error("用户上线事件发布失败：应用类型为空！用户ID：{}", userId);
            return;
        }

        // 类型转换（原逻辑不变）
        String userIdStr = userId.toString();
        String appTypeStr = appType.name();

        // 调用公共事件发布方法
        publishUserStatusChangeEvent(userIdStr, appTypeStr, "上线");
    }

    /**
     * 抽离公共方法：发布用户状态变更事件
     * 替代原Kafka发送逻辑，所有校验、转换完全保留
     */
    private void publishUserStatusChangeEvent(String userIdStr, String appTypeStr, String statusDesc) {
        // 1. 前置参数校验（原逻辑100%保留）
        if (userIdStr == null || userIdStr.trim().isEmpty()) {
            log.error("用户{}事件发布失败：用户ID字符串为空！", statusDesc);
            return;
        }
        if (appTypeStr == null || appTypeStr.trim().isEmpty()) {
            log.error("用户{}事件发布失败：应用类型字符串为空！用户ID：{}", statusDesc, userIdStr);
            return;
        }

        Integer userId;
        AppTypeEnum appType;

        // 2. 用户ID转换（原逻辑不变）
        try {
            userId = Integer.parseInt(userIdStr.trim());
        } catch (NumberFormatException e) {
            log.error("用户{}事件发布失败：用户ID转换为数字失败！ID：{}", statusDesc, userIdStr, e);
            return;
        }

        // 3. 应用类型转换（原逻辑不变）
        try {
            appType = AppTypeEnum.valueOf(appTypeStr.trim());
        } catch (IllegalArgumentException e) {
            log.error("用户{}事件发布失败：应用类型不合法！类型：{}", statusDesc, appTypeStr, e);
            return;
        }

        // 4. 封装事件对象（使用你提供的Event类）
        String appTypeDesc = appType.getDesc();
        Long statusTime = System.currentTimeMillis();

        // 5. 发布Spring本地事件（核心替换Kafka发送）
        UserStatusChangeNotifyEvent event = new UserStatusChangeNotifyEvent(
                this,
                userId,
                appType,
                appTypeDesc,
                statusDesc,
                statusTime
        );

        // 事件发布日志
        log.info("发布用户{}事件，用户ID: {}", statusDesc, userId);
        eventPublisher.publishEvent(event);
        log.info("用户{}事件发布成功，用户ID: {}", statusDesc, userId);
    }
}