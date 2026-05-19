package org.myfx.controls.aione.ServiceCommon.service;

import org.myfx.controls.aione.ServiceCommon.entity.AdminAuditLog;

import java.util.List;

/**
 * 管理员审计日志 业务接口
 */
public interface AdminAuditLogService {

    /**
     * 新增管理员审计日志
     * @param methodName 接口方法名
     * @param requestParams 入参
     * @param responseParams 出参
     * @param operateStatus 操作状态
     * @param errorMsg 错误信息
     */
    void addAdminAuditLog(String methodName, String requestParams, String responseParams, Integer operateStatus, String errorMsg);

    /**
     * 查询全部审计日志
     */
    List<AdminAuditLog> getAllAdminAuditLog();

    /**
     * 获取当前登录管理员的操作日志
     */
    List<AdminAuditLog> getAdminAuditLogByCurrentUser();
}