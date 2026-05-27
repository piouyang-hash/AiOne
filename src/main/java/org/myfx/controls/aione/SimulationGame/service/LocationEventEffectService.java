package org.myfx.controls.aione.SimulationGame.service;

import org.myfx.controls.aione.SimulationGame.entity.LocationEventEffect;

public interface LocationEventEffectService {

    /**
     * 新增
     */
    int addLocationEventEffect(LocationEventEffect effect);

    /**
     * 根据ID查询
     */
    LocationEventEffect getLocationEventEffectById(Integer id);

    /**
     * 根据ID修改
     */
    int editLocationEventEffectById(LocationEventEffect effect);

    /**
     * 根据ID删除
     */
    int removeLocationEventEffectById(Integer id);
}