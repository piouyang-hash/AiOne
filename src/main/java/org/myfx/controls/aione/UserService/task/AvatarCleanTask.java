package org.myfx.controls.aione.UserService.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myfx.controls.aione.UserService.service.UserService;
import org.myfx.controls.aione.UserService.util.AvatarFileTaskUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class AvatarCleanTask {

    // 注入文件操作工具类（处理头像文件）和用户服务（获取所有用户ID）
    private final AvatarFileTaskUtil avatarFileUtil;
    private final UserService userService;


    // 从配置文件注入“保留的头像数量”，默认值3（防止配置缺失）
    @Value("${avatar.clean.reserve-count:3}")
    private int reserveCount;

    /**
     * 每3个月执行一次，选择凌晨1点（服务器负载最低的时间）
     * cron表达式：秒 分 时 日 月 周 → 0 0 1 1 3 ? （每月1号凌晨1点，每3个月一次）
     */
    @Scheduled(cron = "0 0 1 1 */3 ?")
    public void cleanAvatar() {
        log.info("===== 开始执行头像清理任务 =====");

        // 步骤1：获取所有用户ID（分批获取，避免一次性加载太多用户导致内存占用飙升）
        List<Integer> userIds = userService.getAllUserIdsByBatch(100); // 每次查100个用户ID

        // 步骤2：逐个用户处理（单用户单处理，避免并发资源竞争）
        for (Integer userId : userIds) {
            try {
                // 处理单个用户的头像清理
                cleanUserAvatar(userId);
                // 每处理1个用户，休眠100ms（主动让渡CPU，不阻塞其他任务）
                Thread.sleep(100);
            } catch (Exception e) {
                // 单个用户处理失败不中断整个任务，记录日志即可
                log.error("清理用户[{}]的头像失败", userId, e);
            }
        }

        log.info("===== 头像清理任务执行完成 =====");
    }

    // 处理单个用户的头像：保留最新3个，删除其余
    // 处理单个用户的头像清理（核心修改：用reserveCount替换硬编码的3）
    private void cleanUserAvatar(Integer userId) {
        // 步骤1：获取该用户的所有头像文件
        List<String> avatarFiles = avatarFileUtil.listUserAvatars(userId);
        // 不足保留数量，无需清理（用reserveCount替代3）
        if (avatarFiles.size() <= reserveCount) {
            return;
        }

        // 步骤2：按时间戳排序（使用抽离的比较器）
        List<String> sortedFiles = avatarFiles.stream()
                .sorted(timestampAscendingComparator()) // 直接调用封装好的比较器
                .toList();

        // 步骤3：保留最后N个（最新的），删除前面的（用reserveCount替代3）
        int needDeleteCount = sortedFiles.size() - reserveCount;
        for (int i = 0; i < needDeleteCount; i++) {
            String fileToDelete = sortedFiles.get(i);
            boolean deleted = avatarFileUtil.deleteAvatar(fileToDelete);
            if (deleted) {
                log.info("用户[{}]的头像[{}]已删除（保留最新{}个）", userId, fileToDelete, reserveCount);
            } else {
                log.warn("用户[{}]的头像[{}]删除失败", userId, fileToDelete);
            }
        }
    }

    /**
     * 按文件名中的时间戳升序排序（旧文件 → 新文件）
     */
    private Comparator<String> timestampAscendingComparator() {
        return (f1, f2) -> {
            long time1 = extractTimestamp(f1);
            long time2 = extractTimestamp(f2);
            return Long.compare(time1, time2); // 升序：时间戳小的排前面（旧文件）
        };
    }

    // 从文件名提取时间戳（如"avatar_1_1762868001873" → 1762868001873）
    private long extractTimestamp(String fileName) {
        String[] parts = fileName.split("_");
        return Long.parseLong(parts[2]);
    }
}