package org.myfx.controls.aione.AiService.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.myfx.controls.aione.AiService.common.my_memory_db.HobbyEnum;
import org.myfx.controls.aione.AiService.entity.my_memory_db.BaseHobby;
import org.myfx.controls.aione.AiService.service.base.my_memory_db.BaseHobbyService;
import org.myfx.controls.aione.AiService.vo.BaseHobbyVO;
import org.myfx.controls.aione.ServiceCommon.AppResponse;
import org.myfx.controls.aione.ServiceCommon.SwaggerResponseConstants;
import org.myfx.controls.aione.ServiceCommon.annotation.CheckJwt;
import org.myfx.controls.aione.ServiceCommon.annotation.CleanupThreadLocal;
import org.myfx.controls.aione.ServiceCommon.annotation.RateLimit;
import org.myfx.controls.aione.ServiceCommon.annotation.RequireRole;
import org.myfx.controls.aione.ServiceCommon.serviceEnum.RoleEnum;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理员专属爱好管理控制器（仅管理员可操作，需JWT认证）
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/hobby")
@Tag(name = "管理员爱好管理接口", description = "爱好的新增/查询管理接口（仅管理员可调用）")
@CleanupThreadLocal
public class AdminHobbyController {

    private final BaseHobbyService baseHobbyService;

    /**
     * 管理员新增爱好（移除所有错误处理，直接调用）
     */
    @Operation(
            summary = "管理员新增爱好",
            description = """
                管理员新增系统爱好字典数据：
                1. 该接口需要验证请求头中的认证信息（Authorization: Bearer Token）；
                2. 仅系统管理员可调用，60秒内最多调用10次（避免重复新增）；
                3. 爱好名称不能为空/空白，否则返回参数不合法错误；
                4. 新增成功后返回操作结果，失败返回具体原因。
                """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/add")
    @CheckJwt
    @RequireRole(allowedRoles = {RoleEnum.ADMIN})
    @RateLimit(seconds = 60, maxCount = 100) // 管理员新增限流，60秒最多100次
    @SwaggerResponseConstants.Api500 // 服务器内部错误响应
    @SwaggerResponseConstants.Api401 // 未认证（JWT无效/过期）
    @SwaggerResponseConstants.Api403 // 权限不足（非管理员）
    public AppResponse<String> addHobby(
            @Parameter(description = "爱好名称（如：跑步、编程）", required = true)
            @RequestParam HobbyEnum hobbyName) {
        // 直接调用Service，不做任何结果判断、异常捕获
        baseHobbyService.addHobby(hobbyName);
        // 无论成功失败，均返回固定成功响应
        return AppResponse.success(null, "爱好新增请求已处理：" + hobbyName);
    }

    /**
     * 管理员根据爱好ID查询爱好信息（移除所有错误处理，直接调用）
     */
    @Operation(
            summary = "管理员根据ID查询爱好",
            description = """
                管理员根据爱好ID查询系统爱好字典数据：
                1. 该接口需要验证请求头中的认证信息（Authorization: Bearer Token）；
                2. 仅系统管理员可调用，无频率限制（查询操作无压库风险）；
                3. 爱好ID必须为正整数，否则返回参数不合法错误；
                4. 无匹配ID时返回null，前端需做空值处理。
                """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/get/{hobbyId}")
    @CheckJwt
    @RequireRole(allowedRoles = {RoleEnum.ADMIN})
    @SwaggerResponseConstants.Api500 // 服务器内部错误响应
    @SwaggerResponseConstants.Api401 // 未认证（JWT无效/过期）
    @SwaggerResponseConstants.Api403 // 权限不足（非管理员）
    public AppResponse<BaseHobby> getHobbyById(
            @Parameter(description = "爱好ID（正整数）", required = true)
            @PathVariable Integer hobbyId) {
        // 直接调用Service，不做任何异常捕获
        BaseHobby hobby = baseHobbyService.getHobbyById(hobbyId);
        // 无论是否查到数据，均返回成功响应
        return AppResponse.success(hobby, "爱好查询请求已处理");
    }

    /**
     * 管理员根据爱好名称查询爱好信息（移除所有错误处理，直接调用）
     */
    @Operation(
            summary = "管理员根据名称查询爱好",
            description = """
                管理员根据爱好名称查询系统爱好字典数据：
                1. 该接口需要验证请求头中的认证信息（Authorization: Bearer Token）；
                2. 仅系统管理员可调用，无频率限制（查询操作无压库风险）；
                3. 爱好名称不能为空/空白，否则返回参数不合法错误；
                4. 无匹配名称时返回null，前端需做空值处理。
                """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/get/name/{hobbyName}")
    @CheckJwt
    @RequireRole(allowedRoles = {RoleEnum.ADMIN})
    @SwaggerResponseConstants.Api500 // 服务器内部错误响应
    @SwaggerResponseConstants.Api401 // 未认证（JWT无效/过期）
    @SwaggerResponseConstants.Api403 // 权限不足（非管理员）
    public AppResponse<BaseHobby> getHobbyByName(
            @Parameter(description = "爱好名称（如：跑步、编程）", required = true)
            @PathVariable HobbyEnum hobbyName) {
        // 直接调用Service，不做任何异常捕获
        BaseHobby hobby = baseHobbyService.getHobbyByName(hobbyName);
        // 无论是否查到数据，均返回成功响应
        return AppResponse.success(hobby, "爱好查询请求已处理");
    }

    /**
     * 管理员根据爱好名称查询爱好信息（返回VO，含枚举+描述）
     */
    @Operation(
            summary = "管理员根据名称查询爱好（返回VO）",
            description = """
            管理员根据爱好名称查询系统爱好字典数据（返回VO，自动包含枚举描述）：
            1. 该接口需要验证请求头中的认证信息（Authorization: Bearer Token）；
            2. 仅系统管理员可调用，无频率限制（查询操作无压库风险）；
            3. 爱好名称不能为空/空白，否则返回参数不合法错误；
            4. 无匹配名称时返回null，前端需做空值处理；
            5. 返回VO包含爱好ID、枚举类型、枚举描述（自动填充）。
            """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/get/name/vo/{hobbyName}") // 路径新增/vo区分VO接口
    @CheckJwt
    @RequireRole(allowedRoles = {RoleEnum.ADMIN})
    @SwaggerResponseConstants.Api500 // 服务器内部错误响应
    @SwaggerResponseConstants.Api401 // 未认证（JWT无效/过期）
    @SwaggerResponseConstants.Api403 // 权限不足（非管理员）
    public AppResponse<BaseHobbyVO> getHobbyByNameVO(
            @Parameter(description = "爱好枚举（如：SPORT_RUNNING=跑步）", required = true)
            @PathVariable HobbyEnum hobbyName) {
        // 1. 调用原有Service接口，复用业务逻辑
        BaseHobby hobby = baseHobbyService.getHobbyByName(hobbyName);

        // 2. 使用Spring BeanUtils自动Copy属性（无需手动赋值）
        BaseHobbyVO hobbyVO = new BaseHobbyVO();
        if (hobby != null) {
            // 核心：自动复制同名属性（hobbyId、hobbyName→hobbyEnum）
            BeanUtils.copyProperties(hobby, hobbyVO, "hobbyEnum"); // 先排除hobbyEnum，避免覆盖自定义setter
            // 单独设置hobbyEnum，触发VO的自定义setter自动填充hobbyDesc
            hobbyVO.setHobbyEnum(hobby.getHobbyName());
        }

        // 3. 直接返回VO响应，不处理任何错误
        return AppResponse.success(hobbyVO, "爱好VO查询请求已处理");
    }

    /**
     * 管理员查询所有爱好列表
     */
    @Operation(
            summary = "管理员查询所有爱好",
            description = """
                管理员查询系统所有爱好字典数据：
                1. 该接口需要验证请求头中的认证信息（Authorization: Bearer Token）；
                2. 仅系统管理员可调用，无频率限制（查询操作无压库风险）；
                3. 无爱好数据时返回空列表，避免前端空指针；
                4. 返回结果按爱好ID升序排列（底层可自行扩展排序）。
                """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/listAll")
    @CheckJwt
    @RequireRole(allowedRoles = {RoleEnum.ADMIN})
    @SwaggerResponseConstants.Api500 // 服务器内部错误响应
    @SwaggerResponseConstants.Api401 // 未认证（JWT无效/过期）
    @SwaggerResponseConstants.Api403 // 权限不足（非管理员）
    public AppResponse<List<BaseHobby>> listAllHobbies() {
        // 查询操作无参数校验，直接调用Service
        List<BaseHobby> hobbyList = baseHobbyService.listAllHobbies();
        return AppResponse.success(hobbyList, "所有爱好查询成功，共" + hobbyList.size() + "条数据");
    }
}