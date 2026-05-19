package org.myfx.controls.aione.SimulationGame.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 模拟游戏-地点与事件关联新增请求DTO
 * 用于接收前端新增关联的请求参数
 */
@Data // Lombok自动生成getter/setter/toString等
@Schema(name = "SimulateLocationEventRelationAddDTO", description = "新增地点与事件关联请求参数")
public class SimulateLocationEventRelationAddDTO {

    /**
     * 地点编码（唯一标识）
     */
    @NotBlank(message = "地点编码不能为空")
    @Schema(
            description = "地点编码（唯一标识，如HOME、SHOP、DUNGEON）",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "HOME"
    )
    private String locationCode;

    /**
     * 事件编码（唯一标识）
     */
    @NotBlank(message = "事件编码不能为空")
    @Schema(
            description = "事件编码（唯一标识，如TRADING、FIGHT、QUEST_ACCEPT）",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "TRADING"
    )
    private String eventCode;

    // ========== 核心新增：事件在该地点的持续秒数 ==========
    @NotNull(message = "事件持续时长不能为空") // 非空校验（数据库是NOT NULL）
    @Min(value = 1, message = "事件持续时长不能小于1秒") // 业务校验：至少1秒
    @Schema(
            description = "事件在该地点的默认持续秒数（游戏服务器时间跨度）",
            requiredMode = Schema.RequiredMode.REQUIRED, // 必传
            example = "120" // 默认值示例
    )
    private Integer eventDuration;
}