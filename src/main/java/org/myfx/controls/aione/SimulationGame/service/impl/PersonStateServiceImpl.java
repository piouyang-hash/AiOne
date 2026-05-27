package org.myfx.controls.aione.SimulationGame.service.impl;

import jakarta.annotation.Resource;
import org.myfx.controls.aione.SimulationGame.entity.PersonState;
import org.myfx.controls.aione.SimulationGame.mapper.PersonStateMapper;
import org.myfx.controls.aione.SimulationGame.service.PersonStateService;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
public class PersonStateServiceImpl implements PersonStateService {

    // 注入Mapper
    @Resource
    private PersonStateMapper personStateMapper;

    @Override
    public int addPersonState(PersonState personState) {
        // 非空校验
        Assert.notNull(personState, "人物状态不能为空");
        return personStateMapper.insertPersonState(personState);
    }

    @Override
    public PersonState getPersonStateById(Integer id) {
        // 非空校验
        Assert.notNull(id, "查询ID不能为空");
        return personStateMapper.selectPersonStateById(id);
    }

    @Override
    public int editPersonStateById(PersonState personState) {
        // 非空校验
        Assert.notNull(personState, "修改对象不能为空");
        Assert.notNull(personState.getId(), "修改ID不能为空");
        return personStateMapper.updatePersonStateById(personState);
    }

    @Override
    public int removePersonStateById(Integer id) {
        // 非空校验
        Assert.notNull(id, "删除ID不能为空");
        return personStateMapper.deletePersonStateById(id);
    }
}