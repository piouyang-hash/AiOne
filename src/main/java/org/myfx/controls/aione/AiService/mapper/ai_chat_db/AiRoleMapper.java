package org.myfx.controls.aione.AiService.mapper.ai_chat_db;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.myfx.controls.aione.AiService.entity.ai_chat_db.AiRole;

import java.util.List;

/**
 * AI角色表Mapper接口
 */
@Mapper
public interface AiRoleMapper {

    /**
     * 新增AI角色（不包含时间字段，自动填充）
     *
     * @param aiRole AI角色实体
     * @return 影响行数
     */
    int insert(AiRole aiRole);

    /**
     * 根据主键ID修改AI角色
     *
     * @param aiRole AI角色实体
     * @return 影响行数
     */
    int updateById(AiRole aiRole);

    /**
     * 根据ID删除AI角色
     *
     * @param roleId 角色ID
     * @return 影响行数
     */
    int deleteById(@Param("roleId") Integer roleId);

    /**
     * 根据ID查询AI角色
     *
     * @param roleId 角色ID
     * @return AI角色实体
     */
    AiRole selectById(@Param("roleId") Integer roleId);

    /**
     * 根据【创建人ID】查询该用户创建的所有AI角色
     *
     * @param userId 创建人ID（create_user_id）
     * @return AI角色列表
     */
    List<AiRole> selectListByUserId(@Param("userId") Integer userId);

    /**
     * 根据角色编码查询AI角色
     *
     * @param roleCode 角色编码（如MENGYA）
     * @return AI角色实体
     */
    AiRole selectByCode(@Param("roleCode") String roleCode);

    /**
     * 查询所有AI角色（简单列表查询）
     *
     * @return AI角色列表
     */
    List<AiRole> selectList();

}