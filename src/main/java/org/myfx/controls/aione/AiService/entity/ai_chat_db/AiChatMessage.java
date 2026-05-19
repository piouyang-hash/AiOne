package org.myfx.controls.aione.AiService.entity.ai_chat_db;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import org.myfx.controls.aione.AiService.common.ai_chat_db.ChatRoleEnum;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * AI对话消息明细表实体
 *
 * @author xxx
 */
@Data
@TableName(value = "ai_chat_message", autoResultMap = true)
@Schema(name = "AiChatMessage", description = "AI对话消息明细表")
public class AiChatMessage {

    /**
     * 消息主键（雪花ID）
     */
    @TableId(type = IdType.INPUT) // 雪花ID手动传入，非自增
    @Schema(description = "消息主键（雪花ID）", example = "1789234567890123457")
    private Long messageId;

    /**
     * 会话ID（关联会话主表）
     */
    @Schema(description = "会话ID（雪花ID，同一轮对话共用）", example = "1789234567890123456")
    private Long sessionId;

    /**
     * 归属用户ID（0=匿名，>0=登录用户）
     */
    @Schema(description = "归属用户ID（0=匿名，>0=登录用户）", example = "1")
    private Integer userId;

    /**
     * 父消息ID：AI回复指向对应的用户消息ID，用户消息为null
     */
    @Schema(description = "父消息ID（AI回复关联对应的用户消息ID）", example = "1789234567890123457")
    private Long parentMsgId;

    /**
     * 🔥 新增：AI流式任务ID（UUID）
     */
    @Schema(description = "AI流式任务唯一ID(UUID)", example = "93bbafcf-b7ef-4729-9d63-bd66322e53ce")
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
     * 消息切分后的JSON字符串（对应数据库split_content字段，MyBatis映射用）
     */
    @Schema(description = "消息切分后的结构化数据（JSON字符串）",
            // 【仅修改这里】更新为带时间戳的新示例
            example = "{\"total\":2,\"segments\":{\"1\":{\"content\":\"第一段\",\"timestamp\":1775044206732},\"2\":{\"content\":\"第二段\",\"timestamp\":1775044210076}}}")
    private String splitContentJson;

    /**
     * 是否临时消息（对应数据库is_temp字段）
     * 0=否（正式消息），1=是（AI流式占位临时消息）
     */
    @Schema(description = "是否临时消息", example = "0", allowableValues = {"0", "1"})
    private Integer isTemp;

    /**
     * 前端排序专用时间戳（消息完成瞬间毫秒级）
     * 对应数据库 sort_timestamp 字段
     */
    @Schema(description = "排序专用时间戳(毫秒级，前端排序依据)", example = "1744056789000")
    private Long sortTimestamp;

    /**
     * 消息切分后的实体对象（不存入数据库，仅业务使用）
     */
    @TableField(exist = false) // 关键：标记为非数据库字段
    @Schema(description = "消息切分后的实体对象（业务使用，不入库）")
    private SplitContent splitContent;

    /**
     * 消息创建时间（数据库默认值）
     */
    @Schema(description = "消息创建时间", example = "2025-12-30 10:01:00")
    private LocalDateTime createTime;

    // ========== 静态工具：ObjectMapper（全局复用） ==========
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    // ========== 重写splitContentJson的set方法：自动解析填充splitContent ==========
    public void setSplitContentJson(String splitContentJson) {
        // 1. 先给数据库映射字段赋值（满足MyBatis需求）
        this.splitContentJson = splitContentJson;

        // 2. 处理空值：JSON为空时，初始化空的SplitContent（避免业务端空指针）
        if (splitContentJson == null || splitContentJson.trim().isEmpty()) {
            this.splitContent = new SplitContent();
            return;
        }

        // 3. 解析JSON字符串为SplitContent实体，填充业务字段
        try {
            this.splitContent = OBJECT_MAPPER.readValue(splitContentJson, SplitContent.class);
        } catch (Exception e) {
            // 解析失败时初始化空对象，避免业务端NPE
            e.printStackTrace(); // 生产环境建议用日志打印
            this.splitContent = new SplitContent();
        }
    }

    // ========== 轻量化内部类：仅保留Data和核心字段 ==========
    @Data
    public static class SplitContent {
        /**
         * 总段数
         */
        private Integer total = 0; // 默认值，避免空值

        /**
         * 分段内容（键：数字字符串，值：段落对象）
         * 保留 LinkedHashMap 保证插入顺序，解决序号排序问题
         * 【关键修改】从 String 改为 SegmentItem 对象
         */
        private Map<String, SegmentItem> segments = new LinkedHashMap<>();
    }

    /**
     * 分段子对象：存储文本内容 + 生成时间戳
     */
    @Data
    public static class SegmentItem {
        /**
         * 段落文本内容
         */
        private String content;
        /**
         * 生成时间戳（毫秒）
         */
        private Long timestamp;
    }
}