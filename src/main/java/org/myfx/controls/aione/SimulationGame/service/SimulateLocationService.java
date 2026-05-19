package org.myfx.controls.aione.SimulationGame.service;

import org.myfx.controls.aione.SimulationGame.entity.SimulateLocation;

import java.util.List;

/**
 * 模拟游戏-地点业务处理接口（语义化业务层）
 */
public interface SimulateLocationService {

    /**
     * 新增游戏地点
     * @param locationCode 地点编码（枚举类型，非空）
     * @param locationDesc 地点描述（可为空）
     * @return 受影响数据库行数
     */
    int addGameLocation(String locationCode, String locationDesc);

    /**
     * 根据地点ID查询地点详情
     * @param locationId 地点ID（非空且大于0）
     * @return 地点实体信息
     */
    SimulateLocation getGameLocationById(Integer locationId);

    /**
     * 查询所有游戏地点
     * @return 所有地点实体列表
     */
    List<SimulateLocation> listAllGameLocations();

    /**
     * 根据地点编码查询地点详情
     * @param locationCode 地点编码（枚举类型，非空）
     * @return 地点实体信息
     */
    SimulateLocation getGameLocationByCode(String locationCode);

    /**
     * 根据地点编码删除游戏地点
     * @param locationCode 地点编码（枚举类型，非空）
     * @return 受影响数据库行数
     */
    int removeGameLocationByCode(String locationCode);
}