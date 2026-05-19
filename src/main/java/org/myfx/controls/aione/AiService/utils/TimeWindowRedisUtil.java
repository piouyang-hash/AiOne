package org.myfx.controls.aione.AiService.utils;

import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import java.util.concurrent.TimeUnit;

/**
 * 通用时间窗口限制工具（Redis版）
 * 功能：在指定周期内，同一个key只允许通过一次，周期结束自动刷新
 */
@Component
public class TimeWindowRedisUtil {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 获取时间窗口周期（小时）
     * 子类可重写、或从配置文件读取，实现灵活修改
     * @return 默认 8 小时
     */
    public long getWindowHours() {
        return 8;
    }

    /**
     * 通用判断：当前 key 在本周期内是否允许执行
     * @param key 唯一标识（如 userId、业务key）
     * @return true=允许；false=周期内已执行过
     */
    public boolean isAllowed(String key) {
        long hours = getWindowHours();
        Boolean success = stringRedisTemplate.opsForValue()
                .setIfAbsent(key, "1", hours, TimeUnit.HOURS);
        return Boolean.TRUE.equals(success);
    }

    /**
     * 获取当前 key 距离窗口刷新的剩余时间（秒）
     */
    public long getRemainingSeconds(String key) {
        Long expire = stringRedisTemplate.getExpire(key, TimeUnit.SECONDS);
        return Math.max(expire, 0);
    }
}