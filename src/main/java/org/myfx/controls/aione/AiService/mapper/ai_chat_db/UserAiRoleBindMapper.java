package org.myfx.controls.aione.AiService.mapper.ai_chat_db;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.myfx.controls.aione.AiService.entity.ai_chat_db.UserAiRoleBind;

import java.util.List;

@Mapper
public interface UserAiRoleBindMapper {

    /**
     * 新增用户-AI角色绑定关系
     */
    int insert(UserAiRoleBind bind);

    /**
     * 根据用户ID查询绑定的所有角色ID
     */
    List<Integer> selectRoleIdsByUserId(@Param("userId") Integer userId);

    /**
     * 根据用户ID查询完整的用户-AI角色绑定列表
     * @param userId 用户ID
     * @return 绑定实体列表
     */
    List<UserAiRoleBind> selectListByUserId(@Param("userId") Integer userId);

    /**
     * 根据用户ID和角色ID 删除AI角色绑定关系
     * @param userId 用户ID
     * @param roleId 角色ID
     * @return 影响行数
     */
    int deleteByUserIdAndRoleId(
            @Param("userId") Integer userId,
            @Param("roleId") Integer roleId
    );

}