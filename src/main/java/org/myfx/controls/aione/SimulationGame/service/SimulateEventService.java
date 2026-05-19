package org.myfx.controls.aione.SimulationGame.service;

import org.myfx.controls.aione.SimulationGame.dto.SimulateEventAddDTO;
import org.myfx.controls.aione.SimulationGame.entity.SimulateEvent;

import java.util.List;

/**
 * 模拟游戏-事件业务处理接口（语义化业务层）
 */
public interface SimulateEventService {

    /**
     * 新增游戏事件
     * @param eventAddDTO 新增事件请求DTO（封装所有参数）
     * @return 受影响数据库行数
     */
    int addGameEvent(SimulateEventAddDTO eventAddDTO);

    /**
     * 根据事件ID查询事件详情
     * @param eventId 事件ID（非空且大于0）
     * @return 事件实体信息
     */
    SimulateEvent getGameEventById(Integer eventId);

    /**
     * 查询所有游戏事件
     * @return 所有事件实体列表
     */
    List<SimulateEvent> listAllGameEvents();

    /**
     * 根据事件编码查询事件详情
     * @param eventCode 事件编码（枚举类型，非空）
     * @return 事件实体信息
     */
    SimulateEvent getGameEventByCode(String eventCode);

    /**
     * 根据事件编码删除游戏事件
     * @param eventCode 事件编码（枚举类型，非空）
     * @return 受影响数据库行数
     */
    int removeGameEventByCode(String eventCode);
}