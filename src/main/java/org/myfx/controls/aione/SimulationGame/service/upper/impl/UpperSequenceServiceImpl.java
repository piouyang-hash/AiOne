package org.myfx.controls.aione.SimulationGame.service.upper.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.ServiceCommon.entity.feign.EventRecordResponseDTO;
import org.myfx.controls.aione.SimulationGame.common.EventExecStatusEnum;
import org.myfx.controls.aione.SimulationGame.entity.SimulateEvent;
import org.myfx.controls.aione.SimulationGame.entity.SimulateEventRecord;
import org.myfx.controls.aione.SimulationGame.entity.SimulateEventSequence;
import org.myfx.controls.aione.SimulationGame.entity.SimulateLocation;
import org.myfx.controls.aione.SimulationGame.service.*;
import org.myfx.controls.aione.SimulationGame.service.upper.UpperSequenceService;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor // Spring 推荐：构造器注入（替代@Autowired）
public class UpperSequenceServiceImpl implements UpperSequenceService {

    // 注入核心依赖
    private final SimulateEventSequenceService simulateEventSequenceService;
    // 新增：注入事件记录Service
    private final SimulateEventRecordService simulateEventRecordService;

    private final GameGlobalTimeService gameGlobalTimeService;
    private final SimulateEventService simulateEventService;
    private final SimulateLocationService simulateLocationService;

    /**
     * 核心方法：版本+序号 → 下一个序列
     */
    @Override
    public SimulateEventSequence getNextEventSequence(int version, int seqNum) {
        // 参数校验
        Assert.isTrue(version > 0, "日程版本号不能为空且必须大于0");
        Assert.isTrue(seqNum >= 0, "当前执行序号不能为负数");

        List<SimulateEventSequence> sequenceList = simulateEventSequenceService.listEventSequenceByVersion(version);
        if (CollectionUtils.isEmpty(sequenceList)) {
            throw new RuntimeException(String.format("获取下一个事件序列失败：版本[%d]下无任何事件序列", version));
        }
        int listSize = sequenceList.size();

        // 计算下一个序号
        int newSeqNum = seqNum + 1;
        if (newSeqNum > listSize) {
            newSeqNum = newSeqNum - listSize;
        }

        return simulateEventSequenceService.getEventSequenceByVersionAndSeqNum(version, newSeqNum);
    }

    /**
     * 重载方法：地点+事件+开始时间 → 下一个序列（直接复用核心逻辑）
     */
    @Override
    public SimulateEventSequence getNextEventSequence(String locationCode, String eventCode, Integer actualStart) {
        // 1. 入参基础校验（必加）
        Assert.hasText(locationCode, "地点编码不能为空");
        Assert.hasText(eventCode, "事件编码不能为空");
        Assert.notNull(actualStart, "实际开始时间不能为空");

        // 2. 查询当前事件记录
        SimulateEventRecord currentRecord = simulateEventRecordService.getSimulateEventRecord(
                locationCode, eventCode, actualStart);
        if (currentRecord == null) {
            throw new RuntimeException(String.format(
                    "获取下一个事件序列失败：未查询到当前事件记录 | locationCode=%s, eventCode=%s, actualStart=%s",
                    locationCode, eventCode, actualStart));
        }

        // 3. 提取参数（极简校验，重复校验交给核心方法）
        Integer version = currentRecord.getVersion();
        Integer seqNum = currentRecord.getSeqNum();
        Assert.notNull(version, "事件记录中版本号不能为空");
        Assert.notNull(seqNum, "事件记录中执行次序不能为空");

        // 4. 直接调用【核心方法】，百分百复用逻辑
        return getNextEventSequence(version, seqNum);
    }


    @Override
    public EventRecordResponseDTO getCurrentExecutingEvent() {
        // 1. 获取执行中的任务
        SimulateEventRecord currentEvent = simulateEventRecordService.getCurrentTask(EventExecStatusEnum.EXECUTING);

        // 2. 基础非空校验
        Assert.notNull(currentEvent, "当前无执行中的事件记录");
        Assert.hasText(currentEvent.getLocationCode(), "事件位置编码不能为空");
        Assert.hasText(currentEvent.getEventCode(), "事件编码不能为空");
        Assert.notNull(currentEvent.getActualStart(), "事件实际开始时间不能为空");
        Assert.notNull(currentEvent.getDefaultDuration(), "事件默认时长不能为空");

        // 3. 提取核心参数
        String locationCode = currentEvent.getLocationCode();
        String eventCode = currentEvent.getEventCode();
        Integer actualStart = currentEvent.getActualStart();
        Integer defaultDuration = currentEvent.getDefaultDuration();

        // 4. 获取全局游戏时间
        Integer currentGameSeconds = gameGlobalTimeService.getCurrentGameSeconds();

        // 5. 查询事件&位置信息
        SimulateEvent gameEvent = simulateEventService.getGameEventByCode(eventCode);
        SimulateLocation location = simulateLocationService.getGameLocationByCode(locationCode);

        // 6. 数据合法性校验
        Assert.notNull(gameEvent, String.format("未找到编码为[%s]的事件信息", eventCode));
        Assert.hasText(gameEvent.getEventDesc(), String.format("事件[%s]的描述信息不能为空", eventCode));
        Assert.notNull(location, String.format("未找到编码为[%s]的位置信息", locationCode));
        Assert.hasText(location.getLocationDesc(), String.format("位置[%s]的描述信息不能为空", locationCode));

        // 7. 提取描述信息
        String eventDesc = gameEvent.getEventDesc();
        String locationDesc = location.getLocationDesc();

        // 8. 核心时间计算
        Integer endTime = actualStart + defaultDuration;
        int executionTime = currentGameSeconds - actualStart;
        Integer remainingSeconds = Math.max(endTime - currentGameSeconds, 0);
        executionTime = Math.max(executionTime, 0);

        // 9. 封装返回结果
        EventRecordResponseDTO responseDTO = new EventRecordResponseDTO();
        responseDTO.setLocationDesc(locationDesc);
        responseDTO.setEventDesc(eventDesc);
        responseDTO.setActualStart(actualStart);
        responseDTO.setExecutionTime(executionTime);
        responseDTO.setRemainingSeconds(remainingSeconds);

        // 业务日志
        log.info("当前执行事件查询成功，地点：{}，事件：{}", locationDesc, eventDesc);
        return responseDTO;
    }
}