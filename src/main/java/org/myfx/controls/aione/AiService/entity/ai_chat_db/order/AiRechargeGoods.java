package org.myfx.controls.aione.AiService.entity.ai_chat_db.order;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AI积分充值商品实体
 * 对应数据库表：ai_recharge_goods
 */
@Data
@TableName("ai_recharge_goods")
@Schema(description = "AI积分充值商品/档位实体")
public class AiRechargeGoods {

    @TableId(type = IdType.INPUT)
    @Schema(description = "雪花ID主键", example = "1000000000000000001")
    private Long id;

    @Schema(description = "充值金额(元)", example = "5.00")
    private BigDecimal amount;

    @Schema(description = "到账AI积分", example = "5")
    private Long point;

    @Schema(description = "充值档位名称", example = "5元充值档位")
    private String name;

    @Schema(description = "商品描述", example = "基础小额充值，适合体验使用")
    private String description;

    @Schema(description = "状态 1-启用 0-禁用", example = "1")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "备注说明")
    private String remark;
}