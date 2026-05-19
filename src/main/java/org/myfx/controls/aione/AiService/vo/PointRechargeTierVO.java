package org.myfx.controls.aione.AiService.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 积分充值档位 VO（前端展示专用）
 * 只保留业务需要的字段，无冗余枚举信息
 */
@Data
@Schema(description = "积分充值档位信息")
public class PointRechargeTierVO {

    @Schema(description = "充值金额（单位：元）", example = "5")
    private Integer amount;

    @Schema(description = "档位描述", example = "5元充值档位")
    private String desc;

    // 全参构造（用于枚举转VO）
    public PointRechargeTierVO(Integer amount, String desc) {
        this.amount = amount;
        this.desc = desc;
    }
}