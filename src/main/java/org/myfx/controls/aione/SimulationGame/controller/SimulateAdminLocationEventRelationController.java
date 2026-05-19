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
import org.myfx.controls.aione.SimulationGame.dto.SimulateLocationEventRelationAddDTO;
import org.myfx.controls.aione.SimulationGame.entity.SimulateLocationEventRelation;
import org.myfx.controls.aione.SimulationGame.service.SimulateLocationEventRelationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理员专属模拟游戏地点与事件关联管理控制器（仅管理员可操作，需JWT认证）
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/simulation/location-event-relation")
@Tag(
        name = "管理员模拟游戏地点与事件关联管理接口",
        description = "模拟游戏地点与事件关联的新增、查询、删除管理接口（仅管理员可调用）"
)
@CleanupThreadLocal
@AdminWebCors
@CheckJwt // 全局JWT认证（所有接口都需要Token）
public class SimulateAdminLocationEventRelationController {

    // 注入关联业务层接口（Lombok @RequiredArgsConstructor 自动生成构造器注入）
    private final SimulateLocationEventRelationService simulateLocationEventRelationService;

    /**
     * 新增地点与事件的关联关系
     */
    @Operation(
            summary = "新增地点与事件关联",
            description = """
            新增模拟游戏中地点与事件的关联关系，地点编码+事件编码组合不可重复。
            注意：
            1. 地点编码和事件编码需为已存在的有效编码；
            2. 地点编码+事件编码组合为唯一标识，不可重复关联；
            3. 事件持续时长为该地点下该事件的默认持续秒数，最小值为1秒；
            4. 该接口需要验证请求头中的认证信息（如Authorization: Bearer Token）；
            5. 仅管理员账号可调用，普通用户无权限；
            """,
            security = @SecurityRequirement(name = "bearerAuth") // JWT认证标识
    )
    @PostMapping("/add")
    @SwaggerResponseConstants.Api500 // 服务器内部错误
    @SwaggerResponseConstants.Api400 // 请求参数错误（编码为空/重复关联/时长非法）
    @SwaggerResponseConstants.Api401 // 未认证（Token无效/无Token）
    @SwaggerResponseConstants.Api403 // 权限不足（非管理员）
    @RequireRole(allowedRoles = {RoleEnum.ADMIN}) // 仅管理员可调用
    @Audit
    public AppResponse<Void> addLocationEventRelation(
            @Parameter(
                    description = "新增关联请求参数（包含地点编码、事件编码、事件持续时长）",
                    required = true
            )
            @Valid @RequestBody SimulateLocationEventRelationAddDTO relationAddDTO // DTO参数+JSR380校验
    ) throws Exception {
        // 核心修改：直接传入DTO给Service，无需拆字段
        int affectedRows = simulateLocationEventRelationService.addRelation(relationAddDTO);
        return AppResponse.success(null, "地点与事件关联新增成功，受影响行数：" + affectedRows);
    }

    /**
     * 根据地点编码查询所有关联的事件
     */
    @Operation(
            summary = "根据地点编码查询关联事件",
            description = """
            根据地点编码查询该地点下所有关联的事件列表。
            注意：
            1. 地点编码为唯一标识，如HOME、SHOP、DUNGEON；
            2. 该接口需要验证请求头中的认证信息（如Authorization: Bearer Token）；
            3. 仅管理员账号可调用，普通用户无权限；
            4. 传入无效编码将返回空列表，不会抛出异常；
            """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/get/by-location-code")
    @SwaggerResponseConstants.Api500
    @SwaggerResponseConstants.Api400 // 地点编码为空
    @SwaggerResponseConstants.Api401
    @SwaggerResponseConstants.Api403
    @RequireRole(allowedRoles = {RoleEnum.ADMIN})
    public AppResponse<List<SimulateLocationEventRelation>> getRelationsByLocationCode(
            @Parameter(
                    description = "地点编码（唯一标识，如HOME、SHOP、DUNGEON）",
                    required = true
            )
            @RequestParam("locationCode") @NotBlank(message = "地点编码不能为空") String locationCode
    ) throws Exception {
        List<SimulateLocationEventRelation> relations = simulateLocationEventRelationService.getRelationsByLocationCode(locationCode);
        return AppResponse.success(relations, "根据地点编码查询关联事件成功，共查询到：" + relations.size() + " 条");
    }

    /**
     * 根据事件编码查询所有关联的地点
     */
    @Operation(
            summary = "根据事件编码查询关联地点",
            description = """
            根据事件编码查询该事件关联的所有地点列表。
            注意：
            1. 事件编码为唯一标识，如TRADING、FIGHT、QUEST_ACCEPT；
            2. 该接口需要验证请求头中的认证信息（如Authorization: Bearer Token）；
            3. 仅管理员账号可调用，普通用户无权限；
            4. 传入无效编码将返回空列表，不会抛出异常；
            """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/get/by-event-code")
    @SwaggerResponseConstants.Api500
    @SwaggerResponseConstants.Api400 // 事件编码为空
    @SwaggerResponseConstants.Api401
    @SwaggerResponseConstants.Api403
    @RequireRole(allowedRoles = {RoleEnum.ADMIN})
    public AppResponse<List<SimulateLocationEventRelation>> getRelationsByEventCode(
            @Parameter(
                    description = "事件编码（唯一标识，如TRADING、FIGHT、QUEST_ACCEPT）",
                    required = true
            )
            @RequestParam("eventCode") @NotBlank(message = "事件编码不能为空") String eventCode
    ) throws Exception {
        List<SimulateLocationEventRelation> relations = simulateLocationEventRelationService.getRelationsByEventCode(eventCode);
        return AppResponse.success(relations, "根据事件编码查询关联地点成功，共查询到：" + relations.size() + " 条");
    }

    /**
     * 根据地点编码+事件编码精准查询关联关系
     */
    @Operation(
            summary = "精准查询地点与事件关联关系",
            description = """
            根据地点编码+事件编码组合精准查询关联关系详情。
            注意：
            1. 地点编码和事件编码需同时传入，缺一不可；
            2. 该接口需要验证请求头中的认证信息（如Authorization: Bearer Token）；
            3. 仅管理员账号可调用，普通用户无权限；
            4. 无匹配关联时返回null，不会抛出异常；
            """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/get/by-two-code")
    @SwaggerResponseConstants.Api500
    @SwaggerResponseConstants.Api400 // 编码为空
    @SwaggerResponseConstants.Api401
    @SwaggerResponseConstants.Api403
    @RequireRole(allowedRoles = {RoleEnum.ADMIN})
    public AppResponse<SimulateLocationEventRelation> getRelationByTwoCode(
            @Parameter(
                    description = "地点编码（唯一标识，如HOME、SHOP、DUNGEON）",
                    required = true
            )
            @RequestParam("locationCode") @NotBlank(message = "地点编码不能为空") String locationCode,

            @Parameter(
                    description = "事件编码（唯一标识，如TRADING、FIGHT、QUEST_ACCEPT）",
                    required = true
            )
            @RequestParam("eventCode") @NotBlank(message = "事件编码不能为空") String eventCode
    ) throws Exception {
        SimulateLocationEventRelation relation = simulateLocationEventRelationService.getRelationByTwoCode(locationCode, eventCode);
        return AppResponse.success(relation, "精准查询地点与事件关联关系成功");
    }

    /**
     * 查询所有地点与事件的关联关系
     */
    @Operation(
            summary = "查询所有地点与事件关联关系",
            description = """
            查询系统中所有地点与事件的关联关系列表。
            注意：
            1. 该接口无需传入参数，返回全量关联数据；
            2. 该接口需要验证请求头中的认证信息（如Authorization: Bearer Token）；
            3. 仅管理员账号可调用，普通用户无权限；
            4. 无关联数据时返回空列表，不会抛出异常；
            """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/get/all")
    @SwaggerResponseConstants.Api500
    @SwaggerResponseConstants.Api401
    @SwaggerResponseConstants.Api403
    @RequireRole(allowedRoles = {RoleEnum.ADMIN})
    public AppResponse<List<SimulateLocationEventRelation>> getAllRelations() throws Exception {
        List<SimulateLocationEventRelation> relations = simulateLocationEventRelationService.getAllRelations();
        return AppResponse.success(relations, "查询所有地点与事件关联关系成功，共查询到：" + relations.size() + " 条");
    }

    /**
     * 根据地点编码+事件编码精准删除关联关系
     */
    @Operation(
            summary = "删除地点与事件关联关系",
            description = """
            根据地点编码+事件编码组合精准删除关联关系。
            注意：
            1. 地点编码和事件编码需同时传入，缺一不可；
            2. 该接口需要验证请求头中的认证信息（如Authorization: Bearer Token）；
            3. 仅管理员账号可调用，普通用户无权限；
            4. 无匹配关联时返回受影响行数0，不会抛出异常；
            """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @DeleteMapping("/delete/by-two-code")
    @SwaggerResponseConstants.Api500
    @SwaggerResponseConstants.Api400 // 编码为空
    @SwaggerResponseConstants.Api401
    @SwaggerResponseConstants.Api403
    @RequireRole(allowedRoles = {RoleEnum.ADMIN})
    @Audit
    public AppResponse<Void> removeRelationByTwoCode(
            @Parameter(
                    description = "地点编码（唯一标识，如HOME、SHOP、DUNGEON）",
                    required = true
            )
            @RequestParam("locationCode") @NotBlank(message = "地点编码不能为空") String locationCode,

            @Parameter(
                    description = "事件编码（唯一标识，如TRADING、FIGHT、QUEST_ACCEPT）",
                    required = true
            )
            @RequestParam("eventCode") @NotBlank(message = "事件编码不能为空") String eventCode
    ) throws Exception {
        int affectedRows = simulateLocationEventRelationService.removeRelationByTwoCode(locationCode, eventCode);
        return AppResponse.success(null, "删除地点与事件关联关系成功，受影响行数：" + affectedRows);
    }
}