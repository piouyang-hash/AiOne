package org.myfx.controls.aione.AiService.entity.key_word_db;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI对话关键词表实体
 */
@Data
@TableName("ai_chat_keyword")
@Schema(name = "AiChatKeyword", description = "AI对话关键词表（存储每条消息提取的语义关键词，显式关联父子消息）")
public class AiChatKeyword {

    /**
     * 关键词主键（雪花ID，唯一标识单条关键词）
     */
    @TableId(type = IdType.INPUT) // 雪花ID手动传入，非自增
    @Schema(description = "关键词主键（雪花ID，唯一标识单条关键词）", example = "1789234567890123457")
    private Long keywordId;

    /**
     * 会话ID（关联ai_chat_session.session_id，冗余字段）
     */
    @Schema(description = "会话ID（关联ai_chat_session.session_id，冗余字段）", example = "1789234567890123456")
    private Long sessionId;

    /**
     * 消息ID（关联ai_chat_message.id，当前关键词所属消息主键）
     */
    @Schema(description = "消息ID（关联ai_chat_message.id，当前关键词所属消息主键）", example = "1789234567890123458")
    private Long msgId;

    /**
     * 父消息ID：AI回复关键词指向对应用户消息ID，用户消息关键词为null
     */
    @Schema(description = "父消息ID：AI回复关键词指向对应用户消息ID，用户消息关键词为null", example = "1789234567890123458")
    private Long parentMsgId;

    /**
     * 归属用户ID（0=匿名，>0=登录用户）
     */
    @Schema(description = "归属用户ID（0=匿名，>0=登录用户）", example = "1")
    private Integer userId;

    /**
     * 提取的单个核心关键词（如“Redis缓存穿透”）
     */
    @Schema(description = "提取的单个核心关键词（如“Redis缓存穿透”）", example = "Redis缓存穿透")
    private String keywordContent;

    /**
     * 关键词排序（0=第1个，1=第2个...，体现优先级）
     */
    @Schema(description = "关键词排序（0=第1个，1=第2个...，体现优先级）", example = "0")
    private Integer keywordSort;

    /**
     * 关键词提取时间
     */
    @Schema(description = "关键词提取时间", example = "2026-01-16 15:30:00")
    private LocalDateTime createTime;
}