package org.myfx.controls.aione.UserService.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class AvatarFileTaskUtil {
    // 头像存储根路径（配置在application.yml中）
    @Value("${avatar.storage.path}")
    private String avatarRootPath;

    // 获取指定用户的所有头像文件（只查文件名，不加载文件内容，减少IO）
    public List<String> listUserAvatars(Integer userId) {
        File userAvatarDir = new File(avatarRootPath);
        if (!userAvatarDir.exists()) {
            return Collections.emptyList();
        }

        // 构造匹配前缀：用户ID + "-"（如"1-"）
        String prefix = userId + "-";
        // 筛选以"userId-"开头的文件
        File[] files = userAvatarDir.listFiles(f -> {
            // 确保是文件（非目录）且文件名以指定前缀开头
            return f.isFile() && f.getName().startsWith(prefix);
        });

        if (files == null || files.length == 0) {
            return Collections.emptyList();
        }

        // 映射为文件的绝对路径（替换原File::getName）
        return Arrays.stream(files)
                .map(File::getAbsolutePath) // 关键修改：获取绝对路径
                .collect(Collectors.toList());
    }

    /**
     * 公共方法：删除指定用户的所有头像文件
     * @param userId 用户ID
     * @return 删除结果：key=文件名，value=是否删除成功
     */
    public Map<String, Boolean> deleteAllUserAvatars(Integer userId) {
        Map<String, Boolean> deleteResult = new HashMap<>();

        // 1. 获取该用户的所有头像文件
        List<String> allAvatars = listUserAvatars(userId);
        if (allAvatars.isEmpty()) {
            return deleteResult;
        }

        // 2. 逐个删除所有头像
        for (String fileName : allAvatars) {
            boolean deleted = deleteAvatar(fileName);
            deleteResult.put(fileName, deleted);
        }

        return deleteResult;
    }

    // 删除指定头像文件（单个文件删除，避免批量删除导致IO阻塞）
    public boolean deleteAvatar(String fileName) {
        File file = new File(avatarRootPath + File.separator + fileName);
        if (file.exists() && file.isFile()) {
            return file.delete();
        }
        return false;
    }
}
