package org.myfx.controls.aione.AiService.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.myfx.controls.aione.AiService.common.ai_chat_db.AiBehaviorEnum;

/**
 * AI行为VO（视图对象）：用于前端展示，自动关联枚举描述
 */
@Getter
@Setter // 用@Getter/@Setter而非@Data，避免lombok自动生成的setter覆盖自定义逻辑
@Schema(description = "AI行为VO（含枚举+描述）")
public class BaseAiBehaviorVO {

    @Schema(description = "AI行为ID（主键）", example = "1")
    private Integer aiBehaviorId;

    @Schema(description = "AI行为枚举（核心类型）", example = "WAIT")
    private AiBehaviorEnum aiBehaviorEnum;

    @Schema(description = "AI行为描述（自动从枚举填充）", example = "等待")
    private String aiBehaviorDesc;

    /**
     * 重写setAiBehaviorEnum：设置枚举时，自动填充aiBehaviorDesc为枚举的desc
     * @param aiBehaviorEnum AI行为枚举
     */
    public void setAiBehaviorEnum(AiBehaviorEnum aiBehaviorEnum) {
        this.aiBehaviorEnum = aiBehaviorEnum;
        // 枚举非空时，自动取desc赋值给aiBehaviorDesc；空则设为null
        this.aiBehaviorDesc = aiBehaviorEnum != null ? aiBehaviorEnum.getDesc() : null;
    }
}