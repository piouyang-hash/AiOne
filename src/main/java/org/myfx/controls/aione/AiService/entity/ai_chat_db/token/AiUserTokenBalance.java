package org.myfx.controls.aione.AiService.entity.ai_chat_db.token;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Data
@Schema(description = "AI用户Token余额实体")
public class AiUserTokenBalance {

    @Schema(description = "余额ID（主键）")
    private Long balanceId;

    @Schema(description = "用户ID（唯一对应用户）")
    private Integer userId;

    @Schema(description = "Token类型ID（关联ai_token_type表）")
    private Long typeId;

    @Schema(description = "剩余可用Token余额（扣减核心）")
    private Long totalBalance;

    @Schema(description = "累计消耗Token总量")
    private Long totalConsumed;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}