package org.myfx.controls.aione.ServiceCommon.entity.feign;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 模拟事件记录响应DTO
 * 包含事件核心信息、执行时长、Redis过期时间等
 */
@Data
@Schema(description = "模拟事件记录响应DTO")
public class EventRecordResponseDTO {

    @Schema(description = "位置描述", example = "北京朝阳区")
    private String locationDesc;

    @Schema(description = "事件描述", example = "2026年每日签到")
    private String eventDesc;

    @Schema(description = "实际开始时间（秒级时间戳）", example = "1741872000")
    private Integer actualStart;

    @Schema(description = "执行时长（秒）= 默认时长 - 实际开始时间差", example = "3600")
    private Integer executionTime;

    @Schema(description = "剩余执行时长（秒）| 距离事件结束还需多久", example = "2400")
    private Integer remainingSeconds;
}