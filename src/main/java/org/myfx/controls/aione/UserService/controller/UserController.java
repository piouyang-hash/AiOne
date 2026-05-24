package org.myfx.controls.aione.UserService.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.UserService.common.annotation.ValidEmailCode;
import org.myfx.controls.aione.UserService.model.dto.ChangePasswordDTO;
import org.myfx.controls.aione.UserService.model.dto.LoginDTO;
import org.myfx.controls.aione.UserService.model.dto.RefreshTokenDTO;
import org.myfx.controls.aione.UserService.model.vo.LoginResultVO;
import org.myfx.controls.aione.UserService.model.dto.LoginUserProfileDTO;
import org.myfx.controls.aione.UserService.model.entity.User;
import org.myfx.controls.aione.UserService.model.entity.UserProfile;
import org.myfx.controls.aione.UserService.model.vo.RefreshTokenVO;
import org.myfx.controls.aione.UserService.service.UserProfileService;
import org.myfx.controls.aione.UserService.service.UserService;
import org.myfx.controls.aione.UserService.service.VerificationService;
import org.myfx.controls.aione.UserService.model.vo.UserVO;
import org.myfx.controls.aione.UserService.util.EmailDesensitizeUtil;
import org.myfx.controls.aione.ServiceCommon.AppResponse;
import org.myfx.controls.aione.ServiceCommon.SwaggerResponseConstants;
import org.myfx.controls.aione.ServiceCommon.annotation.*;
import org.myfx.controls.aione.ServiceCommon.context.RequestContext;
import org.myfx.controls.aione.ServiceCommon.context.UserContext;
import org.myfx.controls.aione.ServiceCommon.serviceEnum.AppTypeEnum;
import org.myfx.controls.aione.ServiceCommon.serviceEnum.JwtTokenType;
import org.myfx.controls.aione.ServiceCommon.serviceEnum.RoleEnum;
import org.myfx.controls.aione.ServiceCommon.utils.JwtTokenUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@RestController
// 配置允许前端域名跨域，支持OPTIONS预检请求
@PublicAppCors
@RequestMapping("/api/user")
@CleanupThreadLocal
@Slf4j
@ApiTimeRecord("用户接口")
@Tag(name = "用户接口", description = "提供注册、登录、注销、密码修改、邮箱修改、自动登录、退出登录等功能")
public class UserController {

    private final UserService userService;
    private final VerificationService verificationService;
    private final UserProfileService userProfileService;

    private final JwtTokenUtil jwtTokenUtil;
    private final StringRedisTemplate stringRedisTemplate;

    @Operation(
            summary = "注册新用户（需验证码）",
            description = "注册新用户并自动创建对应应用的资料，需验证邮箱验证码；appType（应用标识）需传入，用于多应用隔离创建用户资料"
    )
    @ValidEmailCode
    @SwaggerResponseConstants.Api500
    @SwaggerResponseConstants.Api400
    @RateLimit(seconds = 30, maxCount = 5)
    @PostMapping("/register")
    public AppResponse<Void> register(
            @Parameter(
                    description = "注册所需的登录信息（必填：邮箱、密码、appType；无需填写：用户名）",
                    required = true
            )
            @RequestBody LoginDTO loginDTO, // 核心修改：替换为LoginDTO
            @Parameter(description = "邮箱验证码（必填，需与发送至注册邮箱的验证码一致）", required = true)
            @RequestParam
            @SuppressWarnings("unused") // 抑制未使用参数警告
            // 这个参数在切面受检验（ValidEmailCode注解切面）
            String code) {
        userService.registerUser(loginDTO); // 调用适配LoginDTO的registerUser方法
        return AppResponse.success(null, "注册成功，已自动创建对应应用的用户资料");
    }

    @Operation(
            summary = "注销用户账号",
            description = """
                    注销当前登录用户的账号，会级联删除用户资料（按当前应用隔离）。
                    注意：该接口需要验证请求头中的认证信息（如Authorization: Bearer Token），
                    请确保请求头中已正确填写有效的认证令牌，否则会返回权限错误（401/403）；
                    无需传递用户ID，系统自动从认证信息中获取当前登录用户ID。
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @SwaggerResponseConstants.Api500
    @SwaggerResponseConstants.Api400
    @SwaggerResponseConstants.Api404
    @CheckJwt
    @RateLimit(seconds = 30, maxCount = 5)
    @DeleteMapping("/cancel") // 移除路径中的{id}，接口路径简化为/cancel
    public AppResponse<Void> cancelAccount() { // 移除id参数
        boolean isSuccess = userService.cancelUserAccount(); // 调用无参的Service方法
        if (isSuccess) {
            return AppResponse.success(null, "账号注销成功，所有关联信息已删除");
        } else {
            return AppResponse.error(404, "注销失败，当前用户不存在或已被注销", null);
        }
    }

    @Operation(summary = "找回密码（未登录状态）（需验证码）",
            description = """
                    未登录状态下通过绑定邮箱和验证码重置密码。
                    无需登录（不验证JWT），但需提供有效的邮箱和对应邮箱验证码。
                    请在五分钟内完成，验证码过期后需重新获取。
                    验证通过后直接更新密码（自动加密存储）。
                    """)
    @PostMapping("/reset-password")
    @ValidEmailCode
    @RateLimit(seconds = 30, maxCount = 5) // 限流防滥用
    @SwaggerResponseConstants.Api500
    @SwaggerResponseConstants.Api400
    @SwaggerResponseConstants.Api404 // 邮箱不存在时返回404
    public AppResponse<Void> resetPassword(
            @Parameter(description = "用户绑定的邮箱（用于定位用户）", required = true)
            @RequestParam String email,
            @Parameter(description = "新密码（需符合密码格式要求）", required = true)
            @RequestParam String newPassword,
            @Parameter(description = "邮箱验证码（用于验证身份，由@ValidEmailCode切面自动校验）", required = true)
            @SuppressWarnings("unused")
            @RequestParam String code) {
        // 1. 调用专门的重置密码方法（适配未登录场景）
        userService.resetPassword(email, newPassword);

        return AppResponse.success(null, "密码重置成功，请使用新密码登录");
    }

    @Operation(
            summary = "修改密码（登录态）",
            description = """
                登录状态下修改当前用户密码，需验证原密码正确性。
                注意：
                1. 该接口必须携带有效的 JWT Token；
                2. 用户身份从 Token 中解析，禁止传递 userId/username；
                3. 接口30秒内最多调用5次，防止恶意尝试；
                4. 新密码需符合系统复杂度规则（如8位以上、含字母+数字）。
                """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/change-password")
    @CheckJwt
    @RateLimit(seconds = 30, maxCount = 5)
    @SwaggerResponseConstants.Api500
    @SwaggerResponseConstants.Api400
    @SwaggerResponseConstants.Api401
    @SwaggerResponseConstants.Api404
    public AppResponse<Void> changePassword(
            @Parameter(description = "修改密码参数（原密码+新密码）", required = true)
            @RequestBody @Valid ChangePasswordDTO changePasswordDTO) {
        // 调用Service，传递原密码+新密码（替代原User参数）
        userService.changePassword(changePasswordDTO.getOldPassword(), changePasswordDTO.getNewPassword());
        return AppResponse.success(null, "密码更新成功");
    }

    @Operation(summary = "修改邮箱",
            description = """
                    修改当前登录用户的绑定邮箱，后端自动校验：
                    1. 邮箱格式（仅支持QQ邮箱）；
                    2. 邮箱唯一性（未被其他用户占用）；
                    3. 新邮箱与旧邮箱不一致；
                    4. 新邮箱验证码有效性（通过@ValidEmailCode切面校验）。
                    注意：
                    1. 该接口需要验证请求头中的认证信息（如Authorization: Bearer Token），否则返回401/403；
                    2. 验证码（code）由新邮箱接收，有效期5分钟，错误/过期会直接返回400。
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/change-email")
    @CheckJwt // 验证JWT令牌
    @RateLimit(seconds = 30, maxCount = 5) // 限流：30秒内最多5次请求
    @ValidEmailCode // 新增：验证码校验切面切入点注解
    @SwaggerResponseConstants.Api500
    @SwaggerResponseConstants.Api400
    @SwaggerResponseConstants.Api401
    @SwaggerResponseConstants.Api404
    public AppResponse<Void> changeEmail(
            @Parameter(description = "新的QQ邮箱地址（如123456@qq.com）", required = true, example = "123456@qq.com")
            @RequestParam String email,
            @Parameter(description = "新邮箱收到的验证码（6位数字）", required = true, example = "111111")
            @SuppressWarnings("unused") // 抑制code参数未在方法内直接使用的警告（因code在切面校验）
            @RequestParam String code) { // 新增：验证码参数
        // 调用Service（code在切面已校验，无需传入Service）
        userService.changeEmail(email);
        return AppResponse.success(null, "邮箱更新成功");
    }

    @Operation(summary = "提升为超级管理员（需认证）",
            description = "通过超级管理员认证密钥提升当前登录用户为超级管理员，密钥有效期1分钟",
            security = @SecurityRequirement(name = "bearerAuth") // 关联Swagger中的认证配置
    )
    @CheckJwt
    @RequireRole(allowedRoles = {RoleEnum.USER})
    @SwaggerResponseConstants.Api500
    @SwaggerResponseConstants.Api400
    @SwaggerResponseConstants.Api401
    @RateLimit(seconds = 120, maxCount = 3) // 限制2分钟内最多尝试3次
    @PostMapping("/upgrade-to-admin")
    public AppResponse<Void> upgradeToAdmin(
            @Parameter(description = "超级管理员认证6位Key", required = true)
            @RequestParam String key,
            @Parameter(description = "超级管理员认证32位密码", required = true)
            @RequestParam String value) {
        // 验证超级管理员密钥是否有效
        boolean isInvalid = verificationService.isInvalidAdminAuth(key, value);
        // 认证，绝对是否提升当前用户为超级管理员
        userService.upgradeToAdmin(isInvalid);
        return AppResponse.success(null, "权限提升成功，当前用户已成为超级管理员");
    }

    /**
     * 用户登录（需验证码）接口
     * 核心修改：入参从User改为LoginDTO，保留code单独参数，适配AppType查询用户资料
     */
    @Operation(
            summary = "用户登录（需验证码）",
            description = "仅支持邮箱登录！需提供邮箱（email）、密码及邮箱验证码，username无需填写，需指定AppType；验证通过后返回JWT令牌与用户信息。"
    )
    @PostMapping("/login")
    @ValidEmailCode() // 验证码校验切面注解保留
    @RateLimit(seconds = 30, maxCount = 5) // 限流策略保留
    @SwaggerResponseConstants.Api500
    @SwaggerResponseConstants.Api400
    @SwaggerResponseConstants.Api401
    public AppResponse<LoginResultVO> login(
            @Parameter(description = "登录信息DTO（必填：email、password、AppType；无需填写：username）", required = true)
            @RequestBody @Valid LoginDTO loginDTO, // 核心修改：替换为LoginDTO，保留@Valid校验
            @Parameter(description = "邮箱验证码（必填，需与发送至绑定邮箱的验证码一致）", required = true)
            @RequestParam
            @SuppressWarnings("unused") // 抑制未使用参数警告（切面校验用）
            String code) { // code参数单独保留，逻辑不变

        // 核心修改：调用userService.login时传入LoginDTO（需确保userService.login适配LoginDTO入参）
        LoginResultVO loginResultVO = userService.login(loginDTO);

        // 核心修改：使用LoginDTO的AppType调用双参数的getUserProfileByUserId，适配多应用隔离
        UserProfile userProfile = userProfileService.getUserProfile(loginResultVO.getUserId(), loginDTO.getAppType());
        LoginUserProfileDTO profileDTO = new LoginUserProfileDTO();
        BeanUtils.copyProperties(userProfile, profileDTO);
        loginResultVO.setProfile(profileDTO);

        return AppResponse.success(loginResultVO, "登录成功");
    }

    @Operation(summary = "用户快速登录（免验证码）",
            description = "仅支持邮箱登录！需提供邮箱（email）和密码（password），username无需填写，验证通过后返回JWT令牌与用户信息。适用于信任环境下的快捷登录。"
    )
    @PostMapping("/quick-login")
    @RateLimit(seconds = 30, maxCount = 5) // 保持限流策略
    @SwaggerResponseConstants.Api500
    @SwaggerResponseConstants.Api400
    @SwaggerResponseConstants.Api401
    public AppResponse<LoginResultVO> quickLogin(
            @Parameter(description = "登录用户信息（必填：email、password、AppType）", required = true)
            @RequestBody @Valid LoginDTO loginDTO) { // 新增@Valid触发参数校验
        LoginResultVO loginResultVO = userService.quickLogin(loginDTO);

        // 获取用户资料
        UserProfile userProfile = userProfileService.getUserProfile(loginResultVO.getUserId(), loginDTO.getAppType());

        if (userProfile != null) {
            // 有用户资料，进行拷贝
            LoginUserProfileDTO profileDTO = new LoginUserProfileDTO();
            BeanUtils.copyProperties(userProfile, profileDTO);
            loginResultVO.setProfile(profileDTO);
        } else {
            // 用户第一次登录此应用，用户资料可能正在异步初始化
            log.info("用户第一次登录此应用，userId={}，appType={}，用户资料正在初始化中",
                    loginResultVO.getUserId(), loginDTO.getAppType());
            // loginResult的profile字段保持null
        }

        return AppResponse.success(loginResultVO, "快速登录成功");
    }

    /**
     * 刷新双令牌（AccessToken + RefreshToken）
     * 必须携带 RefreshToken 调用
     */
    @Operation(
            summary = "刷新双令牌",
            description = "基于RefreshToken刷新AccessToken和RefreshToken，无感续期登录状态",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @CheckJwt(tokenType = JwtTokenType.REFRESH)
    @PostMapping("/refresh-token")
    public AppResponse<RefreshTokenVO> refreshToken(
            // 核心：改用DTO接收所有参数
            @Valid @RequestBody RefreshTokenDTO dto
    ) {
        // 1. 从上下文获取用户信息（不变）
        Integer userId = UserContext.getUserId();
        RoleEnum role = UserContext.getRole();
        AppTypeEnum appType = UserContext.getAppType();

        // 2. 从DTO获取参数（替换原RequestParam）
        Boolean rememberMe = dto.getRememberMe();
        String oldAccessToken = dto.getOldAccessToken();
        Long expireTimeMs = dto.getExpireTime();

        // 3. 生成全新双Token（不变）
        String newAccessToken = jwtTokenUtil.generateAccessToken(userId, role, appType);
        String newRefreshToken = jwtTokenUtil.generateRefreshToken(userId, role, appType, rememberMe);

        // 4. 安全加固：旧RefreshToken加入黑名单（不变）
        String oldRefreshToken = RequestContext.getToken();
        String refreshBlackKey = "jwt:blacklist:refresh:" + oldRefreshToken;
        stringRedisTemplate.opsForValue().set(refreshBlackKey, "disabled", 30, TimeUnit.DAYS);

        // ===================== 【新增】旧AccessToken加入黑名单 =====================
        // 过期时间 = DTO传递的毫秒值，严格按要求实现
        String accessBlackKey = "jwt:blacklist:access:" + oldAccessToken;
        stringRedisTemplate.opsForValue().set(accessBlackKey, "disabled", expireTimeMs, TimeUnit.MILLISECONDS);

        // 5. 封装返回VO（不变）
        RefreshTokenVO vo = new RefreshTokenVO();
        vo.setAccessToken(newAccessToken);
        vo.setRefreshToken(newRefreshToken);
        vo.setRememberMe(rememberMe);

        return AppResponse.success(vo, "刷新令牌成功");
    }

    @Operation(
            summary = "查询当前登录用户的脱敏邮箱",
            description = """
                    查询当前登录用户的邮箱（脱敏展示）。
                    注意：
                    1. 该接口必须携带有效的 JWT Token；
                    2. 用户ID从Token中解析，无需前端传递；
                    3. 返回值为脱敏后的邮箱字符串（如123****@qq.com），非合法邮箱返回空字符串。
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @CheckJwt // 仅校验登录态，无前端参数
    @RateLimit(seconds = 30, maxCount = 5) // 和auto-login示例限流规则一致
    @SwaggerResponseConstants.Api500
    @SwaggerResponseConstants.Api400
    @SwaggerResponseConstants.Api401
    @GetMapping("/desensitize-email") // GET请求（查询类），语义清晰
    public AppResponse<String> getDesensitizeEmail() {
        // 1. 从登录上下文获取当前用户ID（和改密码/自动登录逻辑一致）
        Integer currentUserId = UserContext.getUserId();

        // 2. 查询用户信息
        User user = userService.getUserById(currentUserId);

        // 3. 调用工具类做邮箱脱敏，仅返回脱敏后的字符串
        String desensitizedEmail = EmailDesensitizeUtil.desensitize(user.getEmail());

        // 4. 返回脱敏邮箱字符串（核心：只返回这一个字符串）
        return AppResponse.success(desensitizedEmail, "查询脱敏邮箱成功");
    }

    @Operation(
            summary = "自动登录",
            description = """
                    基于JWT令牌自动登录。
                    前端需在Header中携带：`Authorization: Bearer <token>`。
                    验证通过后返回当前登录用户信息。
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @CheckJwt
    @RateLimit(seconds = 30, maxCount = 5)
    @SwaggerResponseConstants.Api500
    @SwaggerResponseConstants.Api400
    @SwaggerResponseConstants.Api401
    @PostMapping("/auto-login")
    public AppResponse<LoginResultVO> autoLogin() {
        LoginResultVO loginResultVO = userService.autoLogin();
        // 自从犯了循环依赖，以后看见耦合就害怕 → 控制器处理用户资料获取，解耦业务层
        // 1. 获取用户资料并创建DTO
        UserProfile userProfile = userProfileService.getCurrentUserProfile();
        LoginUserProfileDTO profileDTO = new LoginUserProfileDTO();

        // 2. 拷贝所有字段到DTO（字段名完全匹配时）
        BeanUtils.copyProperties(userProfile, profileDTO);

        // 3. 设置到LoginResult
        loginResultVO.setProfile(profileDTO);
        return AppResponse.success(loginResultVO, "自动登录成功");
    }

    @Operation(summary = "退出登录",
            description = """
                    用户退出登录，后端清除相关认证状态。
                    注意：该接口需要验证请求头中的认证信息（如Authorization: Bearer Token）
                    请确保请求头中已正确填写，否则会返回权限错误（401/403）。
                    无需传入其他参数，调用即可完成退出流程。
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/logout")
    @CheckJwt
    @RateLimit(seconds = 30, maxCount = 1) // 退出登录严格限制请求
    @SwaggerResponseConstants.Api500
    @SwaggerResponseConstants.Api400
    @SwaggerResponseConstants.Api401
    @SwaggerResponseConstants.Api404
    public AppResponse<Void> logout() {
        userService.logout(); // 调用接口方法，无参数
        return AppResponse.success(null, "退出登录成功");
    }

    /**
     * 根据用户ID查询用户信息（供其他服务调用）
     */
    @Operation(
            summary = "根据ID查询用户",
            description = "通过用户ID查询用户基本信息（用户名、ID等），供订单服务等其他服务调用,不是给前端使用的"
    )
    @SwaggerResponseConstants.Api500
    @SwaggerResponseConstants.Api400
    @SwaggerResponseConstants.Api401
    @SwaggerResponseConstants.Api404
    @GetMapping("/getById")
    @RateLimit(seconds = 30, maxCount = 5)
    @ServiceAuth(allowedServices = {"order-service"})
    public AppResponse<UserVO> getUserById(
            @Parameter(description = "用户ID（必填，正整数）", required = true, example = "1001")
            @RequestParam Integer userId
    ) {
        User user = userService.getUserById(userId);
        UserVO userVO = new UserVO(user.getId());
        return AppResponse.success(userVO, "用户查询成功");
    }
}