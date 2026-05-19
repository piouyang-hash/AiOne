-- 创建新的user表，字段对应username和password
-- 完整建表语句（含逻辑删除+更新时间）
CREATE TABLE IF NOT EXISTS `user`
(
    `id`          BIGINT PRIMARY KEY AUTO_INCREMENT,
    `password`    VARCHAR(100) NOT NULL,
    `email`       VARCHAR(100) NOT NULL UNIQUE,
    `role`        TINYINT      NOT NULL DEFAULT 2 CHECK (role IN (1, 2, 3)),
    `create_time` DATETIME     NOT NULL DEFAULT NOW(),
    -- 新增逻辑删除字段
    `is_deleted`  TINYINT      NOT NULL DEFAULT 0 COMMENT '系统逻辑删除（0=未删除，1=已删除，仅系统底层使用）',
    -- 新增更新时间字段（ON UPDATE NOW() 实现更新时自动刷新）
    `update_time` DATETIME     NOT NULL DEFAULT NOW() ON UPDATE NOW() COMMENT '记录最后更新时间'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- 用户资料表（最终版：新增app_type + 调整唯一索引，实现应用隔离）
CREATE TABLE IF NOT EXISTS `user_profile`
(
    `id`          BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id`     BIGINT      NOT NULL, -- 关联用户表id，移除单独UNIQUE
    `app_type`    TINYINT     NOT NULL DEFAULT 1 COMMENT '应用标识（枚举值：1=阅读器，2=AI聊天，3=拓展位），用于多应用资料隔离', -- 【核心修改：VARCHAR→TINYINT】
    `nickname`    VARCHAR(50) NOT NULL,        -- 昵称
    `bio`         TEXT,                        -- 个人简介
    `avatar_url`  VARCHAR(255),                -- 头像链接
    `create_time` DATETIME    NOT NULL DEFAULT NOW(),
    FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
    -- 联合唯一索引：user_id+app_id保证一个用户在不同应用下有独立资料
    UNIQUE INDEX `idx_user_app` (`user_id`, `app_type`) COMMENT '用户+应用联合唯一，实现多应用资料隔离'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- 打赏记录表
CREATE TABLE IF NOT EXISTS `reward_record`
(
    `id`            BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id`       BIGINT       NULL,     -- 可为NULL，对应SQLite的INTEGER
    `user_username` VARCHAR(50)  NOT NULL, -- 冗余用户名→VARCHAR(50)
    `user_email`    VARCHAR(100) NOT NULL, -- 冗余邮箱→VARCHAR(100)
    `reward_amount` INT          NOT NULL, -- 打赏金额（5/10/15）→INT（足够用）
    `reward_time`   DATETIME     NOT NULL DEFAULT NOW(),
    `remark`        TEXT,                  -- 备注可长，保留TEXT
    -- 外键约束：用户删除时user_id设为NULL，保留ON DELETE SET NULL
    FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE SET NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- 用户意见反馈表
CREATE TABLE IF NOT EXISTS `user_feedback`
(
    `id`            BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id`       BIGINT      NOT NULL,
    `feedback_type` VARCHAR(20) NOT NULL CHECK (feedback_type IN (
                                                                  'add_book',
                                                                  'function',
                                                                  'experience',
                                                                  'bug',
                                                                  'suggestion',
                                                                  'other'
        )),                                                                     -- TEXT→VARCHAR(20)，CHECK约束保留
    `content`       TEXT        NOT NULL,                                       -- 反馈内容较长，保留TEXT
    `contact`       VARCHAR(100)         DEFAULT NULL,                          -- 联系方式→VARCHAR(100)
    `status`        TINYINT     NOT NULL DEFAULT 0 CHECK (status IN (0, 1, 2)), -- 状态用TINYINT（1字节，更省空间）
    `reply_content` TEXT                 DEFAULT NULL,                          -- 回复内容保留TEXT
    `reply_time`    DATETIME             DEFAULT NULL,
    `create_time`   DATETIME    NOT NULL DEFAULT NOW(),
    `update_time`   DATETIME    NOT NULL DEFAULT NOW(),
    -- 外键约束保留
    FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `user_subscription`
(
    `id`                    BIGINT PRIMARY KEY COMMENT '订阅记录ID（雪花ID，程序生成）', -- 移除AUTO_INCREMENT，雪花ID由程序生成
    `user_id`               BIGINT          NOT NULL COMMENT '关联用户表的用户ID（整型）',    -- BIGINT→INT
    `service_type`          TINYINT          NOT NULL COMMENT '服务类型（1-AI微服务，2-文档服务，3-存储服务，可拓展）',
    `subscription_type`     TINYINT          NOT NULL COMMENT '订阅类型（1-月卡，2-年卡，3-终身卡，4-体验卡）',
    `subscription_status`   TINYINT          NOT NULL DEFAULT 4 COMMENT '生效状态（1-生效中，2-已过期，3-暂停，4-未激活，5-已取消）',
    `pay_order_id`          BIGINT       UNIQUE   COMMENT '支付订单号（关联订单微服务的订单ID，long类型对应BIGINT）',
    `effective_time`        DATETIME     NOT NULL COMMENT '订阅生效时间',
    `expire_time`           DATETIME     NOT NULL COMMENT '订阅过期时间（终身卡填9999-12-31 23:59:59）',
    `create_time`           DATETIME     NOT NULL DEFAULT NOW() COMMENT '记录创建时间',
    `update_time`           DATETIME     NOT NULL DEFAULT NOW() ON UPDATE NOW() COMMENT '记录更新时间',
    `remark`                VARCHAR(255)          DEFAULT '' COMMENT '备注（如“2026年1月续费AI年卡”）',
    -- 外键关联user表（注意user表的id需为INT，否则需调整）
    CONSTRAINT fk_subscription_user FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT '用户订阅表（通用，支持AI/文档/存储等多服务订阅）';
