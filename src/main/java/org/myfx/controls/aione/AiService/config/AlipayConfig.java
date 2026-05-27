package org.myfx.controls.aione.AiService.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 支付宝配置类（Spring 配置注入版）
 */
@Getter
@Component // 交给Spring容器管理
public class AlipayConfig {

    // ===================== Getter 方法（必须添加，用于获取配置） =====================
    /**
     * 沙箱网关地址
     */
    @Value("${alipay.gateway-url}")
    private String gatewayUrl;

    /**
     * 沙箱应用APPID
     */
    @Value("${alipay.app-id}")
    private String appId;

    /**
     * 应用私钥（PKCS8 格式）
     */
    @Value("${alipay.app-private-key}")
    private String appPrivateKey;

    /**
     * 支付宝公钥
     */
    @Value("${alipay.alipay-public-key}")
    private String alipayPublicKey;

    /**
     * 返回数据格式
     */
    @Value("${alipay.format}")
    private String format;

    /**
     * 编码格式
     */
    @Value("${alipay.charset}")
    private String charset;

    /**
     * 签名类型
     */
    @Value("${alipay.sign-type}")
    private String signType;

    /**
     * 支付成功同步跳转地址
     */
    @Value("${alipay.return-url}")
    private String returnUrl;

    /**
     * 支付异步通知地址
     */
    @Value("${alipay.notify-url}")
    private String notifyUrl;

}