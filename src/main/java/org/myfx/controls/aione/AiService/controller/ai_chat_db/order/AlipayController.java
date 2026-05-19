package org.myfx.controls.aione.AiService.controller.ai_chat_db.order;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.domain.AlipayTradeQueryModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.myfx.controls.aione.AiService.config.AlipayConfig;
import org.myfx.controls.aione.AiService.dto.order.AlipayPayDTO;
import org.myfx.controls.aione.AiService.service.base.ai_chat_db.order.AlipayPayService;
import org.myfx.controls.aione.ServiceCommon.annotation.RateLimit;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/alipay")
@RequiredArgsConstructor
public class AlipayController {

    // 【关键】注入Spring容器中的AlipayClient Bean（推荐构造器注入，Spring官方最佳实践）
    private final AlipayClient alipayClient;
    private final AlipayPayService alipayPayService;

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
        // 打印参数
        System.out.println("接收到的业务订单ID：" + orderId);

        // 1. 构建支付参数
        AlipayPayDTO payDTO = new AlipayPayDTO();
        payDTO.setOutTradeNo(UUID.randomUUID().toString().replace("-", ""));
        payDTO.setSubject("Vue-APP支付自定义内容测试");
        payDTO.setTotalAmount(new BigDecimal("0.01"));
        payDTO.setBody("这是商品的详细描述信息");

        System.out.println("生成支付宝商户订单号：" + payDTO.getOutTradeNo());

        // 2. 调用Service生成支付表单
        String payForm = alipayPayService.generatePayForm(payDTO);

        // 3. 输出HTML表单到浏览器（自动跳转支付）
        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().write(payForm);
    }


    @PostMapping("/notify")
    public String alipayNotify(HttpServletRequest request) {
        System.out.println("========== 收到支付宝异步通知 ==========");

        try {
            // 1. 把支付宝传来的所有参数 转成 Map（验签必需）
            Map<String, String> params = new HashMap<>();
            Map<String, String[]> requestParams = request.getParameterMap();
            for (String key : requestParams.keySet()) {
                String[] values = requestParams.get(key);
                String valueStr = "";
                for (int i = 0; i < values.length; i++) {
                    valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
                }
                params.put(key, valueStr);
            }

            // 打印所有参数
            System.out.println(params);

            // ==================== 核心：验签代码 ====================
            boolean signVerified = AlipaySignature.rsaCheckV1(
                    params,                     // 支付宝传来的所有参数
                    AlipayConfig.ALIPAY_PUBLIC_KEY,  // 你的支付宝公钥
                    AlipayConfig.CHARSET,            // 编码 UTF-8
                    AlipayConfig.SIGN_TYPE           // 签名类型 RSA2
            );

            // 2. 判断验签结果
            if (signVerified) {
                System.out.println("✅ 验签通过：确认为支付宝官方发送！");

                // 3. 验签通过后，再判断支付是否成功
                String tradeStatus = params.get("trade_status");
                if ("TRADE_SUCCESS".equals(tradeStatus)) {
                    System.out.println("✅ 支付成功！订单号：" + params.get("out_trade_no"));
                    // TODO 这里可以写业务代码：修改数据库订单状态为已支付
                }

                // 必须返回 success，支付宝才会停止推送
                return "success";

            } else {
                // 验签失败：伪造请求！
                System.out.println("❌ 验签失败：该请求为伪造，拒绝处理！");
                return "fail";
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "fail";
        }
    }

    // 🔥 新增：手动查询订单接口（GET请求，浏览器直接访问测试）
    @GetMapping("/query")
    public String queryOrder(String outTradeNo) throws AlipayApiException {
        System.out.println("========== 手动查询订单：" + outTradeNo + " ==========");

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

    // ================== 公网测试接口（新加的） ==================
    @GetMapping("/test")
    public String testNgrok() {
        return "✅ 公网访问成功！ngrok 穿透正常！";
    }
}
