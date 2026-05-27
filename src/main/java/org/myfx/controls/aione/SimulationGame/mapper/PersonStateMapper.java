package org.myfx.controls.aione.SimulationGame.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.myfx.controls.aione.SimulationGame.entity.PersonState;

/**
 * 人物状态 Mapper
 * 纯手写CRUD，无继承，直观易懂
 */
@Mapper
public interface PersonStateMapper {

    /**
     * 新增人物状态
     */
    int insertPersonState(PersonState personState);

    /**
     * 根据ID查询人物状态
     */
    PersonState selectPersonStateById(Integer id);

    /**
     * 根据ID修改人物状态
     */
    int updatePersonStateById(PersonState personState);

    /**
     * 根据ID删除人物状态
     */
    int deletePersonStateById(Integer id);

}