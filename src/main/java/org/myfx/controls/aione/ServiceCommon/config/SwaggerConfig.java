package org.myfx.controls.aione.ServiceCommon.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    // 从配置文件读取标题（指定默认值，避免未配置时报错）
    @Value("${swagger.title}")
    private String title;

    // 从配置文件读取描述
    @Value("${swagger.description}")
    private String description;

    // 从配置文件读取版本
    @Value("${swagger.version}")
    private String version;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title(title)  // 使用注入的标题
                        .description(description)  // 使用注入的描述
                        .version(version));  // 使用注入的版本
    }
}