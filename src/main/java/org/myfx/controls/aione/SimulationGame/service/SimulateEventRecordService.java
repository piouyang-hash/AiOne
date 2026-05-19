package org.myfx.controls.aione.SimulationGame.service;

import org.myfx.controls.aione.SimulationGame.common.EventExecStatusEnum;
import org.myfx.controls.aione.SimulationGame.entity.SimulateEventRecord;

import java.util.List;

/**
 * 模拟游戏事件记录 - 业务层接口
 * 封装Mapper层操作，提供语义化的业务方法+参数校验
 */
public interface SimulateEventRecordService {

    /**
     * 新增模拟游戏事件记录
     * @param record 事件记录实体（必须非空，雪花ID需提前生成）
     */
    void saveSimulateEventRecord(SimulateEventRecord record);

    /**
     * 获取指定状态的任务：仅支持查询【执行中（EXECUTING）/中断（INTERRUPTED）】状态的事件记录
     * 业务约定：该状态下仅允许存在一条任务记录，多于一条则抛出运行时异常
     * @param execStatus 执行状态（仅允许EXECUTING/INTERRUPTED，非空）
     * @return 对应状态的事件记录（无数据返回null）
     * @throws IllegalArgumentException 参数不合法（枚举为空/非允许值）时抛出
     * @throws RuntimeException 对应状态下任务记录多于一条时抛出
     */
    SimulateEventRecord getCurrentTask(EventExecStatusEnum execStatus);

    /**
     * 根据地点编码+事件编码+实际开始时间，查询执行中（EXECUTING）的事件记录
     * 业务约定：该组合条件下只会返回一条记录，无需limit
     * @param locationCode 地点编码（不能为空）
     * @param eventCode 事件编码（不能为空）
     * @param actualStart 实际开始时间（游戏服务器时间，int类型，不能为空且>0）
     * @return 执行中的事件记录（无数据返回null）
     */
    SimulateEventRecord getSimulateEventRecord(String locationCode, String eventCode, Integer actualStart);

    /**
     * 动态更新事件记录（仅允许更新执行中（EXECUTING）状态的记录）
     * 规则：
     * 1. 仅更新传入非空的字段（actualEnd结束时间/execStatus执行状态）；
     * 2. 仅允许更新exec_status = EXECUTING的记录（其他状态禁止更新）；
     * 3. execStatus若传值，仅允许为FINISHED/FAILED（不允许更新为EXECUTING）；
     * 4. 定位条件：locationCode+eventCode+actualStart（三参数必须非空）。
     *
     * @param locationCode  地点编码（非空）
     * @param eventCode     事件编码（非空）
     * @param actualStart   实际开始时间（非空且>=0）
     * @param actualEnd     实际结束时间（可选，非空则更新）
     * @param execStatus    执行状态（可选，仅允许FINISHED/FAILED）
     * @return true=更新成功；false=无符合条件的记录（非执行中/参数错误）
     * @throws IllegalArgumentException 参数不合法时抛出
     */
    boolean updateEventRecord(String locationCode, String eventCode, Integer actualStart, Integer actualEnd, EventExecStatusEnum execStatus);

    /**
     * 根据记录ID查询事件记录详情
     * @param recordId 记录ID（雪花ID，必须非空且>0）
     * @return 事件记录实体（不存在返回null）
     */
    SimulateEventRecord getSimulateEventRecordById(Long recordId);

    /**
     * 根据游戏服务器时间范围查询事件记录列表
     * @param startTime 游戏服务器开始时间（int，线性递增，必须非空）
     * @param endTime 游戏服务器结束时间（int，线性递增，必须非空且≥开始时间）
     * @return 事件记录列表（无数据返回空列表）
     */
    List<SimulateEventRecord> listSimulateEventRecordByTimeRange(Integer startTime, Integer endTime);

    /**
     * 根据地点编码+事件编码查询事件记录列表
     * @param locationCode 地点编码（必须非空）
     * @param eventCode 事件编码（必须非空）
     * @return 事件记录列表（无数据返回空列表）
     */
    List<SimulateEventRecord> listSimulateEventRecordByTwoCode(String locationCode, String eventCode);

    /**
     * 查询所有事件记录
     * @return 事件记录列表（无数据返回空列表）
     */
    List<SimulateEventRecord> listAllSimulateEventRecord();

    /**
     * 根据记录ID删除事件记录
     * @param recordId 记录ID（雪花ID，必须非空且>0）
     * @return true=删除成功，false=删除失败
     */
    boolean removeSimulateEventRecordById(Long recordId);
}