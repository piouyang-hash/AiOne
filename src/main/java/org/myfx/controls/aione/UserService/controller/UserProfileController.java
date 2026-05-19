package org.myfx.controls.aione.UserService.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.myfx.controls.aione.UserService.model.dto.LoginUserProfileDTO;
import org.myfx.controls.aione.UserService.model.dto.UpdatedProfileDTO;
import org.myfx.controls.aione.UserService.service.AvatarService;
import org.myfx.controls.aione.ServiceCommon.AppResponse;
import org.myfx.controls.aione.ServiceCommon.SwaggerResponseConstants;
import org.myfx.controls.aione.ServiceCommon.annotation.CheckJwt;
import org.myfx.controls.aione.ServiceCommon.annotation.CleanupThreadLocal;
import org.myfx.controls.aione.ServiceCommon.annotation.MyVueApp;
import org.myfx.controls.aione.ServiceCommon.annotation.RateLimit;
import org.myfx.controls.aione.UserService.common.UserServiceConstants;
import org.myfx.controls.aione.UserService.model.entity.UserProfile;
import org.myfx.controls.aione.UserService.service.UserProfileService;
import org.myfx.controls.aione.UserService.util.AvatarFileTaskUtil;
import org.myfx.controls.aione.ServiceCommon.context.UserContext;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@RestController
@CrossOrigin
@RequestMapping("/user-profile/api")
@CleanupThreadLocal
@MyVueApp
@Tag(name = "用户信息接口", description = "提供更新用户信息，获取用户头像接口")
public class UserProfileController {

    private final UserProfileService userProfileService;
    private final AvatarFileTaskUtil avatarFileTaskUtil;
    private final AvatarService avatarService;

    @Value("${avatar.storage.path}")
    private String avatarRootPath;

    /**
     * 更新用户资料（支持同时上传头像）
     * @param userProfile 包含更新信息的用户资料对象（需包含userId，无需传appId）
     * @param file 可选头像文件（可为null，null表示不更新头像）
     * @return 统一响应体（按指定静态方法返回）
     * @提醒 该接口需携带JWT令牌（通常在请求头Authorization中，格式如Bearer {token}），
     * @CheckJwt 注解对应的切面会自动从请求头中获取令牌并验证登录状态，无需在方法参数中显式声明请求头；
     * @补充 appType（应用标识）由系统从登录上下文（UserContext）自动获取，无需前端传递

     * 注解心得：
     * 1. 核心用途：明确接口仅接收 "multipart/form-data" 类型请求，专门适配「表单数据+文件上传」的混合请求场景（如本接口的用户资料JSON+头像图片）
     * 2. 替代硬编码：用 MediaType.MULTIPART_FORM_DATA_VALUE 替代字符串 "multipart/form-data"，避免拼写错误，且更具可读性（IDE会自动提示枚举值）
     * 3. 适配Swagger：让Swagger UI自动识别请求类型，正确渲染文件上传组件（避免参数显示异常），前端无需手动猜请求头格式
     * 4. 避免解析异常：明确告知Spring MVC按文件上传格式解析请求，减少 "Failed to parse multipart servlet request" 类异常（之前遇到的解析失败问题，该注解是关键适配项）
     * 5. 语义化清晰：代码即文档，后续维护者一眼能看出接口支持文件上传，无需查看参数或业务逻辑反推
     * 6. 扩展提示：若接口仅传JSON（无文件），用 consumes = MediaType.APPLICATION_JSON_VALUE；若仅传文件，可省略consumes（默认兼容），但混合场景必须显式声明
     */
    @Operation(
            summary = "更新用户资料",
            description = "支持更新用户基础信息（仅需传userId），可选上传头像文件；接口需携带JWT令牌（请求头Authorization），appType由系统自动从登录上下文获取",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @SwaggerResponseConstants.Api500
    @SwaggerResponseConstants.Api400
    @SwaggerResponseConstants.Api401
    @CheckJwt // 添加JWT校验注解，由切面处理登录状态验证
    @RateLimit(seconds = 30, maxCount = 5)
    @PostMapping(
            value = "/update",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public AppResponse<UpdatedProfileDTO> updateUserProfile(
            @Parameter(
                    description = "用户资料对象（必须包含userId，其他更新字段按需传递，无需传appId、id）",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE, // 明确JSON类型
                            schema = @Schema(implementation = UserProfile.class)
                    )
            )
            @RequestPart("userProfile") UserProfile userProfile,

            @Parameter(
                    description = "头像文件（可选，支持JPEG/PNG/GIF/BMP格式）",
                    content = @Content(
                            mediaType = "image/*", // 明确媒体类型为图片，Swagger会显示更贴合的提示
                            schema = @Schema(type = "string", format = "binary") // 核心配置不变，仍遵循Swagger规范
                    )
            )
            @RequestPart(value = "file", required = false) MultipartFile file
    ) throws Exception {
        UserProfile updatedProfile = userProfileService.updateUserProfile(userProfile, file);

        // 成功响应：返回更新后的完整资料（放在data字段）
        UpdatedProfileDTO updatedProfileDTO = new UpdatedProfileDTO();
        BeanUtils.copyProperties(updatedProfile, updatedProfileDTO);

        return AppResponse.success(updatedProfileDTO, "资料更新成功");
    }

    @Operation(
            summary = "获取当前登录用户的头像图片（支持指定图片名称）",
            description = """
                支持两种模式：
                1. 不传fileName参数：返回当前用户的默认头像（二进制流）；
                2. 传fileName参数：返回当前用户的指定头像（需是该用户的头像文件）。
                前端需携带`Authorization: Bearer <token>`，可直接作为img的src使用。
                """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @CheckJwt // 验证JWT令牌
    @RateLimit(seconds = 30, maxCount = 5) // 限流
    @GetMapping(value = "/avatar", produces = {MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE})
    public void getCurrentUserAvatar(
            HttpServletResponse response,
            @Parameter(
                    description = "头像图片名称（可选，需是当前用户的头像文件，如：avatar_4_1763114857985.png）",
                    schema = @Schema(type = "string", example = "avatar_4_1763114857985.png")
            )
            @RequestParam(required = false) String fileName // 可选参数：不传则走原有逻辑
    ) throws IOException
    {
        // 1. 获取当前登录用户ID
        Integer currentUserId = UserContext.getUserId();

        File avatarFile;

        // 2. 判断是否传入fileName：有则按名称查，无则执行原有逻辑
        if (fileName != null && !fileName.isEmpty()) {
            // 2.1 有参数：校验文件是否属于当前用户（防越权）
            List<String> userAvatarFiles = avatarFileTaskUtil.listUserAvatars(currentUserId);
            if (!userAvatarFiles.contains(fileName)) {
                throw new SecurityException("无权访问该头像文件（文件不存在或不属于当前用户）");
            }

            // 2.2 拼接完整路径（自动处理斜杠）
            String fullPath = Paths.get(avatarRootPath, fileName).toString();
            avatarFile = new File(fullPath);

            // 2.3 校验文件是否存在
            if (!avatarFile.exists() || !avatarFile.isFile()) {
                throw new FileNotFoundException("指定头像文件不存在：" + fullPath);
            }
        } else {
            // 2.4 无参数：执行原有逻辑（获取当前用户的默认头像）
            avatarFile = userProfileService.getAvatarUrlByUserId(currentUserId);
        }

        // 3. 设置响应格式（自动匹配图片类型）
        String contentType = Files.probeContentType(avatarFile.toPath());
        response.setContentType(contentType);

        // 4. 读取字节流并返回（原有逻辑不变）
        try (InputStream in = new FileInputStream(avatarFile);
             OutputStream out = response.getOutputStream()) {

            byte[] buffer = new byte[1024];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
            out.flush();
        }
    }


    @Operation(
            summary = "获取当前登录用户的所有头像文件名",
            description = """
                    获取当前登录用户的所有头像文件名列表（用于前端加载图片）。
                    前端需在Header中携带：`Authorization: Bearer <token>`。
                    返回的文件名可结合`/user/avatar?fileName=xxx`接口加载对应图片。
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @SwaggerResponseConstants.Api500
    @SwaggerResponseConstants.Api400
    @SwaggerResponseConstants.Api401
    @CheckJwt // JWT校验：验证登录状态
    @RateLimit(seconds = 30, maxCount = 5) // 限流：30秒内最多5次请求
    @GetMapping(value = "/avatars/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public AppResponse<List<String>> getAllAvatarFileNames()
    {
        // 1. 获取当前登录用户ID
        Integer currentUserId = UserContext.getUserId();

        // 2. 调用工具类获取该用户的所有头像文件名
        List<String> avatarFileNames = avatarService.listUserAvatars(currentUserId);

        // 3. 若列表为空，添加默认头像
        if (CollectionUtils.isEmpty(avatarFileNames)) {
            avatarFileNames = Collections.singletonList(UserServiceConstants.DEFAULT_AVATAR);
        }

        // 4. 返回处理后的列表
        return AppResponse.success(avatarFileNames, "获取所有头像文件名成功");
    }

    @Operation(
            summary = "获取当前登录用户的资料",
            description = """
            获取当前登录用户的资料信息。
            前端需在Header中携带：`Authorization: Bearer <token>`。
            如果用户在当前应用下没有资料，则返回null。
            """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @SwaggerResponseConstants.Api500
    @SwaggerResponseConstants.Api400
    @SwaggerResponseConstants.Api401
    @CheckJwt // JWT校验：验证登录状态
    @RateLimit(seconds = 30, maxCount = 10) // 限流：30秒内最多10次请求
    @GetMapping(value = "/profile", produces = MediaType.APPLICATION_JSON_VALUE)
    public AppResponse<LoginUserProfileDTO> getCurrentUserProfile() {
        // 调用UserProfileService中的getCurrentUserProfile()方法
        UserProfile userProfile = userProfileService.getCurrentUserProfile();

        // 如果没有数据，返回null
        if (userProfile == null) {
            return AppResponse.success(null, "用户资料为空");
        }

        // 如果有数据，复制到LoginUserProfileDTO
        LoginUserProfileDTO profileDTO = new LoginUserProfileDTO();
        BeanUtils.copyProperties(userProfile, profileDTO);

        return AppResponse.success(profileDTO, "获取用户资料成功");
    }

}
