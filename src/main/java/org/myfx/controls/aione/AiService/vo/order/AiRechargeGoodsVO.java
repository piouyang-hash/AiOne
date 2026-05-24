package org.myfx.controls.aione.AiService.vo.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AI充值商品/档位 VO（前端展示专用）
 * 对应数据库表：ai_recharge_goods
 */
@Data
@Schema(description = "AI积分充值商品档位响应VO")
public class AiRechargeGoodsVO {

    @Schema(description = "商品ID（雪花ID）", example = "1000000000000000001")
    private String id;

    @Schema(description = "充值金额(元)", example = "5.00")
    private BigDecimal amount;

    @Schema(description = "到账AI积分", example = "5")
    private String point;

    @Schema(description = "档位名称", example = "5元充值档位")
    private String name;

    @Schema(description = "商品描述", example = "基础小额充值，适合体验使用")
    private String description;

    @Schema(description = "状态", example = "启用")
    private String status;

    @Schema(description = "创建时间", example = "2025-12-07 10:00:00")
    private LocalDateTime createTime;

    // ===================== 核心：ID格式化（Long转String，防前端精度丢失） =====================
    public void setId(Long id) {
        this.id = id == null ? null : id.toString();
    }

    // ===================== 状态枚举转换 =====================
    public void setStatus(Integer status) {
        if (status == null) {
            this.status = "未知";
            return;
        }
        this.status = status == 1 ? "启用" : "禁用";
    }

    public void setPoint(Long point) {
        if (point == null) {
            this.point = "0.000000";
            return;
        }
        // 直接除以100万，必定整除
        BigDecimal result = new BigDecimal(point).divide(new BigDecimal("1000000"));
        // 转普通字符串，无科学计数法
        this.point = result.toPlainString();
    }
}