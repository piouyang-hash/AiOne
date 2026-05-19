package org.myfx.controls.aione.ServiceCommon.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 静态资源映射：前端URL → 服务器本地文件
 */
@Configuration
public class WebMvcResourceConfig implements WebMvcConfigurer {

    // ===================== 直接读取配置（最简单、最稳定） =====================
    // AI角色头像
    @Value("${ai-service.ai-role-avatar.storage.path}")
    private String aiRoleAvatarPhysicalPath;
    @Value("${ai-service.ai-role-avatar.resource.url-prefix}")
    private String aiRoleAvatarNetworkUrl;

    // 用户头像
    @Value("${user-service.user-avatar.storage.path}")
    private String userAvatarPhysicalPath;
    @Value("${user-service.user-avatar.resource.url-prefix}")
    private String userAvatarNetworkUrl;

    // 书籍封面
    @Value("${book-service.book-cover.storage.path}")
    private String bookCoverPhysicalPath;
    @Value("${book-service.book-cover.resource.url-prefix}")
    private String bookCoverNetworkUrl;

    // ===================== 静态资源映射 =====================
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 1. AI角色头像
        registry.addResourceHandler(STR."\{aiRoleAvatarNetworkUrl}/**")
                .addResourceLocations(STR."file:\{aiRoleAvatarPhysicalPath}/");

        // 2. 用户头像
        registry.addResourceHandler(STR."\{userAvatarNetworkUrl}/**")
                .addResourceLocations(STR."file:\{userAvatarPhysicalPath}/");

        // 3. 书籍封面
        registry.addResourceHandler(STR."\{bookCoverNetworkUrl}/**")
                .addResourceLocations(STR."file:\{bookCoverPhysicalPath}/");
    }
}