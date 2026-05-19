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
import org.myfx.controls.aione.SimulationGame.common.EventEnum;
import org.myfx.controls.aione.SimulationGame.dto.SimulateEventAddDTO;
import org.myfx.controls.aione.SimulationGame.entity.SimulateEvent;
import org.myfx.controls.aione.SimulationGame.service.SimulateEventService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理员专属模拟游戏事件管理控制器（仅管理员可操作，需JWT认证）
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/simulation/event")
@Tag(name = "管理员模拟游戏事件管理接口", description = "模拟游戏事件的新增、查询、删除管理接口（仅管理员可调用）")
@CleanupThreadLocal
@AdminWebCors
@CheckJwt
public class SimulateAdminEventController {

    // 注入事件业务服务（final + RequiredArgsConstructor 无需@Resource/@Autowired）
    private final SimulateEventService simulateEventService;

    @Operation(
            summary = "查询所有事件枚举参考（数据库存入参考）",
            description = """
            查询所有预定义的事件枚举数据，返回编码和中文描述，供管理员参考存入数据库的格式。
            注意：
            1. 该接口仅提供参考，枚举数据与数据库无关联；
            2. 该接口需要验证请求头中的认证信息（如Authorization: Bearer Token）；
            3. 仅管理员账号可调用，普通用户无权限；
            """,
            security = @SecurityRequirement(name = "bearerAuth") // 和原有接口一致的JWT认证
    )
    @GetMapping("/enum/list/all")
    @SwaggerResponseConstants.Api500
    @SwaggerResponseConstants.Api401
    @SwaggerResponseConstants.Api403
    @RequireRole(allowedRoles = {RoleEnum.ADMIN})
    public AppResponse<List<EventEnum.EventEnumVO>> listAllEventEnum() throws Exception {
        // 直接调用枚举的静态方法，获取所有枚举数据
        List<EventEnum.EventEnumVO> eventEnumList = EventEnum.listAll();
        return AppResponse.success(eventEnumList, "事件枚举参考数据查询成功，共" + eventEnumList.size() + "条");
    }

    @Operation(
            summary = "新增游戏事件",
            description = """
            新增模拟游戏中的可触发事件，事件编码不可重复。
            注意：
            1. 事件编码为唯一标识，不可与已存在的编码重复；
            2. 该接口需要验证请求头中的认证信息（如Authorization: Bearer Token）；
            3. 仅管理员账号可调用，普通用户无权限；
            4. 事件描述可为空，填写后将在前端展示详细事件说明；
            """,
            security = @SecurityRequirement(name = "bearerAuth") // 和原有接口一致的JWT认证
    )
    @PostMapping("/add")
    @SwaggerResponseConstants.Api500 // 服务器内部错误响应
    @SwaggerResponseConstants.Api400 // 请求参数错误（如编码为空、重复）
    @SwaggerResponseConstants.Api401 // 未认证（无Token或Token无效）
    @SwaggerResponseConstants.Api403 // 权限不足（非管理员调用）
    @RequireRole(allowedRoles = {RoleEnum.ADMIN}) // 方法上注解：优先级更高
    // 修改点1：替换@RequestParam为@RequestBody + DTO，添加@Valid开启参数校验
    @Audit
    public AppResponse<Void> addGameEvent(
            @Parameter(
                    description = "新增事件请求参数",
                    required = true
            )
            @Valid @RequestBody SimulateEventAddDTO eventAddDTO // 接收DTO，@Valid开启校验（触发@NotBlank/@NotNull等）
    ) throws Exception {
        // 直接传递DTO给服务层，不再手动提取零散参数，简化逻辑
        int affectedRows = simulateEventService.addGameEvent(eventAddDTO);

        return AppResponse.success(null, "游戏事件新增成功，受影响行数：" + affectedRows);
    }

    @Operation(
            summary = "根据事件ID查询详情",
            description = """
                根据事件主键ID查询事件的完整信息。
                注意：
                1. 事件ID为数据库自增主键，仅支持正整数；
                2. 该接口需要验证请求头中的认证信息（如Authorization: Bearer Token）；
                3. 仅管理员账号可调用，普通用户无权限；
                4. 传入无效ID将返回空数据，不会抛出异常；
                """,
            security = @SecurityRequirement(name = "bearerAuth") // 和原有接口一致的JWT认证
    )
    @GetMapping("/get/{eventId}")
    @SwaggerResponseConstants.Api500 // 服务器内部错误响应
    @SwaggerResponseConstants.Api400 // 请求参数错误（如ID为负数、非数字）
    @SwaggerResponseConstants.Api401 // 未认证（无Token或Token无效）
    @SwaggerResponseConstants.Api403 // 权限不足（非管理员调用）
    @RequireRole(allowedRoles = {RoleEnum.ADMIN})
    public AppResponse<SimulateEvent> getGameEventById(
            @PathVariable @Parameter(
                    description = "事件ID（数据库自增主键，正整数）",
                    required = true
            ) Integer eventId
    ) throws Exception {
        // 调用服务层查询事件详情
        SimulateEvent simulateEvent = simulateEventService.getGameEventById(eventId);
        return AppResponse.success(simulateEvent, "事件详情查询成功");
    }

    @Operation(
            summary = "根据事件编码查询详情",
            description = """
                根据事件编码查询事件的完整信息。
                注意：
                1. 事件编码为唯一标识，如TRADING、FIGHT、QUEST_ACCEPT；
                2. 该接口需要验证请求头中的认证信息（如Authorization: Bearer Token）；
                3. 仅管理员账号可调用，普通用户无权限；
                4. 传入无效编码将返回空数据，不会抛出异常；
                """,
            security = @SecurityRequirement(name = "bearerAuth") // 和原有接口一致的JWT认证
    )
    @GetMapping("/get/by-code")
    @SwaggerResponseConstants.Api500 // 服务器内部错误响应
    @SwaggerResponseConstants.Api400 // 请求参数错误（如编码为空）
    @SwaggerResponseConstants.Api401 // 未认证（无Token或Token无效）
    @SwaggerResponseConstants.Api403 // 权限不足（非管理员调用）
    @RequireRole(allowedRoles = {RoleEnum.ADMIN})
    public AppResponse<SimulateEvent> getGameEventByCode(
            @Parameter(
                    description = "事件编码（唯一标识，如TRADING、FIGHT、QUEST_ACCEPT）",
                    required = true
            )
            @RequestParam("eventCode") @NotBlank String eventCode
    ) throws Exception {
        // 调用服务层查询事件详情
        SimulateEvent simulateEvent = simulateEventService.getGameEventByCode(eventCode);
        return AppResponse.success(simulateEvent, "事件详情查询成功");
    }

    @Operation(
            summary = "查询所有游戏事件",
            description = """
                查询模拟游戏中所有已配置的事件信息，返回完整列表。
                注意：
                1. 返回结果按事件ID升序排列；
                2. 该接口需要验证请求头中的认证信息（如Authorization: Bearer Token）；
                3. 仅管理员账号可调用，普通用户无权限；
                4. 无配置事件时返回空列表，不会抛出异常；
                """,
            security = @SecurityRequirement(name = "bearerAuth") // 和原有接口一致的JWT认证
    )
    @GetMapping("/list/all")
    @SwaggerResponseConstants.Api500 // 服务器内部错误响应
    @SwaggerResponseConstants.Api401 // 未认证（无Token或Token无效）
    @SwaggerResponseConstants.Api403 // 权限不足（非管理员调用）
    @RequireRole(allowedRoles = {RoleEnum.ADMIN})
    public AppResponse<List<SimulateEvent>> listAllGameEvents() throws Exception {
        // 调用服务层查询所有事件
        List<SimulateEvent> eventList = simulateEventService.listAllGameEvents();
        return AppResponse.success(eventList, "所有事件查询成功，共查询到：" + eventList.size() + " 条记录");
    }

    @Operation(
            summary = "根据事件编码删除事件",
            description = """
                根据事件编码删除对应的游戏事件，删除后关联数据将级联删除（根据数据库外键配置）。
                注意：
                1. 事件编码为唯一标识，删除后不可恢复，请谨慎操作；
                2. 该接口需要验证请求头中的认证信息（如Authorization: Bearer Token）；
                3. 仅管理员账号可调用，普通用户无权限；
                4. 传入无效编码将返回0条受影响行数，不会抛出异常；
                """,
            security = @SecurityRequirement(name = "bearerAuth") // 和原有接口一致的JWT认证
    )
    @DeleteMapping("/remove/by-code")
    @SwaggerResponseConstants.Api500 // 服务器内部错误响应
    @SwaggerResponseConstants.Api400 // 请求参数错误（如编码为空）
    @SwaggerResponseConstants.Api401 // 未认证（无Token或Token无效）
    @SwaggerResponseConstants.Api403 // 权限不足（非管理员调用）
    @RequireRole(allowedRoles = {RoleEnum.ADMIN})
    @Audit
    public AppResponse<Void> removeGameEventByCode(
            @Parameter(
                    description = "事件编码（唯一标识，如TRADING、FIGHT、QUEST_ACCEPT）",
                    required = true
            )
            @RequestParam("eventCode") @NotBlank String eventCode
    ) throws Exception {
        // 调用服务层删除事件
        int affectedRows = simulateEventService.removeGameEventByCode(eventCode);
        return AppResponse.success(null, "游戏事件删除成功，受影响行数：" + affectedRows);
    }
}