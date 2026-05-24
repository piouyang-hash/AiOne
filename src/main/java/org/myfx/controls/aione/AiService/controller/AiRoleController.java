package org.myfx.controls.aione.AiService.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.AiService.dto.AiRoleAddDTO;
import org.myfx.controls.aione.AiService.entity.ai_chat_db.AiRole;
import org.myfx.controls.aione.AiService.entity.ai_chat_db.UserAiRoleBind;
import org.myfx.controls.aione.AiService.service.base.ai_chat_db.AiRoleService;
import org.myfx.controls.aione.AiService.service.base.ai_chat_db.UserAiRoleBindService;
import org.myfx.controls.aione.AiService.vo.AiRoleVO;
import org.myfx.controls.aione.ServiceCommon.AppResponse;
import org.myfx.controls.aione.ServiceCommon.SwaggerResponseConstants;
import org.myfx.controls.aione.ServiceCommon.annotation.AiAppCors;
import org.myfx.controls.aione.ServiceCommon.annotation.CheckJwt;
import org.myfx.controls.aione.ServiceCommon.annotation.CleanupThreadLocal;
import org.myfx.controls.aione.ServiceCommon.annotation.RateLimit;
import org.springframework.beans.BeanUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

/**
 * AI角色管理控制器
 * 负责AI角色的新增、查询、修改等操作
 */
@RestController
@RequestMapping("/ai/role")
@Tag(name = "AI角色管理接口", description = "AI角色的新增、配置管理等操作")
@CleanupThreadLocal
@RequiredArgsConstructor
@AiAppCors
@Slf4j
public class AiRoleController {

    // 注入你的AI角色Service
    private final AiRoleService aiRoleService;

    // 用户角色绑定Service（你要求新增注入的组件）
    private final UserAiRoleBindService userAiRoleBindService;

    /**
     * 新增AI角色
     */
    @Operation(
            summary = "新增AI角色",
            description = "创建一个新的AI角色，支持上传头像；需携带JWT令牌（请求头Authorization）",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @SwaggerResponseConstants.Api500
    @SwaggerResponseConstants.Api400
    @SwaggerResponseConstants.Api401
    @CheckJwt
    @RateLimit(seconds = 60, maxCount = 10)
    @PostMapping(
            value = "/add",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public AppResponse<String> addAiRole(
            // ==================== AI角色信息（JSON格式，标准注解） ====================
            @Parameter(
                    description = "AI角色资料对象（无需传角色编码、创建人ID，系统自动生成）",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AiRoleAddDTO.class)
                    )
            )
            @RequestPart("aiRoleDTO") AiRoleAddDTO addDTO,

            // ==================== 头像文件（可选，标准注解） ====================
            @Parameter(
                    description = "AI角色头像（可选，支持JPEG/PNG/GIF/BMP格式）",
                    content = @Content(
                            mediaType = "image/*",
                            schema = @Schema(type = "string", format = "binary")
                    )
            )
            @RequestPart(value = "roleAvatar", required = false) MultipartFile roleAvatar
    ) {
        // 仅调用业务层，无逻辑代码
        aiRoleService.addAiRole(addDTO, roleAvatar);
        return AppResponse.success(null, "AI角色新增成功");
    }

    /**
     * 查询当前登录用户的所有角色（返回AI角色VO视图对象）
     */
    @Operation(
            summary = "查询我的所有AI角色",
            description = "获取当前登录用户绑定/拥有的全部AI角色完整信息（包含人设、描述、头像等）",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/my/list")
    @CheckJwt
    @RateLimit(seconds = 60, maxCount = 20)
    @SwaggerResponseConstants.Api500
    @SwaggerResponseConstants.Api400
    @SwaggerResponseConstants.Api401
    public AppResponse<List<AiRoleVO>> getMyAiRoleList() {
        log.info("查询当前用户的AI角色完整列表");
        // 1. 获取用户绑定的角色ID列表
        List<UserAiRoleBind> roleBindList = userAiRoleBindService.getMyUserAiRoleBindList();

        // 2. 遍历绑定关系，查询角色并转换为VO
        List<AiRoleVO> aiRoleVOList = new ArrayList<>();
        for (UserAiRoleBind bind : roleBindList) {
            // 2.1 查询完整角色信息
            AiRole aiRole = aiRoleService.getByRoleId(bind.getRoleId());

            // 2.2 SpringBoot原生工具：对象拷贝
            AiRoleVO aiRoleVO = new AiRoleVO();
            BeanUtils.copyProperties(aiRole, aiRoleVO);

            // 2.3 添加到VO列表
            aiRoleVOList.add(aiRoleVO);
        }

        // 3. 返回VO列表
        return AppResponse.success(aiRoleVOList, "查询用户所有角色成功");
    }

}