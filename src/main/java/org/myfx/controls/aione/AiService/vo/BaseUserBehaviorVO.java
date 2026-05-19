package org.myfx.controls.aione.AiService.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.myfx.controls.aione.AiService.common.user_behavior_db.BehaviorEnum;

/**
 * 行为VO（视图对象）：用于前端展示，自动关联枚举描述
 */
@Getter
@Setter // 用@Getter/@Setter而非@Data，避免lombok自动生成的setter覆盖自定义逻辑
@Schema(description = "用户行为VO（含枚举+描述）")
public class BaseUserBehaviorVO {

    @Schema(description = "行为ID（主键）", example = "1")
    private Integer behaviorId;

    @Schema(description = "行为枚举（核心类型）", example = "CHAT_SEND_MSG")
    private BehaviorEnum behaviorEnum;

    @Schema(description = "行为描述（自动从枚举填充）", example = "发送AI聊天消息")
    private String behaviorDesc;

    /**
     * 重写setBehaviorEnum：设置枚举时，自动填充behaviorDesc为枚举的desc
     * @param behaviorEnum 用户行为枚举
     */
    public void setBehaviorEnum(BehaviorEnum behaviorEnum) {
        this.behaviorEnum = behaviorEnum;
        // 枚举非空时，自动取desc赋值给behaviorDesc；空则设为null
        this.behaviorDesc = behaviorEnum != null ? behaviorEnum.getDesc() : null;
    }
}