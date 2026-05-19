package org.myfx.controls.aione.SimulationGame.mapper;

import org.apache.ibatis.annotations.Param;
import org.myfx.controls.aione.SimulationGame.entity.SimulateEvent;

import java.util.List;

/**
 * 模拟游戏-事件表 Mapper 接口
 */
public interface SimulateEventMapper {

    /**
     * 新增事件（传递三个独立参数，对应数据库字段）
     * @param eventCode 事件编码
     * @param eventDesc 事件描述（可为空）
     * @return 受影响行数
     */
    int insert(
            @Param("eventCode") String eventCode,
            @Param("eventDesc") String eventDesc
    );

    /**
     * 根据事件ID查询详情
     * @param eventId 事件ID
     * @return 事件实体
     */
    SimulateEvent selectById(@Param("eventId") Integer eventId);

    /**
     * 查询所有事件（无参数）
     * @return 事件实体列表
     */
    List<SimulateEvent> selectAll();

    /**
     * 根据事件编码精准查询
     * @param eventCode 事件编码
     * @return 事件实体
     */
    SimulateEvent selectByCode(@Param("eventCode") String eventCode);

    /**
     * 根据事件编码精准删除
     * @param eventCode 事件编码
     * @return 受影响行数
     */
    int deleteByCode(@Param("eventCode") String eventCode);
}