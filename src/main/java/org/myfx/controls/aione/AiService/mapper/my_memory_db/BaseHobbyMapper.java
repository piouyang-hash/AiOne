package org.myfx.controls.aione.AiService.mapper.my_memory_db;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.myfx.controls.aione.AiService.common.my_memory_db.HobbyEnum;
import org.myfx.controls.aione.AiService.entity.my_memory_db.BaseHobby;

import java.util.List;

/**
 * 爱好字典表Mapper接口
 */
@Mapper
public interface BaseHobbyMapper {

    /**
     * 新增爱好（仅传爱好名称字符串）
     * @param Hobby@return 受影响行数（1=新增成功）
     */
    // 不要和这个VALUES (#{hobbyName})名称一样，不然会报错
    int insertHobby(BaseHobby Hobby);

    /**
     * 根据ID删除爱好
     */
    int deleteById(@Param("hobbyId") Integer hobbyId);

    /**
     * 根据ID修改爱好名称
     */
    int updateNameById(@Param("hobbyId") Integer hobbyId, @Param("hobbyName") HobbyEnum hobbyName);

    /**
     * 根据ID查询爱好
     */
    BaseHobby selectById(@Param("hobbyId") Integer hobbyId);

    /**
     * 根据名称查询爱好（唯一索引，返回单条）
     */
    BaseHobby selectByName(@Param("hobbyName") HobbyEnum hobbyName);

    /**
     * 查询所有爱好（分页可自行扩展）
     */
    List<BaseHobby> selectAll();
}
