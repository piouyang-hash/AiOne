package org.myfx.controls.aione.AiService.controller.ai_chat_db.order;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.domain.AlipayTradeQueryModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.myfx.controls.aione.AiService.config.AlipayConfig;
import org.myfx.controls.aione.AiService.dto.order.AlipayPayDTO;
import org.myfx.controls.aione.AiService.entity.ai_chat_db.order.AiRechargeGoods;
import org.myfx.controls.aione.AiService.entity.ai_chat_db.order.AiRechargeOrder;
import org.myfx.controls.aione.AiService.service.base.ai_chat_db.order.AiRechargeGoodsService;
import org.myfx.controls.aione.AiService.service.base.ai_chat_db.order.AiRechargeOrderService;
import org.myfx.controls.aione.AiService.service.base.ai_chat_db.order.AlipayCallbackService;
import org.myfx.controls.aione.AiService.service.base.ai_chat_db.order.AlipayPayService;
import org.myfx.controls.aione.ServiceCommon.annotation.RateLimit;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/alipay")
@RequiredArgsConstructor
public class AlipayController {

    // 【关键】注入Spring容器中的AlipayClient Bean（推荐构造器注入，Spring官方最佳实践）
    private final AlipayClient alipayClient;
    private final AlipayPayService alipayPayService;

    private final AiRechargeOrderService aiRechargeOrderService;
    // 注入充值商品服务
    private final AiRechargeGoodsService aiRechargeGoodsService;

    // 注入支付宝回调事务服务
    private final AlipayCallbackService alipayCallbackService;

    // 浏览器直接访问：http://localhost:8080/alipay/pay?orderId=123456
    /**
     * 支付宝支付测试接口
     * 前端通过 window.location.href 直接跳转访问
     */
    @Operation(
            summary = "支付宝电脑网站支付（页面跳转）",
            description = """
                发起支付宝支付，浏览器自动跳转到支付宝收银台：
                1. 登录态接口，需携带有效的JWT Token；
                2. 接口1分钟内最多调用20次，防止高频调用；
                3. 直接返回支付宝自动提交的Form表单HTML，无需JSON返回体；
                4. 必传参数：orderId 业务订单ID。
                """
    )
    @GetMapping("/pay")
    @RateLimit(seconds = 60, maxCount = 20) // 限流：1分钟最多20次
    public void payTest(HttpServletResponse response,
                        @RequestParam Long orderId) throws AlipayApiException, IOException {

        // ===================== 核心修改：查询业务数据 =====================
        // 1. 根据订单ID查询充值订单
        AiRechargeOrder rechargeOrder = aiRechargeOrderService.getRechargeOrderById(orderId);
        if (rechargeOrder == null) {
            response.setContentType("text/html;charset=UTF-8");
            response.getWriter().write("订单不存在，orderId：" + orderId);
            return;
        }

        // 2. 从订单中获取商品ID，查询商品信息
        Long goodsId = rechargeOrder.getGoodsId();
        AiRechargeGoods rechargeGoods = aiRechargeGoodsService.getRechargeGoodsById(goodsId);
        if (rechargeGoods == null) {
            response.setContentType("text/html;charset=UTF-8");
            response.getWriter().write("商品不存在，goodsId：" + goodsId);
            return;
        }
        // =================================================================

        // 3. 构建支付参数（全部使用业务数据库真实数据）
        AlipayPayDTO payDTO = new AlipayPayDTO();
        // ✅ 商户订单号 = 传入的业务订单ID（核心需求）
        payDTO.setOutTradeNo(String.valueOf(orderId));
        // ✅ 订单标题 = 商品名称
        payDTO.setSubject(rechargeGoods.getName());
        // ✅ 支付金额 = 商品金额
        payDTO.setTotalAmount(rechargeGoods.getAmount());

        // 4. 调用Service生成支付表单
        String payForm = alipayPayService.generateWapPayForm(payDTO);

        // 5. 输出HTML表单到浏览器（自动跳转支付）
        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().write(payForm);
    }

    @PostMapping("/notify")
    public String alipayNotify(HttpServletRequest request) {
        try {
            // 1. 转换支付宝回调参数
            Map<String, String> params = new HashMap<>();
            Map<String, String[]> requestParams = request.getParameterMap();
            for (String key : requestParams.keySet()) {
                String[] values = requestParams.get(key);
                StringBuilder valueStr = new StringBuilder();
                for (int i = 0; i < values.length; i++) {
                    valueStr.append(values[i]);
                    if (i != values.length - 1) {
                        valueStr.append(",");
                    }
                }
                params.put(key, valueStr.toString());
            }

            System.out.println("支付宝回调参数：" + params);

            // 2. 验签
            boolean signVerified = AlipaySignature.rsaCheckV1(
                    params,
                    AlipayConfig.ALIPAY_PUBLIC_KEY,
                    AlipayConfig.CHARSET,
                    AlipayConfig.SIGN_TYPE
            );

            if (!signVerified) {
                System.out.println("❌ 验签失败");
                return "fail";
            }

            System.out.println("✅ 验签通过");
            String tradeStatus = params.get("trade_status");
            if ("TRADE_SUCCESS".equals(tradeStatus)) {
                // 3. 核心：调用事务业务类处理
                Long orderId = Long.parseLong(params.get("out_trade_no"));
                alipayCallbackService.rechargeCallback(orderId);
                System.out.println("✅ 订单支付+积分增加 全部完成！订单号：" + orderId);
            }

            // 必须返回success
            return "success";

        } catch (Exception e) {
            e.printStackTrace();
            // 抛出异常 → 事务回滚
            return "fail";
        }
    }

    // ===================== 【核心：本地测试接口】 =====================
    // 用途：手动传入 orderId，直接执行 订单支付 + 积分增加 逻辑
    // 访问地址示例：http://localhost:8080/alipay/test/recharge?orderId=2057459579284881408
    @GetMapping("/test/recharge")
    public String testRechargeCallback(@RequestParam("orderId") Long orderId) {
        try {
            System.out.println("【本地测试】开始处理订单：" + orderId);

            // 🔥 直接调用你原本的核心业务方法（和支付宝回调里的逻辑一模一样）
            alipayCallbackService.rechargeCallback(orderId);

            System.out.println("【本地测试】✅ 订单支付+积分增加 全部完成！订单号：" + orderId);
            return "测试成功：订单 " + orderId + " 处理完成！";

        } catch (Exception e) {
            e.printStackTrace();
            return "测试失败：" + e.getMessage();
        }
    }

    // 🔥 新增：手动查询订单接口（GET请求，浏览器直接访问测试）
    @GetMapping("/query")
    public String queryOrder(String outTradeNo) throws AlipayApiException {
        // 1. 创建请求 + 模型对象（和官方Demo完全一致）
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        AlipayTradeQueryModel model = new AlipayTradeQueryModel();

        // 只设置商户订单号（你只需要这个，其他参数删掉）
        model.setOutTradeNo(outTradeNo);

        // 绑定模型
        request.setBizModel(model);

        // 2. 调用接口
        AlipayTradeQueryResponse response = alipayClient.execute(request);

        // 3. 状态判断（你写的逻辑完美保留）
        if (response.isSuccess()) {
            String tradeStatus = response.getTradeStatus();
            System.out.println("订单状态：" + tradeStatus);

            if ("WAIT_BUYER_PAY".equals(tradeStatus)) {
                return "⌛ 交易创建，等待买家付款：" + outTradeNo;
            }
            if ("TRADE_CLOSED".equals(tradeStatus)) {
                return "❌ 未付款交易超时关闭，或支付完成后全额退款：" + outTradeNo;
            }
            if ("TRADE_SUCCESS".equals(tradeStatus)) {
                return "✅ 交易支付成功：" + outTradeNo;
            }
            if ("TRADE_FINISHED".equals(tradeStatus)) {
                return "✅ 交易结束，不可退款：" + outTradeNo;
            }

            return "ℹ️ 订单原始状态：" + tradeStatus + "，订单号：" + outTradeNo;
        } else {
            return "❌ 查询失败：" + response.getMsg() + "，错误码：" + response.getCode() + "，订单号：" + outTradeNo;
        }
    }

}
