package org.myfx.controls.aione.AiService.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.myfx.controls.aione.AiService.common.ai_chat_db.AiChatSessionStatusEnum;

import java.time.LocalDateTime;

@Data
public class AiChatSessionVO {

    /**
     * 会话UUID（标准UUID v4格式，前端交互核心标识）
     */
    @Schema(description = "会话UUID（标准UUID v4格式）",
            example = "3b9e4f9a-8346-4b0f-9d1e-8f7c6a5b4d3e")
    private String sessionUuid;

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

    @Schema(description = "角色ID（主键，自增）")
    private Integer roleId;

    /**
     * 会话状态（ACTIVE=活跃，CLOSED=关闭）
     */
    @Schema(description = "会话状态（ACTIVE=活跃，CLOSED=关闭）", example = "ACTIVE")
    private AiChatSessionStatusEnum status;

    // ====================== 置顶字段 ======================
    @Schema(description = "是否置顶 0-否 1-是", example = "0")
    private Integer isTop;

    @Schema(description = "置顶时间戳(毫秒)，置顶时更新", example = "1742236800000")
    private String topAt;

    @Schema(description = "非切分模式未读消息数", example = "0")
    private Integer normalUnreadCount;

    @Schema(description = "切分模式未读消息数", example = "0")
    private Integer splitUnreadCount;

    /**
     * 会话最后一条消息内容（预览用）
     */
    @Schema(description = "会话最后一条消息内容（预览用）", example = "你好，Spark怎么用？")
    private String lastMessageContent; // 关键：字段名和实体类完全一致

    /**
     * 设置置顶时间戳
     * @param topAt 置顶时间戳（毫秒级Long类型），内部会转换为String格式存储
     */
    public void setTopAt(Long topAt) {
        // 处理空值：若传入null则赋值null，否则将Long转为String
        this.topAt = topAt != null ? topAt.toString() : null;
    }

}
