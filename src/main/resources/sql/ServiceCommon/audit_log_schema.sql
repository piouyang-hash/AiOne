-- 管理员审计日志表（满足你的字段要求：userId、方法名、入参、出参）
CREATE TABLE IF NOT EXISTS admin_audit_log
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id         BIGINT       NOT NULL COMMENT '管理员用户ID',
    method_name     VARCHAR(255) NOT NULL COMMENT '接口方法名',
    request_params  TEXT COMMENT '接口入参',
    response_params TEXT COMMENT '接口出参',
    operate_status  TINYINT      NOT NULL DEFAULT 1 COMMENT '操作状态 1-成功 0-失败',
    error_msg       TEXT COMMENT '异常信息',
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT '管理员操作审计日志表';