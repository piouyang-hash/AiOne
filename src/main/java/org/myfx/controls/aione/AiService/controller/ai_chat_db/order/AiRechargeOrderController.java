package org.myfx.controls.aione.AiService.controller.ai_chat_db.order;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.AiService.common.ai_chat_db.PointRechargeTierEnum;
import org.myfx.controls.aione.AiService.dto.order.AiRechargeOrderCreateDTO;
import org.myfx.controls.aione.AiService.entity.ai_chat_db.order.AiRechargeGoods;
import org.myfx.controls.aione.AiService.entity.ai_chat_db.order.AiRechargeOrder;
import org.myfx.controls.aione.AiService.service.base.ai_chat_db.order.AiRechargeGoodsService;
import org.myfx.controls.aione.AiService.service.base.ai_chat_db.order.AiRechargeOrderService;
import org.myfx.controls.aione.AiService.vo.PointRechargeTierVO;
import org.myfx.controls.aione.AiService.vo.order.AiRechargeOrderDetailVO;
import org.myfx.controls.aione.AiService.vo.order.AiRechargeOrderVO;
import org.myfx.controls.aione.ServiceCommon.AppResponse;
import org.myfx.controls.aione.ServiceCommon.SwaggerResponseConstants;
import org.myfx.controls.aione.ServiceCommon.annotation.AiAppCors;
import org.myfx.controls.aione.ServiceCommon.annotation.CheckJwt;
import org.myfx.controls.aione.ServiceCommon.annotation.CleanupThreadLocal;
import org.myfx.controls.aione.ServiceCommon.annotation.RateLimit;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
    private final AiRechargeGoodsService aiRechargeGoodsService;

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

    /**
     * 创建AI充值订单
     */
    @Operation(
            summary = "创建AI充值订单",
            description = """
            创建用户AI积分充值订单：
            1. 登录态接口，需携带有效的JWT Token；
            2. 传入充值商品ID；
            3. 自动生成雪花ID订单号；
            4. 订单初始状态为待支付，支付方式固定支付宝；
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
        // 从DTO获取商品ID，调用Service
        Long orderId = aiRechargeOrderService.createRechargeOrder(createDTO.getGoodsId());
        // 返回订单号字符串
        return AppResponse.success(orderId.toString(), "充值订单创建成功");
    }

    /**
     * 查询当前登录用户的所有AI积分充值订单
     */
    @Operation(
            summary = "查询当前用户所有AI积分充值订单",
            description = """
                查询当前登录用户的所有AI积分充值订单列表：
                1. 登录态接口，需携带有效的JWT Token；
                2. 无需传入参数，自动获取当前登录用户ID；
                3. 返回订单列表，包含订单金额、积分、状态、创建时间等信息；
                4. 无订单时返回空列表，不会返回null；
                5. 订单默认按创建时间倒序排列；
                """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/list")
    @CheckJwt
    @SwaggerResponseConstants.Api500
    @SwaggerResponseConstants.Api401
    // 🔥 返回值改为 AiRechargeOrderDetailVO 列表
    public AppResponse<List<AiRechargeOrderDetailVO>> getRechargeOrderList() {
        // 1. 调用业务层查询当前用户所有订单
        List<AiRechargeOrder> orderList = aiRechargeOrderService.getRechargeOrdersByCurrentUser();

        // 2. 转换为 DetailVO 列表
        List<AiRechargeOrderDetailVO> orderVOList = orderList.stream()
                .map(order -> {
                    AiRechargeOrderDetailVO detailVO = new AiRechargeOrderDetailVO();
                    // 拷贝基础属性
                    BeanUtils.copyProperties(order, detailVO);

                    // 🔥 根据订单 goodsId 查询商品
                    AiRechargeGoods goods = aiRechargeGoodsService.getRechargeGoodsById(order.getGoodsId());
                    // 手动 set 金额和积分
                    detailVO.setAmount(goods.getAmount());
                    detailVO.setPoint(goods.getPoint());

                    return detailVO;
                })
                .collect(Collectors.toList());

        // 3. 返回结果
        return AppResponse.success(orderVOList, "查询订单列表成功");
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
    // 🔥 返回值改为 AiRechargeOrderDetailVO
    public AppResponse<AiRechargeOrderDetailVO> getRechargeOrderDetail(@PathVariable Long orderId) {
        // 1. 查询数据库原始实体
        AiRechargeOrder order = aiRechargeOrderService.getRechargeOrderById(orderId);

        // 2. 创建 DetailVO
        AiRechargeOrderDetailVO detailVO = new AiRechargeOrderDetailVO();

        // 3. 拷贝基础属性
        BeanUtils.copyProperties(order, detailVO);

        // 🔥 根据订单 goodsId 查询商品
        AiRechargeGoods goods = aiRechargeGoodsService.getRechargeGoodsById(order.getGoodsId());
        // 手动 set 金额和积分
        detailVO.setAmount(goods.getAmount());
        detailVO.setPoint(goods.getPoint());

        // 4. 返回VO
        return AppResponse.success(detailVO, "查询订单详情成功");
    }

    @Operation(
            summary = "查询当前用户待支付订单",
            description = "获取当前用户唯一的待支付充值订单，无则返回null",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/unpaid/detail")
    @CheckJwt
    @SwaggerResponseConstants.Api401
    public AppResponse<AiRechargeOrderVO> getUnpaidOrderDetail() {
        AiRechargeOrder unpaidOrder = aiRechargeOrderService.getCurrentUserUnpaidOrder();

        // 无订单时直接返回null，而非空对象
        AiRechargeOrderVO orderVO = null;
        if (unpaidOrder != null) {
            orderVO = new AiRechargeOrderVO();
            BeanUtils.copyProperties(unpaidOrder, orderVO);
        }

        return AppResponse.success(orderVO, unpaidOrder != null ? "查询到待支付订单" : "无待支付订单");
    }

    @Operation(
            summary = "关闭待支付订单",
            description = "用户主动关闭待支付订单，订单状态修改为【已取消】，仅允许操作待支付订单",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/close")
    @CheckJwt
    @SwaggerResponseConstants.Api401
    public AppResponse<Void> closeOrder(@RequestParam Long id) {
        // 直接调用业务方法，极简实现
        aiRechargeOrderService.closeOrder(id);
        return AppResponse.success(null, "订单关闭成功");
    }

}