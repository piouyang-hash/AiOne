package org.myfx.controls.aione.ServiceCommon.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.myfx.controls.aione.ServiceCommon.entity.AdminAuditLog;

import java.util.List;

/**
 * 管理员审计日志 Mapper 接口
 * 无继承、仅实现指定3个方法
 */
@Mapper
public interface AdminAuditLogMapper {

    /**
     * 新增审计日志
     * @param log 实体类
     * @return 影响行数
     */
    int insert(AdminAuditLog log);

    /**
     * 查询全部审计日志
     * @return 日志列表
     */
    List<AdminAuditLog> selectAll();

    /**
     * 根据管理员ID查询日志
     * @param userId 管理员ID
     * @return 日志列表
     */
    List<AdminAuditLog> selectByUserId(@Param("userId") Integer userId);

}