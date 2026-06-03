package org.myfx.controls.aione.AiService.config;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration // 声明这是Spring配置类
public class AlipayBeanConfig {

    // 注入你的支付宝配置Bean
    private final AlipayConfig alipayConfig;

    public AlipayBeanConfig(AlipayConfig alipayConfig) {
        this.alipayConfig = alipayConfig;
    }

    // 交给Spring管理的Bean（修复后）
    @Bean
    public AlipayClient alipayClient() {
        return new DefaultAlipayClient(
                alipayConfig.getGatewayUrl(),       // 用getter获取，正确！
                alipayConfig.getAppId(),
                alipayConfig.getAppPrivateKey(),
                alipayConfig.getFormat(),
                alipayConfig.getCharset(),
                alipayConfig.getAlipayPublicKey(),
                alipayConfig.getSignType()
        );
    }
}