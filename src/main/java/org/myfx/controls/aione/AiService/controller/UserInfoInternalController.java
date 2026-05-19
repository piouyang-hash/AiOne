package org.myfx.controls.aione.AiService.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.AiService.dto.UserOperateDTO;
import org.myfx.controls.aione.AiService.service.base.my_memory_db.UserInfoService;
import org.myfx.controls.aione.ServiceCommon.AppResponse;
import org.myfx.controls.aione.ServiceCommon.SwaggerResponseConstants;
import org.myfx.controls.aione.ServiceCommon.annotation.CleanupThreadLocal;
import org.myfx.controls.aione.ServiceCommon.annotation.ServiceAuth;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 内部用户信息接口
 * 供 user-service 等可信微服务调用，用于用户注册/注销操作
 */
@RestController
@RequestMapping("/internal/user-info")
@Tag(name = "内部用户信息接口", description = "供 user-service 等可信微服务调用，用于用户注册/注销操作")
@Slf4j
@RequiredArgsConstructor
@CleanupThreadLocal // 复用你项目中的线程局部变量清理注解
@Validated // 开启控制器参数校验
public class UserInfoInternalController {

    // 注入用户信息业务层（核心：调用注册/注销方法）
    private final UserInfoService userInfoService;

    // ========== 原有接口（保留） ==========
    /**
     * 注册用户（初始化用户基本信息）
     * 控制器仅转发调用，所有业务逻辑/错误处理均由业务层完成
     * @param userOperateDTO 注册用户参数（仅用户ID）
     * @return 统一响应体
     */
    @Operation(
            summary = "注册用户（初始化基本信息）",
            description = """
                用户注册接口，由 user-service 调用：
                1. 初始化用户基础信息（生成雪花ID、默认空的性别/年龄/身份）；
                2. 操作必须幂等（重复调用仅返回成功，不重复创建）；
                3. 仅允许 user-service 调用，其他服务无权限；
                4. 入参仅需用户ID，其他字段由系统自动生成。
                """
    )
    @PostMapping("/register")
    @ServiceAuth(allowedServices = {"user-service"}) // 仅限user-service调用
    @SwaggerResponseConstants.Api500 // 复用你项目中的500响应注解
    @SwaggerResponseConstants.Api400 // 复用你项目中的400响应注解
    public AppResponse<Void> registerUser(
            @Parameter(description = "注册用户请求参数（仅用户ID）", required = true)
            @Valid @RequestBody UserOperateDTO userOperateDTO) {

        // 1. 记录调用日志，不处理业务逻辑
        log.info("【内部接口-注册用户】接收调用请求，用户ID={}", userOperateDTO.getUserId());

        // 2. 直接调用业务层方法，控制器不做任何结果判断/错误处理
        userInfoService.initUserInfo(userOperateDTO.getUserId());

        // 3. 业务层无异常则返回成功（业务层已处理幂等/重复注册校验）
        String successMsg = String.format("用户注册请求处理成功，用户ID=%s", userOperateDTO.getUserId());
        log.info("【内部接口-注册用户】{}", successMsg);
        return AppResponse.success(null, successMsg);
    }

    /**
     * 注销用户（销毁用户基本信息）
     * 控制器仅转发调用，所有业务逻辑/错误处理均由业务层完成
     * @param userOperateDTO 注销用户参数（仅用户ID）
     * @return 统一响应体
     */
    @Operation(
            summary = "注销用户（销毁基本信息）",
            description = """
                用户注销接口，由 user-service 调用：
                1. 销毁用户基础信息（级联删除爱好关联关系）；
                2. 操作必须幂等（重复调用仅返回成功，不重复删除）；
                3. 仅允许 user-service 调用，其他服务无权限；
                4. 入参仅需用户ID，自动级联清理关联数据。
                """
    )
    @PostMapping("/cancel")
    @ServiceAuth(allowedServices = {"user-service"}) // 仅限user-service调用
    @SwaggerResponseConstants.Api500
    @SwaggerResponseConstants.Api400
    public AppResponse<Void> cancelUser(
            @Parameter(description = "注销用户请求参数（仅用户ID）", required = true)
            @Valid @RequestBody UserOperateDTO userOperateDTO) {

        // 1. 记录调用日志，不处理业务逻辑
        log.info("【内部接口-注销用户】接收调用请求，用户ID={}", userOperateDTO.getUserId());

        // 2. 直接调用业务层方法，控制器不做任何结果判断/错误处理
        userInfoService.logicalDeleteForCancel(userOperateDTO.getUserId());

        // 3. 业务层无异常则返回成功（业务层已处理幂等/用户不存在校验）
        String successMsg = String.format("用户注销请求处理成功，用户ID=%s", userOperateDTO.getUserId());
        log.info("【内部接口-注销用户】{}", successMsg);
        return AppResponse.success(null, successMsg);
    }

    // ========== 新增SAGA补偿接口1：注册失败-物理删除用户基本信息 ==========
    /**
     * SAGA补偿-注册失败物理删除用户基本信息
     * 控制器仅转发调用，所有业务逻辑/错误处理均由补偿业务层完成
     * @param userOperateDTO 补偿参数（仅用户ID）
     * @return 统一响应体
     */
    @Operation(
            summary = "SAGA补偿-注册失败物理删除用户基本信息",
            description = """
                SAGA补偿专用接口，仅注册流程失败时由user-service调用：
                1. 物理删除5分钟内创建的临时用户基本信息；
                2. 操作必须幂等（重复调用仅返回成功，不重复删除）；
                3. 仅允许 user-service 调用，其他服务无权限；
                4. 入参仅需用户ID，自动校验「临时记录+无关联数据」。
                """
    )
    @PostMapping("/compensate/register-fail")
    @ServiceAuth(allowedServices = {"user-service"}) // 仅限user-service调用
    @SwaggerResponseConstants.Api500
    @SwaggerResponseConstants.Api400
    public AppResponse<Void> compensateRegisterFail(
            @Parameter(description = "补偿请求参数（仅用户ID）", required = true)
            @Valid @RequestBody UserOperateDTO userOperateDTO) {

        // 1. 记录补偿调用日志
        log.info("【内部接口-SAGA补偿】接收注册失败物理删除请求，用户ID={}", userOperateDTO.getUserId());

        // 2. 调用SAGA补偿业务层方法，控制器不处理逻辑
        boolean success = userInfoService.compensatePhysicalDeleteRegisterFail(userOperateDTO.getUserId());

        // 3. 根据补偿结果返回响应（仅转发结果，不处理业务逻辑）
        String msg;
        if (success) {
            msg = String.format("SAGA补偿成功：用户[%s]注册失败，临时基本信息已物理删除", userOperateDTO.getUserId());
            log.info("【内部接口-SAGA补偿】{}", msg);
            return AppResponse.success(null, msg);
        } else {
            msg = String.format("SAGA补偿失败：用户[%s]注册失败，临时基本信息删除失败（可能创建超5分钟/有关联数据）", userOperateDTO.getUserId());
            log.error("【内部接口-SAGA补偿】{}", msg);
            return AppResponse.error(400, msg, null);
        }
    }

    // ========== 新增SAGA补偿接口2：注销失败-逻辑复原用户基本信息 ==========
    /**
     * SAGA补偿-注销失败逻辑复原用户基本信息
     * 控制器仅转发调用，所有业务逻辑/错误处理均由补偿业务层完成
     * @param userOperateDTO 补偿参数（仅用户ID）
     * @return 统一响应体
     */
    @Operation(
            summary = "SAGA补偿-注销失败逻辑复原用户基本信息",
            description = """
                SAGA补偿专用接口，仅注销流程失败时由user-service调用：
                1. 将已逻辑删除的用户基本信息复原为未删除状态；
                2. 操作必须幂等（重复调用仅返回成功，不重复复原）；
                3. 仅允许 user-service 调用，其他服务无权限；
                4. 入参仅需用户ID，自动校验「已删除+无关联业务锁」。
                """
    )
    @PostMapping("/compensate/cancel-fail")
    @ServiceAuth(allowedServices = {"user-service"}) // 仅限user-service调用
    @SwaggerResponseConstants.Api500
    @SwaggerResponseConstants.Api400
    public AppResponse<Void> compensateCancelFail(
            @Parameter(description = "补偿请求参数（仅用户ID）", required = true)
            @Valid @RequestBody UserOperateDTO userOperateDTO) {

        // 1. 记录补偿调用日志
        log.info("【内部接口-SAGA补偿】接收注销失败逻辑复原请求，用户ID={}", userOperateDTO.getUserId());

        // 2. 调用SAGA补偿业务层方法，控制器不处理逻辑
        boolean success = userInfoService.compensateRecoverCancelFail(userOperateDTO.getUserId());

        // 3. 根据补偿结果返回响应（仅转发结果，不处理业务逻辑）
        String msg;
        if (success) {
            msg = String.format("SAGA补偿成功：用户[%s]注销失败，基本信息已复原为未删除状态", userOperateDTO.getUserId());
            log.info("【内部接口-SAGA补偿】{}", msg);
            return AppResponse.success(null, msg);
        } else {
            msg = String.format("SAGA补偿失败：用户[%s]注销失败，基本信息复原失败（可能不存在/未逻辑删除）", userOperateDTO.getUserId());
            log.error("【内部接口-SAGA补偿】{}", msg);
            return AppResponse.error(400, msg, null);
        }
    }
}