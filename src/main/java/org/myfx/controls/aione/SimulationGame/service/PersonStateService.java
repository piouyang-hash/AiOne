package org.myfx.controls.aione.SimulationGame.service;

import org.myfx.controls.aione.SimulationGame.entity.PersonState;

public interface PersonStateService {

    /**
     * 新增人物状态
     */
    int addPersonState(PersonState personState);

    /**
     * 根据ID查询人物状态
     */
    PersonState getPersonStateById(Integer id);

    /**
     * 根据ID修改人物状态
     */
    int editPersonStateById(PersonState personState);

    /**
     * 根据ID删除人物状态
     */
    int removePersonStateById(Integer id);
}