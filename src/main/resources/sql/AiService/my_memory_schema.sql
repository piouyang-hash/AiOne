-- 1. 爱好字典表（存储所有可选爱好，原子化存储）
CREATE TABLE IF NOT EXISTS `base_hobby`
(
    `hobby_id`    int         NOT NULL AUTO_INCREMENT COMMENT '爱好ID（主键）',
    `hobby_name`  varchar(50) NOT NULL COMMENT '爱好编码（对应HobbyEnum的code，如SPORT_RUNNING）',
    `create_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`hobby_id`),
    UNIQUE KEY `uk_hobby_name` (`hobby_name`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- 2. 个人基本信息表（新增call_name字段 + 在线时间字段 + 逻辑删除字段适配SAGA）
CREATE TABLE IF NOT EXISTS `base_user_info`
(
    `id`                  bigint      NOT NULL COMMENT '雪花ID（主键）',
    `user_id`             int         NOT NULL COMMENT '用户ID（Integer类型）',
    `call_name`           varchar(20) NULL COMMENT 'AI对用户的称呼（如：小明、李总、王同学）', -- 新增：AI专属称呼
    `online_time_range`   varchar(50) NULL COMMENT '用户在线时间段（AI判断是否主动发消息，格式示例：09:00-22:00）', -- 新增：在线时间
    `gender`              tinyint     NULL COMMENT '性别：1=男，2=女，0=未知',
    `age`                 tinyint     NULL COMMENT '年龄（原子值，0-120）',
    `identity`            varchar(50) NULL COMMENT '身份（如：学生、上班族、自由职业）',
    `create_time`         datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`         datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted`          tinyint     DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除（适配SAGA补偿场景）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`),
    INDEX `idx_is_deleted` (`is_deleted`) -- 逻辑删除字段索引
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- 3. 个人爱好关联表（优化：冗余user_id，兼顾范式+易用性 + 逻辑删除字段适配SAGA）
CREATE TABLE IF NOT EXISTS `user_hobby_relation`
(
    `id`           bigint   NOT NULL COMMENT '雪花ID（主键）',
    `user_info_id` bigint   NOT NULL COMMENT '关联base_user_info的主键ID（外键，保证数据一致性）',
    `user_id`      int      NOT NULL COMMENT '冗余：用户ID（直接关联业务维度，避免关联查询）',
    `hobby_id`     int      NOT NULL COMMENT '关联base_hobby的爱好ID',
    `create_time`  datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `is_deleted`   tinyint  DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除（适配SAGA补偿场景）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_hobby` (`user_info_id`, `hobby_id`), -- 保证一个用户一个爱好只关联一次
    UNIQUE KEY `uk_userid_hobby` (`user_id`, `hobby_id`),    -- 新增：业务维度唯一索引
    KEY `idx_hobby_id` (`hobby_id`),
    KEY `idx_user_id` (`user_id`),                           -- 新增：user_id索引，优化查询
    KEY `idx_is_deleted` (`is_deleted`),                     -- 逻辑删除字段索引
    CONSTRAINT `fk_user_hobby_user` FOREIGN KEY (`user_info_id`) REFERENCES `base_user_info` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_user_hobby_hobby` FOREIGN KEY (`hobby_id`) REFERENCES `base_hobby` (`hobby_id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;


