package org.myfx.controls.aione.ServiceCommon.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理员操作审计日志 实体类
 * 对应数据库表：admin_audit_log
 */
@Data
@TableName("admin_audit_log")
@Schema(name = "AdminAuditLog", description = "管理员审计日志实体")
public class AdminAuditLog {

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "管理员用户ID")
    private Integer userId;

    @Schema(description = "接口方法名")
    private String methodName;

    @Schema(description = "接口入参")
    private String requestParams;

    @Schema(description = "接口出参")
    private String responseParams;

    @Schema(description = "操作状态 1-成功 0-失败")
    private Integer operateStatus;

    @Schema(description = "异常信息")
    private String errorMsg;

    @Schema(description = "操作时间")
    private LocalDateTime createTime;
}