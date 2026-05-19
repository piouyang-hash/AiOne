package org.myfx.controls.aione.AiService.utils;

import cn.hutool.core.util.StrUtil;
import org.myfx.controls.aione.ServiceCommon.entity.feign.EventRecordResponseDTO;

/**
 * 事件驱动AI提示词构建工具类
 * 专门封装事件相关的人性化提示词拼接逻辑，贴合AI人设（萌芽温柔风格）
 *
 * @author 开发者
 * @date 2026-03-14
 */
public class EventPromptWordUtil {

    /**
     * 构建人性化完整事件描述（基础方法，硬编码映射，用于AI提示词）
     * @param locationDesc 地点枚举（如HOME/SCHOOL/PARK/LIBRARY）
     * @param eventDesc 事件枚举（如SLEEP/EAT_BREAKFAST/WALK等）
     * @param eventDuration 事件持续时间（秒，可为null）
     * @return 完整事件描述文本，可直接作为{event_full_description}提示词
     */
    public static String buildEventFullDescription(String locationDesc, String eventDesc, Integer eventDuration) {
        // 1. 格式化持续时间为易读文本（秒→小时/分钟/秒，空值则用“一段时间”）
        String durationText = formatDurationToReadableText(eventDuration);

        // 2. 硬编码地点+事件的人性化基础描述（贴合萌芽温柔人设的语气）
        String baseDesc = getBaseEventDesc(locationDesc, eventDesc);

        // 3. 拼接成完整温柔风格的事件描述（符合萌芽人设的语气）
        return String.format("%s，持续了%s，整个人都放松又惬意。", baseDesc, durationText);
    }

    /**
     * 适配EventRecordResponseDTO的事件提示词构建方法（核心扩展方法）
     * 整合「开始时间+执行时长+预计完成时间」全维度信息，生成完整AI提示词
     * @param dto 事件记录响应DTO（包含位置/事件描述、时间维度信息）
     * @return 包含时间维度的完整人性化事件提示词
     */
    public static String buildEventPromptFromDTO(EventRecordResponseDTO dto) {
        // 1. 空值兜底（避免NPE）
        if (dto == null) {
            return "暂无当前执行的事件，整个人都轻松自在～";
        }

        // 2. 提取DTO字段（空值默认处理）
        String locationDesc = StrUtil.blankToDefault(dto.getLocationDesc(), "未知地点");
        String eventDesc = StrUtil.blankToDefault(dto.getEventDesc(), "日常小事");
        Integer actualStart = dto.getActualStart(); // 开始时间（秒）
        Integer executionTime = dto.getExecutionTime(); // 已执行时长（秒）
        Integer expireSeconds = dto.getRemainingSeconds(); // 预计完成剩余时间（秒）

        // 3. 格式化各时间维度为易读文本
        String startSecondText = formatDurationToReadableText(actualStart);
        String executionTimeText = formatDurationToReadableText(executionTime);
        String expireSecondsText = formatDurationToReadableText(expireSeconds);

        // 4. 基础事件描述（复用原有逻辑）
        String baseDesc = getBaseEventDesc(locationDesc, eventDesc);

        // 5. 拼接时间维度信息（温柔语气，适配AI人设）
        StringBuilder fullPrompt = new StringBuilder();
        fullPrompt.append(baseDesc);
        fullPrompt.append(String.format("，从%s开始执行", startSecondText));
        fullPrompt.append(String.format("，已经执行了%s", executionTimeText));

        // 6. 预计完成时间拼接（空值兜底）
        if (expireSeconds != null && expireSeconds > 0) {
            fullPrompt.append(String.format("，还需要%s就能完成啦，整个人都慢悠悠的～", expireSecondsText));
        } else {
            fullPrompt.append("，暂时还没确定完成时间，慢慢做就好啦～");
        }

        return fullPrompt.toString();
    }

    // ===================== 私有辅助方法（封装通用逻辑，提高可读性） =====================
    /**
     * 格式化时长为易读文本（秒→小时/分钟/秒，空值兜底）
     */
    private static String formatDurationToReadableText(Integer duration) {
        if (duration == null) {
            return "一段时间";
        } else if (duration >= 3600) {
            int hours = duration / 3600;
            return hours + "小时";
        } else if (duration >= 60) {
            int minutes = duration / 60;
            return minutes + "分钟";
        } else {
            return duration + "秒";
        }
    }

    /**
     * 获取地点+事件的基础人性化描述（硬编码映射，抽离为独立方法）
     */
    private static String getBaseEventDesc(String locationDesc, String eventDesc) {
        // 空值兜底
        String locDesc = StrUtil.blankToDefault(locationDesc, "未知地点");
        String evtDesc = StrUtil.blankToDefault(eventDesc, "日常小事");

        return switch (locDesc) {
            case "HOME" -> switch (evtDesc) {
                case "SLEEP" -> "在家中安稳入睡";
                case "EAT_BREAKFAST" -> "在家悠闲享用早餐";
                case "EAT_LUNCH" -> "在家享用丰盛午餐";
                case "EAT_DINNER" -> "在家享用温馨晚餐";
                case "GAME" -> "在家轻松玩会儿游戏";
                case "BEDTIME_REST" -> "在家进行睡前放松";
                case "READ_BOOK" -> "在家安静阅读";
                default -> "在家中度过日常时光";
            };
            case "SCHOOL" -> switch (evtDesc) {
                case "ATTEND_CLASS" -> "在学校认真上课";
                case "STUDY" -> "在学校专注学习";
                case "BREAK" -> "在学校课间休息";
                default -> "在学校参与日常学习";
            };
            case "PARK" -> switch (evtDesc) {
                case "WALK" -> "在公园悠闲散步";
                case "RUN" -> "在公园畅快跑步";
                case "REST" -> "在公园的长椅上休息";
                default -> "在公园享受户外时光";
            };
            case "LIBRARY" -> switch (evtDesc) {
                case "READ_BOOK" -> "在图书馆安静阅读";
                case "STUDY" -> "在图书馆沉浸学习";
                default -> "在图书馆专注提升自己";
            };
            default -> "在" + locDesc + "度过了一段轻松的时光，做了" + evtDesc + "这件事";
        };
    }
}