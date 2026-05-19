package org.myfx.controls.aione.SimulationGame.service.upper;

import org.myfx.controls.aione.ServiceCommon.entity.feign.EventRecordResponseDTO;
import org.myfx.controls.aione.SimulationGame.entity.SimulateEventSequence;

/**
 * 事件序列上层服务
 * 抽离：获取下一个循环执行的事件序列
 */
public interface UpperSequenceService {

    /**
     * 查询下一个要执行的事件序列（支持循环）
     * @param version 日程版本号（周一/周二...）
     * @param seqNum 当前执行的序号
     * @return 下一个事件序列
     */
    SimulateEventSequence getNextEventSequence(int version, int seqNum);

    /**
     * 【重载接口】根据地点+事件+实际开始时间 获取下一个事件序列
     */
    SimulateEventSequence getNextEventSequence(String locationCode, String eventCode, Integer actualStart);

    /**
     * 获取当前执行中的游戏事件信息
     * @return 事件记录响应DTO
     */
    EventRecordResponseDTO getCurrentExecutingEvent();
}