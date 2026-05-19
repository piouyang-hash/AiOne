package org.myfx.controls.aione.SimulationGame.service.upper.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.SimulationGame.common.EventExecStatusEnum;
import org.myfx.controls.aione.SimulationGame.dto.EventEndMessageDTO;
import org.myfx.controls.aione.SimulationGame.entity.SimulateEvent;
import org.myfx.controls.aione.SimulationGame.entity.SimulateEventRecord;
import org.myfx.controls.aione.SimulationGame.entity.SimulateEventSequence;
import org.myfx.controls.aione.SimulationGame.entity.SimulateLocation;
import org.myfx.controls.aione.SimulationGame.service.*;
import org.myfx.controls.aione.SimulationGame.service.upper.ScheduleEventLoopService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 定时事件循环服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleEventLoopServiceImpl implements ScheduleEventLoopService {

    // 注入全局游戏时间服务（必用）
    private final GameGlobalTimeService gameGlobalTimeService;
    private final SimulateEventRecordService simulateEventRecordService;
    private final UpperSequenceServiceImpl upperSequenceService;
    private final SimulateLocationEventRelationService simulateLocationEventRelationService;
    private final SimulateEventSequenceService simulateEventSequenceService;

    // 新增注入：地点/事件服务（用于通过编码查描述）
    private final SimulateLocationService simulateLocationService;

    private final SimulateEventService simulateEventService;

    private final SimGameEventPublisher simGameEventPublisher;
    /**
     * 初始化事件循环（项目启动时执行）
     * 规则：数据库无任何事件记录时，才初始化第一条事件；有记录则直接跳过
     */
    @Override
    public void initEventLoop() {
        // ========== 第一步：查询所有事件记录，判断是否需要初始化 ==========
        List<SimulateEventRecord> eventRecordList = simulateEventRecordService.listAllSimulateEventRecord();
        if (eventRecordList != null && !eventRecordList.isEmpty()) {
            return; // 幂等性：有任何记录，直接终止初始化
        }

        // ========== 第二步：获取首个事件序列（周一第一个事件） ==========
        SimulateEventSequence firstEventSequence = simulateEventSequenceService.getFirstEventSequence();
        if (firstEventSequence == null) {
            log.error("【事件循环初始化】失败：未查询到首个事件序列数据");
            throw new RuntimeException("初始化失败：未查询到首个事件序列数据");
        }

        // ========== 第三步：构建并保存第一条执行中事件 ==========
        // 初始游戏时间为 0（全局时间基准）
        SimulateEventRecord eventRecord = buildEventRecord(firstEventSequence, 0);
        simulateEventRecordService.saveSimulateEventRecord(eventRecord);
    }

    /**
     * 执行事件循环（定时任务核心调用）
     * 逻辑：判断事件到期 → 结束当前事件 → 创建下一个事件 → 周循环
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void executeEventLoop() {
        // 1. 获取当前全局游戏时间
        Integer currentGameSeconds = gameGlobalTimeService.getCurrentGameSeconds();

        // 2. 查询当前执行中的事件
        SimulateEventRecord currentRecord = simulateEventRecordService.getCurrentTask(EventExecStatusEnum.EXECUTING);
        // 无执行中事件，直接返回
        if (currentRecord == null) {
            return;
        }

        // 【修正后】正确到期判断逻辑
        // 到期时间 = 开始时间 + 事件默认时长
        // 当前时间 >= 到期时间 → 事件完成
        Integer actualStart = currentRecord.getActualStart();
        Integer defaultDuration = currentRecord.getDefaultDuration();
        // 计算事件应该到期的时间点
        Integer expireTime = actualStart + defaultDuration;
        // 判断：当前游戏时间 大于等于 到期时间 → 已完成
        boolean isExpired = currentGameSeconds >= expireTime;

        // 4. 未到期：直接返回
        if (!isExpired) {
            return;
        }

        // ========================
        // 5. 已到期：更新事件为已完成
        // ========================
        String locationCode = currentRecord.getLocationCode();
        String eventCode = currentRecord.getEventCode();
        simulateEventRecordService.updateEventRecord(
                locationCode,
                eventCode,
                actualStart,
                currentGameSeconds,
                EventExecStatusEnum.FINISHED
        );

        // ========================
        // 6. 查询下一个事件/下一周事件
        // ========================
        SimulateEventSequence nextSequence = upperSequenceService.getNextEventSequence(
                locationCode,
                eventCode,
                actualStart
        );
        // 无下一个事件，直接返回
        if (nextSequence == null) {
            return;
        }

        // 7. 插入新的执行中事件
        // 下一个事件的开始时间 = 当前游戏时间（上一个事件的结束时间）
        SimulateEventRecord newEventRecord = buildEventRecord(
                nextSequence,
                currentGameSeconds
        );
        simulateEventRecordService.saveSimulateEventRecord(newEventRecord);

        // ✅ 【移动到这里】事件结束 → 生产Kafka消息
        EventEndMessageDTO dto = new EventEndMessageDTO();
        // 当前已结束事件参数
        dto.setLocationCode(locationCode);
        dto.setEventCode(eventCode);
        // ===================== 核心：直接计算 秒 → 分钟（你要求的语句） =====================
        // 计算持续时间（单位：分钟，秒转分钟 + 非负兜底）
        Integer eventDuration = Math.max((currentGameSeconds - actualStart) / 60, 0);
        dto.setEventDuration(eventDuration);
        // 下一个事件参数（必填）
        dto.setNextLocationCode(nextSequence.getLocationCode());
        dto.setNextEventCode(nextSequence.getEventCode());
        // nextEventDuration 不填写，留空
        produceEventMessage(dto);
    }

    /**
     * 生产事件结束消息（使用DTO入参）
     * @param dto 事件结束消息DTO
     */
    private void produceEventMessage(EventEndMessageDTO dto) {
        // 从DTO获取核心参数
        String locationCode = dto.getLocationCode();
        String eventCode = dto.getEventCode();
        // 直接获取已计算好的 持续时间（分钟）
        Integer eventDuration = dto.getEventDuration();
        // 下一个事件参数
        String nextLocationCode = dto.getNextLocationCode();
        String nextEventCode = dto.getNextEventCode();

        // ========== 通过编码获取描述（原有逻辑+新增下一个参数的描述转换） ==========
        String locationDesc;
        String eventDesc;
        String nextLocationDesc; // 下一个地点描述
        String nextEventDesc;    // 下一个事件描述
        try {
            // 当前地点/事件描述查询（原有逻辑）
            SimulateLocation location = simulateLocationService.getGameLocationByCode(locationCode);
            locationDesc = location != null ? location.getLocationDesc() : locationCode;

            SimulateEvent event = simulateEventService.getGameEventByCode(eventCode);
            eventDesc = event != null ? event.getEventDesc() : eventCode;

            // 新增：下一个地点/事件描述查询（规则和当前完全一致）
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

        // ========== 核心：替换为事件发布器（参数完全一致，无任何改动） ==========
        simGameEventPublisher.sendGameEventMessage(
                locationDesc,       // 1. 当前地点描述
                eventDesc,          // 2. 当前事件描述
                eventDuration,      // 3. 当前事件持续时间（分钟）
                nextLocationDesc,   // 4. 下一个地点描述
                nextEventDesc       // 5. 下一个事件描述
        );
    }

    /**
     * 私有辅助方法：构建数据库事件记录实体
     * 内部生成雪花ID和获取事件持续时间
     *
     * @param sequence    事件序列数据
     * @param actualStart 实际开始时间
     * @return 数据库事件记录实体
     */
    private SimulateEventRecord buildEventRecord(SimulateEventSequence sequence, Integer actualStart) {
        // 1. 获取事件持续时间（Redis过期时间）
        Integer defaultDuration = simulateLocationEventRelationService.getDurationByTwoCode(sequence.getLocationCode(), sequence.getEventCode());

        // 2. 构建数据库记录实体
        SimulateEventRecord dbRecord = new SimulateEventRecord();
        dbRecord.setSequenceId(sequence.getSequenceId());
        dbRecord.setLocationCode(sequence.getLocationCode());
        dbRecord.setEventCode(sequence.getEventCode());
        dbRecord.setActualStart(actualStart); // 使用传入的开始时间参数
        dbRecord.setActualEnd(null); // 执行中状态，结束时间不填写
        dbRecord.setExecStatus(EventExecStatusEnum.EXECUTING);
        dbRecord.setVersion(sequence.getVersion());
        dbRecord.setSeqNum(sequence.getSeqNum());
        dbRecord.setDefaultDuration(defaultDuration); // 填充默认执行时间
        return dbRecord;
    }
}