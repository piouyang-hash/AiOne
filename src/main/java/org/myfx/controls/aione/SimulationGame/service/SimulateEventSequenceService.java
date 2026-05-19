package org.myfx.controls.aione.SimulationGame.service;

import org.myfx.controls.aione.SimulationGame.dto.SimulateEventSequenceAddDTO;
import org.myfx.controls.aione.SimulationGame.entity.SimulateEventSequence;

import java.util.List;

/**
 * 模拟游戏-事件执行序列规则 业务层接口
 */
public interface SimulateEventSequenceService {

    /**
     * 新增事件序列规则（适配DTO入参）
     * @param addDTO 新增事件序列规则请求DTO（非空，核心字段必填）
     * @return 新增是否成功（true=成功，false=失败）
     */
    boolean saveEventSequence(SimulateEventSequenceAddDTO addDTO);

    /**
     * 根据版本号查询事件序列规则列表
     * @param version 版本号（非空，如1、2）
     * @return 该版本下的所有事件序列规则（按执行次序升序排列），无数据返回空列表
     */
    List<SimulateEventSequence> listEventSequenceByVersion(Integer version);

    /**
     * 查询所有事件序列规则列表
     * @return 所有事件序列规则（按执行次序升序排列），无数据返回空列表
     */
    List<SimulateEventSequence> listAllEventSequence();

    /**
     * 查找第一个事件（最小版本号中最小执行次序的事件）
     * @return 第一个事件序列规则，无数据返回null
     */
    SimulateEventSequence getFirstEventSequence();

    /**
     * 获取指定版本+执行次序的事件队列（事件序列规则）
     * @param version 版本号（如1=周一、2=周二，不能为空且>0）
     * @param seqNum 当日执行次序（如1=第1件事、2=第2件事，不能为空且>0）
     * @return 匹配的事件序列规则（唯一结果）
     */
    SimulateEventSequence getEventSequenceByVersionAndSeqNum(Integer version, Integer seqNum);

    /**
     * 根据版本号删除事件序列规则
     * @param version 版本号（非空）
     * @return 删除是否成功（true=成功，false=失败/无数据）
     */
    boolean removeEventSequenceByVersion(Integer version);
}