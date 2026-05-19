package org.myfx.controls.aione.SimulationGame.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 模拟游戏-事件执行序列规则新增请求DTO
 * 用于接收前端新增事件序列规则的请求参数
 */
@Data // Lombok自动生成getter/setter/toString/equals等方法
@Schema(name = "SimulateEventSequenceAddDTO", description = "新增事件执行序列规则请求参数")
public class SimulateEventSequenceAddDTO {

    /**
     * 版本号（区分不同每日事件规则）
     */
    @NotNull(message = "版本号不能为空")
    @Schema(
            description = "版本号（区分不同每日事件规则）",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "1"
    )
    private Integer version;

    /**
     * 地点编码（该事件序列所属地点）
     */
    @NotBlank(message = "地点编码不能为空")
    @Schema(
            description = "地点编码（该事件序列所属地点，如CITY_CENTER、FOREST）",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "CITY_CENTER"
    )
    private String locationCode;

    /**
     * 事件编码（该步骤执行的事件）
     */
    @NotBlank(message = "事件编码不能为空")
    @Schema(
            description = "事件编码（该步骤执行的事件，如TRADING、FIGHT）",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "TRADING"
    )
    private String eventCode;

    /**
     * 执行次序（1=第一步、2=第二步...）
     */
    @NotNull(message = "执行次序不能为空")
    @Schema(
            description = "执行次序（1=第一步、2=第二步...）",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "1"
    )
    private Integer seqNum;
}