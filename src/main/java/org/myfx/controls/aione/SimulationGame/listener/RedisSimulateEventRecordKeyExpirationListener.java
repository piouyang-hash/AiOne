package org.myfx.controls.aione.SimulationGame.listener;

import cn.hutool.core.util.StrUtil;
import jakarta.annotation.Resource;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.SimulationGame.entity.SimulateEvent;
import org.myfx.controls.aione.SimulationGame.entity.SimulateEventSequence;
import org.myfx.controls.aione.SimulationGame.entity.SimulateLocation;
import org.myfx.controls.aione.SimulationGame.entity.SimulateLocationEventRelation;
import org.myfx.controls.aione.SimulationGame.service.SimGameEventPublisher;
import org.myfx.controls.aione.SimulationGame.service.SimulateEventService;
import org.myfx.controls.aione.SimulationGame.service.SimulateLocationEventRelationService;
import org.myfx.controls.aione.SimulationGame.service.SimulateLocationService;
import org.myfx.controls.aione.SimulationGame.service.upper.EventRelayService;
import org.myfx.controls.aione.SimulationGame.service.upper.UpperSequenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * Redis模拟游戏事件记录键过期事件监听器
 * 专门监听 SIMULATE:EVENT_RECORD:{locationCode}:{eventCode} 格式的键过期，处理事件记录过期逻辑
 */
@Slf4j
@Component
public class RedisSimulateEventRecordKeyExpirationListener extends KeyExpirationEventMessageListener {

    @Resource
    @Qualifier("eventRecordKeyPrefix")
    private String eventRecordKeyPrefix;

    @Autowired
    private SimulateLocationEventRelationService simulateLocationEventRelationService;

    @Autowired
    private EventRelayService eventRelayService;

    // 新增注入：地点/事件服务（用于通过编码查描述）
    @Autowired
    private SimulateLocationService simulateLocationService;

    @Autowired
    private SimulateEventService simulateEventService;

    @Autowired
    private SimGameEventPublisher simGameEventPublisher;

    @Autowired
    private UpperSequenceService upperSequenceService;

    /**
     * 构造方法，注入Redis消息监听容器（父类必须的构造参数）
     */
    public RedisSimulateEventRecordKeyExpirationListener(RedisMessageListenerContainer listenerContainer) {
        super(listenerContainer);
    }

    /**
     * 核心方法：监听并处理Redis键过期事件
     * 仅处理以配置前缀开头的事件记录键
     * @param message 过期的键名（字节数组形式）
     * @param pattern 监听的频道模式
     */
    @Override
    public void onMessage(@NonNull Message message, byte[] pattern) {
        // 1. 将字节数组转换为字符串（过期的键名），指定UTF-8编码避免乱码
        String expiredKey;
        try {
            expiredKey = new String(message.getBody(), StandardCharsets.UTF_8);
            log.info("【Redis过期监听】捕获到Redis键过期，键名：{}", expiredKey);
        } catch (Exception e) {
            log.error("【Redis过期监听】过期键名转换字符串失败", e);
            return;
        }

        // 2. 拼接前缀的完整匹配规则（配置前缀 + 冒号，和工具类Key构建逻辑一致）
        String fullPrefix = eventRecordKeyPrefix + ":";
        // 只处理事件记录前缀的键，过滤无关过期键
        if (expiredKey.startsWith(fullPrefix)) {
            handleEventRecordKeyExpired(expiredKey);
        }
    }


    private void handleEventRecordKeyExpired(String expiredKey) {
        String locationCode = null;
        String eventCode = null;
        Integer actualStart = null;
        String[] keyParts = expiredKey.split(":");

        try {
            // 1. 校验键格式合法性
            if (keyParts.length != 5) {
                log.error("【Redis过期监听】事件记录键格式错误，不符合规范（需5段），键名：{}", expiredKey);
                return;
            }

            // 2. 解析核心参数
            locationCode = keyParts[2];
            eventCode = keyParts[3];
            try {
                actualStart = Integer.parseInt(keyParts[4]);
            } catch (NumberFormatException e) {
                log.error("【Redis过期监听】实际开始时间转换失败，keyParts[4]={}，键名：{}", keyParts[4], expiredKey, e);
                return;
            }

            // 3. 校验编码/开始时间合法性
            if (StrUtil.isBlank(locationCode) || StrUtil.isBlank(eventCode)) {
                log.error("【Redis过期监听】事件记录键中编码为空，locationCode={}，eventCode={}，键名：{}",
                        locationCode, eventCode, expiredKey);
                return;
            }
            if (actualStart < 0) {
                log.error("【Redis过期监听】实际开始时间不合法，actualStart={}，键名：{}", actualStart, expiredKey);
                return;
            }

            // ========== 核心业务逻辑 ==========
            log.info("【Redis过期监听】开始处理过期业务逻辑 | locationCode={}, eventCode={}, actualStart={}",
                    locationCode, eventCode, actualStart);

            // 4. 调用事件传递服务
            boolean transferSuccess = eventRelayService.transferEventRelay(locationCode, eventCode, actualStart);

            if (!transferSuccess) {
                log.error("【Redis过期监听】事件传递失败，不生产消息 | locationCode={}, eventCode={}, actualStart={}",
                        locationCode, eventCode, actualStart);
                return;
            }

            // 5. 生产事件消息（仅在传递成功后执行）
            // 需要重新获取事件持续时间来计算结束时间
            SimulateLocationEventRelation relation = simulateLocationEventRelationService.getRelationByTwoCode(locationCode, eventCode);
            if (relation == null) {
                log.error("【Redis过期监听】生产消息失败：未查询到关联关系，locationCode={}，eventCode={}", locationCode, eventCode);
                return;
            }

            Integer eventDuration = relation.getEventDuration();
            if (eventDuration == null || eventDuration <= 0) {
                log.error("【Redis过期监听】生产消息失败：事件持续时间无效，eventDuration={}，locationCode={}，eventCode={}",
                        eventDuration, locationCode, eventCode);
                return;
            }

            // 计算结束时间
            Integer actualEnd = actualStart + eventDuration;

            // 生产事件消息
            produceEventMessage(locationCode, eventCode, actualStart, actualEnd);
            log.info("【Redis过期监听】事件消息生产完成 | locationCode={}, eventCode={}, actualStart={}, actualEnd={}",
                    locationCode, eventCode, actualStart, actualEnd);

        } catch (ArrayIndexOutOfBoundsException e) {
            log.error("【Redis过期监听】事件记录键拆分失败，数组越界，键名：{}", expiredKey, e);
        } catch (Exception e) {
            log.error("【Redis过期监听】处理事件记录键过期失败，键名：{}", expiredKey, e);
        }
    }

    /**
     * 私有方法：生产事件消息（补充编码转描述+发布本地事件）
     * @param locationCode 地点编码
     * @param eventCode 事件编码
     * @param actualStart 实际开始时间（时间戳/秒，空则设为0）
     * @param actualEnd 实际结束时间（时间戳/秒，空则设为0）
     */
    private void produceEventMessage(String locationCode, String eventCode, Integer actualStart, Integer actualEnd) {
        // ========== 第一步：计算事件持续时间（单位：秒） ==========
        int eventDuration = 0; // 默认值
        try {
            // 判空处理：start/end为空则设为0
            int start = (actualStart == null) ? 0 : actualStart;
            int end = (actualEnd == null) ? 0 : actualEnd;
            // 避免负数（结束时间不能早于开始时间）
            eventDuration = Math.max(end - start, 0);
        } catch (Exception e) {
            log.warn("计算事件持续时间异常 | actualStart={}, actualEnd={}", actualStart, actualEnd, e);
        }
        log.info("【Redis过期监听-生产事件消息】计算出事件持续时间：{}秒", eventDuration);

        // ========== 第二步：获取【下一个事件序列】（核心补充） ==========
        String nextLocationCode = null;
        String nextEventCode = null;
        try {
            // 调用接口获取下一个事件序列（你提供的重载方法）
            SimulateEventSequence nextSequence = upperSequenceService.getNextEventSequence(locationCode, eventCode, actualStart);
            if (nextSequence != null) {
                nextLocationCode = nextSequence.getLocationCode();
                nextEventCode = nextSequence.getEventCode();
            }
        } catch (Exception e) {
            log.error("获取下一个事件序列失败 | locationCode={}, eventCode={}", locationCode, eventCode, e);
        }

        // ========== 第三步：通过编码获取描述（当前+下一个） ==========
        String locationDesc;
        String eventDesc;
        String nextLocationDesc;
        String nextEventDesc;
        try {
            // 1. 当前地点/事件描述
            SimulateLocation location = simulateLocationService.getGameLocationByCode(locationCode);
            locationDesc = location != null ? location.getLocationDesc() : locationCode;

            SimulateEvent event = simulateEventService.getGameEventByCode(eventCode);
            eventDesc = event != null ? event.getEventDesc() : eventCode;

            // 2. 下一个地点/事件描述（新增，对齐规则）
            SimulateLocation nextLocation = simulateLocationService.getGameLocationByCode(nextLocationCode);
            nextLocationDesc = nextLocation != null ? nextLocation.getLocationDesc() : nextLocationCode;

            SimulateEvent nextEvent = simulateEventService.getGameEventByCode(nextEventCode);
            nextEventDesc = nextEvent != null ? nextEvent.getEventDesc() : nextEventCode;

        } catch (Exception e) {
            log.error("通过编码查询描述失败 | locationCode={}, eventCode={}, nextLocationCode={}, nextEventCode={}",
                    locationCode, eventCode, nextLocationCode, nextEventCode, e);
            // 异常兜底：全部使用编码作为描述
            locationDesc = locationCode;
            eventDesc = eventCode;
            nextLocationDesc = nextLocationCode;
            nextEventDesc = nextEventCode;
        }

        // ========== 第四步：调用事件发布器（替换Kafka，参数完全一致） ==========
        simGameEventPublisher.sendGameEventMessage(
                locationDesc,       // 1. 当前地点描述
                eventDesc,          // 2. 当前事件描述
                eventDuration,      // 3. 事件持续时间(秒)
                nextLocationDesc,   // 4. 下一个地点描述
                nextEventDesc       // 5. 下一个事件描述
        );
    }
}