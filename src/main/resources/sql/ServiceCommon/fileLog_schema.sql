CREATE TABLE IF NOT EXISTS file_delete_failed_log
(
    id              BIGINT UNSIGNED NOT NULL COMMENT '主键ID（雪花ID）', -- 核心修改：去掉AUTO_INCREMENT，注释改为雪花ID
    business_id     BIGINT             NULL COMMENT '业务ID（如书籍ID、用户ID），用于日志追溯',
    file_path       VARCHAR(512)    NOT NULL COMMENT '待删除的文件路径（唯一路径字段，不区分主文件/封面）',
    business_type   INT             NOT NULL COMMENT '业务类型编码：1=私有书籍 2=共有书籍 3=用户头像 4=共有书籍封面',
    fail_count      TINYINT UNSIGNED DEFAULT 0 COMMENT '文件删除失败次数（用于控制重试上限）',
    fail_reason     VARCHAR(1024)   NULL COMMENT '删除失败原因（记录异常信息、系统提示等）',
    create_time     DATETIME         DEFAULT CURRENT_TIMESTAMP COMMENT '首次失败时间',
    last_retry_time DATETIME        NULL COMMENT '最后一次重试删除的时间',
    is_resolved     TINYINT          DEFAULT 0 COMMENT '是否已解决（0=未解决，1=已解决）',
    PRIMARY KEY (id),
    INDEX idx_file_path (file_path),
    INDEX idx_business_id (business_id),
    INDEX idx_business_type (business_type),
    INDEX idx_is_resolved (is_resolved)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='文件删除失败日志表（雪花ID版）';



CREATE TABLE IF NOT EXISTS file_operation_log
(
    id              BIGINT UNSIGNED NOT NULL COMMENT '主键ID（雪花ID）',
    business_id     BIGINT          NOT NULL COMMENT '业务ID（AI角色ID/用户ID/书籍ID）',
    business_type   INT             NOT NULL COMMENT '业务类型：1=AI角色头像 2=用户头像 3=书籍封面',
    file_path       VARCHAR(512)    NOT NULL COMMENT '文件存储路径',
    operation_type  TINYINT         NOT NULL COMMENT '操作类型：1=文件上传 2=文件删除',
    operation_status TINYINT        NOT NULL COMMENT '操作状态：1=成功 2=失败',
    fail_reason     VARCHAR(1024)    NULL COMMENT '失败原因（成功时为空）',
    operator_id     BIGINT          NOT NULL COMMENT '操作人ID',
    create_time     DATETIME         DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    update_time     DATETIME         DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    INDEX idx_business (business_id, business_type),
    INDEX idx_operation (operation_type, operation_status),
    INDEX idx_file_path (file_path)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='文件操作全量日志表（上传/删除）';

CREATE TABLE IF NOT EXISTS file_exception_handler
(
    id              BIGINT UNSIGNED NOT NULL COMMENT '主键ID（雪花ID）',
    log_id          BIGINT UNSIGNED NOT NULL COMMENT '关联操作日志ID',
    business_id     BIGINT          NOT NULL COMMENT '业务ID',
    business_type   INT             NOT NULL COMMENT '业务类型：1=AI角色头像 2=用户头像 3=书籍封面',
    file_path       VARCHAR(512)    NOT NULL COMMENT '异常文件路径',
    operation_type  TINYINT         NOT NULL COMMENT '异常操作类型：1=上传 2=删除',
    retry_count     TINYINT UNSIGNED DEFAULT 0 COMMENT '已重试次数',
    max_retry       TINYINT UNSIGNED DEFAULT 3 COMMENT '最大重试次数',
    final_fail_reason VARCHAR(1024) NOT NULL COMMENT '最终失败原因',
    handle_status   TINYINT         DEFAULT 0 COMMENT '处理状态：0=待人工处理 1=处理中 2=已处理 3=忽略',
    handle_remark   VARCHAR(512)     NULL COMMENT '人工处理备注',
    create_time     DATETIME         DEFAULT CURRENT_TIMESTAMP COMMENT '异常创建时间',
    update_time     DATETIME         DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_log_id (log_id),
    INDEX idx_business (business_id, business_type),
    INDEX idx_handle_status (handle_status)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='文件异常处理表（重试失败-人工处理）';