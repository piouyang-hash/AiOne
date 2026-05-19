package org.myfx.controls.aione.AiService.entity.ai_chat_db.token;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;
import org.myfx.controls.aione.AiService.common.ai_chat_db.TokenChangeTypeEnum;
import org.myfx.controls.aione.AiService.common.ai_chat_db.TokenChangeWayEnum;

import java.time.LocalDateTime;

@Data
@Schema(description = "AI用户Token变动流水实体")
public class AiUserTokenRecord {

    @Schema(description = "流水ID（主键）")
    private Long recordId;

    @Schema(description = "用户ID")
    private Integer userId;

    @Schema(description = "Token类型ID（关联ai_token_type表）")
    private Long typeId;

    @Schema(description = "变动类型 1-消耗(累加) 2-计费/重置(扣减)")
    private TokenChangeTypeEnum changeType;

    @Schema(description = "变动数量（正数）")
    private Long changeAmount;

    @Schema(description = "变动方式 CHAT-聊天消耗 GIFT-系统赠送 RECHARGE-充值 ACTIVITY-活动奖励")
    private TokenChangeWayEnum changeWay;

    @Schema(description = "变动前累计消耗")
    private Long beforeConsumed;

    @Schema(description = "变动后累计消耗")
    private Long afterConsumed;

    @Schema(description = "变动前可用余额")
    private Long beforeBalance;

    @Schema(description = "变动后可用余额")
    private Long afterBalance;

    @Schema(description = "备注描述（如：GPT-4对话消耗）")
    private String remark;

    @Schema(description = "变动时间")
    private LocalDateTime createTime;
}