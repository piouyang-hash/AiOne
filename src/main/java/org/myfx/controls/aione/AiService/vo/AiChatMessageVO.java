package org.myfx.controls.aione.AiService.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.myfx.controls.aione.AiService.common.ai_chat_db.ChatRoleEnum;
import org.myfx.controls.aione.AiService.entity.ai_chat_db.AiChatMessage;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * AI对话消息VO（视图对象）
 * 用于前端展示/接口返回，剔除数据库主键id，仅保留业务相关字段
 */
@Data
@Schema(name = "AiChatMessageVO", description = "AI对话消息明细表-视图对象")
public class AiChatMessageVO {

    /**
     * 会话UUID（标准UUID v4格式，前端交互核心标识）
     */
    @Schema(description = "会话UUID（标准UUID v4格式）",
            example = "3b9e4f9a-8346-4b0f-9d1e-8f7c6a5b4d3e")
    private String sessionUuid;

    /**
     * 消息主键（雪花ID）
     */
    @Schema(description = "消息主键（雪花ID）", example = "1789234567890123457")
    private String messageId;

    /**
     * 归属用户ID（0=匿名，>0=登录用户）
     */
    @Schema(description = "归属用户ID（0=匿名，>0=登录用户）", example = "1")
    private Integer userId;

    @Schema(description = "流式任务ID（传给前端，让前端可以正确的编排消息）")
    private String taskId;

    /**
     * 消息角色（1=用户，2=AI助手，3=SpringBoot系统）
     */
    @Schema(description = "消息角色（1=用户，2=AI助手，3=SpringBoot系统）", example = "1")
    private ChatRoleEnum role;

    /**
     * 消息内容
     */
    @Schema(description = "消息内容", example = "你好呀")
    private String content;

    /**
     * 消息切分后的段落列表（业务使用，不入库）
     * 【修改】从 List<String> 改为 List<SegmentItem>，包含文本+时间戳
     */
    @Schema(description = "消息切分后的段落列表（业务使用，不入库），包含内容和生成时间戳")
    private List<AiChatMessage.SegmentItem> splitContent;

    /**
     * 前端排序专用时间戳（消息完成瞬间毫秒级）
     */
    @Schema(description = "前端排序专用时间戳(毫秒级)", example = "1744056789000")
    private String sortTimestamp;

    /**
     * 消息创建时间
     */
    @Schema(description = "消息创建时间", example = "2025-12-30 10:01:00")
    private LocalDateTime createTime;

    /**
     * 设置消息主键（雪花ID）
     * @param messageId 消息主键（雪花ID），Long类型，内部会转换为String格式存储
     */
    public void setMessageId(Long messageId) {
        // 处理空值：若传入null则赋值null，否则将Long转为String
        this.messageId = messageId != null ? messageId.toString() : null;
    }

    /**
     * 设置前端排序专用时间戳
     * @param sortTimestamp 排序时间戳，Long类型，内部会转换为String格式存储
     */
    public void setSortTimestamp(Long sortTimestamp) {
        // 处理空值：若传入null则赋值null，否则将Long转为String
        this.sortTimestamp = sortTimestamp != null ? sortTimestamp.toString() : null;
    }

    // ==================== 重写的setter方法 ====================
    /**
     * 重写set方法：入参仍为SplitContent，内部转换为 List<SegmentItem>
     * @param splitContent 原分段实体（包含total和segments）
     */
    public void setSplitContent(AiChatMessage.SplitContent splitContent) {
        // 初始化空列表，避免空指针
        List<AiChatMessage.SegmentItem> segmentList = new ArrayList<>();

        // 1. 入参为空时，直接赋值空列表
        if (splitContent == null) {
            this.splitContent = segmentList;
            return;
        }

        // 2. 【关键修改】获取 Segments：从 Map<String, String> 改为 Map<String, SegmentItem>
        Map<String, AiChatMessage.SegmentItem> segments = splitContent.getSegments();
        if (segments != null && !segments.isEmpty()) {
            // 按分段序号（数字序）排序后，提取内容存入List
            segmentList = segments.entrySet().stream()
                    // 把字符串序号转数字排序（解决1,10,2的问题）
                    .sorted((entry1, entry2) -> {
                        try {
                            int num1 = Integer.parseInt(entry1.getKey());
                            int num2 = Integer.parseInt(entry2.getKey());
                            return Integer.compare(num1, num2);
                        } catch (NumberFormatException e) {
                            // 非数字序号退回到字典序
                            return entry1.getKey().compareTo(entry2.getKey());
                        }
                    })
                    // 【修改】直接获取 SegmentItem 对象，保留content+timestamp
                    .map(Map.Entry::getValue)
                    .collect(Collectors.toList());
        }

        // 3. 赋值给splitContent字段（List<SegmentItem>类型）
        this.splitContent = segmentList;
    }

}
