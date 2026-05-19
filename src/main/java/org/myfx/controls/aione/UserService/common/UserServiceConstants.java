package org.myfx.controls.aione.UserService.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 用户服务常量与配置管理类
 * 集中维护用户业务相关的固定常量和动态配置项
 */
@Component
public class UserServiceConstants {

    /**
     * 业务描述：书籍（用于日志、通用方法的业务类型标识）
     */
    public static final String USER_BUSINESS_DESC = "用户";

    /**
     * 业务类型
     * 头像默认公有
     */
    public static final Integer SHARE_ENABLED = 1;

    /**
     * 业务类型描述：头像
     */
    public static final String AVATAR_BUSINESS_TYPE = "头像";

    /**
     * 默认头像文件名（基础名）
     */
    public static String DEFAULT_AVATAR = "default_avatar.png";

    // ------------------- 动态配置项（从配置文件读取） -------------------
    /**
     * 用户头像存储的硬盘路径（物理目录）
     */
    public static String AVATAR_STORAGE_PATH;

    /**
     * 用户头像访问的URL前缀（网络访问路径）
     */
    public static String AVATAR_URL_PREFIX;


    // ------------------- 配置注入Setter方法 -------------------
    /**
     * 注入头像存储的硬盘路径，并拼接默认头像完整路径
     */
    @Value("${avatar.storage.path}")
    public void setAvatarStoragePath(String avatarStoragePath) {
        AVATAR_STORAGE_PATH = avatarStoragePath;
        // 拼接默认头像的完整物理路径（如：D:/my-user-db/avatar/default_avatar.png）
        DEFAULT_AVATAR = avatarStoragePath + "/" + DEFAULT_AVATAR;
    }

    /**
     * 注入头像访问的URL前缀
     */
    @Value("${avatar.resource.url-prefix}")
    public void setAvatarUrlPrefix(String avatarUrlPrefix) {
        AVATAR_URL_PREFIX = avatarUrlPrefix;
    }

}