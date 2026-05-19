package org.myfx.controls.aione.SimulationGame.mapper;

import org.apache.ibatis.annotations.Param;
import org.myfx.controls.aione.SimulationGame.entity.SimulateLocation;

import java.util.List;

/**
 * 模拟游戏-地点表 Mapper 接口
 */
public interface SimulateLocationMapper {

    /**
     * 新增地点（枚举code当成字符串传入，忽略时间字段）
     * @param locationCode 地点编码
     * @param locationDesc 地点描述（可为空）
     * @return 受影响行数
     */
    int insert(@Param("locationCode") String locationCode, @Param("locationDesc") String locationDesc);

    /**
     * 根据地点ID查询详情
     * @param locationId 地点ID
     * @return 地点实体
     */
    SimulateLocation selectById(@Param("locationId") Integer locationId);

    /**
     * 查询所有地点（无参数）
     * @return 地点实体列表
     */
    List<SimulateLocation> selectAll();

    /**
     * 根据地点编码精准查询
     * @param locationCode 地点编码
     * @return 地点实体
     */
    SimulateLocation selectByCode(@Param("locationCode") String locationCode);

    /**
     * 根据地点编码精准删除
     * @param locationCode 地点编码
     * @return 受影响行数
     */
    int deleteByCode(@Param("locationCode") String locationCode);
}