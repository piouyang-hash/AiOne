package org.myfx.controls.aione.AiService.entity.ai_chat_db.token;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * AI用户统一算力积分表
 * 唯一可消耗资产：总可用算力积分余额
 */
@Data
@TableName("ai_user_point_balance")
@Schema(description = "AI用户-统一算力积分余额实体")
public class AiUserPointBalance {

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "用户ID")
    @TableField("user_id")
    private Integer userId;

    @Schema(description = "总可用算力积分（唯一可消耗余额）")
    @TableField("total_point")
    private Long totalPoint;

    @Schema(description = "创建时间")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}