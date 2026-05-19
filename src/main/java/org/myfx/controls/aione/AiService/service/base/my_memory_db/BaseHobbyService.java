package org.myfx.controls.aione.AiService.service.base.my_memory_db;

import org.myfx.controls.aione.AiService.common.my_memory_db.HobbyEnum;
import org.myfx.controls.aione.AiService.entity.my_memory_db.BaseHobby;

import java.util.List;

/**
 * 爱好业务服务接口（语义化封装，屏蔽底层CRUD细节）
 */
public interface BaseHobbyService {

    /**
     * 新增爱好
     * @param hobbyName 爱好名称（如：跑步、看书）
     * @return 是否新增成功（true=成功，false=失败）
     */
    boolean addHobby(HobbyEnum hobbyName);

    /**
     * 根据爱好ID查询爱好信息
     * @param hobbyId 爱好ID
     * @return 爱好信息（null=无匹配数据）
     */
    BaseHobby getHobbyById(Integer hobbyId);

    /**
     * 根据爱好名称查询爱好信息（唯一名称）
     * @param hobbyName 爱好名称
     * @return 爱好信息（null=无匹配数据）
     */
    BaseHobby getHobbyByName(HobbyEnum hobbyName);

    /**
     * 查询所有爱好列表
     * @return 所有爱好列表（无数据返回空列表）
     */
    List<BaseHobby> listAllHobbies();
}