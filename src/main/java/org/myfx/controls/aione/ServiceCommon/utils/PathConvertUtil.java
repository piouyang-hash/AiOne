package org.myfx.controls.aione.ServiceCommon.utils;

import java.nio.file.Path;
import java.nio.file.Paths;

public class PathConvertUtil {

    /**
     * 物理路径转前端可访问的URL
     * @param physicalPath 本地物理路径（如D:/my-book-db/covers/xxx.jpg）
     * @param urlPrefix 对应URL前缀（如/covers）
     * @param localDir 本地目录（如D:/my-book-db/covers）
     * @return 前端可访问的URL（如/covers/xxx.jpg）
     */
    public static String physicalPathToUrl(String physicalPath, String urlPrefix, String localDir) {
        if (physicalPath == null || physicalPath.isEmpty()) {
            return null;
        }

        // 1. 处理Windows路径分隔符（\转/）
        String normalizedPath = physicalPath.replace("\\", "/");
        String normalizedLocalDir = localDir.replace("\\", "/");

        // 2. 获取文件名（或相对路径）
        Path path = Paths.get(normalizedPath);
        Path localPath = Paths.get(normalizedLocalDir);
        Path relativePath = localPath.relativize(path); // 得到相对于本地目录的路径

        // 3. 拼接URL前缀
        return urlPrefix + "/" + relativePath.toString().replace("\\", "/");
    }

    // ===================== 🔥 新增方法：相对路径 + URL前缀 拼接完整URL =====================
    /**
     * 相对路径拼接URL前缀 → 生成前端可访问的完整URL（适配AI头像/文件场景）
     * @param relativePath 文件相对路径（例：deepseek.png、avatar/1.png）
     * @param urlPrefix 前端访问URL前缀（例：/api/ai/resources/role-avatars）
     * @return 前端可直接访问的完整URL（例：/api/ai/resources/role-avatars/deepseek.png）
     */
    public static String relativePathToUrl(String relativePath, String urlPrefix) {
        // 空值判断
        if (relativePath == null || relativePath.isBlank() || urlPrefix == null || urlPrefix.isBlank()) {
            return null;
        }

        // 统一分隔符 + 去除首尾多余斜杠（防止拼接出 // ）
        String prefix = urlPrefix.replace("\\", "/").replaceAll("/$", ""); // 移除前缀末尾的 /
        String relative = relativePath.replace("\\", "/").replaceAll("^/", ""); // 移除相对路径开头的 /

        // 拼接最终URL
        return prefix + "/" + relative;
    }
}