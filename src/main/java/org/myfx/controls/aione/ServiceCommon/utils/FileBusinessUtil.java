package org.myfx.controls.aione.ServiceCommon.utils;

import org.myfx.controls.aione.ServiceCommon.config.FileStorageConfig;
import org.myfx.controls.aione.ServiceCommon.serviceEnum.FileBusinessTypeEnum;

/**
 * 文件业务工具类（纯静态版）
 * 提供：根据枚举获取物理路径、网络路径、最大文件大小
 */
public class FileBusinessUtil {

    // 私有构造，禁止实例化
    private FileBusinessUtil() {}

    /**
     * 根据枚举 获取【物理存储路径】
     */
    public static String getPhysicalPath(FileBusinessTypeEnum typeEnum) {
        return switch (typeEnum) {
            case AI_ROLE_AVATAR -> FileStorageConfig.getAiRoleAvatarPhysicalPath();
            case USER_AVATAR -> FileStorageConfig.getUserAvatarPhysicalPath();
            case BOOK_COVER -> FileStorageConfig.getBookCoverPhysicalPath();
        };
    }

    /**
     * 根据枚举 获取【网络访问路径】
     */
    public static String getNetworkUrl(FileBusinessTypeEnum typeEnum) {
        return switch (typeEnum) {
            case AI_ROLE_AVATAR -> FileStorageConfig.getAiRoleAvatarNetworkUrl();
            case USER_AVATAR -> FileStorageConfig.getUserAvatarNetworkUrl();
            case BOOK_COVER -> FileStorageConfig.getBookCoverNetworkUrl();
        };
    }

    /**
     * 根据枚举 获取【最大文件大小限制】
     */
    public static String getMaxFileSize(FileBusinessTypeEnum typeEnum) {
        return switch (typeEnum) {
            case AI_ROLE_AVATAR -> FileStorageConfig.getAiRoleAvatarMaxFileSize();
            case USER_AVATAR -> FileStorageConfig.getUserAvatarMaxFileSize();
            case BOOK_COVER -> FileStorageConfig.getBookCoverMaxFileSize();
        };
    }
}