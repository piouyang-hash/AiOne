package org.myfx.controls.aione.AiService.service.base.ai_chat_db.order.impl;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import lombok.RequiredArgsConstructor;
import org.myfx.controls.aione.AiService.config.AlipayConfig;
import org.myfx.controls.aione.AiService.dto.order.AlipayPayDTO;
import org.myfx.controls.aione.AiService.service.base.ai_chat_db.order.AlipayPayService;
import org.springframework.stereotype.Service;

/**
 * 支付宝支付业务实现类
 */
@Service
@RequiredArgsConstructor
public class AlipayPayServiceImpl implements AlipayPayService {

    // 注入支付宝客户端（你的配置类中已初始化的Bean）
    private final AlipayClient alipayClient;

    @Override
    public String generatePayForm(AlipayPayDTO payDTO) throws AlipayApiException {
        // 1. 创建支付宝电脑端支付请求对象
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        // 2. 设置同步/异步回调地址
        request.setReturnUrl(AlipayConfig.RETURN_URL);
        request.setNotifyUrl(AlipayConfig.NOTIFY_URL);

        // 3. 拼接支付宝要求的业务参数
        String bizContent = String.format("{"
                        + "\"out_trade_no\":\"%s\","
                        + "\"total_amount\":\"%s\","
                        + "\"subject\":\"%s\","
                        + "\"body\":\"%s\","
                        + "\"timeout_express\":\"%s\","
                        + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\""
                        + "}",
                payDTO.getOutTradeNo(),
                payDTO.getTotalAmount(),
                payDTO.getSubject(),
                // 空值安全处理
                payDTO.getBody() == null ? "" : payDTO.getBody(),
                payDTO.getTimeoutExpress()
        );

        // 4. 设置业务参数
        request.setBizContent(bizContent);

        // 5. 调用支付宝接口，返回自动提交的HTML表单
        return alipayClient.pageExecute(request).getBody();
    }
}