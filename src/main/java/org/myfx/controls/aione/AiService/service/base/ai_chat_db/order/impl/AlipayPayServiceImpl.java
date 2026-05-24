package org.myfx.controls.aione.AiService.service.base.ai_chat_db.order.impl;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeWapPayRequest;
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

    // ===================== 新增：手机H5版支付表单生成（官方标准） =====================
    @Override
    public String generateWapPayForm(AlipayPayDTO payDTO) throws AlipayApiException {
        // 1. 创建支付宝【手机H5端】支付请求对象（官方核心标识）
        AlipayTradeWapPayRequest request = new AlipayTradeWapPayRequest();
        // 2. 设置同步/异步回调地址（和电脑版共用配置）
        request.setReturnUrl(AlipayConfig.RETURN_URL);
        request.setNotifyUrl(AlipayConfig.NOTIFY_URL);

        // 3. 拼接支付宝要求的业务参数
        String bizContent = String.format("{"
                        + "\"out_trade_no\":\"%s\","
                        + "\"total_amount\":\"%s\","
                        + "\"subject\":\"%s\","
                        + "\"body\":\"%s\","
                        + "\"timeout_express\":\"%s\","
                        + "\"product_code\":\"QUICK_WAP_WAY\""
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

        // 5. 调用支付宝接口（官方固定用法：pageExecute），返回自动提交的HTML表单
        return alipayClient.pageExecute(request).getBody();
    }
}