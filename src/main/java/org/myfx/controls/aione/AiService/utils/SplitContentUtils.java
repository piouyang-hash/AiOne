package org.myfx.controls.aione.AiService.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import java.util.Map;
import java.util.stream.StreamSupport;

/**
 * SplitContent JSON 解析工具类
 * 核心功能：快速获取splitContentJson中的最后一句话（优先用total字段，兜底反向遍历）
 */
@Slf4j
public class SplitContentUtils {
    // 单例ObjectMapper（复用，避免重复创建）
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 从splitContentJson中提取最后一句话（适配【内容+时间戳】新格式）
     * JSON格式：{"total":N,"segments":{"1":{"content":"xxx","timestamp":123456},"2":{"content":"xxx","timestamp":123457}...}}
     * @param splitContentJson 带时间戳的分段JSON字符串
     * @return 最后一句话的文本内容（null=解析失败/JSON为空）
     */
    public static String getLastSegment(String splitContentJson) {
        // 1. 空值快速返回（性能优化）
        if (splitContentJson == null || splitContentJson.trim().isEmpty()) {
            return null;
        }

        try {
            // 2. 解析JSON根节点（仅解析节点，不反序列化实体，性能更高）
            JsonNode rootNode = OBJECT_MAPPER.readTree(splitContentJson);

            // 3. 优先用total字段直接定位（O(1)，无遍历，最快）
            JsonNode totalNode = rootNode.get("total");
            if (totalNode != null && totalNode.isInt()) {
                int total = totalNode.asInt();
                if (total > 0) {
                    JsonNode segmentsNode = rootNode.get("segments");
                    if (segmentsNode != null && segmentsNode.has(String.valueOf(total))) {
                        JsonNode segmentNode = segmentsNode.get(String.valueOf(total));
                        return segmentNode.get("content").asText();
                    }
                }
            }

            // 4. 降级方案：反向遍历 segments（手动转换 Iterator）
            JsonNode segmentsNode = rootNode.get("segments");
            if (segmentsNode != null && segmentsNode.isObject() && !segmentsNode.isEmpty()) {

                // 将 Iterator 包装成 Iterable 从而使用 Stream
                Iterable<Map.Entry<String, JsonNode>> iterable = segmentsNode::fields;

                return StreamSupport.stream(iterable.spliterator(), false)
                        .max((entry1, entry2) -> {
                            try {
                                // 比较数字大小，选出最大的 Key
                                return Integer.compare(
                                        Integer.parseInt(entry1.getKey()),
                                        Integer.parseInt(entry2.getKey())
                                );
                            } catch (NumberFormatException e) {
                                // 如果不是数字，回退到字符串比较
                                return entry1.getKey().compareTo(entry2.getKey());
                            }
                        })
                        .map(entry -> entry.getValue().get("content").asText())
                        .orElse(null);
            }

            // 5. 无segments节点
            log.warn("splitContentJson解析失败：未找到segments节点，JSON={}", splitContentJson);
            return null;

        } catch (Exception e) {
            // 解析异常（JSON格式错误等）
            log.warn("splitContentJson解析异常，JSON={}", splitContentJson, e);
            return null;
        }
    }

    // ===================== 新增：获取切分消息总数 =====================
    /**
     * 从splitContentJson中提取切分消息总数（total字段）
     * JSON格式：{"total":N,"segments":{"1":{},"2":{}...}}
     * @param splitContentJson 分段JSON字符串
     * @return 切分消息总数，解析失败/空值返回 0
     */
    public static Integer getSplitTotalCount(String splitContentJson) {
        // 1. 空值快速返回0
        if (splitContentJson == null || splitContentJson.trim().isEmpty()) {
            return 0;
        }
        try {
            // 2. 解析JSON
            JsonNode rootNode = OBJECT_MAPPER.readTree(splitContentJson);
            // 3. 提取total字段
            JsonNode totalNode = rootNode.get("total");
            if (totalNode != null && totalNode.isInt()) {
                return totalNode.asInt();
            }
        } catch (Exception e) {
            // 解析异常打印日志，返回0，不影响主事务
            log.error("解析切分消息总数失败，json内容：{}", splitContentJson, e);
        }
        // 默认返回0
        return 0;
    }
}