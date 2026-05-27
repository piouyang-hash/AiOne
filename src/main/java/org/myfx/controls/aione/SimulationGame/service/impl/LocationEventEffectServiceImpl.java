package org.myfx.controls.aione.SimulationGame.service.impl;

import jakarta.annotation.Resource;
import org.myfx.controls.aione.SimulationGame.entity.LocationEventEffect;
import org.myfx.controls.aione.SimulationGame.mapper.LocationEventEffectMapper;
import org.myfx.controls.aione.SimulationGame.service.LocationEventEffectService;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
public class LocationEventEffectServiceImpl implements LocationEventEffectService {

    @Resource
    private LocationEventEffectMapper locationEventEffectMapper;

    @Override
    public int addLocationEventEffect(LocationEventEffect effect) {
        Assert.notNull(effect, "属性影响配置不能为空");
        return locationEventEffectMapper.insertLocationEventEffect(effect);
    }

    @Override
    public LocationEventEffect getLocationEventEffectById(Integer id) {
        Assert.notNull(id, "ID不能为空");
        return locationEventEffectMapper.selectLocationEventEffectById(id);
    }

    @Override
    public int editLocationEventEffectById(LocationEventEffect effect) {
        Assert.notNull(effect, "修改对象不能为空");
        Assert.notNull(effect.getEffectId(), "修改ID不能为空");
        return locationEventEffectMapper.updateLocationEventEffectById(effect);
    }

    @Override
    public int removeLocationEventEffectById(Integer id) {
        Assert.notNull(id, "删除ID不能为空");
        return locationEventEffectMapper.deleteLocationEventEffectById(id);
    }
}