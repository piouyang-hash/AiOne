package org.myfx.controls.aione.AiService.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * 用户在线时间段解析工具类
 * 专门处理格式为 "HH:mm-HH:mm" 的时间段字符串，转换为LocalDateTime（基于当前日期）
 */
public class OnlineTimeRangeParserUtil {

    // 时间格式化器（严格匹配24小时制的HH:mm格式，补零，如09:00而非9:00）
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    // 私有构造方法：防止工具类被实例化
    private OnlineTimeRangeParserUtil() {
        throw new UnsupportedOperationException("工具类禁止实例化");
    }

    /**
     * 解析时间段字符串，获取开始时间（LocalDateTime，日期为当天）
     * @param timeRange 时间段字符串，格式必须为 "HH:mm-HH:mm"（如：09:00-22:00）
     * @return 开始时间（LocalDateTime），如 2026-01-12T09:00:00
     * @throws IllegalArgumentException 格式错误时抛出异常
     */
    public static LocalDateTime getStartTime(String timeRange) {
        // 1. 前置校验
        validateTimeRange(timeRange);
        // 2. 拆分并解析开始时间
        LocalTime startTime = parseStartTimeFromRange(timeRange);
        // 3. 结合当前日期转为LocalDateTime
        return LocalDateTime.of(LocalDate.now(), startTime);
    }

    /**
     * 解析时间段字符串，获取结束时间（LocalDateTime，日期为当天）
     * @param timeRange 时间段字符串，格式必须为 "HH:mm-HH:mm"（如：09:00-22:00）
     * @return 结束时间（LocalDateTime），如 2026-01-12T22:00:00
     * @throws IllegalArgumentException 格式错误时抛出异常
     */
    public static LocalDateTime getEndTime(String timeRange) {
        // 1. 前置校验
        validateTimeRange(timeRange);
        // 2. 拆分并解析结束时间
        LocalTime endTime = parseEndTimeFromRange(timeRange);
        // 3. 结合当前日期转为LocalDateTime
        return LocalDateTime.of(LocalDate.now(), endTime);
    }

    // ------------------- 私有辅助方法 -------------------

    /**
     * 校验时间段字符串格式是否合法
     */
    private static void validateTimeRange(String timeRange) {
        if (timeRange == null || timeRange.trim().isEmpty()) {
            throw new IllegalArgumentException("时间段字符串不能为空！");
        }
        String trimedRange = timeRange.trim();
        // 拆分后必须是2部分（开始时间-结束时间）
        String[] timeParts = trimedRange.split("-");
        if (timeParts.length != 2) {
            throw new IllegalArgumentException(
                    String.format("时间段格式错误！必须为 HH:mm-HH:mm 格式，当前值：%s", timeRange)
            );
        }
        // 校验开始/结束时间的格式（提前解析，快速失败）
        try {
            LocalTime.parse(timeParts[0].trim(), TIME_FORMATTER);
            LocalTime.parse(timeParts[1].trim(), TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    String.format("时间格式错误！必须为 HH:mm 格式（24小时制），当前值：%s", timeRange), e
            );
        }
    }

    /**
     * 从时间段字符串中拆分并解析开始时间（LocalTime）
     */
    private static LocalTime parseStartTimeFromRange(String timeRange) {
        String[] timeParts = timeRange.trim().split("-");
        return LocalTime.parse(timeParts[0].trim(), TIME_FORMATTER);
    }

    /**
     * 从时间段字符串中拆分并解析结束时间（LocalTime）
     */
    private static LocalTime parseEndTimeFromRange(String timeRange) {
        String[] timeParts = timeRange.trim().split("-");
        return LocalTime.parse(timeParts[1].trim(), TIME_FORMATTER);
    }
}