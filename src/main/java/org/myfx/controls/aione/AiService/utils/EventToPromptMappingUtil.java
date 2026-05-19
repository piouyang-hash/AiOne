package org.myfx.controls.aione.AiService.utils;

import org.myfx.controls.aione.ServiceCommon.entity.feign.EventRecordResponseDTO;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static org.yaml.snakeyaml.nodes.Tag.STR;

/**
 * 事件/地点 -> AI提示词 映射工具
 * 功能：根据地点描述、事件描述，生成标准话术：我在{地点}做了{事件}
 */
@Component
public class EventToPromptMappingUtil {

    // ======================== 【核心：配置字典】 ========================
    // 1. 地点映射字典：简洁名称 → 生活化详细描述（你只需要在这里加新地点！）
    private static final Map<String, String> LOCATION_MAP = new HashMap<>();
    // 2. 事件映射字典：简洁名称 → 生活化自然描述（你只需要在这里加新事件！）
    private static final Map<String, String> EVENT_MAP = new HashMap<>();

    // 静态初始化：提前把你的日常生活场景配置好
    static {
        // ========== 地点映射（你之前的所有地点） ==========
        LOCATION_MAP.put("教室", "A栋教学楼二楼阶梯教室");
        LOCATION_MAP.put("寝室", "6B332宿舍");
        LOCATION_MAP.put("食堂", "东苑食堂");
        LOCATION_MAP.put("图书馆", "龙湖图书馆");
        LOCATION_MAP.put("琴房", "音乐教室琴房");
        LOCATION_MAP.put("卫生间", "教学楼卫生间");
        LOCATION_MAP.put("操场", "东门操场");
        LOCATION_MAP.put("大学", "大学校园");
        LOCATION_MAP.put("澡堂", "东苑澡堂");

        // ========== 事件映射（你每日循环的所有事件） ==========
        EVENT_MAP.put("上课", "上计算机网络课程");
        EVENT_MAP.put("睡觉", "睡觉");
        EVENT_MAP.put("写代码", "编写代码");
        EVENT_MAP.put("自习", "自习");
        EVENT_MAP.put("早饭", "吃早餐，两个包子，一个鸡蛋");
        EVENT_MAP.put("午饭", "吃午餐，西兰花和辣椒小炒肉配饭");
        EVENT_MAP.put("晚饭", "吃晚餐，天天面条的青椒肉丝面");
        EVENT_MAP.put("上厕所", "上卫生间");
        EVENT_MAP.put("弹钢琴", "练习钢琴");
        EVENT_MAP.put("休息", "放松");
        EVENT_MAP.put("打篮球", "打篮球");
        EVENT_MAP.put("跑步", "跑步锻炼");
        EVENT_MAP.put("散步", "漫无目的的闲逛");
        EVENT_MAP.put("骑行", "校园骑行");
        EVENT_MAP.put("洗澡", "洗漱洗澡");
    }

    /**
     * 适配EventRecordResponseDTO的事件提示词构建方法
     * @param dto 事件记录响应DTO
     * @return 生活化日常提示词
     */
    public String buildEventPromptFromDTO(EventRecordResponseDTO dto) {
        // 空值兜底
        if (dto == null) {
            return "暂无当前执行的事件，整个人都轻松自在～";
        }

        // 只提取 地点描述 + 事件描述
        String locationDesc = dto.getLocationDesc();
        String eventDesc = dto.getEventDesc();

        // 直接调用转换方法，返回结果
        return convertToDailyPrompt(locationDesc, eventDesc);
    }

    // ======================== 核心转换方法 ========================
    /**
     * 转换为【生活化日常话术】
     * @param locationDesc 原始地点（寝室/教室/食堂...）
     * @param eventDesc 原始事件（睡觉/上课/自习...）
     * @return 自然的日常提示词
     */
    public String convertToDailyPrompt(String locationDesc, String eventDesc) {
        // 1. 空值保护
        String rawLocation = (locationDesc == null || locationDesc.isBlank()) ? "未知地点" : locationDesc;
        String rawEvent = (eventDesc == null || eventDesc.isBlank()) ? "未知事件" : eventDesc;

        // 2. 查字典替换：有配置就用生活化描述，没有就用原值
        String realLocation = LOCATION_MAP.getOrDefault(rawLocation, rawLocation);
        String realEvent = EVENT_MAP.getOrDefault(rawEvent, rawEvent);

        // 3. 生成【最贴合日常】的话术（完全自然，不生硬）
        return String.format("在%s%s", realLocation, realEvent);
    }
}