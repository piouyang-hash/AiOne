package org.myfx.controls.aione.UserService.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.ServiceCommon.annotation.RateLimit;
import org.myfx.controls.aione.UserService.model.dto.RewardRequestDTO;
import org.myfx.controls.aione.UserService.service.RewardRecordService;
import org.myfx.controls.aione.ServiceCommon.AppResponse;
import org.myfx.controls.aione.ServiceCommon.SwaggerResponseConstants;
import org.myfx.controls.aione.ServiceCommon.annotation.CheckJwt;
import org.myfx.controls.aione.ServiceCommon.annotation.CleanupThreadLocal;

import org.myfx.controls.aione.ServiceCommon.annotation.ServiceAuth;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 打赏相关接口控制器
 */
@RestController
@RequestMapping("/rewards")
@Tag(name = "打赏接口", description = "用户打赏相关操作")
@RequiredArgsConstructor
@CleanupThreadLocal
@ServiceAuth(allowedServices = {"order-service"})
@Slf4j
public class RewardController {

    private final RewardRecordService rewardRecordService;

    /**
     * 微服务内部调用的打赏接口
     * 由其他微服务触发，创建用户打赏记录
     */
    @Operation(
            summary = "内部服务-创建打赏记录",
            description = """
                【微服务内部接口，禁止前端直接调用】
                供其他微服务（如订单服务）调用，需携带微服务认证令牌。
                打赏金额限5、10、15，调用方需先完成业务规则校验。
                """,
            security = @SecurityRequirement(name = "serviceAuth") // 改为微服务间认证标识（非用户JWT）
    )
    @CheckJwt
    @RateLimit(seconds = 30, maxCount = 5) // 保留限流，控制服务间调用频率
    @SwaggerResponseConstants.Api500
    @SwaggerResponseConstants.Api400
    @SwaggerResponseConstants.Api401 // 未授权（服务令牌无效）
    @PostMapping("/add")
    public AppResponse<?> addReward(
            @RequestBody RewardRequestDTO rewardDTO // 接收DTO请求体并启用校验
    ) {
        // 调用服务层，传递DTO中的参数
        boolean success = rewardRecordService.addRewardRecord(
                rewardDTO.getAmount(),
                rewardDTO.getMessage()
        );


        // 可根据需要记录调用方服务名（用于日志追溯）
        String caller = rewardDTO.getCallerServiceName();
        log.info("打赏接口被服务[{}]调用，金额：{}", caller, rewardDTO.getAmount());


        return success ?
                AppResponse.success(null, "打赏记录创建成功") :
                AppResponse.error(400, "打赏记录创建失败，请重试", null);
    }
}