package org.myfx.controls.aione.AiService.entity.ai_chat_db;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * AI对话会话-系统提示词表实体类
 * 对应表：ai_chat_session_system_prompt
 */
@Data
@Schema(description = "AI对话会话-系统提示词实体")
public class AiChatSessionSystemPrompt {

    @Schema(description = "主键ID（手动填入雪花ID，非自增）", example = "1789012345678901234")
    private Long id;

    @Schema(description = "关联的会话ID", example = "1001")
    private Long sessionId;

    @Schema(description = "归属用户ID（0=匿名，>0=登录用户）", example = "1")
    private Integer userId;

    @Schema(description = "序列号（同一user_id+session_id下从1开始递增）", example = "1")
    private Integer serialNumber;

    @Schema(description = "系统提示词文本", example = "你是一个专业的Java技术顾问，回答需准确简洁")
    private String systemPrompt;

    @Schema(description = "创建时间", example = "2026-01-21 10:00:00")
    private LocalDateTime createTime;

    @Schema(description = "更新时间戳", example = "2026-01-21 10:30:00")
    private LocalDateTime updateTime;

    // ========== 新增：便捷拼接方法（核心） ==========

    /**
     * 追加文本到系统提示词末尾（无换行）
     * @param content 要追加的内容
     * @return 当前实体对象（链式调用）
     */
    public AiChatSessionSystemPrompt appendSystemPrompt(String content) {
        // 处理空值：若原提示词为null，初始化为空字符串
        String original = this.systemPrompt == null ? "" : this.systemPrompt;
        this.systemPrompt = original + content;
        return this; // 返回this支持链式调用
    }

    /**
     * 追加文本到系统提示词末尾（带换行符，适配多段提示词）
     * @param content 要追加的内容
     * @return 当前实体对象（链式调用）
     */
    public AiChatSessionSystemPrompt appendLineSystemPrompt(String content) {
        String original = this.systemPrompt == null ? "" : this.systemPrompt;
        // 换行符用System.lineSeparator()适配不同系统（Windows=\r\n，Linux=\n）
        this.systemPrompt = original + System.lineSeparator() + content;
        return this;
    }

    /**
     * 清空系统提示词（可选，便于重置）
     * @return 当前实体对象（链式调用）
     */
    public AiChatSessionSystemPrompt clearSystemPrompt() {
        this.systemPrompt = "";
        return this;
    }
}