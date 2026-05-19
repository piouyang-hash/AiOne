package org.myfx.controls.aione.ServiceCommon.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 文件异常处理 请求DTO
 */
@Data
@Schema(description = "文件异常处理请求参数")
public class FileExceptionHandlerDTO {

    @Schema(description = "关联操作日志ID", example = "1798888888888888888", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long logId;

    @Schema(description = "业务ID", example = "1001", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long businessId;

    @Schema(description = "业务类型：1=AI角色头像 2=用户头像 3=书籍封面", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer businessType;

    @Schema(description = "异常文件路径", example = "/upload/avatar/ai_role/1001.png", requiredMode = Schema.RequiredMode.REQUIRED)
    private String filePath;

    @Schema(description = "异常操作类型：1=上传 2=删除", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer operationType;

    @Schema(description = "已重试次数", example = "3", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer retryCount;

    @Schema(description = "最大重试次数", example = "3", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer maxRetry;

    @Schema(description = "最终失败原因", example = "文件不存在，重试3次后仍失败", requiredMode = Schema.RequiredMode.REQUIRED)
    private String finalFailReason;

    @Schema(description = "处理状态：0=待人工处理 1=处理中 2=已处理 3=忽略", example = "0", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer handleStatus;

    @Schema(description = "人工处理备注", example = "手动删除无效文件")
    private String handleRemark;
}