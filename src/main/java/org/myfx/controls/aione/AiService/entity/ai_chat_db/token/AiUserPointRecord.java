package org.myfx.controls.aione.AiService.entity.ai_chat_db.token;
import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * AI用户算力积分变动流水表
 * 用于余额变动审计、对账、溯源
 */
@Data
@TableName("ai_user_point_record")
@Schema(description = "AI用户-算力积分变动流水实体")
public class AiUserPointRecord {

    /**
     * 手动雪花ID，无自增
     */
    @TableId(type = IdType.INPUT)
    @Schema(description = "流水ID（手动雪花ID）")
    private Long recordId;

    @Schema(description = "用户ID")
    @TableField("user_id")
    private Integer userId;

    @Schema(description = "变动类型 1-消耗扣减 2-充值增加 3-赠送增加")
    @TableField("change_type")
    private Integer changeType;

    @Schema(description = "变动积分数量（正数）")
    @TableField("change_amount")
    private Long changeAmount;

    @Schema(description = "变动方式 USER_CHAT/AI_REPLY/RECHARGE/GIFT")
    @TableField("change_way")
    private String changeWay;

    @Schema(description = "变动前可用积分余额")
    @TableField("before_point")
    private Long beforePoint;

    @Schema(description = "变动后可用积分余额")
    @TableField("after_point")
    private Long afterPoint;

    @Schema(description = "备注")
    @TableField("remark")
    private String remark;

    @Schema(description = "记录时间")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}