package org.myfx.controls.aione.ServiceCommon.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.myfx.controls.aione.ServiceCommon.serviceEnum.FileDeleteBusinessTypeEnum;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文件删除失败日志实体类
 * 对应表：fileLog_db.file_delete_failed_log
 */
@Data
@Schema(description = "文件删除失败日志实体")
public class FileDeleteFailedLog implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID（雪花ID）", example = "1718000000000000000")
    private Long id; // 雪花ID用Long类型（适配BIGINT UNSIGNED）

    @Schema(description = "业务ID（如书籍ID、用户ID）", example = "1001")
    private long businessId;

    @Schema(description = "待删除文件路径", example = "/data/books/1001.epub", requiredMode = Schema.RequiredMode.REQUIRED)
    private String filePath;

    @Schema(description = "业务类型编码（1=私有书籍 2=共有书籍 3=用户头像 4=共有书籍封面）", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private FileDeleteBusinessTypeEnum businessType;

    @Schema(description = "删除失败次数", example = "1")
    private Integer failCount;

    @Schema(description = "删除失败原因", example = "文件被其他进程占用")
    private String failReason;

    @Schema(description = "首次失败时间", example = "2025-06-01 10:00:00")
    private LocalDateTime createTime;

    @Schema(description = "最后一次重试时间", example = "2025-06-01 10:05:00")
    private LocalDateTime lastRetryTime;

    @Schema(description = "是否已解决（0=未解决 1=已解决）", example = "0")
    private Integer isResolved;
}