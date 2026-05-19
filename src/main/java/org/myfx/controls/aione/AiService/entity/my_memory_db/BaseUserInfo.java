package org.myfx.controls.aione.AiService.entity.my_memory_db;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.myfx.controls.aione.AiService.common.my_memory_db.GenderEnum;
import org.myfx.controls.aione.ServiceCommon.serviceEnum.LogicalDeleteEnum;

import java.time.LocalDateTime;

/**
 * 用户基本信息表实体
 */
@Data
@Schema(description = "用户基本信息表")
public class BaseUserInfo {

    @Schema(description = "雪花ID（主键）", example = "145678901234")
    private Long id;

    @Schema(description = "用户ID（Integer类型）", example = "101")
    private Integer userId;

    @Schema(description = "AI对用户的称呼（如：小明、李总、王同学）", example = "小明", nullable = true)
    private String callName;

    @Schema(description = "用户在线时间段（AI判断是否主动发消息，格式示例：09:00-22:00）",
            example = "09:00-22:00", nullable = true)
    private String onlineTimeRange;

    @Schema(description = "性别（1=男，2=女，0=未知）", example = "1", allowableValues = {"0", "1", "2"})
    private GenderEnum gender; // 对应数据库tinyint，Java用Byte更贴合

    @Schema(description = "年龄（0-120）", example = "25")
    private Byte age;

    @Schema(description = "身份（如：学生、上班族、自由职业）", example = "上班族")
    private String identity;

    @Schema(description = "创建时间", example = "2025-12-30 10:05:00")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", example = "2025-12-30 10:05:00")
    private LocalDateTime updateTime;

    /**
     * 逻辑删除标记（适配SAGA场景）
     * 不设置默认值：依赖数据库is_deleted字段的DEFAULT 0
     */
    @Schema(description = "逻辑删除：0-未删除 1-已删除", hidden = true)
    private LogicalDeleteEnum isDeleted;
}