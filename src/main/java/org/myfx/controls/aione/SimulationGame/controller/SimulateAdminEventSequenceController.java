package org.myfx.controls.aione.SimulationGame.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.myfx.controls.aione.ServiceCommon.AppResponse;
import org.myfx.controls.aione.ServiceCommon.SwaggerResponseConstants;
import org.myfx.controls.aione.ServiceCommon.annotation.*;
import org.myfx.controls.aione.ServiceCommon.serviceEnum.RoleEnum;
import org.myfx.controls.aione.SimulationGame.dto.SimulateEventSequenceAddDTO;
import org.myfx.controls.aione.SimulationGame.entity.SimulateEventSequence;
import org.myfx.controls.aione.SimulationGame.service.SimulateEventSequenceService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理员专属模拟游戏事件序列规则管理控制器（仅管理员可操作，需JWT认证）
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/simulation/event-sequence")
@Tag(name = "管理员模拟游戏事件序列规则管理接口", description = "模拟游戏事件执行序列规则的新增、查询、删除管理接口（仅管理员可调用）")
@CleanupThreadLocal
@AdminWebCors
@CheckJwt
public class SimulateAdminEventSequenceController {

    // 注入序列规则业务服务（final + RequiredArgsConstructor 无需@Autowired）
    private final SimulateEventSequenceService simulateEventSequenceService;

    @Operation(
            summary = "新增事件执行序列规则",
            description = """
            新增模拟游戏的事件执行序列规则（版本化每日事件次序）。
            注意：
            1. 同一版本+地点+执行次序的组合不可重复；
            2. 地点编码/事件编码需提前在数据库中存在（无外键约束，需手动确认）；
            3. 该接口需要验证请求头中的认证信息（如Authorization: Bearer Token）；
            4. 仅管理员账号可调用，普通用户无权限；
            5. 默认开始时间格式为HH:mm:ss（如08:00:00）；
            """,
            security = @SecurityRequirement(name = "bearerAuth") // 与原有接口一致的JWT认证
    )
    @PostMapping("/add")
    @SwaggerResponseConstants.Api500 // 服务器内部错误响应
    @SwaggerResponseConstants.Api400 // 请求参数错误（如字段为空、重复）
    @SwaggerResponseConstants.Api401 // 未认证（无Token或Token无效）
    @SwaggerResponseConstants.Api403 // 权限不足（非管理员调用）
    @RequireRole(allowedRoles = {RoleEnum.ADMIN}) // 仅管理员可调用
    @Audit
    public AppResponse<Void> addEventSequence(
            @Parameter(
                    description = "新增事件序列规则请求参数",
                    required = true
            )
            @Valid @RequestBody SimulateEventSequenceAddDTO addDTO // @Valid触发DTO参数校验
    ) throws Exception {
        // 调用服务层新增方法
        boolean isSuccess = simulateEventSequenceService.saveEventSequence(addDTO);

        if (isSuccess) {
            return AppResponse.success(null, "事件执行序列规则新增成功");
        } else {
            return AppResponse.error(500,"查询指定版本号的事件失败",null);
        }
    }

    @Operation(
            summary = "按版本号查询事件执行序列规则",
            description = """
            根据版本号查询该版本下的所有事件执行序列规则（按执行次序升序排列）。
            注意：
            1. 版本号为非负整数（如1、2）；
            2. 该接口需要验证请求头中的认证信息（如Authorization: Bearer Token）；
            3. 仅管理员账号可调用，普通用户无权限；
            4. 无数据时返回空列表，不会返回null；
            """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/list/by-version")
    @SwaggerResponseConstants.Api500
    @SwaggerResponseConstants.Api400
    @SwaggerResponseConstants.Api401
    @SwaggerResponseConstants.Api403
    @RequireRole(allowedRoles = {RoleEnum.ADMIN})
    public AppResponse<List<SimulateEventSequence>> listEventSequenceByVersion(
            @Parameter(
                    description = "版本号（如1、2）",
                    required = true,
                    example = "1"
            )
            @RequestParam Integer version
    ) throws Exception {
        List<SimulateEventSequence> sequenceList = simulateEventSequenceService.listEventSequenceByVersion(version);
        return AppResponse.success(sequenceList, "按版本号查询事件序列规则成功，共" + sequenceList.size() + "条");
    }

    /**
     * 查询全部事件执行序列规则（适配前端getEventSequenceListAllApi）
     */
    @Operation(
            summary = "查询全部事件执行序列规则",
            description = """
            查询系统中所有的事件执行序列规则（按版本号+执行次序升序排列）。
            注意：
            1. 该接口需要验证请求头中的认证信息（Authorization: Bearer Token）；
            2. 仅管理员账号可调用，普通用户无权限；
            3. 无数据时返回空列表，不会返回null；
            """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/list/all")
    @SwaggerResponseConstants.Api500
    @SwaggerResponseConstants.Api401
    @SwaggerResponseConstants.Api403
    @RequireRole(allowedRoles = {RoleEnum.ADMIN})
    public AppResponse<List<SimulateEventSequence>> listAllEventSequence() throws Exception {
        List<SimulateEventSequence> sequenceList = simulateEventSequenceService.listAllEventSequence();
        return AppResponse.success(
                sequenceList,
                "查询全部事件执行序列规则成功，共" + sequenceList.size() + "条"
        );
    }

    @Operation(
            summary = "按版本号删除事件执行序列规则",
            description = """
            根据版本号删除该版本下的所有事件执行序列规则（批量删除）。
            注意：
            1. 删除后不可恢复，请谨慎操作；
            2. 该接口需要验证请求头中的认证信息（如Authorization: Bearer Token）；
            3. 仅管理员账号可调用，普通用户无权限；
            4. 无对应版本数据时，返回删除失败提示；
            """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @DeleteMapping("/remove/by-version")
    @SwaggerResponseConstants.Api500
    @SwaggerResponseConstants.Api400
    @SwaggerResponseConstants.Api401
    @SwaggerResponseConstants.Api403
    @RequireRole(allowedRoles = {RoleEnum.ADMIN})
    @Audit
    public AppResponse<Void> removeEventSequenceByVersion(
            @Parameter(
                    description = "版本号（如1、2）",
                    required = true,
                    example = "1"
            )
            @RequestParam Integer version
    ) throws Exception {
        boolean isSuccess = simulateEventSequenceService.removeEventSequenceByVersion(version);

        if (isSuccess) {
            return AppResponse.success(null, "版本" + version + "的事件序列规则删除成功");
        } else {
            return AppResponse.error(500,"删除指定版本号的事件失败",null);
        }
    }
}