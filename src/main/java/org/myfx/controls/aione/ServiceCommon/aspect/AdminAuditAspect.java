package org.myfx.controls.aione.ServiceCommon.aspect;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.myfx.controls.aione.ServiceCommon.AppResponse;
import org.myfx.controls.aione.ServiceCommon.service.AdminAuditLogService;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

/**
 * 数据型管理员专用审计切面
 * 环绕通知：记录管理员操作、校验方法名/入参/出参
 * 【硬性要求❗】
 * Order 一定要大于 1（JWT 解析切面的 order=1）
 * 必须在 JWT 解析之后执行，否则拿不到 UserContext 上下文，无法保存审计日志
 */
@Slf4j
@Aspect
@Component
@Order(10) // 数值>1即可，满足JWT解析后执行
@RequiredArgsConstructor
public class AdminAuditAspect {

    // ===================== 注入审计日志Service =====================
    private final AdminAuditLogService adminAuditLogService;
    private final ObjectMapper objectMapper;

    /**
     * 切点：
     * 1. 类上标注 @Audit → 所有方法切入
     * 2. 方法上标注 @Audit → 单个方法切入
     * 注解全路径：org.myfx.controls.servicecommon.annotation.Audit
     */
    @Pointcut("@within(org.myfx.controls.aione.ServiceCommon.annotation.Audit) || @annotation(org.myfx.controls.aione.ServiceCommon.annotation.Audit)")
    public void auditPointCut() {
    }

    /**
     * 环绕通知：核心审计逻辑
     */
    @Around("auditPointCut()")
    public Object auditAround(ProceedingJoinPoint joinPoint) throws Throwable {

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.getMethod().getName();

        log.debug("==================== 管理员审计开始 ====================");
        log.debug("操作接口(方法名)：{}", methodName);

        checkMethodName(methodName);

        Object[] args = joinPoint.getArgs();
        checkRequestParams(args);

        // ==================== 使用 Jackson ====================
        String requestParams = "[]";
        try {
            requestParams = objectMapper.writeValueAsString(args);
        } catch (Exception e) {
            log.warn("入参序列化失败", e);
        }
        log.debug("接口入参：{}", requestParams);

        Object result = null;
        try {
            result = joinPoint.proceed();

            checkResponseParams(result);

            String responseParams = "null";
            try {
                responseParams = objectMapper.writeValueAsString(result);
            } catch (Exception e) {
                log.warn("出参序列化失败", e);
            }
            log.debug("接口出参：{}", responseParams);

            adminAuditLogService.addAdminAuditLog(methodName, requestParams, responseParams, 1, null);

        } catch (Throwable e) {
            adminAuditLogService.addAdminAuditLog(methodName, requestParams, null, 0, e.getMessage());
            log.error("管理员操作失败 | 审计日志已保存", e);
            throw e;
        }

        log.debug("==================== 管理员审计结束 ====================\n");
        return result;
    }

    // ===================== 自定义校验方法（可根据业务扩展） =====================

    /**
     * 检查方法名：校验是否为管理员合法操作接口
     * 规则：禁止查询方法、仅允许 增/删/改 操作
     */
    private void checkMethodName(String methodName) {
        // 禁止：查询/获取/列表类方法（新增 list 前缀拦截）
        if (methodName.startsWith("query")
                || methodName.startsWith("get")
                || methodName.startsWith("select")
                || methodName.startsWith("list")) { // 新增这一行
            throw new RuntimeException("审计切面异常：查询/列表接口禁止使用@Audit注解，仅管理员操作接口可用");
        }
        log.debug("方法名校验通过：符合管理员操作接口规范");
    }

    /**
     * 检查入参：管理员操作入参合法性校验
     */
    private void checkRequestParams(Object[] args) {
        if (args == null || args.length == 0) {
            log.warn("入参校验：管理员接口入参为空");
            return;
        }
        // 可扩展：非空校验、参数格式校验、敏感词校验等
        log.debug("入参校验通过");
    }

    /**
     * 检查出参：管理员操作返回结果校验
     * 【微服务规范】所有接口出参必须是 AppResponse 统一返回体
     * 成功：code=200 | 失败：code为自定义错误码
     */
    private void checkResponseParams(Object result) {
        // 1. 基础判空
        if (result == null) {
            log.warn("出参校验：接口返回结果为空");
            return;
        }

        // 2. 严格校验：所有管理员接口必须返回 AppResponse 统一格式（微服务硬性规范）
        if (!(result instanceof AppResponse<?> appResponse)) {
            throw new RuntimeException("审计切面异常：管理员接口出参不合法！必须返回 AppResponse 统一响应体");
        }

        // 3. 强转为统一返回体，校验核心字段
        Integer code = appResponse.getCode();
        // 校验状态码不能为空（微服务强制要求）
        if (code == null) {
            throw new RuntimeException("审计切面异常：接口返回状态码 code 不能为空");
        }

        // 4. 区分成功/失败，打印日志
        if (code == 200) {
            log.debug("出参校验通过：接口执行成功，响应码[200]");
        } else {
            log.warn("出参校验通过：接口执行失败，响应码[{}]，错误信息[{}]", code, appResponse.getMessage());
        }
    }
}