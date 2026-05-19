package org.myfx.controls.aione.AiService.entity.my_memory_db;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.myfx.controls.aione.ServiceCommon.serviceEnum.LogicalDeleteEnum;

import java.time.LocalDateTime;

/**
 * 用户爱好关联表实体
 */
@Data
@Schema(description = "用户爱好关联表（多对多关联）")
public class UserHobbyRelation {

    @Schema(description = "雪花ID（主键）", example = "987654321011")
    private Long id;

    @Schema(description = "关联base_user_info的主键ID", example = "145678901234")
    private Long userInfoId;

    // ========== 新增：userId字段 ==========
    @Schema(description = "用户ID（冗余字段，直接关联业务维度）", example = "10086")
    private Integer userId;

    @Schema(description = "关联base_hobby的爱好ID", example = "1")
    private Integer hobbyId;

    @Schema(description = "创建时间", example = "2025-12-30 10:06:00")
    private LocalDateTime createTime;

    /**
     * 逻辑删除标记（适配SAGA场景）
     * 不设置默认值：依赖数据库is_deleted字段的DEFAULT 0
     */
    @Schema(description = "逻辑删除：0-未删除 1-已删除", hidden = true)
    private LogicalDeleteEnum isDeleted;
}