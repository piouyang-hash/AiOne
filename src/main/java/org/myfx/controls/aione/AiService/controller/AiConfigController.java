package org.myfx.controls.aione.AiService.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.AiService.entity.ai_chat_db.AiConfig;
import org.myfx.controls.aione.AiService.service.base.ai_chat_db.AiConfigService;
import org.myfx.controls.aione.ServiceCommon.AppResponse;
import org.myfx.controls.aione.ServiceCommon.SwaggerResponseConstants;
import org.myfx.controls.aione.ServiceCommon.annotation.AiAppCors;
import org.myfx.controls.aione.ServiceCommon.annotation.CheckJwt;
import org.myfx.controls.aione.ServiceCommon.annotation.CleanupThreadLocal;
import org.myfx.controls.aione.ServiceCommon.annotation.RateLimit;
import org.myfx.controls.aione.ServiceCommon.context.UserContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI配置控制器
 * 负责用户AI个性化配置管理（主动聊天模式等）
 */
@RestController
@RequestMapping("/ai/config")
@Tag(name = "AI用户配置接口", description = "AI用户个性化配置、主动聊天模式开关管理")
@CleanupThreadLocal
@RequiredArgsConstructor
@AiAppCors
@Slf4j
public class AiConfigController {

    // 注入AI配置业务层
    private final AiConfigService aiConfigService;

    /**
     * 翻转主动聊天模式（开关）
     */
    @Operation(
            summary = "翻转主动聊天模式",
            description = "切换AI主动聊天开关：0关闭/1开启，自动切换",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/toggle/active-chat")
    @CheckJwt
    @RateLimit(seconds = 60, maxCount = 12)
    @SwaggerResponseConstants.Api500
    @SwaggerResponseConstants.Api400
    @SwaggerResponseConstants.Api401
    public AppResponse<AiConfig> toggleActiveChatMode() {
        Integer userId = UserContext.getUserId();
        AiConfig aiConfig = aiConfigService.toggleActiveChat(userId);
        return AppResponse.success(aiConfig, "主动聊天模式切换成功");
    }

    /**
     * 翻转AI消息切分模式（开关）
     */
    @Operation(
            summary = "翻转AI消息切分模式",
            description = "切换AI消息切分开关：0不切分/1切分，自动切换",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/toggle/split-ai-message")
    @CheckJwt
    @RateLimit(seconds = 60, maxCount = 12)
    @SwaggerResponseConstants.Api500
    @SwaggerResponseConstants.Api400
    @SwaggerResponseConstants.Api401
    public AppResponse<AiConfig> toggleSplitAiMessage() {
        Integer userId = UserContext.getUserId();
        AiConfig aiConfig = aiConfigService.toggleSplitAiMessage(userId);
        return AppResponse.success(aiConfig, "消息切分模式切换成功");
    }

    /**
     * 查询当前用户AI配置
     */
    @Operation(
            summary = "查询用户AI配置",
            description = "获取当前登录用户的全部AI个性化配置信息",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/info")
    @CheckJwt
    @RateLimit(seconds = 60, maxCount = 20)
    @SwaggerResponseConstants.Api500
    @SwaggerResponseConstants.Api400
    @SwaggerResponseConstants.Api401
    public AppResponse<AiConfig> getUserAiConfig() {
        Integer userId = UserContext.getUserId();
        AiConfig aiConfig = aiConfigService.getConfig(userId);
        return AppResponse.success(aiConfig, "查询用户AI配置成功");
    }
}