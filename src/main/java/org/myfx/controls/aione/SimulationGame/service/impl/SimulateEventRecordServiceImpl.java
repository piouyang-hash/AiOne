package org.myfx.controls.aione.SimulationGame.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.ServiceCommon.utils.SnowflakeGenerator;
import org.myfx.controls.aione.SimulationGame.common.EventExecStatusEnum;
import org.myfx.controls.aione.SimulationGame.entity.SimulateEventRecord;
import org.myfx.controls.aione.SimulationGame.mapper.SimulateEventRecordMapper;
import org.myfx.controls.aione.SimulationGame.service.SimulateEventRecordService;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 模拟游戏事件记录 - 业务层实现类
 * 实现接口契约，完成参数校验+Mapper调用
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SimulateEventRecordServiceImpl implements SimulateEventRecordService {

    // 注入Mapper层（业务层统一注入风格）
    private final SimulateEventRecordMapper simulateEventRecordMapper;

    @Override
    public void saveSimulateEventRecord(SimulateEventRecord record) {
        // 1. 参数校验：实体非空
        Assert.notNull(record, "新增事件记录失败：实体对象不能为空！");

        // 核心字段必填校验（适配新实体类，新增version/seqNum校验）
        Assert.notNull(record.getSequenceId(), "新增事件记录失败：关联序列规则ID不能为空！");
        Assert.hasText(record.getLocationCode(), "新增事件记录失败：地点编码不能为空！");
        Assert.hasText(record.getEventCode(), "新增事件记录失败：事件编码不能为空！");
        Assert.notNull(record.getActualStart(), "新增事件记录失败：游戏服务器开始时间不能为空！");
        // ========== 新增：version/seqNum 非空+合法性校验 ==========
        Assert.notNull(record.getVersion(), "新增事件记录失败：每日安排版本号不能为空！");
        Assert.isTrue(record.getVersion() > 0, "新增事件记录失败：每日安排版本号必须为正整数（如1=周一）！");
        Assert.notNull(record.getSeqNum(), "新增事件记录失败：当日执行次序不能为空！");
        Assert.isTrue(record.getSeqNum() > 0, "新增事件记录失败：当日执行次序必须为正整数（如1=第1件事）！");

        // 2. 执行状态枚举校验（替换原字符串校验）
        EventExecStatusEnum execStatus = record.getExecStatus();
        Assert.notNull(execStatus, "新增事件记录失败：执行状态不能为空！");

        // 3. 核心CHECK：FINISHED状态下结束时间必须有值
        if (EventExecStatusEnum.FINISHED.equals(execStatus)) {
            Assert.notNull(record.getActualEnd(), "新增事件记录失败：执行状态为【已完成】时，游戏服务器结束时间不能为空！");
        }

        // 4. 开始时间 <= 结束时间校验（仅当结束时间有值时执行）
        if (record.getActualEnd() != null) {
            Assert.isTrue(record.getActualStart() <= record.getActualEnd(),
                    "新增事件记录失败：开始时间不能大于结束时间（游戏服务器时间）！");
        }

        // 5. 生成雪花ID
        record.setRecordId(SnowflakeGenerator.generateId());

        // 6. 调用Mapper执行新增
        simulateEventRecordMapper.insert(record);
    }

    @Override
    public SimulateEventRecord getCurrentTask(EventExecStatusEnum execStatus) {
        // 1. 参数校验：枚举非空 + 仅允许EXECUTING/INTERRUPTED
        if (execStatus == null) {
            log.warn("获取当前任务失败：执行状态不能为空！");
            return null;
        }
        if (!EventExecStatusEnum.EXECUTING.equals(execStatus) && !EventExecStatusEnum.INTERRUPTED.equals(execStatus)) {
            log.warn("获取当前任务失败：仅支持查询【执行中/中断】状态，不支持[{}]状态", execStatus.getDesc());
            return null;
        }

        // 2. 调用Mapper查询指定状态的所有记录
        List<SimulateEventRecord> taskList = simulateEventRecordMapper.selectByExecStatus(execStatus);

        // 3. 处理查询结果
        if (CollectionUtil.isEmpty(taskList)) {
            log.info("获取【{}】状态的任务：未查询到任何记录", execStatus.getDesc());
            return null; // 0条结果返回null
        }

        if (taskList.size() > 1) {
            // 多于1条记录仍视为数据异常，抛运行时异常
            String errorMsg = String.format("获取【%s】状态任务失败：查询到%d条记录（仅允许1条），记录ID列表：%s",
                    execStatus.getDesc(),
                    taskList.size(),
                    taskList.stream().map(SimulateEventRecord::getRecordId).collect(Collectors.toList()));
            log.error(errorMsg);
            throw new RuntimeException(errorMsg);
        }

        // 4. 仅1条记录：直接返回
        return taskList.get(0);
    }

    @Override
    public SimulateEventRecord getSimulateEventRecord(String locationCode, String eventCode, Integer actualStart) {
        // ========== 1. 参数校验（用Assert简化，location在前） ==========
        Assert.hasLength(locationCode, "查询事件记录失败：地点编码不能为空");
        Assert.hasText(eventCode, "查询事件记录失败：事件编码不能为空");
        Assert.notNull(actualStart, "查询事件记录失败：实际开始时间不能为空");
        // 关键修改：允许actualStart等于0（服务器时间起始点），条件改为 >= 0
        Assert.isTrue(actualStart >= 0, "查询事件记录失败：实际开始时间必须大于等于0，当前值：" + actualStart);

        // ========== 2. 调用Mapper接口查询（同步参数顺序：location在前） ==========
        SimulateEventRecord eventRecord = simulateEventRecordMapper.selectRecordByThreeCode(
                locationCode, eventCode, actualStart);

        // ========== 3. 日志打印（参数顺序同步调整） ==========
        if (eventRecord == null) {
            log.warn("未查询到符合条件的执行中事件记录 | 地点编码={}, 事件编码={}, 实际开始时间={}",
                    locationCode, eventCode, actualStart);
        } else {
            log.info("查询到符合条件的执行中事件记录 | 记录ID={}, 地点编码={}, 事件编码={}, 实际开始时间={}",
                    eventRecord.getRecordId(), locationCode, eventCode, actualStart);
        }

        // ========== 4. 返回查询结果 ==========
        return eventRecord;
    }

    // ========== 动态更新事件记录（参数顺序调整：location在前 + Assert简化校验） ==========
    @Override
    public boolean updateEventRecord(String locationCode, String eventCode, Integer actualStart, Integer actualEnd, EventExecStatusEnum execStatus) {
        // 1. 核心定位参数校验（用Assert简化，location在前）
        Assert.hasText(locationCode, "更新事件记录失败：地点编码不能为空！");
        Assert.hasText(eventCode, "更新事件记录失败：事件编码不能为空！");
        Assert.notNull(actualStart, "更新事件记录失败：实际开始时间（actualStart）不能为空！");
        Assert.isTrue(actualStart >= 0, "更新事件记录失败：实际开始时间（actualStart）不能为负数！");

        // ========== 2. 执行状态参数校验（修改逻辑：加入EXECUTING，允许更新为该状态） ==========
        Assert.notNull(execStatus, "更新事件记录失败：执行状态不能为空！");
        // 业务规则：允许更新为EXECUTING/FINISHED/FAILED/INTERRUPTED（加入EXECUTING）
        List<EventExecStatusEnum> allowExecStatusList = List.of(
                EventExecStatusEnum.EXECUTING,  // 新增：允许更新为EXECUTING
                EventExecStatusEnum.FINISHED,
                EventExecStatusEnum.FAILED,
                EventExecStatusEnum.INTERRUPTED
        );
        Assert.isTrue(allowExecStatusList.contains(execStatus),
                "更新事件记录失败：执行状态仅允许更新为" +
                        allowExecStatusList.stream().map(EventExecStatusEnum::name).collect(Collectors.joining("/")) + "！");

        // ========== 3. FINISHED状态时actualEnd非空校验（不受EXECUTING影响，保留） ==========
        if (EventExecStatusEnum.FINISHED.equals(execStatus)) {
            Assert.notNull(actualEnd, "更新事件记录失败：执行状态为FINISHED时，实际结束时间（actualEnd）不能为空！");
        }

        // ========== 4. 查询待更新记录并校验状态 ==========
        SimulateEventRecord currentRecord = simulateEventRecordMapper.selectRecordByThreeCode(
                locationCode, eventCode, actualStart);
        // 记录不存在则抛异常
        Assert.notNull(currentRecord,
                "更新事件记录失败：未查询到【地点编码:" + locationCode + ",事件编码:" + eventCode + ",实际开始时间:" + actualStart + "】的记录，无法更新！");

        // 仅允许更新EXECUTING/INTERRUPTED状态的记录（核心业务规则调整）
        Assert.isTrue(EventExecStatusEnum.EXECUTING.equals(currentRecord.getExecStatus())
                        || EventExecStatusEnum.INTERRUPTED.equals(currentRecord.getExecStatus()),
                "更新事件记录失败：记录当前状态为【" + currentRecord.getExecStatus().name() + "】，仅允许更新EXECUTING/INTERRUPTED状态的记录！");

        // ========== 5. 调用Mapper执行动态更新（同步参数顺序：location在前） ==========
        int affectedRows = simulateEventRecordMapper.updateEventRecordByThreeCode(
                locationCode, eventCode, actualStart, actualEnd, execStatus);

        // ========== 6. 结果处理 + 日志（参数顺序同步调整） ==========
        String execStatusStr = execStatus.name();
        if (affectedRows > 0) {
            log.info("更新事件记录成功：地点编码[{}]，事件编码[{}]，实际开始时间[{}]，更新结束时间[{}]，更新状态[{}]",
                    locationCode, eventCode, actualStart, actualEnd, execStatusStr);
            return true;
        } else {
            String errorMsg = "更新事件记录失败：查询到记录但更新行数为0（可能记录状态已变更），地点编码[" + locationCode + "]，事件编码[" + eventCode + "]，实际开始时间[" + actualStart + "]";
            log.error(errorMsg);
            throw new RuntimeException(errorMsg);
        }
    }

    @Override
    public SimulateEventRecord getSimulateEventRecordById(Long recordId) {
        // 1. 参数校验：ID非空且>0（雪花ID为正数）
        Assert.notNull(recordId, "查询事件记录失败：记录ID不能为空！");
        Assert.isTrue(recordId > 0, "查询事件记录失败：记录ID必须为正整数（雪花ID）！");

        // 2. 调用Mapper查询
        SimulateEventRecord record = simulateEventRecordMapper.selectById(recordId);
        log.info("根据ID{}查询事件记录，结果：{}", recordId, Objects.nonNull(record) ? "存在" : "不存在");
        return record;
    }

    // ========== 核心修改：时间范围查询适配游戏服务器int时间 ==========
    @Override
    public List<SimulateEventRecord> listSimulateEventRecordByTimeRange(Integer startTime, Integer endTime) {
        // 1. 参数校验：游戏服务器时间非空 + 开始时间<=结束时间（int数字比较）
        Assert.notNull(startTime, "查询事件记录失败：游戏服务器开始时间不能为空！");
        Assert.notNull(endTime, "查询事件记录失败：游戏服务器结束时间不能为空！");
        Assert.isTrue(startTime <= endTime, "查询事件记录失败：开始时间不能大于结束时间（游戏服务器时间）！");

        // 2. 调用Mapper查询（无数据返回空列表，避免NPE）
        List<SimulateEventRecord> recordList = simulateEventRecordMapper.selectByTimeRange(startTime, endTime);
        log.info("查询游戏服务器时间[{} - {}]区间的事件记录，共查询到{}条", startTime, endTime, recordList.size());
        return recordList;
    }

    @Override
    public List<SimulateEventRecord> listSimulateEventRecordByTwoCode(String locationCode, String eventCode) {
        // 1. 参数校验：两个编码非空
        Assert.hasText(locationCode, "查询事件记录失败：地点编码不能为空！");
        Assert.hasText(eventCode, "查询事件记录失败：事件编码不能为空！");

        // 2. 调用Mapper查询（无数据返回空列表）
        List<SimulateEventRecord> recordList = simulateEventRecordMapper.selectByTwoCode(locationCode, eventCode);
        log.info("查询地点编码[{}]、事件编码[{}]的事件记录，共查询到{}条", locationCode, eventCode, recordList.size());
        return recordList;
    }

    @Override
    public List<SimulateEventRecord> listAllSimulateEventRecord() {
        // 1. 无入参，无需参数校验（保持结构统一，注释说明）
        // 2. 调用Mapper查询（无数据返回空列表）
        List<SimulateEventRecord> recordList = simulateEventRecordMapper.selectAll();
        log.info("查询所有事件记录，共查询到{}条", recordList.size());
        return recordList;
    }

    @Override
    public boolean removeSimulateEventRecordById(Long recordId) {
        // 1. 参数校验：ID非空且>0
        Assert.notNull(recordId, "删除事件记录失败：记录ID不能为空！");
        Assert.isTrue(recordId > 0, "删除事件记录失败：记录ID必须为正整数（雪花ID）！");

        // 2. 调用Mapper删除
        int affectedRows = simulateEventRecordMapper.deleteById(recordId);
        boolean isSuccess = affectedRows > 0;
        log.info("删除模拟游戏事件记录{}，记录ID：{}", isSuccess ? "成功" : "失败", recordId);
        return isSuccess;
    }
}