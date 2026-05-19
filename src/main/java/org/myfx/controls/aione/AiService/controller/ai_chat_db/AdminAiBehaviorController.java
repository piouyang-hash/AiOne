package org.myfx.controls.aione.AiService.controller.ai_chat_db;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.myfx.controls.aione.AiService.common.ai_chat_db.AiBehaviorEnum;
import org.myfx.controls.aione.AiService.entity.ai_chat_db.BaseAiBehavior;
import org.myfx.controls.aione.AiService.service.base.status.ai_behavior_db.BaseAiBehaviorService;
import org.myfx.controls.aione.AiService.vo.BaseAiBehaviorVO;
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
 * 管理员专属AI行为管理控制器（仅管理员可操作，需JWT认证）
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/ai-behavior")
@Tag(name = "管理员AI行为管理接口", description = "AI行为的新增/查询/更新/删除管理接口（仅管理员可调用）")
@CleanupThreadLocal
public class AdminAiBehaviorController {

    private final BaseAiBehaviorService baseAiBehaviorService;

    /**
     * 管理员根据AI行为ID查询行为信息
     */
    @Operation(
            summary = "管理员根据ID查询AI行为",
            description = """
                管理员根据AI行为ID查询系统AI行为字典数据：
                1. 该接口需要验证请求头中的认证信息（Authorization: Bearer Token）；
                2. 仅系统管理员可调用，无频率限制（查询操作无压库风险）；
                3. AI行为ID必须为正整数，否则返回参数不合法错误；
                4. 无匹配ID时返回null，前端需做空值处理。
                """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/get/{aiBehaviorId}")
    @CheckJwt
    @RequireRole(allowedRoles = {RoleEnum.ADMIN})
    @SwaggerResponseConstants.Api500
    @SwaggerResponseConstants.Api401
    @SwaggerResponseConstants.Api403
    public AppResponse<BaseAiBehavior> getAiBehaviorById(
            @Parameter(description = "AI行为ID（正整数）", required = true)
            @PathVariable Integer aiBehaviorId) {
        BaseAiBehavior aiBehavior = baseAiBehaviorService.getAiBehaviorById(aiBehaviorId);
        return AppResponse.success(aiBehavior, "AI行为查询请求已处理");
    }

    /**
     * 管理员根据AI行为编码查询行为信息
     */
    @Operation(
            summary = "管理员根据编码查询AI行为",
            description = """
                管理员根据AI行为编码查询系统AI行为字典数据：
                1. 该接口需要验证请求头中的认证信息（Authorization: Bearer Token）；
                2. 仅系统管理员可调用，无频率限制（查询操作无压库风险）；
                3. AI行为编码不能为空/空白，否则返回参数不合法错误；
                4. 无匹配编码时返回null，前端需做空值处理。
                """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/get/code/{aiBehaviorCode}")
    @CheckJwt
    @RequireRole(allowedRoles = {RoleEnum.ADMIN})
    @SwaggerResponseConstants.Api500
    @SwaggerResponseConstants.Api401
    @SwaggerResponseConstants.Api403
    public AppResponse<BaseAiBehavior> getAiBehaviorByCode(
            @Parameter(description = "AI行为编码（如：WAIT=等待、SLEEP=睡眠）", required = true)
            @PathVariable AiBehaviorEnum aiBehaviorCode) {
        BaseAiBehavior aiBehavior = baseAiBehaviorService.getAiBehaviorByCode(aiBehaviorCode);
        return AppResponse.success(aiBehavior, "AI行为查询请求已处理");
    }

    /**
     * 管理员根据AI行为编码查询行为信息（返回VO，含枚举+描述）
     */
    @Operation(
            summary = "管理员根据编码查询AI行为（返回VO）",
            description = """
            管理员根据AI行为编码查询系统AI行为字典数据（返回VO，自动包含枚举描述）：
            1. 该接口需要验证请求头中的认证信息（Authorization: Bearer Token）；
            2. 仅系统管理员可调用，无频率限制（查询操作无压库风险）；
            3. AI行为编码不能为空/空白，否则返回参数不合法错误；
            4. 无匹配编码时返回null，前端需做空值处理；
            5. 返回VO包含AI行为ID、枚举类型、枚举描述（自动填充）。
            """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/get/code/vo/{aiBehaviorCode}")
    @CheckJwt
    @RequireRole(allowedRoles = {RoleEnum.ADMIN})
    @SwaggerResponseConstants.Api500
    @SwaggerResponseConstants.Api401
    @SwaggerResponseConstants.Api403
    public AppResponse<BaseAiBehaviorVO> getAiBehaviorByCodeVO(
            @Parameter(description = "AI行为枚举（如：WAIT=等待、SLEEP=睡眠）", required = true)
            @PathVariable AiBehaviorEnum aiBehaviorCode) {
        BaseAiBehavior aiBehavior = baseAiBehaviorService.getAiBehaviorByCode(aiBehaviorCode);
        BaseAiBehaviorVO aiBehaviorVO = new BaseAiBehaviorVO();
        if (aiBehavior != null) {
            BeanUtils.copyProperties(aiBehavior, aiBehaviorVO, "aiBehaviorEnum");
            aiBehaviorVO.setAiBehaviorEnum(aiBehavior.getBehaviorCode());
        }
        return AppResponse.success(aiBehaviorVO, "AI行为VO查询请求已处理");
    }

    /**
     * 管理员查询所有AI行为列表
     */
    @Operation(
            summary = "管理员查询所有AI行为",
            description = """
                管理员查询系统所有AI行为字典数据：
                1. 该接口需要验证请求头中的认证信息（Authorization: Bearer Token）；
                2. 仅系统管理员可调用，无频率限制（查询操作无压库风险）；
                3. 无AI行为数据时返回空列表，避免前端空指针；
                4. 返回结果按AI行为ID升序排列（底层可自行扩展排序）。
                """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/listAll")
    @CheckJwt
    @RequireRole(allowedRoles = {RoleEnum.ADMIN})
    @SwaggerResponseConstants.Api500
    @SwaggerResponseConstants.Api401
    @SwaggerResponseConstants.Api403
    public AppResponse<List<BaseAiBehavior>> listAllAiBehaviors() {
        List<BaseAiBehavior> aiBehaviorList = baseAiBehaviorService.listAllAiBehaviors();
        return AppResponse.success(aiBehaviorList, "所有AI行为查询成功，共" + aiBehaviorList.size() + "条数据");
    }

    // ========== 新增扩展接口（适配behaviorName字段+完整CRUD） ==========
    /**
     * 管理员新增AI行为（指定行为名称）
     */
    @Operation(
            summary = "管理员新增AI行为（指定行为名称）",
            description = """
            管理员新增系统AI行为字典数据（指定行为名称）：
            1. 该接口需要验证请求头中的认证信息（Authorization: Bearer Token）；
            2. 仅系统管理员可调用，60秒内最多调用100次（避免重复新增）；
            3. AI行为编码、行为名称不能为空，否则返回参数不合法错误；
            4. 新增成功后返回操作结果，失败返回具体原因。
            """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/add/withName")
    @CheckJwt
    @RequireRole(allowedRoles = {RoleEnum.ADMIN})
    @RateLimit(seconds = 60, maxCount = 100)
    @SwaggerResponseConstants.Api500
    @SwaggerResponseConstants.Api401
    @SwaggerResponseConstants.Api403
    public AppResponse<String> addAiBehaviorWithName(
            @Parameter(description = "AI行为编码（如：CHAT、LIKE、WAIT）", required = true)
            @RequestParam AiBehaviorEnum aiBehaviorCode,
            @Parameter(description = "AI行为名称（如：用户聊天互动、用户点赞）", required = true, example = "用户聊天互动")
            @RequestParam String behaviorName) {
        baseAiBehaviorService.addAiBehavior(aiBehaviorCode, behaviorName);
        return AppResponse.success(null, "AI行为新增请求已处理：" + aiBehaviorCode + "（名称=" + behaviorName + "）");
    }

    /**
     * 管理员根据AI行为编码更新行为名称
     */
    @Operation(
            summary = "管理员根据编码更新AI行为名称",
            description = """
            管理员更新系统AI行为字典的行为名称：
            1. 该接口需要验证请求头中的认证信息（Authorization: Bearer Token）；
            2. 仅系统管理员可调用，60秒内最多调用50次（避免频繁修改）；
            3. AI行为编码、行为名称不能为空，否则返回参数不合法错误；
            4. AI行为编码不存在时返回操作失败，前端需处理。
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
    public AppResponse<String> updateAiBehaviorNameByCode(
            @Parameter(description = "AI行为编码（如：CHAT、LIKE、WAIT）", required = true)
            @RequestParam AiBehaviorEnum aiBehaviorCode,
            @Parameter(description = "新的AI行为名称", required = true, example = "用户日常聊天互动")
            @RequestParam String behaviorName) {
        baseAiBehaviorService.updateAiBehaviorNameByCode(aiBehaviorCode, behaviorName);
        return AppResponse.success(null, "AI行为名称更新请求已处理：" + aiBehaviorCode + "（新名称=" + behaviorName + "）");
    }

    /**
     * 管理员根据AI行为ID更新编码/行为名称（有值则更）
     */
    @Operation(
            summary = "管理员根据ID更新AI行为编码/行为名称",
            description = """
            管理员根据AI行为ID动态更新编码或行为名称（有值则更新）：
            1. 该接口需要验证请求头中的认证信息（Authorization: Bearer Token）；
            2. 仅系统管理员可调用，60秒内最多调用50次（避免频繁修改）；
            3. AI行为ID必须为正整数，名称为可选参数，否则返回参数不合法错误；
            4. AI行为ID不存在时返回操作失败，前端需处理。
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
    public AppResponse<String> updateAiBehaviorById(
            @Parameter(description = "AI行为ID（正整数）", required = true)
            @RequestParam Integer aiBehaviorId,
            @Parameter(description = "新的AI行为编码（可选，如：CHAT、LIKE）", required = false)
            @RequestParam(required = false) AiBehaviorEnum aiBehaviorCode,
            @Parameter(description = "新的AI行为名称（可选）", required = false, example = "用户主动点赞")
            @RequestParam(required = false) String behaviorName) {
        BaseAiBehavior aiBehavior = new BaseAiBehavior();
        aiBehavior.setBehaviorId(aiBehaviorId);
        aiBehavior.setBehaviorCode(aiBehaviorCode);
        // 替换为行为名称，删除score
        aiBehavior.setBehaviorName(behaviorName);
        baseAiBehaviorService.updateAiBehaviorById(aiBehavior);
        return AppResponse.success(null, "AI行为更新请求已处理：ID=" + aiBehaviorId);
    }

    /**
     * 管理员根据AI行为编码删除行为
     */
    @Operation(
            summary = "管理员根据编码删除AI行为",
            description = """
                管理员根据AI行为编码物理删除系统AI行为字典数据：
                1. 该接口需要验证请求头中的认证信息（Authorization: Bearer Token）；
                2. 仅系统管理员可调用，60秒内最多调用20次（避免误删）；
                3. AI行为编码不能为空，否则返回参数不合法错误；
                4. AI行为编码不存在时返回操作失败，前端需处理。
                """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @DeleteMapping("/delete/code/{aiBehaviorCode}")
    @CheckJwt
    @RequireRole(allowedRoles = {RoleEnum.ADMIN})
    @RateLimit(seconds = 60, maxCount = 20)
    @SwaggerResponseConstants.Api500
    @SwaggerResponseConstants.Api401
    @SwaggerResponseConstants.Api403
    public AppResponse<String> deleteAiBehaviorByCode(
            @Parameter(description = "AI行为编码（如：WAIT、SLEEP、WALK）", required = true)
            @PathVariable AiBehaviorEnum aiBehaviorCode) {
        baseAiBehaviorService.deleteAiBehaviorByCode(aiBehaviorCode);
        return AppResponse.success(null, "AI行为删除请求已处理：" + aiBehaviorCode);
    }

    /**
     * 管理员根据AI行为ID删除行为
     */
    @Operation(
            summary = "管理员根据ID删除AI行为",
            description = """
                管理员根据AI行为ID物理删除系统AI行为字典数据：
                1. 该接口需要验证请求头中的认证信息（Authorization: Bearer Token）；
                2. 仅系统管理员可调用，60秒内最多调用20次（避免误删）；
                3. AI行为ID必须为正整数，否则返回参数不合法错误；
                4. AI行为ID不存在时返回操作失败，前端需处理。
                """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @DeleteMapping("/delete/id/{aiBehaviorId}")
    @CheckJwt
    @RequireRole(allowedRoles = {RoleEnum.ADMIN})
    @RateLimit(seconds = 60, maxCount = 20)
    @SwaggerResponseConstants.Api500
    @SwaggerResponseConstants.Api401
    @SwaggerResponseConstants.Api403
    public AppResponse<String> deleteAiBehaviorById(
            @Parameter(description = "AI行为ID（正整数）", required = true)
            @PathVariable Integer aiBehaviorId) {
        baseAiBehaviorService.deleteAiBehaviorById(aiBehaviorId);
        return AppResponse.success(null, "AI行为删除请求已处理：ID=" + aiBehaviorId);
    }
}