package org.myfx.controls.aione.ServiceCommon.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.myfx.controls.aione.ServiceCommon.serviceEnum.FileBusinessTypeEnum;
import org.myfx.controls.aione.ServiceCommon.serviceEnum.FileOperationStatusEnum;
import org.myfx.controls.aione.ServiceCommon.serviceEnum.FileOperationTypeEnum;

import java.time.LocalDateTime;

/**
 * 文件操作全量日志实体（上传/删除 所有操作记录）
 * 对应数据库表：file_operation_log
 */
@Data
@TableName("file_operation_log")
@Schema(description = "文件操作全量日志")
public class FileOperationLog {

    @Schema(description = "主键ID（雪花ID）", example = "1798888888888888888")
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "业务ID（AI角色ID/用户ID/书籍ID）", example = "1001")
    private Long businessId;

    @Schema(description = "业务类型：1=AI角色头像 2=用户头像 3=书籍封面", example = "1")
    private FileBusinessTypeEnum businessType;

    @Schema(description = "文件存储路径", example = "/upload/avatar/ai_role/1001.png")
    private String filePath;

    @Schema(description = "操作类型：1=文件上传 2=文件删除", example = "1")
    private FileOperationTypeEnum operationType;

    @Schema(description = "操作状态：1=成功 2=失败", example = "1")
    private FileOperationStatusEnum operationStatus;

    @Schema(description = "失败原因（成功时为空）", example = "文件存储权限不足")
    private String failReason;

    @Schema(description = "操作人ID（0=系统自动操作）", example = "10001")
    private Long operatorId;

    @Schema(description = "操作时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}