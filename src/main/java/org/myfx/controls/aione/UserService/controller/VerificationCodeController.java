package org.myfx.controls.aione.UserService.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.UserService.model.entity.User;
import org.myfx.controls.aione.UserService.common.annotation.ValidEmailCode;

import org.myfx.controls.aione.UserService.service.UserService;
import org.myfx.controls.aione.ServiceCommon.AppResponse;
import org.myfx.controls.aione.ServiceCommon.SwaggerResponseConstants;
import org.myfx.controls.aione.ServiceCommon.annotation.*;
import org.myfx.controls.aione.UserService.service.VerificationService;
import org.myfx.controls.aione.ServiceCommon.context.UserContext;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 验证码发送接口（仅处理验证码发送逻辑）
 */
@RestController
@RequestMapping("/code") // 路径调整为更贴合功能的名称
@RequiredArgsConstructor
@CleanupThreadLocal
@PublicAppCors
@Slf4j
@Tag(name = "验证码发送", description = "专门用于发送验证码的接口集合，当前包含邮箱验证码发送、超级管理员认证码生成接口")
public class VerificationCodeController {

    private final VerificationService verificationService;
    private final UserService userService;

    @Operation(summary = "发送验证码（测试版）",
            description = "向指定邮箱发送登录验证码（测试阶段发送固定验证码111111），请勿频繁请求。"
    )
    @PostMapping("/verification-code/send")
    @RateLimit(maxCount = 1) // 60秒内最多请求1次，保留限流逻辑
    @SwaggerResponseConstants.Api500
    @SwaggerResponseConstants.Api400
    @SwaggerResponseConstants.Api401
    public AppResponse<Void> sendVerificationCode(
            @Parameter(description = "接收验证码的邮箱地址", required = true)
            @RequestParam String email) throws InterruptedException {

        // 2. 测试阶段：替换真实发送逻辑，仅打印日志（固定验证码111111）
        String testCode = "111111"; // 测试专用验证码
        log.info("【测试阶段】向邮箱{}发送验证码，测试验证码为：{}", email, testCode);

        Thread.sleep(3000);
        // 注释掉真实的发送逻辑（测试完成后恢复）
        // verificationService.sendCode(email);

        // 3. 返回值保持不变，前端感知不到逻辑变化
        return AppResponse.success(null, "验证码已发送至邮箱：" + email + "，请注意查收");
    }

    // 新增接口（适配登录态用户，自动使用绑定邮箱）
    @Operation(summary = "发送验证码（登录态用户）",
            description = "向当前登录用户的绑定邮箱发送验证码，无需传入邮箱，验证码有效期由系统配置决定，请勿频繁请求。"
    )
    @PostMapping("/verification-code/send/current-user")
    @CheckJwt // 校验JWT登录态，确保用户已登录
    @RateLimit(maxCount = 1) // 同原有接口，60秒内最多请求1次
    @SwaggerResponseConstants.Api500
    @SwaggerResponseConstants.Api400
    @SwaggerResponseConstants.Api401
    public AppResponse<Void> sendVerificationCodeForCurrentUser() throws InterruptedException {
        // 1. 从UserContext获取当前登录用户ID（@CheckJwt确保此处能拿到有效ID）
        Integer userId = UserContext.getUserId();
        // 2. 根据ID查询用户信息
        User user = userService.getUserById(userId);

        // 3. 获取用户绑定的邮箱（不暴露给前端）
        String userEmail = user.getEmail();

        // 4. 核心：直接复用上面接口的公共逻辑（不再手动调用verificationService）
        return sendVerificationCode(userEmail);
    }

    // ====================== 接口1：通用版（带email+code，无登录态，无业务逻辑） ======================
    @Operation(summary = "验证邮箱验证码（通用版）",
            description = "验证指定邮箱对应的短信验证码是否正确，无需登录态，验证码有效期由系统配置决定。"
    )
    @PostMapping("/verification-code/verify")
    @RateLimit(maxCount = 5)
    @ValidEmailCode // 切面仍校验验证码正确性
    @SwaggerResponseConstants.Api500
    @SwaggerResponseConstants.Api400
    @SwaggerResponseConstants.Api401
    @SuppressWarnings("unused")
    public AppResponse<Void> verifyEmailCode(
            @Parameter(description = "待验证的邮箱地址", required = true)
            @RequestParam String email,

            @Parameter(description = "邮箱收到的验证码", required = true)
            @RequestParam String code) {
        // 按要求：去掉所有业务逻辑（身份校验等），直接返回成功
        return AppResponse.success(null, "邮箱短信验证码验证成功");
    }

    // ====================== 接口2：登录态版（仅code，自动关联当前用户邮箱） ======================
    @Operation(summary = "验证邮箱验证码（登录态版）",
            description = "验证当前登录用户绑定邮箱的短信验证码是否正确，仅需传验证码，无需传邮箱。"
    )
    @PostMapping("/verification-code/verify/current-user") // 路径区分，实现重载
    @RateLimit(maxCount = 5)
    @CheckJwt // 仅登录用户可调用
    @SwaggerResponseConstants.Api500
    @SwaggerResponseConstants.Api400
    @SwaggerResponseConstants.Api401
    @SuppressWarnings("unused")
    public AppResponse<Void> verifyEmailCode(
            // 方法名重载（参数列表不同）
            @Parameter(description = "邮箱收到的验证码", required = true)
            @RequestParam String code) {
        // 1. 从UserContext获取当前登录用户ID（@CheckJwt确保有效）
        Integer currentUserId = UserContext.getUserId();
        // 2. 查询当前用户绑定的邮箱
        User currentUser = userService.getUserById(currentUserId);
        String userEmail = currentUser.getEmail();

        // 不调用基础方法是因为代理被绕过了，认输了
        // 放弃调用通用版verifyEmailCode(email, code)，直接在当前方法内做验证码校验
        // ---------------------- 测试版验证码校验逻辑（测试阶段使用） ----------------------
        String testCode = "111111";
        if (!testCode.equals(code)) {
            throw new RuntimeException("测试阶段仅支持验证码：111111");
        }

        // ---------------------- 正式版验证码校验逻辑（测试完可恢复，当前注释掉） ----------------------
        // 注释掉真实验证码校验逻辑，测试完可恢复
        // if (verificationService.isInvalidCode(userEmail, code)) {
        //     throw new RuntimeException("验证码错误或已过期");
        // }

        return AppResponse.success(null, "邮箱短信验证码验证成功");
    }

    @Operation(summary = "生成超级管理员认证码",
            description = "生成超级管理员专属认证密钥对（6位Key+32位密码），请查看后端日志获取密钥信息，密钥有效期1分钟。"
    )
    @RateLimit(seconds = 30, maxCount = 1)
    @PostMapping("/admin/auth/generate") // 独立路径，区分普通验证码
    @SwaggerResponseConstants.Api500
    public AppResponse<Void> generateAdminAuthCode() {
        verificationService.generateAdminAuthKey(); // 触发认证码生成
        return AppResponse.success(null, "超级管理员认证密钥已生成，请查看后端日志获取密钥信息（有效期1分钟）");
    }
}