package org.myfx.controls.aione.AiService.entity.ai_chat_db;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.UUID;
import org.myfx.controls.aione.AiService.common.ai_chat_db.AiChatSessionStatusEnum;
import org.myfx.controls.aione.AiService.common.ai_chat_db.AiChatSystemSessionEnum;
import org.myfx.controls.aione.AiService.common.ai_chat_db.RecycleStatusEnum;
import org.myfx.controls.aione.ServiceCommon.serviceEnum.LogicalDeleteEnum;

import java.time.LocalDateTime;

/**
 * AI对话会话主表实体
 */
@Data
@TableName("ai_chat_session")
@Schema(name = "AiChatSession", description = "AI对话会话主表")
public class AiChatSession {

    /**
     * 会话UUID（前端预生成的标准UUIDv4，核心关联键）
     */
    @NotBlank(message = "会话UUID不能为空")  // 必填校验：前端必须传UUID
    @UUID(message = "会话UUID格式错误，请传入标准UUIDv4格式（如：3b9e4f9a-8346-4b0f-9d1e-8f7c6a5b4d3e）") // 可选：UUID格式校验
    @Parameter(description = "对话会话UUID（前端预生成的标准UUIDv4）",
            required = true,
            example = "3b9e4f9a-8346-4b0f-9d1e-8f7c6a5b4d3e")
    @Schema(description = "会话UUID（标准UUIDv4格式）",
            example = "3b9e4f9a-8346-4b0f-9d1e-8f7c6a5b4d3e",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String sessionUuid; // 替换原sessionId，类型改为String（对应前端UUID）

    /**
     * 会话ID（雪花ID，主键）
     */
    @TableId(type = IdType.INPUT) // 雪花ID手动传入，非自增
    @Schema(description = "会话ID（雪花ID，核心关联键）", example = "1789234567890123456")
    private Long sessionId;

    /**
     * 归属用户ID（0=匿名，>0=登录用户）
     */
    @Schema(description = "归属用户ID（0=匿名，>0=登录用户）", example = "1")
    private Integer userId;

    /**
     * 对话标题（默认新对话）
     */
    @Schema(description = "对话标题", example = "新对话")
    private String chatTitle;

    /**
     * 对话头像路径（默认空字符串）
     */
    @Schema(description = "对话头像路径", example = "/avatars/ai/session_123.png")
    private String chatAvatarPath;

    // 新增：最后一条消息内容（预览用）
    @Schema(description = "会话最后一条消息内容（预览用）", example = "你好，Spark怎么用？")
    private String lastMessageContent;

    // 新增：上次聊天时间戳（毫秒级，用于时间计算，可null）
    @Schema(description = "上次聊天时间戳（毫秒级，用于时间计算）", example = "1742236800000")
    private Long lastChatTimestamp;

    /**
     * 会话创建时间（数据库默认值）
     */
    @Schema(description = "会话创建时间", example = "2025-12-30 10:00:00")
    private LocalDateTime createTime;

    /**
     * 会话更新时间（仅更新lastMessageContent时同步更新）
     */
    @Schema(description = "会话更新时间", example = "2025-12-30 10:05:00")
    private LocalDateTime updateTime;

    /**
     * 会话状态（ACTIVE=活跃，CLOSED=关闭）
     */
    @Schema(description = "会话状态（ACTIVE=活跃，CLOSED=关闭）", example = "ACTIVE")
    private AiChatSessionStatusEnum status;

    /**
     * 是否是系统会话（0=非系统会话，1=系统会话）
     */
    @Schema(description = "是否是系统会话（0=非系统会话，1=系统会话）", example = "0")
    private AiChatSystemSessionEnum isSystemSession;

    /**
     * 回收站状态（0=未放入回收站，1=已放入回收站）
     */
    @Schema(description = "回收站状态（0=未放入回收站，1=已放入回收站）", example = "0", defaultValue = "0")
    private RecycleStatusEnum isRecycle;

    // ====================== 置顶字段 ======================
    @Schema(description = "是否置顶 0-否 1-是", example = "0")
    private Integer isTop;

    @Schema(description = "置顶时间戳(毫秒)，置顶时更新", example = "1742236800000")
    private Long topAt;

    @Schema(description = "非切分模式未读消息数", example = "0")
    private Integer normalUnreadCount;

    @Schema(description = "切分模式未读消息数", example = "0")
    private Integer splitUnreadCount;

    @Schema(description = "AI角色ID，关联事件驱动微服务", example = "1")
    private Integer roleId;

    /**
     * 删除状态（0=未删除，1=已删除）
     */
    @Schema(description = "删除状态（0=未删除，1=已删除）", example = "0", defaultValue = "0")
    private LogicalDeleteEnum isDeleted; // 对应数据库tinyint，用Integer适配
}