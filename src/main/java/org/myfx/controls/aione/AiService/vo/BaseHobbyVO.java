package org.myfx.controls.aione.AiService.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.myfx.controls.aione.AiService.common.my_memory_db.HobbyEnum;

/**
 * 爱好VO（视图对象）：用于前端展示，自动关联枚举描述
 */
@Getter
@Setter // 用@Getter/@Setter而非@Data，避免lombok自动生成的setter覆盖自定义逻辑
@Schema(description = "爱好VO（含枚举+描述）")
public class BaseHobbyVO {

    @Schema(description = "爱好ID（主键）", example = "1")
    private Integer hobbyId;

    @Schema(description = "爱好枚举（核心类型）", example = "SPORT_RUNNING")
    private HobbyEnum hobbyEnum;

    @Schema(description = "爱好描述（自动从枚举填充）", example = "跑步")
    private String hobbyDesc;

    /**
     * 重写setHobbyEnum：设置枚举时，自动填充hobbyDesc为枚举的desc
     * @param hobbyEnum 爱好枚举
     */
    public void setHobbyEnum(HobbyEnum hobbyEnum) {
        this.hobbyEnum = hobbyEnum;
        // 枚举非空时，自动取desc赋值给hobbyDesc；空则设为null
        this.hobbyDesc = hobbyEnum != null ? hobbyEnum.getDesc() : null;
    }
}
