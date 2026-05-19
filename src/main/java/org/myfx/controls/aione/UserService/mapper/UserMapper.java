package org.myfx.controls.aione.UserService.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.myfx.controls.aione.ServiceCommon.serviceEnum.LogicalDeleteEnum;
import org.myfx.controls.aione.UserService.model.entity.User;

import java.util.List;

@Mapper // 标记这是 MyBatis 的接口，Spring 会自动找它
public interface UserMapper {

    /**
     * 新增用户
     * @param user 用户实体
     * @return 受影响的行数
     */
    Integer add(User user);

    /**
     * 注册失败补偿：物理删除5分钟内创建的用户（用于Saga补偿）
     * 直接物理删除指定用户在5分钟内创建的用户记录
     *
     * @param id 用户ID
     * @return 受影响的行数
     */
    int physicalDeleteUserForRegisterFailCompensation(@Param("id") Integer id);

    /**
     * 逻辑删除用户：将is_deleted标记为1
     * @param id 用户ID
     * @return 受影响的行数
     */
    int logicalDeleteById(@Param("id") Integer id);

    /**
     * 逻辑复原用户：将is_deleted从1改回0
     * @param id 用户ID
     * @return 受影响的行数
     */
    int recoverLogicalDeleteById(@Param("id") Integer id);

    /**
     * 物理删除用户：从数据库中彻底删除，只允许删除已逻辑删除的记录
     * @param id 用户ID
     * @return 受影响的行数
     */
    int physicalDeleteById(@Param("id") Integer id);

    /**
     * 更新用户密码
     * @param user 用户实体
     * @return 受影响的行数
     */
    int updatePassword(User user);

    /**
     * 更新用户邮箱
     * @param id 用户ID
     * @param email 新邮箱
     * @return 受影响的行数
     */
    int updateEmail(@Param("id") Integer id, @Param("email") String email);

    /**
     * 根据ID将用户权限提升为超级管理员
     * @param id 用户ID
     * @return 受影响的行数
     */
    int updateUserRoleToAdmin(@Param("id") Integer id);

    /**
     * 根据ID查询用户
     * @param id 用户ID
     * @param isDeleted 删除状态枚举
     * @return 用户实体
     */
    User findById(@Param("id") Integer id, @Param("isDeleted") LogicalDeleteEnum isDeleted);

    /**
     * 根据邮箱查询用户
     * @param email 邮箱
     * @return 用户实体
     */
    User findByEmail(@Param("email") String email);

    /**
     * 检查邮箱是否存在
     * @param email 邮箱
     * @return 是否存在
     */
    boolean existsByEmail(@Param("email") String email);

    /**
     * 分页查询用户ID
     * @param offset 偏移量
     * @param batchSize 每页大小
     * @return 用户ID列表
     */
    List<Integer> selectUserIdsByBatch(@Param("offset") Integer offset, @Param("batchSize") Integer batchSize);
}