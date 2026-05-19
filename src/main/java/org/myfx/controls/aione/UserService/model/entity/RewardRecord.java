package org.myfx.controls.aione.UserService.model.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "打赏记录响应对象 - 衷心感谢各位支持者的慷慨打赏！")
public class RewardRecord {

    @Schema(description = "记录ID", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer id;

    @Schema(description = "打赏用户ID", example = "1001", requiredMode =  Schema.RequiredMode.REQUIRED)
    private Integer userId;

    @Schema(description = "打赏用户邮箱", example = "reader@example.com", requiredMode =  Schema.RequiredMode.REQUIRED)
    private String userEmail;

    @Schema(description = "打赏金额（单位：分）", example = "1000", requiredMode =  Schema.RequiredMode.REQUIRED)
    private Integer rewardAmount;

    @Schema(description = "打赏时间", example = "2023-11-15T14:30:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime rewardTime;

    @Schema(description = "打赏备注/留言", example = "支持作者继续创作！")
    private String remark;
}