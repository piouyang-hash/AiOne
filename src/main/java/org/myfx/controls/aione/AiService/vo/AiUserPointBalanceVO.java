package org.myfx.controls.aione.AiService.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AI用户算力积分余额 VO
 * 前端展示专用，剔除敏感/无用字段，自动转换积分单位
 */
@Data
@Schema(description = "AI用户算力积分余额VO")
public class AiUserPointBalanceVO {

    @Schema(description = "用户ID")
    private Integer userId;

    @Schema(description = "总可用算力积分（单位：个，1积分 = 1000000微积分）")
    // 字符串存储，无精度问题，前端直接展示
    private String totalPoint;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    // ===================== 核心：重写set方法，自动换算积分 =====================
    /**
     * 重写积分赋值：数据库Long(微积分) → 前端显示积分(÷1000000)
     * 例：1 → 0.000001；1000000 → 1；500000 → 0.5
     * 【无四舍五入】：整数 ÷ 1000000 必定整除
     */
    public void setTotalPoint(Long totalPoint) {
        if (totalPoint == null) {
            this.totalPoint = "0.000000";
            return;
        }
        // 直接除以100万，无需舍入（必定整除）
        BigDecimal result = new BigDecimal(totalPoint).divide(new BigDecimal("1000000"));
        // 转普通字符串，无科学计数法
        this.totalPoint = result.toPlainString();
    }
}