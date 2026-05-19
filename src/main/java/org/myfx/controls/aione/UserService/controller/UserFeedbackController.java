package org.myfx.controls.aione.UserService.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.UserService.model.dto.FeedbackDTO;
import org.myfx.controls.aione.UserService.model.dto.FeedbackReplyDTO;
import org.myfx.controls.aione.UserService.service.UserFeedbackService;
import org.myfx.controls.aione.ServiceCommon.AppResponse;
import org.myfx.controls.aione.ServiceCommon.SwaggerResponseConstants;
import org.myfx.controls.aione.ServiceCommon.annotation.*;
import org.myfx.controls.aione.ServiceCommon.context.UserContext;

import org.myfx.controls.aione.ServiceCommon.serviceEnum.RoleEnum;
import org.springframework.web.bind.annotation.*;

/**
 * 用户意见反馈控制器
 */
@RestController
@RequestMapping("/user-api/feedback") // 接口前缀：用户API + 反馈模块
@RequiredArgsConstructor
@MyVueApp
@CleanupThreadLocal
@Tag(
        name = "用户体验反馈接口",
        description = "提供意见反馈提交/删除等功能"
) // 补充@Tag，在原有功能后追加反馈相关功能
@Slf4j
public class UserFeedbackController {

    private final UserFeedbackService userFeedbackService;

    @Operation(
            summary = "提交用户意见反馈",
            description = """
            提交当前登录用户的意见反馈，后端自动绑定用户ID并设置默认状态为「未处理」。
            注意：
            1. 该接口需要验证请求头中的认证信息（如Authorization: Bearer Token）；
            2. 请确保请求头中已正确填写Token，否则会返回权限错误（401/403）；
            3. 反馈类型仅支持：function（功能问题）、experience（体验优化）、bug（Bug反馈）、suggestion（功能建议）、other（其他问题）、add_book（请求添加书籍）；
            4. 反馈内容不能为空，联系方式为可选参数（手机号/邮箱）。
            """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/submit")
    @CheckJwt
    @RateLimit(seconds = 30, maxCount = 5)
    @SwaggerResponseConstants.Api500
    @SwaggerResponseConstants.Api400
    @SwaggerResponseConstants.Api401
    @SwaggerResponseConstants.Api403
    public AppResponse<Void> submitFeedback(
            @Parameter(description = "意见反馈参数（需包含feedbackType、content，contact可选）", required = true)
            @RequestBody FeedbackDTO feedbackDTO) {

        // 1. 从UserContext获取当前用户ID，赋值给DTO
        Integer currentUserId = UserContext.getUserId();
        feedbackDTO.setUserId(currentUserId);

        // 2. TODO：当反馈类型是"请求添加书籍"时，给运维发钉钉通知（暂未实现）
        if ("add_book".equals(feedbackDTO.getFeedbackType())) {
            // 打印日志，方便后续调试（知道有用户请求加书了）
            log.warn("[TODO] 用户({})提交了添加书籍的反馈，需给运维发钉钉通知！反馈内容：{}，联系方式：{}",
                    currentUserId,
                    feedbackDTO.getContent(),
                    feedbackDTO.getContact() == null ? "未填写" : feedbackDTO.getContact());

            // ========== TODO 待实现逻辑 ==========
            // 1. 引入钉钉SDK依赖
            // 2. 调用钉钉机器人接口，发送以下内容给运维群：
            //    【紧急】用户ID：{currentUserId} 请求添加书籍！
            //    反馈内容：{feedbackDTO.getContent()}
            //    联系方式：{feedbackDTO.getContact() || "未填写"}
            // =====================================
        }

        // 3. 调用Service层，把DTO转成数据库实体类UserFeedback
        userFeedbackService.submitFeedback(feedbackDTO);

        return AppResponse.success(null, "意见反馈提交成功，我们会尽快处理～");
    }

    // 可选：补充「删除当前用户所有反馈」接口（按相同格式）
    @Operation(
            summary = "删除当前用户所有意见反馈",
            description = """
                    删除当前登录用户的所有意见反馈，无需传用户ID（后端从Token解析）。
                    注意：
                    1. 该接口需要验证请求头中的认证信息（如Authorization: Bearer Token）；
                    2. 请确保请求头中已正确填写Token，否则会返回权限错误（401/403）；
                    3. 该操作不可逆，请谨慎调用。
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @DeleteMapping("/my/all")
    @CheckJwt
    @RateLimit(seconds = 30, maxCount = 3)
    @SwaggerResponseConstants.Api500
    @SwaggerResponseConstants.Api400
    @SwaggerResponseConstants.Api401
    @SwaggerResponseConstants.Api403
    public AppResponse<Void> deleteMyAllFeedback() {
        userFeedbackService.deleteFeedbackByUserId();
        return AppResponse.success(null, "已成功删除您的所有意见反馈");
    }

    /**
     * 管理员回复/关闭用户反馈（仅ADMIN角色可调用）
     */
    @Operation(
            summary = "管理员回复用户意见反馈",
            description = """
                    管理员回复或关闭用户的意见反馈，需ADMIN角色权限。
                    注意：
                    1. 该接口需要验证请求头中的认证信息（如Authorization: Bearer Token）；
                    2. 仅系统管理员（ADMIN角色）可调用，否则返回403权限错误；
                    3. 处理状态仅支持：1（已回复）、2（已关闭）；
                    4. 反馈ID和回复内容为必填参数，回复内容不能为空；
                    5. 回复后自动更新反馈的replyContent、status、replyTime字段。
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/admin/reply") // 管理员专属接口路径，区分用户端
    @CheckJwt // 验证Token有效性
    @RequireRole(allowedRoles = {RoleEnum.ADMIN}) // 仅ADMIN角色可访问
    @RateLimit(seconds = 30, maxCount = 5) // 限流：30秒最多调用5次
    @SwaggerResponseConstants.Api500 // 服务器内部错误
    @SwaggerResponseConstants.Api400 // 参数非法（如反馈ID为空、状态错误）
    @SwaggerResponseConstants.Api401 // 未登录/Token失效
    @SwaggerResponseConstants.Api403 // 非ADMIN角色，权限不足
    public AppResponse<Void> replyFeedback(
            @Parameter(description = "管理员回复反馈参数（feedbackId/replyContent/status为必填）", required = true)
            @RequestBody FeedbackReplyDTO replyDTO) {

        // 调用Service层处理回复逻辑
        userFeedbackService.replyFeedback(replyDTO);

        // 返回成功响应
        String msg = replyDTO.getStatus() == 1 ? "回复反馈成功" : "关闭反馈成功";
        return AppResponse.success(null, msg + "，用户可在“我的反馈”中查看回复～");
    }
}