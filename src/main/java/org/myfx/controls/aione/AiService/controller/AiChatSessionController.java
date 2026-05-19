package org.myfx.controls.aione.AiService.controller;

import cn.hutool.core.bean.BeanUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.AiService.dto.AiChatSessionUnreadUpdateDTO;
import org.myfx.controls.aione.AiService.dto.ChatSessionUnreadClearDTO;
import org.myfx.controls.aione.AiService.entity.ai_chat_db.AiChatSession;
import org.myfx.controls.aione.AiService.service.base.ai_chat_db.AiChatMessageService;
import org.myfx.controls.aione.AiService.service.base.ai_chat_db.AiChatSessionService;
import org.myfx.controls.aione.AiService.vo.AiChatSessionVO;
import org.myfx.controls.aione.ServiceCommon.AppResponse;
import org.myfx.controls.aione.ServiceCommon.SwaggerResponseConstants;
import org.myfx.controls.aione.ServiceCommon.annotation.AiAppCors;
import org.myfx.controls.aione.ServiceCommon.annotation.CheckJwt;
import org.myfx.controls.aione.ServiceCommon.annotation.CleanupThreadLocal;
import org.myfx.controls.aione.ServiceCommon.annotation.RateLimit;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AI对话会话控制器
 * 负责会话创建相关接口（登录用户/游客）
 */
@RestController
@RequestMapping("/ai/chat/session")
@Tag(name = "AI对话会话接口", description = "AI对话会话的创建、查询等操作")
@CleanupThreadLocal
@RequiredArgsConstructor
@AiAppCors
@Slf4j
public class AiChatSessionController {

    private final AiChatSessionService aiChatSessionService;
    private final AiChatMessageService aiChatMessageService;

    /**
     * 查询当前登录用户的所有未删除对话（正常会话列表）
     */
    @Operation(
            summary = "查询当前登录用户的所有正常对话",
            description = """
                    查询当前登录用户下所有未删除的AI对话会话：
                    1. 登录态接口，需携带有效的JWT Token；
                    2. 自动从登录上下文获取用户ID，无需传入；
                    3. 仅返回is_deleted=0的会话，按创建时间倒序（默认）；
                    4. 接口1分钟内最多调用5次，防止高频查询；
                    5. 返回结果包含lastMessageContent字段（会话最后一条消息内容），供前端预览展示。
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/sessions/normal")
    @CheckJwt // JWT登录校验（复用现有注解）
    @RateLimit(seconds = 60, maxCount = 20) // 限流规则和现有接口一致
    @SwaggerResponseConstants.Api500
    @SwaggerResponseConstants.Api400
    @SwaggerResponseConstants.Api401
    public AppResponse<List<AiChatSessionVO>> listUserNormalSessions() {
        // 1. 调用Service层查询未删除会话（实体列表）
        List<AiChatSession> sessionList = aiChatSessionService.listUserAllNormalSessions();

        // 2. 工具类批量转换：实体列表 → VO列表（自动携带lastMessageContent字段）
        List<AiChatSessionVO> voList = BeanUtil.copyToList(sessionList, AiChatSessionVO.class);

        // 3. 返回VO列表（VO中包含lastMessageContent，供前端预览消息）
        return AppResponse.success(voList, "正常对话会话查询成功");
    }

    /**
     * 查询当前登录用户的所有已删除对话（回收站）
     */
    @Operation(
            summary = "查询当前登录用户的回收站对话",
            description = """
        查询当前登录用户下所有已删除的AI对话会话（回收站）：
        1. 登录态接口，需携带有效的JWT Token；
        2. 自动从登录上下文获取用户ID，无需传入；
        3. 仅返回is_deleted=1的会话，按删除时间倒序（默认）；
        4. 接口1分钟内最多调用5次，防止高频查询。
        """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/sessions/recycle")
    @CheckJwt // JWT登录校验
    @RateLimit(seconds = 60, maxCount = 5) // 统一限流规则
    @SwaggerResponseConstants.Api500
    @SwaggerResponseConstants.Api400
    @SwaggerResponseConstants.Api401
    // 关键修改1：返回类型从List<AiChatSession>改为List<AiChatSessionVO>
    public AppResponse<List<AiChatSessionVO>> listUserDeletedSessions() {
        // 1. 调用Service层查询已删除会话（实体列表）
        List<AiChatSession> sessionList = aiChatSessionService.listUserAllDeletedSessions();

        // 2. 工具类批量转换：实体列表 → VO列表（核心，无需手动set）
        List<AiChatSessionVO> voList = BeanUtil.copyToList(sessionList, AiChatSessionVO.class);

        // 3. 返回VO列表
        return AppResponse.success(voList, "回收站对话会话查询成功");
    }

    /**
     * 置顶会话（仅传sessionUuid，自动关联当前登录用户）
     */
    @Operation(
            summary = "置顶对话会话",
            description = """
        置顶指定AI对话会话：
        1. 登录态接口，需携带有效的JWT Token；
        2. 会话UUID通过路径参数传入；
        3. 自动关联当前登录用户，仅能操作自己的会话；
        4. 接口1分钟内最多调用5次，防止高频操作。
        """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/{sessionUuid}/top")
    @CheckJwt
    @RateLimit(seconds = 60, maxCount = 5)
    @SwaggerResponseConstants.Api500
    @SwaggerResponseConstants.Api400
    @SwaggerResponseConstants.Api401
    public AppResponse<Void> topSession(
            @Parameter(description = "对话会话UUID", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable String sessionUuid
    ) {
        // 从UUID获取会话ID，校验结果
        AppResponse<Long> sessionIdResp = getSessionIdFromUuid(sessionUuid);
        // 校验响应码：非200直接返回错误
        if (!sessionIdResp.getCode().equals(200)) {
            return AppResponse.error(sessionIdResp.getCode(), sessionIdResp.getMessage(), null);
        }
        aiChatSessionService.topSession(sessionIdResp.getData());
        return AppResponse.success(null, "会话置顶成功");
    }

    /**
     * 取消置顶会话（仅传sessionUuid，自动关联当前登录用户）
     */
    @Operation(
            summary = "取消置顶对话会话",
            description = """
        取消置顶指定AI对话会话：
        1. 登录态接口，需携带有效的JWT Token；
        2. 会话UUID通过路径参数传入；
        3. 自动关联当前登录用户，仅能操作自己的会话；
        4. 接口1分钟内最多调用5次，防止高频操作。
        """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/{sessionUuid}/untop")
    @CheckJwt
    @RateLimit(seconds = 60, maxCount = 5)
    @SwaggerResponseConstants.Api500
    @SwaggerResponseConstants.Api400
    @SwaggerResponseConstants.Api401
    public AppResponse<Void> untopSession(
            @Parameter(description = "对话会话UUID", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable String sessionUuid
    ) {
        // 从UUID获取会话ID，校验结果
        AppResponse<Long> sessionIdResp = getSessionIdFromUuid(sessionUuid);
        // 校验响应码：非200直接返回错误
        if (!sessionIdResp.getCode().equals(200)) {
            return AppResponse.error(sessionIdResp.getCode(), sessionIdResp.getMessage(), null);
        }
        aiChatSessionService.untopSession(sessionIdResp.getData());
        return AppResponse.success(null, "会话取消置顶成功");
    }

    /**
     * 更新会话未读消息数（自动关联当前登录用户）
     */
    @Operation(
            summary = "更新对话会话未读消息数",
            description = """
    更新指定AI对话会话的未读消息数：
    1. 登录态接口，需携带有效的JWT Token；
    2. 会话UUID+未读数值通过请求体传入；
    3. 自动关联当前登录用户，仅能操作自己的会话；
    4. 支持动态更新：传哪个数值更新哪个，传null不修改；
    5. 接口1分钟内最多调用5次，防止高频操作。
    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/update-unread")
    @CheckJwt
    @RateLimit(seconds = 60, maxCount = 5)
    @SwaggerResponseConstants.Api500
    @SwaggerResponseConstants.Api400
    @SwaggerResponseConstants.Api401
    public AppResponse<Void> updateSessionUnread(
            @RequestBody AiChatSessionUnreadUpdateDTO dto
    ) {
        // 从UUID获取会话ID，校验结果
        AppResponse<Long> sessionIdResp = getSessionIdFromUuid(dto.getSessionUuid());
        // 校验响应码：非200直接返回错误
        if (!sessionIdResp.getCode().equals(200)) {
            return AppResponse.error(sessionIdResp.getCode(), sessionIdResp.getMessage(), null);
        }

        // 调用Service三参方法（自动取当前用户ID）
        aiChatSessionService.increaseSessionUnreadCount(
                sessionIdResp.getData(),
                dto.getNormalUnreadCount(),
                dto.getSplitUnreadCount()
        );

        return AppResponse.success(null, "会话未读消息数更新成功");
    }

    /**
     * 清空会话未读消息数（自动关联当前登录用户）
     */
    @Operation(
            summary = "清空对话会话未读消息数",
            description = """
    清空指定AI对话会话的所有未读消息数：
    1. 登录态接口，需携带有效的JWT Token；
    2. 会话UUID通过请求体传入；
    3. 自动关联当前登录用户，仅能操作自己的会话；
    4. 同时清空普通未读+切分未读，全部置为0；
    5. 接口1分钟内最多调用5次，防止高频操作。
    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/clear-unread")
    @CheckJwt
    @RateLimit(seconds = 60, maxCount = 5)
    @SwaggerResponseConstants.Api500
    @SwaggerResponseConstants.Api400
    @SwaggerResponseConstants.Api401
    public AppResponse<Void> clearSessionUnread(
            @RequestBody ChatSessionUnreadClearDTO clearDTO
    ) {
        // 从DTO获取会话UUID
        String sessionUuid = clearDTO.getSessionUuid();

        // 从UUID获取会话ID，校验结果
        AppResponse<Long> sessionIdResp = getSessionIdFromUuid(sessionUuid);
        // 校验响应码：非200直接返回错误
        if (!sessionIdResp.getCode().equals(200)) {
            return AppResponse.error(sessionIdResp.getCode(), sessionIdResp.getMessage(), null);
        }

        // 调用Service重载方法（自动取当前用户ID）
        aiChatSessionService.clearSessionUnreadCount(sessionIdResp.getData());

        return AppResponse.success(null, "会话未读消息数清空成功");
    }

    /**
     * 删除会话（仅传sessionUuid，自动关联当前登录用户）
     */
    @Operation(
            summary = "删除对话会话（移入回收站）",
            description = """
        删除指定AI对话会话（移入回收站）：
        1. 登录态接口，需携带有效的JWT Token；
        2. 会话UUID通过路径参数传入；
        3. 自动关联当前登录用户，仅能删除自己的会话；
        4. 接口1分钟内最多调用5次，防止高频操作。
        """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/{sessionUuid}/delete")
    @CheckJwt
    @RateLimit(seconds = 60, maxCount = 5)
    @SwaggerResponseConstants.Api500
    @SwaggerResponseConstants.Api400
    @SwaggerResponseConstants.Api401
    public AppResponse<Void> deleteSession(
            @Parameter(description = "对话会话UUID", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable String sessionUuid
    ) {
        // 从UUID获取会话ID，校验结果
        AppResponse<Long> sessionIdResp = getSessionIdFromUuid(sessionUuid);
        // 校验响应码：非200直接返回错误
        if (!sessionIdResp.getCode().equals(200)) {
            return AppResponse.error(sessionIdResp.getCode(), sessionIdResp.getMessage(), null);
        }
        aiChatSessionService.deleteSession(sessionIdResp.getData());
        return AppResponse.success(null, "会话删除成功（已移入回收站）");
    }

    /**
     * 复原会话（仅传sessionUuid，自动关联当前登录用户）
     */
    @Operation(
            summary = "复原对话会话（移出回收站）",
            description = """
        复原指定AI对话会话（移出回收站）：
        1. 登录态接口，需携带有效的JWT Token；
        2. 会话UUID通过路径参数传入；
        3. 自动关联当前登录用户，仅能复原自己的会话；
        4. 接口1分钟内最多调用5次，防止高频操作。
        """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/{sessionUuid}/recover")
    @CheckJwt
    @RateLimit(seconds = 60, maxCount = 5)
    @SwaggerResponseConstants.Api500
    @SwaggerResponseConstants.Api400
    @SwaggerResponseConstants.Api401
    public AppResponse<Void> recoverSession(
            @Parameter(description = "对话会话UUID", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable String sessionUuid
    ) {
        // 从UUID获取会话ID，校验结果
        AppResponse<Long> sessionIdResp = getSessionIdFromUuid(sessionUuid);
        // 校验响应码：非200直接返回错误
        if (!sessionIdResp.getCode().equals(200)) {
            return AppResponse.error(sessionIdResp.getCode(), sessionIdResp.getMessage(), null);
        }
        aiChatSessionService.recoverSession(sessionIdResp.getData());
        return AppResponse.success(null, "会话复原成功（已移出回收站）");
    }

    /**
     * 通用工具：从 sessionUuid 获取 sessionId，统一异常处理 + 标准返回体
     */
    private AppResponse<Long> getSessionIdFromUuid(String sessionUuid) {
        // 1. 基础参数校验
        if (sessionUuid == null || sessionUuid.isBlank()) {
            return AppResponse.error(400, "会话UUID不能为空", null);
        }

        // 2. 通过UUID查询雪花ID
        Long sessionId;
        try {
            sessionId = aiChatSessionService.getSessionIdByUUID(sessionUuid);
        } catch (IllegalArgumentException e) {
            return AppResponse.error(400, e.getMessage(), null);
        }

        // 3. 校验会话是否存在
        if (sessionId == null) {
            return AppResponse.error(400, "会话不存在，sessionUuid=" + sessionUuid, null);
        }

        // 4. 正常返回 sessionId
        return AppResponse.success(sessionId,"转换成功");
    }
}