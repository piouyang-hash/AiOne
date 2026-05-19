package org.myfx.controls.aione.ConnectService.utils;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.ConnectService.entity.UserStatus;
import org.myfx.controls.aione.ServiceCommon.serviceEnum.AppTypeEnum;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.concurrent.TimeUnit;

/**
 * 微服务Redis用户状态工具类（解耦，方便其他服务调用）
 */
@Component
@Slf4j
public class UserStatusRedisUtil {
    // Redis键前缀（微服务隔离：connect-service专属）
    private static final String USER_STATUS_PREFIX = "connect:user:status:";
    // 默认过期时间：5分钟（登录状态有效期）
    private static final long DEFAULT_EXPIRE_SECONDS = 300L; // 5分钟
    // 心跳超时阈值：30秒（无心跳则判定离线）
    public static final long HEARTBEAT_TIMEOUT = 30 * 1000L;

    @Resource(name = "userStatusRedisTemplate")
    private RedisTemplate<String, UserStatus> redisTemplate;

    /**
     * 生成带appType的Redis Key（统一封装，避免重复拼接）
     */
    private String generateKey(Integer userId, AppTypeEnum appType) {
        String appName = appType.name();
        return String.format("%s%s:%s", USER_STATUS_PREFIX, userId, appName);
    }

    /**
     * 存储用户状态（用户微服务登录后调用）
     * 【核心修改】：Redis Key拼接appType，格式：user:status:{userId}:{appTypeCode}
     */
    public void saveUserStatus(UserStatus status) {
        // 1. 核心参数校验（强化：必传userId + appType）
        Assert.notNull(status, "用户状态对象不能为空");
        Assert.notNull(status.getUserId(), "用户ID不能为空");
        Assert.notNull(status.getAppType(), "应用类型编码（appType）不能为空");

        // 2. 生成包含appType的Redis Key（核心修改点）
        String key = generateKey(status.getUserId(), status.getAppType());

        // 3. 存储到Redis（保留原有逻辑，仅Key变更）
        redisTemplate.opsForValue().set(
                key,
                status,
                DEFAULT_EXPIRE_SECONDS,
                TimeUnit.SECONDS);
     }

    /**
     * 刷新心跳（长连接心跳触发）
     * 【核心修改】：补充appType参数，定位到对应应用的用户状态
     */
    public boolean refreshHeartbeat(Integer userId, AppTypeEnum appType) {
        // 参数校验
        Assert.notNull(userId, "用户ID不能为空");
        Assert.notNull(appType, "应用类型编码（appType）不能为空");

        // 生成带appType的Key
        String key = generateKey(userId, appType);
        UserStatus status = redisTemplate.opsForValue().get(key);

        if (status == null) {
            log.warn("用户{}（应用：{}）心跳刷新失败：Redis中无该状态（Key：{}）", userId, appType, key);
            return false; // 用户未登录/状态过期
        }

        // 更新最后心跳时间+重置过期时间
        status.setLastHeartbeatTime(System.currentTimeMillis());
        redisTemplate.opsForValue().set(key, status, DEFAULT_EXPIRE_SECONDS, TimeUnit.SECONDS);
        return true;
    }

    /**
     * 主动退出登录：删除指定应用的用户状态（精准删除）
     * 【核心修改】：补充appType，仅删除对应应用的状态
     */
    public void deleteUserStatus(Integer userId, AppTypeEnum appType) {
        // 参数校验
        Assert.notNull(userId, "用户ID不能为空");
        Assert.notNull(appType, "应用类型编码（appType）不能为空");

        String key = generateKey(userId, appType);
        Boolean deleted = redisTemplate.delete(key);

        if (deleted) {
            log.info("用户{}（应用：{}）已退出登录，Redis状态已删除（Key：{}）", userId, appType, key);
        } else {
            log.warn("用户{}（应用：{}）退出登录失败：Redis中无该状态（Key：{}）", userId, appType, key);
        }
    }

    /**
     * 【重载方法】主动退出登录：删除用户所有应用的状态（慎用，生产建议精准删除）
     * 注：keys模糊匹配性能较低，仅在全端退出时使用
     */
    public void deleteUserAllAppStatus(Integer userId) {
        Assert.notNull(userId, "用户ID不能为空");
        String pattern = USER_STATUS_PREFIX + userId + ":*";
        Long deletedCount = redisTemplate.delete(redisTemplate.keys(pattern));

        if (deletedCount > 0) {
            log.info("用户{}所有应用的状态已删除，共删除{}个Key", userId, deletedCount);
        } else {
            log.warn("用户{}所有应用状态删除失败：Redis中无相关Key", userId);
        }
    }

    /**
     * 检测用户是否在线（指定应用）
     * 【核心修改】：补充appType，仅校验对应应用的状态
     */
    public boolean isUserOnline(Integer userId, AppTypeEnum appType) {
        // 参数校验
        Assert.notNull(userId, "用户ID不能为空");
        Assert.notNull(appType, "应用类型编码（appType）不能为空");

        String key = generateKey(userId, appType);
        UserStatus status = redisTemplate.opsForValue().get(key);

        if (status == null) {
            return false;
        }

        // 校验：最后心跳时间未超时
        return System.currentTimeMillis() - status.getLastHeartbeatTime() <= HEARTBEAT_TIMEOUT;
    }

    /**
     * 绑定用户与长连接通道ID（指定应用）
     * 【核心修改】：补充appType，定位到对应应用的用户状态
     */
    public void bindChannel(Integer userId, AppTypeEnum appType, String channelId) {
        // 参数校验
        Assert.notNull(userId, "用户ID不能为空");
        Assert.notNull(appType, "应用类型编码（appType）不能为空");
        Assert.notNull(channelId, "通道ID不能为空");

        String key = generateKey(userId, appType);
        UserStatus status = redisTemplate.opsForValue().get(key);

        if (status != null) {
            status.setChannelId(channelId);
            redisTemplate.opsForValue().set(key, status, DEFAULT_EXPIRE_SECONDS, TimeUnit.SECONDS);
            log.info("用户{}（应用：{}）已绑定通道ID：{}", userId, appType, channelId);
        } else {
            log.warn("用户{}（应用：{}）绑定通道失败：Redis中无该状态", userId, appType);
        }
    }
}