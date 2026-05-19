package org.myfx.controls.aione.AiService.controller.ai_chat_db;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.AiService.common.ai_chat_db.PointRechargeTierEnum;
import org.myfx.controls.aione.AiService.entity.ai_chat_db.token.AiUserPointBalance;
import org.myfx.controls.aione.AiService.service.base.ai_chat_db.token.AiUserPointBalanceService;
import org.myfx.controls.aione.AiService.vo.AiUserPointBalanceVO;
import org.myfx.controls.aione.AiService.vo.PointRechargeTierVO;
import org.myfx.controls.aione.ServiceCommon.AppResponse;
import org.myfx.controls.aione.ServiceCommon.SwaggerResponseConstants;
import org.myfx.controls.aione.ServiceCommon.annotation.AiAppCors;
import org.myfx.controls.aione.ServiceCommon.annotation.CheckJwt;
import org.myfx.controls.aione.ServiceCommon.annotation.CleanupThreadLocal;
import org.myfx.controls.aione.ServiceCommon.annotation.RateLimit;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

/**
 * AI用户积分控制器
 * 负责用户积分余额查询、积分增加等操作
 */
@RestController
@RequestMapping("/ai/user/point")
@Tag(name = "AI用户积分接口", description = "AI用户积分余额查询、积分增加等操作")
@CleanupThreadLocal
@RequiredArgsConstructor
@AiAppCors
@Slf4j
public class AiUserPointController {

    // 注入积分服务
    private final AiUserPointBalanceService aiUserPointBalanceService;

    /**
     * 增加用户总可用积分（硬编码参数）
     */
    @Operation(
            summary = "增加用户总可用积分",
            description = """
                    为指定用户增加总可用积分余额：
                    1. 登录态接口，需携带有效的JWT Token；
                    2. 接口内部硬编码用户ID和增加积分数值，无需前端传入；
                    3. 接口1分钟内最多调用20次，防止高频调用；
                    4. 返回数据库受影响行数。
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/add")
    @CheckJwt
    @RateLimit(seconds = 60, maxCount = 20)
    @SwaggerResponseConstants.Api500
    @SwaggerResponseConstants.Api400
    @SwaggerResponseConstants.Api401
    public AppResponse<Integer> addUserTotalPoint() {
        // 硬编码：用户ID、增加的积分数量
        Integer userId = 1;
        Long addPoint = 100L;

        // 调用Service层增加积分
        int affectRows = aiUserPointBalanceService.addTotalPointByUserId(userId, addPoint);

        // 返回响应结果
        return AppResponse.success(affectRows, "积分增加成功");
    }

    /**
     * 查询当前登录用户积分余额
     */
    @Operation(
            summary = "查询当前登录用户积分余额",
            description = """
                查询当前登录用户的积分余额信息：
                1. 登录态接口，需携带有效的JWT Token；
                2. 自动从上下文获取用户ID，无需传参；
                3. 接口1分钟内最多调用20次；
                4. 返回积分VO（自动转换积分单位，剔除ID字段）。
                """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/balance")
    @CheckJwt
    @RateLimit(seconds = 60, maxCount = 20)
    @SwaggerResponseConstants.Api500
    @SwaggerResponseConstants.Api400
    @SwaggerResponseConstants.Api401
// 返回值修改为 VO
    public AppResponse<AiUserPointBalanceVO> getUserPointBalance() {
        // 1. 查询数据库实体
        AiUserPointBalance balance = aiUserPointBalanceService.getAiUserPointBalance();

        // 2. Spring自带工具类 复制属性
        AiUserPointBalanceVO vo = new AiUserPointBalanceVO();
        BeanUtils.copyProperties(balance, vo);

        // 3. 返回VO（自动完成积分单位换算）
        return AppResponse.success(vo, "积分余额查询成功");
    }

    // ====================== 【新增】查询可用充值档位接口 ======================
    /**
     * 查询系统支持的积分充值档位
     */
    @Operation(
            summary = "查询积分充值档位",
            description = """
                查询系统预设的所有积分充值金额档位：
                1. 登录态接口，需携带有效的JWT Token；
                2. 无请求参数，直接返回所有可用档位；
                3. 接口1分钟内最多调用20次，防止高频查询；
                4. 返回包含金额、描述的充值档位列表。
                """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/recharge/tiers")
    @CheckJwt
    @RateLimit(seconds = 60, maxCount = 20)
    @SwaggerResponseConstants.Api500
    @SwaggerResponseConstants.Api400
    @SwaggerResponseConstants.Api401
    public AppResponse<List<PointRechargeTierVO>> getRechargeTiers() {
        // 1. 获取所有枚举
        List<PointRechargeTierEnum> tierList = Arrays.asList(PointRechargeTierEnum.values());

        // 2. 枚举 转 VO（核心优化）
        List<PointRechargeTierVO> voList = tierList.stream()
                .map(tier -> new PointRechargeTierVO(tier.getAmount(), tier.getDesc()))
                .toList();

        // 3. 返回干净的VO列表
        return AppResponse.success(voList, "充值档位查询成功");
    }
}
