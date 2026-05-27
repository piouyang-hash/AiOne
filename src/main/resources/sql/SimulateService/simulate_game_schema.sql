-- 1. 地点表（无修改，保持原有逻辑）
CREATE TABLE IF NOT EXISTS `simulate_location`
(
    `location_id`   int         NOT NULL AUTO_INCREMENT COMMENT '地点ID（主键）',
    `location_code` varchar(50) NOT NULL COMMENT '地点编码（如CITY_CENTER、FOREST、HOME、SCHOOL）',
    `location_desc` varchar(255) COMMENT '地点描述',
    `create_time`   datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间（真实时间）',
    `update_time`   datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录更新时间（真实时间）',
    PRIMARY KEY (`location_id`),
    UNIQUE KEY `uk_location_code` (`location_code`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT = '模拟游戏-地点表';

-- 2. 事件表（核心修改：移除default_duration字段）
CREATE TABLE IF NOT EXISTS `simulate_event`
(
    `event_id`     int         NOT NULL AUTO_INCREMENT COMMENT '事件ID（主键）',
    `event_code`   varchar(50) NOT NULL COMMENT '事件编码（如TRADING、SLEEP、STUDY）',
    `event_desc`   varchar(255) COMMENT '事件描述（仅描述事件本身，如“睡觉”“交易”）',
    `create_time`  datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间（真实时间）',
    `update_time`  datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录更新时间（真实时间）',
    PRIMARY KEY (`event_id`),
    UNIQUE KEY `uk_event_code` (`event_code`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT = '模拟游戏-事件表（仅存储事件本身属性，不包含地点相关属性）';

-- 3. 地点-事件关联表（核心修改：新增event_duration字段，存储该地点下该事件的持续时间）
CREATE TABLE IF NOT EXISTS `simulate_location_event_relation`
(
    `relation_id`   int         NOT NULL AUTO_INCREMENT COMMENT '关联ID（主键）',
    `location_code` varchar(50) NOT NULL COMMENT '地点编码',
    `event_code`    varchar(50) NOT NULL COMMENT '事件编码',
    -- 核心新增：该地点下该事件的默认持续秒数（业务核心属性）
    `event_duration` int         NOT NULL DEFAULT 120 COMMENT '事件在该地点的默认持续秒数（游戏服务器时间跨度，如睡觉在家=28800秒，在学校=3600秒）',
    `create_time`   datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间（真实时间）',
    `update_time`   datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录更新时间（真实时间）',
    PRIMARY KEY (`relation_id`),
    UNIQUE KEY `uk_location_event` (`location_code`, `event_code`), -- 保证一个地点+一个事件仅一条关联
    KEY `idx_location` (`location_code`),
    KEY `idx_event` (`event_code`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT = '模拟游戏-地点与事件多对多关联表（存储关联属性：事件在该地点的持续时间）';

-- 4. 地点-事件-属性影响表（核心：定义事件对人物状态的改变）
CREATE TABLE IF NOT EXISTS `simulate_location_event_effect`
(
    `effect_id`     int         NOT NULL AUTO_INCREMENT COMMENT '影响效果ID（主键）',
    `location_code` varchar(50) NOT NULL COMMENT '地点编码（关联simulate_location）',
    `event_code`    varchar(50) NOT NULL COMMENT '事件编码（关联simulate_event）',
    -- 三维属性变化值（正数=增加，负数=减少）
    `hunger_effect` decimal(4,1) NOT NULL DEFAULT 0.0 COMMENT '饥饿值变化量（执行事件后）',
    `energy_effect` decimal(4,1) NOT NULL DEFAULT 0.0 COMMENT '精力值变化量（执行事件后）',
    `mood_effect`   decimal(4,1) NOT NULL DEFAULT 0.0 COMMENT '心情值变化量（执行事件后）',
    `create_time`   datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间（真实时间）',
    `update_time`   datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录更新时间（真实时间）',
    PRIMARY KEY (`effect_id`),
    UNIQUE KEY `uk_location_event` (`location_code`, `event_code`), -- 一个地点+事件 仅一套属性效果
    KEY `idx_location` (`location_code`),
    KEY `idx_event` (`event_code`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT = '模拟游戏-地点事件对人物三维属性的影响表';

-- 4. 事件执行序列规则表（适配：版本号=每日安排，如1=周一、2=周二，按地点+次序执行事件）
CREATE TABLE IF NOT EXISTS `simulate_event_sequence`
(
    `sequence_id`   bigint      NOT NULL AUTO_INCREMENT COMMENT '序列规则ID（主键）',
    `roleId`        int         NOT NULL DEFAULT 6 COMMENT '角色ID', -- 新增字段
    `version`       int         NOT NULL DEFAULT 1 COMMENT '每日安排版本号（如1=周一、2=周二、3=周三，区分不同日期的事件安排）',
    `location_code` varchar(50) NOT NULL COMMENT '地点编码（如HOME=家、SCHOOL=学校、SUPERMARKET=超市）',
    `event_code`    varchar(50) NOT NULL COMMENT '事件编码（如SLEEP=睡觉、EAT=吃饭、STUDY=学习、SHOP=逛街）',
    `seq_num`       int         NOT NULL COMMENT '当日执行次序（1=第1件事、2=第2件事…按次序执行）',
    `create_time`   datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间（真实时间）',
    `update_time`   datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录更新时间（真实时间）',
    PRIMARY KEY (`sequence_id`),
    UNIQUE KEY uk_version_seq (`version`, `seq_num`) COMMENT '防重复：同一日期版本+执行次序，仅能安排一个事件',
    KEY `idx_version` (`version`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT = '模拟游戏-每日事件安排表（按版本号区分每日事件序列，循环执行）';

-- 5. 事件执行记录表（核心：全部用线性游戏服务器时间）
CREATE TABLE IF NOT EXISTS `simulate_event_record`
(
    `record_id`        bigint      NOT NULL COMMENT '记录ID（雪花ID）',
    `roleId`           int         NOT NULL DEFAULT 6 COMMENT '角色ID', -- 新增字段
    `sequence_id`      bigint      NOT NULL COMMENT '关联序列规则ID',
    `event_code`       varchar(50) NOT NULL COMMENT '事件编码',
    `location_code`    varchar(50) NOT NULL COMMENT '地点编码',
    `actual_start`     int         NOT NULL COMMENT '实际开始（游戏服务器时间，int，线性递增）',
    `actual_end`       int         NULL COMMENT '实际结束（游戏服务器时间，int，线性递增）【非必填】',
    `default_duration` int         NULL COMMENT '默认执行时间（秒，事件自身特性，冗余存储仅用于查阅，非必填）',
    `exec_status`      varchar(20) NOT NULL DEFAULT 'FINISHED' COMMENT 'EXECUTING-执行中/FINISHED-已完成/FAILED-失败/INTERRUPTED-中断',
    `version`          int         NOT NULL COMMENT '关联每日安排版本号（对应simulate_event_sequence表）',
    `seq_num`          int         NOT NULL COMMENT '关联当日执行次序（对应simulate_event_sequence表）',
    `create_time`      datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间（真实时间）',
    `update_time`      datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录更新时间（真实时间）',
    CONSTRAINT `ck_event_record_end_status`
        CHECK (
            (`exec_status` != 'FINISHED') OR (`actual_end` IS NOT NULL)
            ),
    PRIMARY KEY (`record_id`),
    UNIQUE KEY `uk_event_location_start` (`event_code`, `location_code`, `actual_start`),
    KEY `idx_version_seq` (`version`, `seq_num`),
    KEY `idx_sequence_id` (`sequence_id`),
    KEY `idx_actual_range` (`actual_start`, `actual_end`),
    KEY `idx_event_location` (`event_code`, `location_code`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT = '模拟游戏-事件执行记录表（全局线性游戏服务器时间）';


-- 全局游戏线性时间表（核心：仅1条数据，每次定时扫描 +60 秒）
CREATE TABLE IF NOT EXISTS `simulate_game_global_time`
(
    `id`                 int         NOT NULL AUTO_INCREMENT COMMENT '主键ID（固定1条数据）',
    `global_game_seconds` int         NOT NULL DEFAULT 0 COMMENT '全局游戏总秒数（核心：每次扫描+60，永不递减）',
    `create_time`        datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`        datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT = '模拟游戏-全局线性时间（每次扫描+60秒，游戏唯一时间基准）';

-- 初始化唯一数据（必须执行！项目启动后只有这一条时间数据）
INSERT IGNORE INTO `simulate_game_global_time` (`id`, `global_game_seconds`) VALUES (1, 0);

-- 修复后：完美运行，无报错
CREATE TABLE IF NOT EXISTS `simulate_person_state`
(
    `id` tinyint NOT NULL DEFAULT 1 COMMENT '主键（固定1，单角色模拟）',
    -- 修复：decimal(4,1) 支持存储 100.0
    `hunger` decimal(4,1) NOT NULL DEFAULT 0 COMMENT '饥饿值 0-100（越高越饿，每分钟+1.5）',
    `energy` decimal(4,1) NOT NULL DEFAULT 100 COMMENT '精力值 0-100（越低越累，每分钟-0.8）',
    `mood` decimal(4,1) NOT NULL DEFAULT 50 COMMENT '心情值 0-100（越高越开心，每分钟-0.3）',
    `current_location_code` varchar(50) NULL COMMENT '当前地点编码',
    `current_event_code` varchar(50) NULL COMMENT '当前事件编码',
    `activity_end_global_sec` bigint NOT NULL DEFAULT 0 COMMENT '当前活动结束的全局秒数',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '状态更新时间（真实时间）',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT = '模拟游戏-人物实时状态表（精简版）';

-- 初始化人物状态：在家、无活动、三维属性默认值
INSERT INTO `simulate_person_state`
(hunger, energy, mood, current_location_code, current_event_code, activity_end_global_sec)
VALUES (0, 100, 50, 'HOME', 'SLEEP', 28800);