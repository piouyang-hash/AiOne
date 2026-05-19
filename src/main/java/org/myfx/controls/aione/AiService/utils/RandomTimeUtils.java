package org.myfx.controls.aione.AiService.utils;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * 随机时间生成工具类：生成指定数量、指定范围内不重复的随机时间点
 */
public class RandomTimeUtils {

    // 全局随机数实例，避免频繁创建
    private static final Random RANDOM = new Random();

    /**
     * 生成指定数量的不重复随机时间点
     * @param count 要生成的时间点数量（如5次）
     * @param start 时间范围起始（包含，如00:00:00）
     * @param end 时间范围结束（包含，如23:59:59）
     * @return 排序后的随机时间列表（便于查看）
     * @throws IllegalArgumentException 入参不合法时抛出
     */
    public static List<LocalTime> generateRandomTimes(int count, LocalTime start, LocalTime end) {
        // 入参校验：避免无效参数
        if (count <= 0) {
            throw new IllegalArgumentException("触发次数必须大于0，当前值：" + count);
        }
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("起始时间不能晚于结束时间！起始：" + start + "，结束：" + end);
        }

        // 转换为一天中的秒数（便于随机生成）
        long startSeconds = start.toSecondOfDay();
        long endSeconds = end.toSecondOfDay();
        long totalSeconds = endSeconds - startSeconds + 1;

        // 若请求数量超过总秒数，直接返回所有时间（避免死循环）
        if (count >= totalSeconds) {
            List<LocalTime> allTimes = new ArrayList<>();
            for (long sec = startSeconds; sec <= endSeconds; sec++) {
                allTimes.add(LocalTime.ofSecondOfDay(sec));
            }
            return allTimes;
        }

        // 生成不重复的随机秒数
        List<Long> randomSeconds = new ArrayList<>();
        while (randomSeconds.size() < count) {
            // 生成[startSeconds, endSeconds]范围内的随机数
            long randomSec = startSeconds + RANDOM.nextLong(endSeconds - startSeconds + 1);
            if (!randomSeconds.contains(randomSec)) {
                randomSeconds.add(randomSec);
            }
        }

        // 排序（可选，便于日志查看执行顺序）
        Collections.sort(randomSeconds);

        // 转换为LocalTime并返回
        List<LocalTime> randomTimes = new ArrayList<>();
        for (long sec : randomSeconds) {
            randomTimes.add(LocalTime.ofSecondOfDay(sec));
        }
        return randomTimes;
    }

    // 2. 新增方法：生成「当前分钟内」的随机秒数（核心测试用）
    public static List<Integer> generateRandomSecondsInMinute(int count, int startSec, int endSec) {
        if (count <= 0) {
            throw new IllegalArgumentException("触发次数必须大于0，当前值：" + count);
        }
        if (startSec < 0 || endSec > 59 || startSec > endSec) {
            throw new IllegalArgumentException("秒数范围必须是 0-59 且起始≤结束！当前：" + startSec + "-" + endSec);
        }

        // 生成5个不重复的秒数（0-59）
        List<Integer> randomSeconds = new ArrayList<>();
        while (randomSeconds.size() < count) {
            int sec = RANDOM.nextInt(endSec - startSec + 1) + startSec;
            if (!randomSeconds.contains(sec)) {
                randomSeconds.add(sec);
            }
        }
        Collections.sort(randomSeconds); // 排序，便于日志查看
        return randomSeconds;
    }
}