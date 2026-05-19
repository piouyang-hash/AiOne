package org.myfx.controls.aione.AiService.mapper.my_memory_db;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.myfx.controls.aione.AiService.entity.my_memory_db.BaseUserInfo;

/**
 * 用户基本信息表Mapper接口
 */
@Mapper
public interface BaseUserInfoMapper {

    /**
     * 新增用户基本信息
     */
    int insert(BaseUserInfo baseUserInfo);

    /**
     * 根据雪花ID删除用户信息
     */
    int deleteById(@Param("id") Long id);

    /**
     * 逻辑删除用户基本信息（注销场景）
     */
    int updateLogicalDeleteByUserId(@Param("userId") Integer userId);

    /**
     * 逻辑复原用户基本信息（注销失败SAGA补偿）
     */
    int updateLogicalRecoverByUserId(@Param("userId") Integer userId);

    /**
     * 物理删除用户基本信息（注册失败SAGA补偿）
     */
    int physicalDeleteByUserId(@Param("userId") Integer userId);

    /**
     * 根据用户ID动态更新字段（只更新非null的字段）
     * @param baseUserInfo 必须包含userId，可选包含gender/age/identity
     * @return 受影响行数
     */
    int updateByUserId(BaseUserInfo baseUserInfo);

    /**
     * 根据雪花ID查询用户信息
     */
    BaseUserInfo selectById(@Param("id") Long id);

    /**
     * 通用查询：仅查未逻辑删除的用户信息
     */
    BaseUserInfo selectNotDeletedByUserId(@Param("userId") Integer userId);

    /**
     * 特殊查询：查所有用户信息（含已逻辑删除，用于SAGA补偿校验）
     */
    BaseUserInfo selectByUserIdWithDeleted(@Param("userId") Integer userId);

}