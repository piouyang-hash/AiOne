package org.myfx.controls.aione.SimulationGame.mapper;

import org.apache.ibatis.annotations.Param;
import org.myfx.controls.aione.SimulationGame.entity.SimulateLocationEventRelation;

import java.util.List;

/**
 * 模拟游戏-地点与事件关联表 Mapper 接口
 */
public interface SimulateLocationEventRelationMapper {

    /**
     * 新增关联关系（三个参数：地点编码、事件编码、事件持续时长）
     * @param locationCode 地点编码
     * @param eventCode 事件编码
     * @param eventDuration 事件在该地点的持续秒数
     * @return 受影响行数
     */
    int insert(
            @Param("locationCode") String locationCode,
            @Param("eventCode") String eventCode,
            @Param("eventDuration") Integer eventDuration
    );

    /**
     * 根据地点编码查询所有关联事件
     * @param locationCode 地点编码
     * @return 关联关系列表
     */
    List<SimulateLocationEventRelation> selectByLocationCode(@Param("locationCode") String locationCode);

    /**
     * 根据事件编码查询所有关联地点
     * @param eventCode 事件编码
     * @return 关联关系列表
     */
    List<SimulateLocationEventRelation> selectByEventCode(@Param("eventCode") String eventCode);

    /**
     * 根据地点编码和事件编码精准查询
     * @param locationCode 地点编码
     * @param eventCode 事件编码
     * @return 关联关系实体
     */
    SimulateLocationEventRelation selectByTwoCode(@Param("locationCode") String locationCode, @Param("eventCode") String eventCode);

    /**
     * 查询所有关联关系
     * @return 关联关系列表
     */
    List<SimulateLocationEventRelation> selectAll();

    /**
     * 根据地点编码和事件编码精准删除
     * @param locationCode 地点编码
     * @param eventCode 事件编码
     * @return 受影响行数
     */
    int deleteByTwoCode(@Param("locationCode") String locationCode, @Param("eventCode") String eventCode);
}