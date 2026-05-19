package org.myfx.controls.aione.AiService.controller.user_behavior;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.myfx.controls.aione.AiService.common.user_behavior_db.BehaviorEnum;
import org.myfx.controls.aione.AiService.entity.user_behavior_db.BaseUserBehavior;
import org.myfx.controls.aione.AiService.service.base.status.user_behavior_db.BaseUserBehaviorService;
import org.myfx.controls.aione.AiService.vo.BaseUserBehaviorVO;
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
 * 管理员专属用户行为管理控制器（仅管理员可操作，需JWT认证）
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/behavior")
@Tag(name = "管理员用户行为管理接口", description = "用户行为的新增/查询/更新/删除管理接口（仅管理员可调用）")
@CleanupThreadLocal
public class AdminUserBehaviorController {

    private final BaseUserBehaviorService baseUserBehaviorService;

    /**
     * 管理员根据行为ID查询行为信息
     */
    @Operation(
            summary = "管理员根据ID查询用户行为",
            description = """
                管理员根据行为ID查询系统用户行为字典数据：
                1. 该接口需要验证请求头中的认证信息（Authorization: Bearer Token）；
                2. 仅系统管理员可调用，无频率限制（查询操作无压库风险）；
                3. 行为ID必须为正整数，否则返回参数不合法错误；
                4. 无匹配ID时返回null，前端需做空值处理。
                """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/get/{behaviorId}")
    @CheckJwt
    @RequireRole(allowedRoles = {RoleEnum.ADMIN})
    @SwaggerResponseConstants.Api500
    @SwaggerResponseConstants.Api401
    @SwaggerResponseConstants.Api403
    public AppResponse<BaseUserBehavior> getBehaviorById(
            @Parameter(description = "行为ID（正整数）", required = true)
            @PathVariable Integer behaviorId) {
        BaseUserBehavior behavior = baseUserBehaviorService.getBehaviorById(behaviorId);
        return AppResponse.success(behavior, "用户行为查询请求已处理");
    }

    /**
     * 管理员根据行为编码查询行为信息
     */
    @Operation(
            summary = "管理员根据编码查询用户行为",
            description = """
                管理员根据行为编码查询系统用户行为字典数据：
                1. 该接口需要验证请求头中的认证信息（Authorization: Bearer Token）；
                2. 仅系统管理员可调用，无频率限制（查询操作无压库风险）；
                3. 行为编码不能为空/空白，否则返回参数不合法错误；
                4. 无匹配编码时返回null，前端需做空值处理。
                """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/get/code/{behaviorCode}")
    @CheckJwt
    @RequireRole(allowedRoles = {RoleEnum.ADMIN})
    @SwaggerResponseConstants.Api500
    @SwaggerResponseConstants.Api401
    @SwaggerResponseConstants.Api403
    public AppResponse<BaseUserBehavior> getBehaviorByCode(
            @Parameter(description = "行为编码（如：CHAT_SEND_MSG=发送AI聊天消息）", required = true)
            @PathVariable BehaviorEnum behaviorCode) {
        BaseUserBehavior behavior = baseUserBehaviorService.getBehaviorByCode(behaviorCode);
        return AppResponse.success(behavior, "用户行为查询请求已处理");
    }

    /**
     * 管理员根据行为编码查询行为信息（返回VO，含枚举+描述）
     */
    @Operation(
            summary = "管理员根据编码查询用户行为（返回VO）",
            description = """
            管理员根据行为编码查询系统用户行为字典数据（返回VO，自动包含枚举描述）：
            1. 该接口需要验证请求头中的认证信息（Authorization: Bearer Token）；
            2. 仅系统管理员可调用，无频率限制（查询操作无压库风险）；
            3. 行为编码不能为空/空白，否则返回参数不合法错误；
            4. 无匹配编码时返回null，前端需做空值处理；
            5. 返回VO包含行为ID、枚举类型、枚举描述（自动填充）。
            """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/get/code/vo/{behaviorCode}")
    @CheckJwt
    @RequireRole(allowedRoles = {RoleEnum.ADMIN})
    @SwaggerResponseConstants.Api500
    @SwaggerResponseConstants.Api401
    @SwaggerResponseConstants.Api403
    public AppResponse<BaseUserBehaviorVO> getBehaviorByCodeVO(
            @Parameter(description = "行为枚举（如：CHAT_SEND_MSG=发送AI聊天消息）", required = true)
            @PathVariable BehaviorEnum behaviorCode) {
        BaseUserBehavior behavior = baseUserBehaviorService.getBehaviorByCode(behaviorCode);
        BaseUserBehaviorVO behaviorVO = new BaseUserBehaviorVO();
        if (behavior != null) {
            BeanUtils.copyProperties(behavior, behaviorVO, "behaviorEnum");
            behaviorVO.setBehaviorEnum(behavior.getBehaviorCode());
        }
        return AppResponse.success(behaviorVO, "用户行为VO查询请求已处理");
    }

    /**
     * 管理员查询所有用户行为列表
     */
    @Operation(
            summary = "管理员查询所有用户行为",
            description = """
                管理员查询系统所有用户行为字典数据：
                1. 该接口需要验证请求头中的认证信息（Authorization: Bearer Token）；
                2. 仅系统管理员可调用，无频率限制（查询操作无压库风险）；
                3. 无行为数据时返回空列表，避免前端空指针；
                4. 返回结果按行为ID升序排列（底层可自行扩展排序）。
                """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/listAll")
    @CheckJwt
    @RequireRole(allowedRoles = {RoleEnum.ADMIN})
    @SwaggerResponseConstants.Api500
    @SwaggerResponseConstants.Api401
    @SwaggerResponseConstants.Api403
    public AppResponse<List<BaseUserBehavior>> listAllBehaviors() {
        List<BaseUserBehavior> behaviorList = baseUserBehaviorService.listAllBehaviors();
        return AppResponse.success(behaviorList, "所有用户行为查询成功，共" + behaviorList.size() + "条数据");
    }

    // ========== 新增扩展接口（适配score字段+完整CRUD） ==========
    /**
     * 管理员新增用户行为（指定行为名称）
     */
    @Operation(
            summary = "管理员新增用户行为（指定名称）",
            description = """
                管理员新增系统用户行为字典数据（指定行为中文名称）：
                1. 该接口需要验证请求头中的认证信息（Authorization: Bearer Token）；
                2. 仅系统管理员可调用，60秒内最多调用100次（避免重复新增）；
                3. 行为编码、行为名称不能为空，否则返回参数不合法错误；
                4. 新增成功后返回操作结果，重复编码会返回错误。
                """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/add")
    @CheckJwt
    @RequireRole(allowedRoles = {RoleEnum.ADMIN})
    @RateLimit(seconds = 60, maxCount = 100)
    @SwaggerResponseConstants.Api500
    @SwaggerResponseConstants.Api401
    @SwaggerResponseConstants.Api403
    public AppResponse<String> addBehavior(
            @Parameter(description = "行为编码（如：CHAT_SEND_MSG、CHAT_LIKE）", required = true)
            @RequestParam BehaviorEnum behaviorCode,
            @Parameter(description = "行为中文名称", required = true, example = "发送聊天消息")
            @RequestParam String behaviorName) {
        baseUserBehaviorService.addBehavior(behaviorCode, behaviorName);
        return AppResponse.success(null, "用户行为新增成功：" + behaviorCode + "（名称=" + behaviorName + "）");
    }

    /**
     * 管理员根据行为编码更新行为名称
     */
    @Operation(
            summary = "管理员根据编码更新行为名称",
            description = """
                管理员更新系统用户行为字典的中文名称：
                1. 该接口需要验证请求头中的认证信息（Authorization: Bearer Token）；
                2. 仅系统管理员可调用，60秒内最多调用50次（避免频繁修改）；
                3. 行为编码、新名称不能为空，否则返回参数不合法错误；
                4. 行为编码不存在时返回操作失败。
                """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PutMapping("/update/name/code")
    @CheckJwt
    @RequireRole(allowedRoles = {RoleEnum.ADMIN})
    @RateLimit(seconds = 60, maxCount = 50)
    @SwaggerResponseConstants.Api500
    @SwaggerResponseConstants.Api401
    @SwaggerResponseConstants.Api403
    public AppResponse<String> updateBehaviorNameByCode(
            @Parameter(description = "行为编码（如：CHAT_SEND_MSG、CHAT_LIKE）", required = true)
            @RequestParam BehaviorEnum behaviorCode,
            @Parameter(description = "新的行为中文名称", required = true, example = "点赞AI回复")
            @RequestParam String behaviorName) {
        baseUserBehaviorService.updateBehaviorNameByCode(behaviorCode, behaviorName);
        return AppResponse.success(null, "行为名称更新成功：" + behaviorCode + "（新名称=" + behaviorName + "）");
    }

    /**
     * 管理员根据行为ID动态更新（编码/名称）
     */
    @Operation(
            summary = "管理员根据ID更新行为编码/名称",
            description = """
                管理员根据行为ID动态更新编码或名称（有值则更新）：
                1. 该接口需要验证请求头中的认证信息（Authorization: Bearer Token）；
                2. 仅系统管理员可调用，60秒内最多调用50次；
                3. 行为ID必须为正整数，编码/名称为可选参数；
                4. 行为ID不存在时返回操作失败。
                """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PutMapping("/update/id")
    @CheckJwt
    @RequireRole(allowedRoles = {RoleEnum.ADMIN})
    @RateLimit(seconds = 60, maxCount = 50)
    @SwaggerResponseConstants.Api500
    @SwaggerResponseConstants.Api401
    @SwaggerResponseConstants.Api403
    public AppResponse<String> updateBehaviorById(
            @Parameter(description = "行为ID（正整数）", required = true)
            @RequestParam Integer behaviorId,
            @Parameter(description = "新的行为编码（可选）", required = false)
            @RequestParam(required = false) BehaviorEnum behaviorCode,
            @Parameter(description = "新的行为名称（可选）", required = false, example = "取消点赞")
            @RequestParam(required = false) String behaviorName) {
        BaseUserBehavior behavior = new BaseUserBehavior();
        behavior.setBehaviorId(behaviorId);
        behavior.setBehaviorCode(behaviorCode);
        behavior.setBehaviorName(behaviorName);
        // 彻底移除score字段，纯行为更新
        baseUserBehaviorService.updateBehaviorById(behavior);
        return AppResponse.success(null, "行为信息更新成功：ID=" + behaviorId);
    }

    /**
     * 管理员根据行为编码删除行为
     */
    @Operation(
            summary = "管理员根据编码删除用户行为",
            description = """
                管理员根据行为编码物理删除系统用户行为字典数据：
                1. 该接口需要验证请求头中的认证信息（Authorization: Bearer Token）；
                2. 仅系统管理员可调用，60秒内最多调用20次（避免误删）；
                3. 行为编码不能为空，否则返回参数不合法错误；
                4. 行为编码不存在时返回操作失败，前端需处理。
                """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @DeleteMapping("/delete/code/{behaviorCode}")
    @CheckJwt
    @RequireRole(allowedRoles = {RoleEnum.ADMIN})
    @RateLimit(seconds = 60, maxCount = 20)
    @SwaggerResponseConstants.Api500
    @SwaggerResponseConstants.Api401
    @SwaggerResponseConstants.Api403
    public AppResponse<String> deleteBehaviorByCode(
            @Parameter(description = "行为编码（如：CHAT_SEND_MSG、CHAT_LIKE）", required = true)
            @PathVariable BehaviorEnum behaviorCode) {
        baseUserBehaviorService.deleteBehaviorByCode(behaviorCode);
        return AppResponse.success(null, "行为删除请求已处理：" + behaviorCode);
    }

    /**
     * 管理员根据行为ID删除行为
     */
    @Operation(
            summary = "管理员根据ID删除用户行为",
            description = """
                管理员根据行为ID物理删除系统用户行为字典数据：
                1. 该接口需要验证请求头中的认证信息（Authorization: Bearer Token）；
                2. 仅系统管理员可调用，60秒内最多调用20次（避免误删）；
                3. 行为ID必须为正整数，否则返回参数不合法错误；
                4. 行为ID不存在时返回操作失败，前端需处理。
                """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @DeleteMapping("/delete/id/{behaviorId}")
    @CheckJwt
    @RequireRole(allowedRoles = {RoleEnum.ADMIN})
    @RateLimit(seconds = 60, maxCount = 20)
    @SwaggerResponseConstants.Api500
    @SwaggerResponseConstants.Api401
    @SwaggerResponseConstants.Api403
    public AppResponse<String> deleteBehaviorById(
            @Parameter(description = "行为ID（正整数）", required = true)
            @PathVariable Integer behaviorId) {
        baseUserBehaviorService.deleteBehaviorById(behaviorId);
        return AppResponse.success(null, "行为删除请求已处理：ID=" + behaviorId);
    }
}