package org.myfx.controls.aione.AiService.entity.my_memory_db;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.myfx.controls.aione.AiService.common.my_memory_db.HobbyEnum;

import java.time.LocalDateTime;

/**
 * 爱好字典表实体
 */
@Data
@Schema(description = "爱好字典表")
public class BaseHobby {

    @Schema(description = "爱好ID（主键，自增）", example = "1")
    private Integer hobbyId;

    @Schema(description = "爱好名称（如：阅读、运动、编程）", example = "阅读")
    private HobbyEnum hobbyName;

    @Schema(description = "创建时间", example = "2025-12-30 10:00:00")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", example = "2025-12-30 10:00:00")
    private LocalDateTime updateTime;
}