package org.myfx.controls.aione.AiService.entity.memory_db;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户长期记忆向量表实体类
 * 对应 PostgreSQL 表：public.user_long_term_memory
 */
@Data
@Schema(description = "用户长期记忆向量实体")
public class UserLongTermMemory {

    @Schema(description = "主键ID（自增整数）", example = "1")
    private Integer id;

    @Schema(description = "记忆内容文本", example = "用户喜欢喝咖啡，不加糖")
    private String content;

    @Schema(description = "元数据（JSON格式）", example = "{\"scene\":\"日常对话\",\"time\":\"2026-03-08\"}")
    private String metadata;

    @Schema(description = "文本向量（浮点数数组列表）", example = "[[0.123,0.456,0.789],[0.987,0.654,0.321]]")
    private String embedding;

    @Schema(description = "归属用户ID", example = "1")
    private Integer userId;

    @Schema(description = "记录创建时间（数据库自动填充）", example = "2026-03-08T18:30:00")
    private LocalDateTime createTime;

    /**
     * 转换为RAG检索结果字符串（toRag Retrieval Result）
     * 仅处理content和metadata，忽略embedding等向量相关字段
     * @return 格式化的检索结果字符串
     */
    public String toRagRetrievalResult() {
        // 空值处理：避免content/metadata为空导致拼接异常
        String safeContent = this.content == null ? "" : this.content;
        String safeMetadata = this.metadata == null ? "" : this.metadata;

        // 格式化输出：仅保留核心的记忆内容和元数据
        return String.format("记忆内容：%s | 元数据：%s", safeContent, safeMetadata);
    }
}