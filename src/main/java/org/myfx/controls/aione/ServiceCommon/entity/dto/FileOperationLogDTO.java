package org.myfx.controls.aione.ServiceCommon.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.myfx.controls.aione.ServiceCommon.serviceEnum.FileBusinessTypeEnum;
import org.myfx.controls.aione.ServiceCommon.serviceEnum.FileOperationStatusEnum;
import org.myfx.controls.aione.ServiceCommon.serviceEnum.FileOperationTypeEnum;

/**
 * 文件操作日志 请求DTO
 */
@Data
@Schema(description = "文件操作日志请求参数")
public class FileOperationLogDTO {

    @Schema(description = "业务ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1001")
    private Long businessId;

    @Schema(description = "业务类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private FileBusinessTypeEnum businessType;

    @Schema(description = "文件存储路径", requiredMode = Schema.RequiredMode.REQUIRED, example = "/upload/avatar/ai_role/1001.png")
    private String filePath;

    @Schema(description = "操作类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private FileOperationTypeEnum operationType;

    @Schema(description = "操作状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private FileOperationStatusEnum operationStatus;

    @Schema(description = "失败原因", example = "文件存储权限不足")
    private String failReason;

    @Schema(description = "操作人ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "10001")
    private Long operatorId;
}