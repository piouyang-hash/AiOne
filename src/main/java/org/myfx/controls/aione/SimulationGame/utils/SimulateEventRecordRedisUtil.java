package org.myfx.controls.aione.SimulationGame.utils;

import cn.hutool.core.util.StrUtil;
import jakarta.annotation.Resource;
import org.myfx.controls.aione.SimulationGame.entity.SimulateEventRecordRedis;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 模拟游戏事件记录 Redis 操作工具类（精简版）
 * 仅保留核心：存、取、删 + Key构建
 */
@Component
public class SimulateEventRecordRedisUtil {

    // 注入通用/专属RedisTemplate（根据你的实际配置选择，这里用专属模板示例）
    @Resource
    @Qualifier("eventRecordRedisTemplate")
    private RedisTemplate<String, SimulateEventRecordRedis> eventRecordRedisTemplate;

    @Resource
    @Qualifier("eventRecordKeyPrefix")
    private String eventRecordKeyPrefix;


    // ========== 新增：公共参数校验方法（Key构建/查询/删除共用） ==========
    /**
     * 私有方法：校验Redis Key相关参数（地点编码+事件编码+实际开始时间）
     * @param locationCode 地点编码
     * @param eventCode 事件编码
     * @param actualStart 实际开始时间（游戏服务器时间）
     * @throws IllegalArgumentException 参数不合法时抛出
     */
    private void validateEventRecordKeyParams(String locationCode, String eventCode, Integer actualStart) {
        // 地点编码校验
        if (StrUtil.isBlank(locationCode)) {
            throw new IllegalArgumentException("地点编码不能为空！");
        }
        // 事件编码校验
        if (StrUtil.isBlank(eventCode)) {
            throw new IllegalArgumentException("事件编码不能为空！");
        }
        // 实际开始时间校验
        if (actualStart == null) {
            throw new IllegalArgumentException("实际开始时间（actualStart）不能为空！");
        }
        if (actualStart < 0) {
            throw new IllegalArgumentException("实际开始时间（actualStart）不能为负数！");
        }
    }

    // ========== 核心修改：构建Redis Key（复用公共校验） ==========
    /**
     * 私有核心方法：构建Redis Key（使用配置的前缀 + 地点编码 + 事件编码 + 实际开始时间）
     * Key格式：{prefix}:{locationCode}:{eventCode}:{actualStart}
     * @param locationCode 地点编码（非空）
     * @param eventCode 事件编码（非空）
     * @param actualStart 实际开始时间（游戏服务器时间，非空且>=0）
     * @return 完整的Redis Key
     */
    private String buildEventRecordKey(String locationCode, String eventCode, Integer actualStart) {
        // 复用公共校验方法，删除原有重复校验逻辑
        validateEventRecordKeyParams(locationCode, eventCode, actualStart);

        // 动态拼接Key：前缀:地点:事件:实际开始时间
        return StrUtil.format("{}:{}:{}:{}",
                eventRecordKeyPrefix.trim(),
                locationCode.trim(),
                eventCode.trim(),
                actualStart);
    }

    // ========== 修改：存储事件记录到Redis（适配新Key） ==========
    /**
     * 存储事件记录到Redis（新增过期时间参数，必填）
     * @param record 事件记录实体（非空）
     * @param expireSeconds 过期时间（秒数，必填，且必须大于0）
     */
    public void saveEventRecord(SimulateEventRecordRedis record, Integer expireSeconds) {
        // 原有参数校验
        if (record == null) {
            throw new IllegalArgumentException("事件记录实体不能为空！");
        }
        // 复用公共校验方法（校验record中的Key相关参数）
        validateEventRecordKeyParams(record.getLocationCode(), record.getEventCode(), record.getActualStart());

        // 过期时间参数校验
        if (expireSeconds == null || expireSeconds <= 0) {
            throw new RuntimeException("过期时间参数不能为空，且必须是大于0的秒数！");
        }

        ValueOperations<String, SimulateEventRecordRedis> operations = eventRecordRedisTemplate.opsForValue();
        // 调用构建Key方法（自动触发参数校验）
        String key = buildEventRecordKey(record.getLocationCode(), record.getEventCode(), record.getActualStart());
        operations.set(key, record, expireSeconds, TimeUnit.SECONDS);
    }

    // ========== 修改：查询事件记录（适配新Key，复用公共校验） ==========
    /**
     * 根据地点+事件编码+实际开始时间查询事件记录
     * @param locationCode 地点编码（非空）
     * @param eventCode 事件编码（非空）
     * @param actualStart 实际开始时间（游戏服务器时间，非空且>=0）
     * @return 事件记录（无数据返回null）
     */
    public SimulateEventRecordRedis getEventRecord(String locationCode, String eventCode, Integer actualStart) {
        ValueOperations<String, SimulateEventRecordRedis> operations = eventRecordRedisTemplate.opsForValue();
        // 调用构建Key方法（自动触发公共参数校验）
        String key = buildEventRecordKey(locationCode, eventCode, actualStart);
        return operations.get(key);
    }

    // ========== 新增：获取事件记录Redis键的过期时间（秒），返回值改为Integer ==========
    /**
     * 根据地点+事件编码+实际开始时间，获取Redis键的剩余过期时间（单位：秒）
     * @param locationCode 地点编码（非空）
     * @param eventCode 事件编码（非空）
     * @param actualStart 实际开始时间（游戏服务器时间，非空且>=0）
     * @return 过期时间：>0=剩余秒数；-1=永久有效；-2=键不存在
     * @throws IllegalArgumentException 参数不合法时抛出
     */
    public Integer getEventRecordExpireSeconds(String locationCode, String eventCode, Integer actualStart) {
        // 调用构建Key方法（自动触发公共参数校验）
        String key = buildEventRecordKey(locationCode, eventCode, actualStart);
        // RedisTemplate.getExpire返回Long，安全转换为Integer（过期时间不会超过int上限）
        Long expireLong = eventRecordRedisTemplate.getExpire(key);
        return expireLong.intValue(); // 兼容null返回-2（键不存在）
    }

    // ========== 修改：删除事件记录（适配新Key，复用公共校验） ==========
    /**
     * 根据地点+事件编码+实际开始时间删除事件记录
     * @param locationCode 地点编码（非空）
     * @param eventCode 事件编码（非空）
     * @param actualStart 实际开始时间（游戏服务器时间，非空且>=0）
     * @return true=删除成功/存在该键；false=删除失败/无该键（兼容集群返回null）
     */
    public boolean deleteEventRecord(String locationCode, String eventCode, Integer actualStart) {
        // 调用构建Key方法（自动触发公共参数校验）
        String key = buildEventRecordKey(locationCode, eventCode, actualStart);
        // 处理Redis集群返回null的情况，统一返回false
        return eventRecordRedisTemplate.delete(key);
    }
}