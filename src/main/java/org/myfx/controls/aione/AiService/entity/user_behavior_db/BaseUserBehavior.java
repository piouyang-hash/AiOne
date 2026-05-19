package org.myfx.controls.aione.AiService.entity.user_behavior_db;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import com.baomidou.mybatisplus.annotation.*;
import org.myfx.controls.aione.AiService.common.user_behavior_db.BehaviorEnum;

import java.time.LocalDateTime;

/**
 * 用户行为字典表实体（仅存储行为定义，无分值）
 */
@Data
@TableName("base_user_behavior")
public class BaseUserBehavior {

    @Schema(description = "行为ID（主键，自增）", example = "1")
    @TableId(type = IdType.AUTO)
    private Integer behaviorId;

    @Schema(description = "行为编码（对应BehaviorEnum的code）", example = "CHAT_SEND_MSG")
    @TableField("behavior_code")
    private BehaviorEnum behaviorCode;

    @Schema(description = "行为名称（中文描述）", example = "发送聊天消息")
    @TableField("behavior_name")
    private String behaviorName;

    @Schema(description = "创建时间", example = "2026-01-11 10:00:00")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "更新时间", example = "2026-01-11 11:00:00")
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

}