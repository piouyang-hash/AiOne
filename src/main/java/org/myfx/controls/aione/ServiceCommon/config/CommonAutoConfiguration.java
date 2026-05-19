package org.myfx.controls.aione.ServiceCommon.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

// 标记为配置类，让 Spring 扫描到
@Configuration
// 启用配置绑定（如果之前有 JwtProperties 等自定义配置，这里可以一起注册）
@EnableConfigurationProperties
public class CommonAutoConfiguration {
    // 空类即可，作用是让 Spring 识别这是一个配置类，触发配置加载
}