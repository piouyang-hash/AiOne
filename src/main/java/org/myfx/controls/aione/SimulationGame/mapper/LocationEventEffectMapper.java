package org.myfx.controls.aione.SimulationGame.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.myfx.controls.aione.SimulationGame.entity.LocationEventEffect;

@Mapper
public interface LocationEventEffectMapper {

    /**
     * 新增属性影响配置
     */
    int insertLocationEventEffect(LocationEventEffect effect);

    /**
     * 根据ID查询
     */
    LocationEventEffect selectLocationEventEffectById(Integer id);

    /**
     * 根据ID动态修改
     */
    int updateLocationEventEffectById(LocationEventEffect effect);

    /**
     * 根据ID删除
     */
    int deleteLocationEventEffectById(Integer id);
}