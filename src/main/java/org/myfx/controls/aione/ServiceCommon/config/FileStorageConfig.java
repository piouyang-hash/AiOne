package org.myfx.controls.aione.ServiceCommon.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * 文件存储配置类 - 单体应用版
 * 项目启动时加载配置，无动态刷新，支持全局静态调用
 */
@Getter
@Component
public class FileStorageConfig {

    // ===================== 静态Getter（调用方式完全不变） =====================
    // ===================== 静态配置变量 =====================
    // AI角色头像
    @Getter
    private static String aiRoleAvatarPhysicalPath;
    @Getter
    private static String aiRoleAvatarNetworkUrl;
    @Getter
    private static String aiRoleAvatarMaxFileSize;

    // 用户头像
    @Getter
    private static String userAvatarPhysicalPath;
    @Getter
    private static String userAvatarNetworkUrl;
    @Getter
    private static String userAvatarMaxFileSize;

    // 书籍封面
    @Getter
    private static String bookCoverPhysicalPath;
    @Getter
    private static String bookCoverNetworkUrl;
    @Getter
    private static String bookCoverMaxFileSize;

    // ===================== 配置注入（实例变量接收） =====================
    @Value("${ai-service.ai-role-avatar.storage.path}")
    private String injectAiRoleAvatarPhysicalPath;

    @Value("${ai-service.ai-role-avatar.resource.url-prefix}")
    private String injectAiRoleAvatarNetworkUrl;

    @Value("${ai-service.ai-role-avatar.max-file-size}")
    private String injectAiRoleAvatarMaxFileSize;

    @Value("${user-service.user-avatar.storage.path}")
    private String injectUserAvatarPhysicalPath;

    @Value("${user-service.user-avatar.resource.url-prefix}")
    private String injectUserAvatarNetworkUrl;

    @Value("${user-service.user-avatar.max-file-size}")
    private String injectUserAvatarMaxFileSize;

    @Value("${book-service.book-cover.storage.path}")
    private String injectBookCoverPhysicalPath;

    @Value("${book-service.book-cover.resource.url-prefix}")
    private String injectBookCoverNetworkUrl;

    @Value("${book-service.book-cover.max-file-size}")
    private String injectBookCoverMaxFileSize;

    // ===================== 初始化赋值 + 自动创建文件夹（仅项目启动执行一次） =====================
    @PostConstruct
    public void initConfig() {
        // 1. 赋值配置到静态变量
        aiRoleAvatarPhysicalPath = injectAiRoleAvatarPhysicalPath;
        aiRoleAvatarNetworkUrl = injectAiRoleAvatarNetworkUrl;
        aiRoleAvatarMaxFileSize = injectAiRoleAvatarMaxFileSize;

        userAvatarPhysicalPath = injectUserAvatarPhysicalPath;
        userAvatarNetworkUrl = injectUserAvatarNetworkUrl;
        userAvatarMaxFileSize = injectUserAvatarMaxFileSize;

        bookCoverPhysicalPath = injectBookCoverPhysicalPath;
        bookCoverNetworkUrl = injectBookCoverNetworkUrl;
        bookCoverMaxFileSize = injectBookCoverMaxFileSize;

        // 2. 🔥 自动创建所有文件存储目录（核心代码）
        createDirectoryIfNotExists(aiRoleAvatarPhysicalPath);
        createDirectoryIfNotExists(userAvatarPhysicalPath);
        createDirectoryIfNotExists(bookCoverPhysicalPath);
    }

    /**
     * 工具方法：如果目录不存在，则递归创建（支持多级文件夹）
     */
    private void createDirectoryIfNotExists(String path) {
        if (path == null || path.isEmpty()) {
            return;
        }
        File directory = new File(path);
        // mkdirs()：自动创建父级目录 + 子目录，比 mkdir() 强大
        if (!directory.exists()) {
            boolean success = directory.mkdirs();
            if (success) {
                System.out.println("目录创建成功：" + path);
            } else {
                System.err.println("目录创建失败：" + path);
            }
        }
    }

}