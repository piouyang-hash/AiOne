package org.myfx.controls.aione.AiService.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.AiService.service.schedule.AiActiveChatAggregateService;
import org.myfx.controls.aione.ServiceCommon.AppResponse;
import org.myfx.controls.aione.ServiceCommon.SwaggerResponseConstants;
import org.myfx.controls.aione.ServiceCommon.annotation.CleanupThreadLocal;
import org.myfx.controls.aione.ServiceCommon.annotation.RateLimit;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI主动消息手动触发控制器（测试专用：手动调用总接口，触发AI主动发消息）
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/ai/active")
@Tag(name = "AI主动消息接口", description = "AI主动消息相关接口（手动触发/测试专用）")
@CleanupThreadLocal // 复用你原有线程变量清理注解
public class AiActiveMessageController {

    // 注入AI主动消息总业务接口（核心：调用你封装的完整流程）
    private final AiActiveChatAggregateService aiActiveChatAggregateService;

    /**
     * 手动触发AI主动消息调度（测试专用：直接调用总接口，指定用户ID触发AI主动发消息）
     * 支持登录态校验（可选），允许手动传用户ID，便于测试不同用户场景
     */
    @Operation(
            summary = "手动触发AI主动消息调度",
            description = """
            测试专用接口：手动调用AI主动消息总接口，触发完整的主动消息流程：
            1. 该接口可携带JWT Token（可选，@CheckJwt注解可根据测试需求开启/关闭）；
            2. 手动传入目标用户ID（必填），支持测试不同用户的主动消息场景；
            3. 接口1分钟内最多调用5次（测试环境限流，防止频繁调用）；
            4. 调用流程：检查用户激活会话→无则创建→生成系统指令→触发AI主动回复；
            5. 返回AI主动生成的回复内容（已自动存入数据库）；
            6. 异常场景：用户ID为空/会话创建失败/AI回复生成失败，返回对应错误码。
            """,
            security = @SecurityRequirement(name = "bearerAuth") // 复用你原有JWT安全校验标识
    )
    @PostMapping("/dispatch")
    @RateLimit(seconds = 60, maxCount = 5) // 测试环境限流：1分钟最多调5次
    @SwaggerResponseConstants.Api500 // 复用你原有500异常响应注解
    @SwaggerResponseConstants.Api400 // 复用你原有400参数错误注解
    @SwaggerResponseConstants.Api401 // JWT无效/过期返回401（若开启必填）
    public AppResponse<String> manualTriggerAiActiveMessage(
            @Parameter(description = "目标用户ID（必填：触发AI主动发消息的接收用户）", required = true, example = "1")
            @RequestParam Integer userId) {

        log.info("[AI主动消息-手动触发] 开始调用总接口，目标用户ID：{}", userId);

        // 核心：调用你封装的总接口，执行完整的AI主动消息流程
        String aiReply = aiActiveChatAggregateService.executeAiActiveMessageDispatch(userId);

        return AppResponse.success("AI主动消息触发成功", aiReply);

    }
}