package org.myfx.controls.aione.ServiceCommon.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文件异常处理实体（重试失败、需人工处理）
 * 对应数据库表：file_exception_handler
 */
@Data
@TableName("file_exception_handler")
@Schema(description = "文件异常处理记录")
public class FileExceptionHandler {

    @Schema(description = "主键ID（雪花ID）", example = "1799999999999999999")
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "关联操作日志ID", example = "1798888888888888888")
    private Long logId;

    @Schema(description = "业务ID", example = "1001")
    private Long businessId;

    @Schema(description = "业务类型：1=AI角色头像 2=用户头像 3=书籍封面", example = "1")
    private Integer businessType;

    @Schema(description = "异常文件路径", example = "/upload/avatar/ai_role/1001.png")
    private String filePath;

    @Schema(description = "异常操作类型：1=上传 2=删除", example = "2")
    private Integer operationType;

    @Schema(description = "已重试次数", example = "3")
    private Integer retryCount;

    @Schema(description = "最大重试次数", example = "3")
    private Integer maxRetry;

    @Schema(description = "最终失败原因", example = "文件不存在，重试3次后仍失败")
    private String finalFailReason;

    @Schema(description = "处理状态：0=待人工处理 1=处理中 2=已处理 3=忽略", example = "0")
    private Integer handleStatus;

    @Schema(description = "人工处理备注", example = "手动删除无效文件")
    private String handleRemark;

    @Schema(description = "异常创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
