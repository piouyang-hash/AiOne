package org.myfx.controls.aione.ServiceCommon.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.ServiceCommon.context.UserContext;
import org.myfx.controls.aione.ServiceCommon.entity.AdminAuditLog;
import org.myfx.controls.aione.ServiceCommon.mapper.AdminAuditLogMapper;
import org.myfx.controls.aione.ServiceCommon.service.AdminAuditLogService;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import java.util.List;

/**
 * 管理员审计日志 业务实现类
 * 完全对标 FileDeleteFailedLogServiceImpl 格式
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminAuditLogServiceImpl implements AdminAuditLogService {

    // 构造器注入Mapper
    private final AdminAuditLogMapper adminAuditLogMapper;

    /**
     * 新增审计日志（业务化方法）
     */
    @Override
    public void addAdminAuditLog(String methodName, String requestParams, String responseParams, Integer operateStatus, String errorMsg) {
        // 1. 参数校验
        Assert.notNull(methodName, "接口方法名不能为空");
        Assert.notNull(operateStatus, "操作状态不能为空");

        // 2. 组装实体（ID自增、时间数据库自动生成，无需处理）
        AdminAuditLog adminAuditLog = new AdminAuditLog();
        adminAuditLog.setUserId(UserContext.getUserId()); // 从上下文获取管理员ID
        adminAuditLog.setMethodName(methodName);
        adminAuditLog.setRequestParams(requestParams);
        adminAuditLog.setResponseParams(responseParams);
        adminAuditLog.setOperateStatus(operateStatus);
        adminAuditLog.setErrorMsg(errorMsg);

        // 3. 调用Mapper插入
        adminAuditLogMapper.insert(adminAuditLog);
        log.info("管理员审计日志保存成功，用户ID：{}", adminAuditLog.getUserId());
    }

    /**
     * 查询全部日志（业务化方法）
     */
    @Override
    public List<AdminAuditLog> getAllAdminAuditLog() {
        return adminAuditLogMapper.selectAll();
    }

    /**
     * 查询当前管理员自己的操作日志（业务化方法）
     */
    @Override
    public List<AdminAuditLog> getAdminAuditLogByCurrentUser() {
        // 获取当前登录用户ID
        Integer userId = UserContext.getUserId();
        Assert.notNull(userId, "当前登录用户ID不能为空");

        // 调用Mapper查询
        return adminAuditLogMapper.selectByUserId(userId);
    }
}