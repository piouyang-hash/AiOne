package org.myfx.controls.aione.SimulationGame.service;

import org.myfx.controls.aione.SimulationGame.dto.SimulateLocationEventRelationAddDTO;
import org.myfx.controls.aione.SimulationGame.entity.SimulateLocationEventRelation;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * 模拟游戏-地点与事件关联 业务层接口
 * 语义化封装Mapper的CRUD操作，增加业务参数校验和语义化方法名
 */
@Service
public interface SimulateLocationEventRelationService {

    /**
     * 新增地点与事件的关联关系
     * @param relationAddDTO 新增关联请求DTO（包含地点编码、事件编码、事件持续时长）
     * @return 受影响行数（1=新增成功，0=新增失败）
     */
    int addRelation(SimulateLocationEventRelationAddDTO relationAddDTO);

    /**
     * 根据地点编码查询所有关联的事件
     * @param locationCode 地点编码（非空）
     * @return 地点与事件关联关系列表
     */
    List<SimulateLocationEventRelation> getRelationsByLocationCode(String locationCode);

    /**
     * 根据事件编码查询所有关联的地点
     * @param eventCode 事件编码（非空）
     * @return 地点与事件关联关系列表
     */
    List<SimulateLocationEventRelation> getRelationsByEventCode(String eventCode);

    /**
     * 根据地点编码+事件编码精准查询关联关系
     * @param locationCode 地点编码（非空）
     * @param eventCode 事件编码（非空）
     * @return 关联关系实体（无匹配则返回null）
     */
    SimulateLocationEventRelation getRelationByTwoCode(String locationCode, String eventCode);

    /**
     * 根据地点编码+事件编码 获取【有效事件持续时间】
     * 内部自动校验：关联关系存在、时长合法（>0）
     * @param locationCode 地点编码
     * @param eventCode 事件编码
     * @return 合法的持续秒数
     * @throws RuntimeException 数据不存在/时长无效时抛出
     */
    Integer getDurationByTwoCode(String locationCode, String eventCode);

    /**
     * 查询所有地点与事件的关联关系
     * @return 全量关联关系列表
     */
    List<SimulateLocationEventRelation> getAllRelations();

    /**
     * 根据地点编码+事件编码精准删除关联关系
     * @param locationCode 地点编码（非空）
     * @param eventCode 事件编码（非空）
     * @return 受影响行数（1=删除成功，0=无匹配数据）
     */
    int removeRelationByTwoCode(String locationCode, String eventCode);
}