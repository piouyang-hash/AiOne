package org.myfx.controls.aione.AiService.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.AiService.entity.ai_chat_db.token.AiUserTokenBalance;
import org.myfx.controls.aione.AiService.service.base.ai_chat_db.token.AiUserTokenBalanceService;
import org.myfx.controls.aione.ServiceCommon.AppResponse;
import org.myfx.controls.aione.ServiceCommon.SwaggerResponseConstants;
import org.myfx.controls.aione.ServiceCommon.annotation.AiAppCors;
import org.myfx.controls.aione.ServiceCommon.annotation.CheckJwt;
import org.myfx.controls.aione.ServiceCommon.annotation.CleanupThreadLocal;
import org.myfx.controls.aione.ServiceCommon.annotation.RateLimit;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

/**
 * AI用户Token余额 控制器
 */
@Slf4j
@RestController
@RequestMapping("/ai/token-balance")
@RequiredArgsConstructor
@CleanupThreadLocal
@AiAppCors
@Tag(name = "AI的Token余额查询接口", description = "AI对话的Token查询和充值")
public class AiUserTokenBalanceController {

    private final AiUserTokenBalanceService aiUserTokenBalanceService;

    /**
     * 查询当前登录用户的所有类型Token余额
     */
    @Operation(
            summary = "查询我的Token余额",
            description = "获取当前登录用户的所有AI Token类型余额信息（包含总余额、已使用、剩余额度等）",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/my/list")
    @CheckJwt
    @RateLimit(seconds = 60, maxCount = 20)
    @SwaggerResponseConstants.Api500
    @SwaggerResponseConstants.Api400
    @SwaggerResponseConstants.Api401
    public AppResponse<List<AiUserTokenBalance>> getMyTokenBalanceList() {
        log.info("查询当前用户的所有AI Token余额列表");
        // 直接调用无参Service方法，从上下文获取用户ID
        List<AiUserTokenBalance> balanceList = aiUserTokenBalanceService.getAiUserTokenBalanceList();
        return AppResponse.success(balanceList, "查询用户Token余额成功");
    }
}