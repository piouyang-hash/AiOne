package org.myfx.controls.aione.SimulationGame.mapper;

import org.apache.ibatis.annotations.Param;
import org.myfx.controls.aione.SimulationGame.common.EventExecStatusEnum;
import org.myfx.controls.aione.SimulationGame.entity.SimulateEventRecord;

import java.util.List;

/**
 * 模拟游戏-事件记录表 Mapper 接口
 */
public interface SimulateEventRecordMapper {

    /**
     * 新增事件记录（雪花ID由程序生成，忽略入库时间默认值）
     * @param record 事件记录实体
     * @return 受影响行数
     */
    int insert(SimulateEventRecord record);


    /**
     * 根据地点编码+事件编码+实际开始时间，查询事件记录（EXECUTING）
     * 业务约定：该组合条件下只会返回一条记录，无需limit
     * @param locationCode 地点编码
     * @param eventCode 事件编码
     * @param actualStart 实际开始时间（游戏服务器时间，int类型）
     * @return 事件记录（无数据返回null）
     */
    SimulateEventRecord selectRecordByThreeCode(
            @Param("locationCode") String locationCode,
            @Param("eventCode") String eventCode,
            @Param("actualStart") Integer actualStart); // 新增actualStart参数

    /**
     * 查询指定执行状态的所有事件记录（无其他条件限制）
     * @param execStatus 执行状态（枚举，MP自动处理与数据库的映射）
     * @return 符合状态的所有事件记录列表（无数据则返回空列表）
     */
    List<SimulateEventRecord> selectByExecStatus(@Param("execStatus") EventExecStatusEnum execStatus);

    /**
     * 动态更新事件记录（仅更新非空的结束时间/执行状态，仅允许更新执行中/中断状态）
     * @param locationCode 地点编码（非空）
     * @param eventCode 事件编码（非空）
     * @param actualStart 实际开始时间（非空）
     * @param actualEnd 结束时间（可选，非空则更新）
     * @param execStatus 执行状态（可选，非空则更新）
     * @return 受影响行数（0=无符合条件的记录/未更新，>0=更新成功）
     */
    int updateEventRecordByThreeCode(
            @Param("locationCode") String locationCode,
            @Param("eventCode") String eventCode,
            @Param("actualStart") Integer actualStart,
            @Param("actualEnd") Integer actualEnd,
            @Param("execStatus") EventExecStatusEnum execStatus);

    /**
     * 根据记录ID查询详情
     * @param recordId 记录ID（雪花ID）
     * @return 事件记录实体
     */
    SimulateEventRecord selectById(@Param("recordId") Long recordId);

    /**
     * 根据游戏服务器时间范围查询事件记录
     * @param startTime 游戏服务器开始时间（int，线性递增，如10000）
     * @param endTime 游戏服务器结束时间（int，线性递增，如10480）
     * @return 事件记录列表（无数据返回空列表）
     */
    List<SimulateEventRecord> selectByTimeRange(
            @Param("startTime") Integer startTime,
            @Param("endTime") Integer endTime
    );

    /**
     * 根据地点编码和事件编码查询记录
     * @param locationCode 地点编码
     * @param eventCode 事件编码
     * @return 事件记录列表
     */
    List<SimulateEventRecord> selectByTwoCode(@Param("locationCode") String locationCode, @Param("eventCode") String eventCode);

    /**
     * 查询所有事件记录（无参数）
     * @return 所有事件记录列表
     */
    List<SimulateEventRecord> selectAll();

    /**
     * 根据记录ID删除
     * @param recordId 记录ID（雪花ID）
     * @return 受影响行数
     */
    int deleteById(@Param("recordId") Long recordId);
}