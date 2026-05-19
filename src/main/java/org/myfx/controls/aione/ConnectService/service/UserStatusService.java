package org.myfx.controls.aione.ConnectService.service;

import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.ConnectService.common.DeviceTypeEnum;
import org.myfx.controls.aione.ConnectService.entity.UserStatus;
import org.myfx.controls.aione.ConnectService.utils.UserStatusRedisUtil;
import org.myfx.controls.aione.ServiceCommon.event.publisher.UserStatusChangeEventPublisher;
import org.myfx.controls.aione.ServiceCommon.serviceEnum.AppTypeEnum;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 用户状态业务处理类
 * 封装：存储状态到Redis + 生成WebSocket连接地址
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserStatusService {

    private final UserStatusRedisUtil userStatusRedisUtil;
    private final UserStatusChangeEventPublisher userStatusChangeEventPublisher;

    // 配置化读取WebSocket地址（仅用于生成字符串，不存实体）
    @Value("${connect.websocket.server.addr}")
    private String websocketServerAddr;
    @Value("${connect.websocket.path}")
    private String websocketPath;

    /**
     * 存储用户状态到Redis
     * 初始化心跳时间 + 保存用户状态 + 发布用户上线事件
     * @param status 核心用户状态（无冗余字段）
     */
    public void saveUserStatus(UserStatus status) {
        // 1. 初始化心跳时间（核心逻辑保留）
        if (status.getLastHeartbeatTime() == null) {
            status.setLastHeartbeatTime(System.currentTimeMillis());
        }

        // 2. 存储核心状态到Redis
        userStatusRedisUtil.saveUserStatus(status);

        // 3. 查询Redis：判断用户是否已在线，仅不在线时发布上线事件
        boolean isOnline = userStatusRedisUtil.isUserOnline(status.getUserId(), status.getAppType());
        if (!isOnline) {
            // 调用事件发布器（替换原Kafka生产者）
            userStatusChangeEventPublisher.sendUserOnlineNotify(
                    status.getUserId(),
                    status.getAppType()
            );
        }
    }

    /**
     * 构建登录用户状态（通用版：支持HTTP接口 + WS连接）
     * 所有参数外部传入，无任何上下文依赖！
     */
    public void buildLoginUserStatusAndInitWsConnect(
            Integer userId,          // 用户ID（HTTP从JWT拿，WS从握手拿）
            AppTypeEnum appType,     // 应用类型
            String realIp,           // 真实IP（自动获取）
            String userAgent         // 设备信息（自动获取）
    ) {
        // 1. 参数校验
        if (userId == null) throw new IllegalArgumentException("用户ID不能为空");
        if (StrUtil.isBlank(realIp)) throw new IllegalArgumentException("IP不能为空");
        if (StrUtil.isBlank(userAgent)) userAgent = "WebSocket-Client";

        // 2. 解析设备类型
        DeviceTypeEnum deviceType = DeviceTypeEnum.parseFromUserAgent(userAgent);

        // 3. 构建用户状态（你的原有逻辑，完全不动）
        UserStatus loginUserStatus = new UserStatus();
        loginUserStatus.setUserId(userId);
        long now = System.currentTimeMillis();
        loginUserStatus.setLoginTime(now);
        loginUserStatus.setLastHeartbeatTime(now);
        loginUserStatus.setDevice(deviceType);
        loginUserStatus.setIp(realIp);
        loginUserStatus.setAppType(appType);

        // 4. 存Redis + 发本地事件（你的原有逻辑）
        saveUserStatus(loginUserStatus);
    }

//    /**
//     * 【业务方法】主动退出登录：删除用户整个在线状态
//     * 调用方：用户微服务（用户点击退出登录时）
//     * @param userId 用户ID
//     */
//    public void deleteUserStatus(Integer userId) {
//        // 委托Redis工具类执行删除操作
//        userStatusRedisUtil.deleteUserStatus(userId);
//        log.info("业务层：用户{}主动退出登录，已触发Redis状态删除", userId);
//    }
//
//    /**
//     * 【业务方法】查询用户是否在线（活跃在线：登录态有效+心跳未超时）
//     * 调用方：订单/消息等微服务（判断用户在线状态时）
//     * @param userId 用户ID
//     * @return true=在线，false=离线
//     */
//    public boolean isUserOnline(Integer userId) {
//        // 委托Redis工具类执行在线状态判断
//        boolean isOnline = userStatusRedisUtil.isUserOnline(userId);
//        log.info("业务层：查询用户{}在线状态，结果：{}", userId, isOnline);
//        return isOnline;
//    }
}
