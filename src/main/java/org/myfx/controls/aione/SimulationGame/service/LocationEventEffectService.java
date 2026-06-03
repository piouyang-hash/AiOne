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
     * 根据地点编码+事件编码查询（新增）
     */
    LocationEventEffect getByLocationAndEventCode(String locationCode, String eventCode);

    /**
     * 根据ID修改
     */
    int editLocationEventEffectById(LocationEventEffect effect);

    /**
     * 根据ID删除
     */
    int removeLocationEventEffectById(Integer id);
}