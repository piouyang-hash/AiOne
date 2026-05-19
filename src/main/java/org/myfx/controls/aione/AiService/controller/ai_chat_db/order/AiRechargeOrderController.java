package org.myfx.controls.aione.AiService.controller.ai_chat_db.order;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.AiService.dto.order.AiRechargeOrderCreateDTO;
import org.myfx.controls.aione.AiService.entity.ai_chat_db.order.AiRechargeOrder;
import org.myfx.controls.aione.AiService.service.base.ai_chat_db.order.AiRechargeOrderService;
import org.myfx.controls.aione.AiService.vo.order.AiRechargeOrderVO;
import org.myfx.controls.aione.ServiceCommon.AppResponse;
import org.myfx.controls.aione.ServiceCommon.SwaggerResponseConstants;
import org.myfx.controls.aione.ServiceCommon.annotation.AiAppCors;
import org.myfx.controls.aione.ServiceCommon.annotation.CheckJwt;
import org.myfx.controls.aione.ServiceCommon.annotation.CleanupThreadLocal;
import org.myfx.controls.aione.ServiceCommon.annotation.RateLimit;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;;

/**
 * AI充值订单控制器
 * 负责充值订单创建、订单查询、订单状态更新等操作
 */
@RestController
@RequestMapping("/ai/recharge/order")
@Tag(name = "AI充值订单接口", description = "AI充值订单创建、订单状态管理等操作")
@CleanupThreadLocal
@RequiredArgsConstructor
@AiAppCors
@Slf4j
public class AiRechargeOrderController {

    // 注入充值订单服务
    private final AiRechargeOrderService aiRechargeOrderService;

    /**
     * 创建AI充值订单
     */
    @Operation(
            summary = "创建AI充值订单",
            description = """
                创建用户AI积分充值订单：
                1. 登录态接口，需携带有效的JWT Token；
                2. 传入充值金额；
                3. 自动生成雪花ID订单号、计算对应积分；
                4. 订单初始状态为待支付，支付方式固定微信/支付宝；
                5. 接口1分钟内最多调用20次，防止高频调用；
                """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/create")
    @CheckJwt
    @RateLimit(seconds = 60, maxCount = 20)
    @SwaggerResponseConstants.Api500
    @SwaggerResponseConstants.Api400
    @SwaggerResponseConstants.Api401
    public AppResponse<String> createRechargeOrder(@RequestBody AiRechargeOrderCreateDTO createDTO) {
        // 从DTO获取金额，调用Service
        Long orderId = aiRechargeOrderService.createRechargeOrder(createDTO.getAmount());
        // 返回订单号字符串
        return AppResponse.success(orderId.toString(), "充值订单创建成功");
    }

    /**
     * 根据订单ID查询AI充值订单详情
     */
    @Operation(
            summary = "查询AI充值订单详情",
            description = """
                根据订单ID查询充值订单完整信息：
                1. 登录态接口，需携带有效的JWT Token；
                2. 传入订单主键ID；
                3. 返回订单金额、积分、状态、创建时间等全部信息；
                """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/detail/{orderId}")
    @CheckJwt
    @SwaggerResponseConstants.Api500
    @SwaggerResponseConstants.Api400
    @SwaggerResponseConstants.Api401
// 🔥 返回值改为 VO
    public AppResponse<AiRechargeOrderVO> getRechargeOrderDetail(@PathVariable Long orderId) {
        // 1. 查询数据库原始实体
        AiRechargeOrder order = aiRechargeOrderService.getRechargeOrderById(orderId);

        // 2. 创建VO对象
        AiRechargeOrderVO orderVO = new AiRechargeOrderVO();

        // 3. Spring自带工具类：拷贝基础属性（id、amount等）
        BeanUtils.copyProperties(order, orderVO);

        // 5. 返回VO
        return AppResponse.success(orderVO, "查询订单详情成功");
    }
}