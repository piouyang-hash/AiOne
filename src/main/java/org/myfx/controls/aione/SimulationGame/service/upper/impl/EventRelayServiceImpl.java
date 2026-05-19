package org.myfx.controls.aione.SimulationGame.service.upper.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.SimulationGame.common.EventExecStatusEnum;
import org.myfx.controls.aione.SimulationGame.entity.SimulateEventRecord;
import org.myfx.controls.aione.SimulationGame.entity.SimulateEventRecordRedis;
import org.myfx.controls.aione.SimulationGame.entity.SimulateEventSequence;
import org.myfx.controls.aione.SimulationGame.entity.SimulateLocationEventRelation;
import org.myfx.controls.aione.SimulationGame.service.SimulateEventRecordService;
import org.myfx.controls.aione.SimulationGame.service.SimulateEventSequenceService;
import org.myfx.controls.aione.SimulationGame.service.SimulateLocationEventRelationService;
import org.myfx.controls.aione.SimulationGame.service.upper.EventRelayService;
import org.myfx.controls.aione.SimulationGame.service.upper.UpperSequenceService;
import org.myfx.controls.aione.SimulationGame.utils.SimulateEventRecordRedisUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 事件接力棒服务实现类
 * Event Relay Service Implementation
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EventRelayServiceImpl implements EventRelayService {

    // 依赖注入的服务和工具类
    private final SimulateEventRecordService simulateEventRecordService;
    private final SimulateEventSequenceService simulateEventSequenceService;
    private final SimulateLocationEventRelationService simulateLocationEventRelationService;
    private final SimulateEventRecordRedisUtil simulateEventRecordRedisUtil;
    private final UpperSequenceService upperSequenceService;

    @Override
    public void initializeEventRelay() {

        // ========== 第一步：判断是否已初始化 ==========
        List<SimulateEventRecord> eventRecordList = simulateEventRecordService.listAllSimulateEventRecord();
        if (eventRecordList != null && !eventRecordList.isEmpty()) {
            log.info("系统已有事件记录，初始化已执行，无需重复初始化");
            return; // 幂等性：如果有记录，直接返回
        }

        // 1. 调用方法获取事件序列数据
        SimulateEventSequence firstEventSequence = simulateEventSequenceService.getFirstEventSequence();
        if (firstEventSequence == null) {
            throw new RuntimeException("初始化失败：未查询到首个事件序列数据");
        }

        // 2. 构建数据库事件记录（获取过期时间）
        SimulateEventRecord eventRecord = buildEventRecord(firstEventSequence, 0);
        Integer expireSeconds = eventRecord.getDefaultDuration();

        // 3. 存储数据库事件记录
        simulateEventRecordService.saveSimulateEventRecord(eventRecord);

        // 4. 基于已构建的record，构建Redis事件记录实体
        SimulateEventRecordRedis eventRecordRedis = buildEventRecordRedis(eventRecord);

        // 5. 存储Redis事件记录
        simulateEventRecordRedisUtil.saveEventRecord(eventRecordRedis, expireSeconds);
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

    /**
     * 私有辅助方法：从数据库记录构建Redis事件记录实体
     * 修改方法名为newEventRecordRedis
     *
     * @param record 数据库事件记录
     * @return Redis事件记录实体
     */
    private SimulateEventRecordRedis buildEventRecordRedis(SimulateEventRecord record) {
        SimulateEventRecordRedis redisRecord = new SimulateEventRecordRedis();
        // 从数据库record中提取所有字段
        redisRecord.setRecordId(record.getRecordId());
        redisRecord.setSequenceId(record.getSequenceId());
        redisRecord.setEventCode(record.getEventCode());
        redisRecord.setLocationCode(record.getLocationCode());
        redisRecord.setActualStart(record.getActualStart());
        redisRecord.setActualEnd(record.getActualEnd());
        redisRecord.setExecStatus(record.getExecStatus());
        return redisRecord;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void interruptEventRelay() {
        // 1. 查询「执行中」状态的任务
        SimulateEventRecord executingTask = simulateEventRecordService.getCurrentTask(EventExecStatusEnum.EXECUTING);

        // 2. 如果没有执行中任务，直接返回（幂等性）
        if (executingTask == null) {
            log.info("未查询到EXECUTING状态的任务记录，中断事件接力棒无需执行");
            return;
        }

        // 3. 处理执行中任务：计算结束时间并更新为中断状态
        // 3.1 提取核心参数
        String eventCode = executingTask.getEventCode();
        String locationCode = executingTask.getLocationCode();
        Integer actualStart = executingTask.getActualStart();
        Integer defaultDuration = executingTask.getDefaultDuration();

        // 3.2 校验默认执行时间
        if (defaultDuration == null || defaultDuration <= 0) {
            log.error("手动中断任务失败：任务[recordId={}]的默认执行时间无效（{}）",
                    executingTask.getRecordId(), defaultDuration);
            throw new RuntimeException("任务[recordId=" + executingTask.getRecordId() + "]的默认执行时间无效");
        }

        // 3.3 调用Redis工具类获取剩余过期时间
        Integer remainExpireSeconds = simulateEventRecordRedisUtil.getEventRecordExpireSeconds(
                locationCode, eventCode, actualStart);

        // 3.4 校验剩余过期时间（仅处理>0的有效场景）
        if (remainExpireSeconds <= 0 && remainExpireSeconds != -1) {
            log.warn("手动中断任务：任务[recordId={}]的Redis键无有效过期时间（剩余秒数={}），跳过结束时间计算",
                    executingTask.getRecordId(), remainExpireSeconds);
            // 无有效过期时间时，结束时间设为当前开始时间
            remainExpireSeconds = 0;
        }

        // 3.5 计算结束时间：开始时间 + (默认执行时间 - 剩余过期时间)
        Integer actualEnd = actualStart + (defaultDuration - remainExpireSeconds);

        // 3.6 调用更新方法，将任务状态改为「中断」
        simulateEventRecordService.updateEventRecord(
                locationCode,       // 地点编码（调整为第一个参数）
                eventCode,          // 事件编码（调整为第二个参数）
                actualStart,        // 实际开始时间
                actualEnd,          // 实际结束时间
                EventExecStatusEnum.INTERRUPTED // 中断状态
        );

        // 3.7 删除Redis中的对应记录
        simulateEventRecordRedisUtil.deleteEventRecord(
                locationCode, eventCode, actualStart);

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean transferEventRelay(String locationCode, String eventCode, Integer actualStart) {

        // 第一步：使用断言校验参数
        assert locationCode != null && !locationCode.trim().isEmpty() : "locationCode不能为空";
        assert eventCode != null && !eventCode.trim().isEmpty() : "eventCode不能为空";
        assert actualStart != null && actualStart >= 0 : "actualStart不能为空且必须大于等于0";

        // 第二步：查询当前事件记录
        SimulateEventRecord currentEventRecord = simulateEventRecordService.getSimulateEventRecord(
                locationCode, eventCode, actualStart);
        if (currentEventRecord == null) {
            log.error("传递事件接力棒失败：未找到当前事件记录，locationCode={}，eventCode={}，actualStart={}",
                    locationCode, eventCode, actualStart);
            throw new RuntimeException("未找到当前事件记录");
        }

        // 第三步：幂等性检查
        EventExecStatusEnum currentStatus = currentEventRecord.getExecStatus();
        if (currentStatus == EventExecStatusEnum.FINISHED) {
            // 当前事件已经是完成状态，检查下一个事件是否已存在
            Integer actualEnd = currentEventRecord.getActualEnd();
            if (actualEnd == null) {
                log.error("传递事件接力棒失败：当前事件状态为FINISHED但actualEnd为空，recordId={}",
                        currentEventRecord.getRecordId());
                throw new RuntimeException("当前事件状态为FINISHED但actualEnd为空");
            }

            // 获取下一个事件序列
            SimulateEventSequence nextSequence = upperSequenceService.getNextEventSequence(locationCode, eventCode, actualStart);

            // 检查下一个事件是否已存在
            SimulateEventRecord nextEventRecord = simulateEventRecordService.getSimulateEventRecord(
                    nextSequence.getLocationCode(), nextSequence.getEventCode(), actualEnd);
            if (nextEventRecord != null) {
                log.info("当前事件已完成，且下一个事件已存在，recordId={}，接力流程已完成，无需重复传递",
                        nextEventRecord.getRecordId());
                return true; // 幂等返回
            } else {
                log.warn("当前事件已完成，但下一个事件不存在，将重新创建下一个事件");
                // 继续执行，创建下一个事件
            }
        } else if (currentStatus != EventExecStatusEnum.EXECUTING) {
            log.error("传递事件接力棒失败：当前事件状态不是EXECUTING，当前状态={}，recordId={}",
                    currentStatus, currentEventRecord.getRecordId());
            throw new RuntimeException("当前事件状态不是EXECUTING，无法传递");
        }
        // 如果状态是EXECUTING，继续执行传递流程

        // 第四步：接棒 - 将当前事件状态更新为FINISHED
        SimulateLocationEventRelation relation = simulateLocationEventRelationService.getRelationByTwoCode(locationCode, eventCode);
        if (relation == null) {
            log.error("传递事件接力棒失败：未查询到关联关系，locationCode={}，eventCode={}", locationCode, eventCode);
            throw new RuntimeException("未查询到[" + locationCode + "][" + eventCode + "]的关联关系");
        }

        Integer eventDuration = relation.getEventDuration();
        if (eventDuration == null || eventDuration <= 0) {
            log.error("传递事件接力棒失败：事件持续时间无效，eventDuration={}，locationCode={}，eventCode={}",
                    eventDuration, locationCode, eventCode);
            throw new RuntimeException("事件[" + eventCode + "]持续时间无效：" + eventDuration);
        }

        // 计算结束时间
        Integer actualEnd = actualStart + eventDuration;

        // 更新数据库记录为FINISHED
        boolean updateSuccess = simulateEventRecordService.updateEventRecord(locationCode, eventCode, actualStart, actualEnd, EventExecStatusEnum.FINISHED);
        if (!updateSuccess) {
            log.error("传递事件接力棒失败：更新当前事件为FINISHED状态失败");
            throw new RuntimeException("更新当前事件状态失败");
        }

        // 第五步：寻找下一棒
        SimulateEventSequence nextSequence = upperSequenceService.getNextEventSequence(locationCode, eventCode, actualStart);

        // 第六步：开始跑 - 创建并保存下一个事件记录
        // 获取下一个事件的关联关系
        SimulateLocationEventRelation nextRelation = simulateLocationEventRelationService.getRelationByTwoCode(
                nextSequence.getLocationCode(), nextSequence.getEventCode());
        if (nextRelation == null) {
            log.error("传递事件接力棒失败：未查询到下一个事件的关联关系，locationCode={}，eventCode={}",
                    nextSequence.getLocationCode(), nextSequence.getEventCode());
            throw new RuntimeException("未查询到下一个事件的关联关系");
        }

        Integer nextEventDuration = nextRelation.getEventDuration();
        if (nextEventDuration == null || nextEventDuration <= 0) {
            log.error("传递事件接力棒失败：下一个事件持续时间无效，eventDuration={}，locationCode={}，eventCode={}",
                    nextEventDuration, nextSequence.getLocationCode(), nextSequence.getEventCode());
            throw new RuntimeException("下一个事件持续时间无效：" + nextEventDuration);
        }

        // 构建下一个事件记录
        SimulateEventRecord nextEventRecord = buildEventRecord(nextSequence, actualEnd);

        // 存储数据库事件记录
        simulateEventRecordService.saveSimulateEventRecord(nextEventRecord);

        // 构建Redis事件记录实体
        SimulateEventRecordRedis nextEventRecordRedis = buildEventRecordRedis(nextEventRecord);

        // 存储Redis事件记录
        simulateEventRecordRedisUtil.saveEventRecord(nextEventRecordRedis, nextEventDuration);

        return true; // 传递成功
    }

    @Override
    public void continueEventRelay() {
        // 第一步：查询中断状态的任务
        SimulateEventRecord interruptedTask = simulateEventRecordService.getCurrentTask(EventExecStatusEnum.INTERRUPTED);

        // 如果没有中断任务，则直接返回（幂等性）
        if (interruptedTask == null) {
            log.info("未查询到INTERRUPTED状态的任务记录，延续事件接力棒无需执行");
            return;
        }

        // 第二步：校验关键字段（避免空指针）
        if (interruptedTask.getActualEnd() == null) {
            log.error("中断任务recordId={}的actualEnd字段为空，无法计算过期时间", interruptedTask.getRecordId());
            throw new RuntimeException("中断任务recordId=" + interruptedTask.getRecordId() + "的actualEnd字段为空，无法计算过期时间");
        }
        if (interruptedTask.getDefaultDuration() == null) {
            log.error("中断任务recordId={}的defaultDuration字段为空，无法计算过期时间", interruptedTask.getRecordId());
            throw new RuntimeException("中断任务recordId=" + interruptedTask.getRecordId() + "的defaultDuration字段为空，无法计算过期时间");
        }

        // 计算过期时间：defaultDuration - (actualEnd - actualStart)
        Integer timeDiff = interruptedTask.getActualEnd() - interruptedTask.getActualStart();
        Integer expireSeconds = interruptedTask.getDefaultDuration() - timeDiff;

        // 校验过期时间有效性（必须>0，避免Redis存储报错）
        if (expireSeconds <= 0) {
            log.error("中断任务recordId={}计算出的过期时间无效：{}秒（需大于0）", interruptedTask.getRecordId(), expireSeconds);
            throw new RuntimeException("中断任务recordId=" + interruptedTask.getRecordId() + "计算出的过期时间无效：" + expireSeconds + "秒（需大于0）");
        }

        // 第三步：调用updateEventRecord将状态改为执行中（EXECUTING）
        boolean updateSuccess = simulateEventRecordService.updateEventRecord(
                interruptedTask.getLocationCode(),    // 地点编码（调整为第一个参数）
                interruptedTask.getEventCode(),       // 事件编码（调整为第二个参数）
                interruptedTask.getActualStart(),     // 实际开始时间
                interruptedTask.getActualEnd(),       // 实际结束时间
                EventExecStatusEnum.EXECUTING         // 第五个参数固定为执行中
        );

        if (!updateSuccess) {
            log.error("中断任务recordId={}更新状态为EXECUTING失败", interruptedTask.getRecordId());
            throw new RuntimeException("中断任务recordId=" + interruptedTask.getRecordId() + "更新状态为EXECUTING失败");
        }

        // 第四步：构建SimulateEventRecordRedis实例
        SimulateEventRecordRedis redisRecord = buildEventRecordRedis(interruptedTask);
        // 由于中断任务的record中状态仍然是INTERRUPTED，我们需要更新为EXECUTING
        redisRecord.setExecStatus(EventExecStatusEnum.EXECUTING);

        // 第五步：调用Redis存储方法
        simulateEventRecordRedisUtil.saveEventRecord(redisRecord, expireSeconds);
    }

}
