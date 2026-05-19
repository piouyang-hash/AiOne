package org.myfx.controls.aione.SimulationGame.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.myfx.controls.aione.ServiceCommon.AppResponse;
import org.myfx.controls.aione.ServiceCommon.SwaggerResponseConstants;
import org.myfx.controls.aione.ServiceCommon.annotation.*;
import org.myfx.controls.aione.ServiceCommon.serviceEnum.RoleEnum;
import org.myfx.controls.aione.SimulationGame.common.LocationEnum;
import org.myfx.controls.aione.SimulationGame.dto.SimulateLocationAddDTO;
import org.myfx.controls.aione.SimulationGame.entity.SimulateLocation;
import org.myfx.controls.aione.SimulationGame.service.SimulateLocationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理员专属模拟游戏地点管理控制器（仅管理员可操作，需JWT认证）
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/simulation/location")
@Tag(name = "管理员模拟游戏地点管理接口", description = "模拟游戏地点的新增、查询、删除管理接口（仅管理员可调用）")
@CleanupThreadLocal
@AdminWebCors
@CheckJwt
public class SimulateAdminLocationController {

    // 注入地点业务服务（final + RequiredArgsConstructor 无需@Resource/@Autowired）
    private final SimulateLocationService simulateLocationService;

    @Operation(
            summary = "查询所有地点枚举参考（数据库存入参考）",
            description = """
            查询所有预定义的地点枚举数据，返回编码和中文描述，供管理员参考存入数据库的格式。
            注意：
            1. 该接口仅提供参考，枚举数据与数据库无关联；
            2. 该接口需要验证请求头中的认证信息（如Authorization: Bearer Token）；
            3. 仅管理员账号可调用，普通用户无权限；
            """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/enum/list/all")
    @SwaggerResponseConstants.Api500
    @SwaggerResponseConstants.Api401
    @SwaggerResponseConstants.Api403
    @RequireRole(allowedRoles = {RoleEnum.ADMIN})
    public AppResponse<List<LocationEnum.LocationEnumVO>> listAllLocationEnum() throws Exception {
        List<LocationEnum.LocationEnumVO> locationEnumList = LocationEnum.listAll();
        return AppResponse.success(locationEnumList, "地点枚举参考数据查询成功，共" + locationEnumList.size() + "条");
    }

    @Operation(
            summary = "新增游戏地点",
            description = """
            新增模拟游戏中的可用地点，地点编码不可重复。
            注意：
            1. 地点编码为唯一标识，不可与已存在的编码重复；
            2. 该接口需要验证请求头中的认证信息（如Authorization: Bearer Token）；
            3. 仅管理员账号可调用，普通用户无权限；
            4. 地点描述可为空，填写后将在前端展示详细场景说明；
            """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/add")
    @SwaggerResponseConstants.Api500 // 服务器内部错误响应
    @SwaggerResponseConstants.Api400 // 请求参数错误（如编码为空、重复）
    @SwaggerResponseConstants.Api401 // 未认证（无Token或Token无效）
    @SwaggerResponseConstants.Api403 // 权限不足（非管理员调用）
    @RequireRole(allowedRoles = {RoleEnum.ADMIN}) // 方法上注解：优先级更高
    // 修改点1：替换@RequestParam为@RequestBody + DTO，添加@Valid开启参数校验
    @Audit
    public AppResponse<Void> addGameLocation(
            @Parameter(
                    description = "新增地点请求参数",
                    required = true
            )
            @Valid @RequestBody SimulateLocationAddDTO locationAddDTO // 接收DTO，@Valid开启校验（触发@NotBlank）
    ) throws Exception {
        // 修改点2：从DTO中获取参数，调用服务层
        String locationCode = locationAddDTO.getLocationCode();
        String locationDesc = locationAddDTO.getLocationDesc();
        int affectedRows = simulateLocationService.addGameLocation(locationCode, locationDesc);

        return AppResponse.success(null, "游戏地点新增成功，受影响行数：" + affectedRows);
    }

    @Operation(
            summary = "根据地点ID查询详情",
            description = """
                根据地点主键ID查询地点的完整信息。
                注意：
                1. 地点ID为数据库自增主键，仅支持正整数；
                2. 该接口需要验证请求头中的认证信息（如Authorization: Bearer Token）；
                3. 仅管理员账号可调用，普通用户无权限；
                4. 传入无效ID将返回空数据，不会抛出异常；
                """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/get/{locationId}")
    @SwaggerResponseConstants.Api500 // 服务器内部错误响应
    @SwaggerResponseConstants.Api400 // 请求参数错误（如ID为负数、非数字）
    @SwaggerResponseConstants.Api401 // 未认证（无Token或Token无效）
    @SwaggerResponseConstants.Api403 // 权限不足（非管理员调用）
    @RequireRole(allowedRoles = {RoleEnum.ADMIN})
    public AppResponse<SimulateLocation> getGameLocationById(
            @Parameter(
                    description = "地点ID（数据库自增主键，正整数）",
                    required = true
            )
            @PathVariable("locationId") Integer locationId
    ) throws Exception {
        // 调用服务层查询地点详情
        SimulateLocation simulateLocation = simulateLocationService.getGameLocationById(locationId);
        return AppResponse.success(simulateLocation, "地点详情查询成功");
    }

    @Operation(
            summary = "根据地点编码查询详情",
            description = """
                根据地点编码查询地点的完整信息。
                注意：
                1. 地点编码为唯一标识，如CITY_CENTER、FOREST；
                2. 该接口需要验证请求头中的认证信息（如Authorization: Bearer Token）；
                3. 仅管理员账号可调用，普通用户无权限；
                4. 传入无效编码将返回空数据，不会抛出异常；
                """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/get/by-code")
    @SwaggerResponseConstants.Api500 // 服务器内部错误响应
    @SwaggerResponseConstants.Api400 // 请求参数错误（如编码为空）
    @SwaggerResponseConstants.Api401 // 未认证（无Token或Token无效）
    @SwaggerResponseConstants.Api403 // 权限不足（非管理员调用）
    @RequireRole(allowedRoles = {RoleEnum.ADMIN})
    public AppResponse<SimulateLocation> getGameLocationByCode(
            @Parameter(
                    description = "地点编码（唯一标识，如CITY_CENTER、FOREST）",
                    required = true
            )
            @RequestParam("locationCode") @NotBlank String locationCode
    ) throws Exception {
        // 调用服务层查询地点详情
        SimulateLocation simulateLocation = simulateLocationService.getGameLocationByCode(locationCode);
        return AppResponse.success(simulateLocation, "地点详情查询成功");
    }

    @Operation(
            summary = "查询所有游戏地点",
            description = """
                查询模拟游戏中所有已配置的地点信息，返回完整列表。
                注意：
                1. 返回结果按地点ID升序排列；
                2. 该接口需要验证请求头中的认证信息（如Authorization: Bearer Token）；
                3. 仅管理员账号可调用，普通用户无权限；
                4. 无配置地点时返回空列表，不会抛出异常；
                """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/list/all")
    @SwaggerResponseConstants.Api500 // 服务器内部错误响应
    @SwaggerResponseConstants.Api401 // 未认证（无Token或Token无效）
    @SwaggerResponseConstants.Api403 // 权限不足（非管理员调用）
    @RequireRole(allowedRoles = {RoleEnum.ADMIN})
    public AppResponse<List<SimulateLocation>> listAllGameLocations() throws Exception {
        // 调用服务层查询所有地点
        List<SimulateLocation> locationList = simulateLocationService.listAllGameLocations();
        return AppResponse.success(locationList, "所有地点查询成功，共查询到：" + locationList.size() + " 条记录");
    }

    @Operation(
            summary = "根据地点编码删除地点",
            description = """
                根据地点编码删除对应的游戏地点，删除后关联数据将级联删除（根据数据库外键配置）。
                注意：
                1. 地点编码为唯一标识，删除后不可恢复，请谨慎操作；
                2. 该接口需要验证请求头中的认证信息（如Authorization: Bearer Token）；
                3. 仅管理员账号可调用，普通用户无权限；
                4. 传入无效编码将返回0条受影响行数，不会抛出异常；
                """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @DeleteMapping("/remove/by-code")
    @SwaggerResponseConstants.Api500 // 服务器内部错误响应
    @SwaggerResponseConstants.Api400 // 请求参数错误（如编码为空）
    @SwaggerResponseConstants.Api401 // 未认证（无Token或Token无效）
    @SwaggerResponseConstants.Api403 // 权限不足（非管理员调用）
    @RequireRole(allowedRoles = {RoleEnum.ADMIN})
    @Audit
    public AppResponse<Void> removeGameLocationByCode(
            @Parameter(
                    description = "地点编码（唯一标识，如CITY_CENTER、FOREST）",
                    required = true
            )
            @RequestParam("locationCode") @NotBlank String locationCode
    ) throws Exception {
        // 调用服务层删除地点
        int affectedRows = simulateLocationService.removeGameLocationByCode(locationCode);
        return AppResponse.success(null, "游戏地点删除成功，受影响行数：" + affectedRows);
    }
}