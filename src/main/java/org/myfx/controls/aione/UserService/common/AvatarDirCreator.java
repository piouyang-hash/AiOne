package org.myfx.controls.aione.UserService.common;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

/**
 * 头像目录创建器
 * 应用启动时自动创建头像存储目录
 */
@Component
@Slf4j
public class AvatarDirCreator {

    @Autowired
    private ApplicationContext context;

    @PostConstruct
    public void checkHandlers() {
        // 打印所有异常处理器Bean
        Map<String, Object> handlers = context.getBeansWithAnnotation(RestControllerAdvice.class);
        log.info("Loaded exception handlers: {}", handlers.keySet());
    }

    // 头像存储根路径（从配置文件读取）
    @Value("${avatar.storage.path}")
    private String avatarStoragePath;

    @PostConstruct  // Spring容器初始化后自动执行
    public void createAvatarDir() {
        try {
            // 直接创建头像存储目录（包括不存在的父目录）
            Files.createDirectories(Paths.get(avatarStoragePath));

            log.info("✅ 头像存储目录创建成功: {}", avatarStoragePath);
        } catch (Exception e) {
            log.error("❌ 头像存储目录创建失败: {}", e.getMessage(), e);
        }
    }
}