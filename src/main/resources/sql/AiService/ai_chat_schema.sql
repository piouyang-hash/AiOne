-- 1. AI对话会话主表（建表幂等：IF NOT EXISTS 保证有表就不创建）
CREATE TABLE IF NOT EXISTS `ai_chat_session`
(
    `session_uuid`         CHAR(36)   NOT NULL COMMENT '前端生成的临时会话凭证(UUID)',
    `session_id`           BIGINT     NOT NULL COMMENT '会话ID（雪花ID，数字类型，核心关联键）',
    `user_id`              BIGINT              DEFAULT 0 COMMENT '归属用户ID（0=匿名，>0=登录用户）',
    `chat_title`           varchar(128)        DEFAULT '新聊天' COMMENT '对话标题（比如取第一条用户消息，如“关于AI的提问”）',
    `chat_avatar_path`     varchar(255)        DEFAULT '' COMMENT '对话头像路径（存储头像文件的相对/绝对路径，如/avatars/ai/session_123.png）',
    `last_message_content` text COMMENT '冗余字段：会话最后一条消息内容（用于列表预览展示，无需完整内容）',
    `last_chat_timestamp`  BIGINT              DEFAULT 0 COMMENT '上次聊天时间戳（毫秒级，对应Java Long类型，用于时间计算）',
    `create_time`          datetime            DEFAULT CURRENT_TIMESTAMP COMMENT '会话创建时间',
    `update_time`          datetime            DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '会话更新时间（仅更新last_message_content时同步更新）',
    `status`               TINYINT             DEFAULT 1 COMMENT '会话状态（0=关闭，1=活跃）',
    `is_system_session`    TINYINT             DEFAULT 0 COMMENT '是否是系统会话（0=非系统会话，1=系统会话）',
    `is_deleted`           TINYINT    NOT NULL DEFAULT 0 COMMENT '系统逻辑删除（0=未删除，1=已删除，仅系统底层使用）',
    `is_recycle`           TINYINT    NOT NULL DEFAULT 0 COMMENT '回收站删除（0=未放入回收站，1=已放入回收站，用户侧删除操作）',
    `is_top`               TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否置顶 0-否 1-是',
    `top_at`               BIGINT              DEFAULT NULL COMMENT '置顶时间戳(毫秒)，置顶时更新',
    `normal_unread_count`  INT                 DEFAULT 0 COMMENT '非切分模式未读消息数',
    `split_unread_count`   INT                 DEFAULT 0 COMMENT '切分模式未读消息数',
    `role_id`              INT                 DEFAULT 1 NULL COMMENT 'AI角色ID，关联角色表（默认值1）',
    PRIMARY KEY (`session_id`),
    UNIQUE INDEX idx_session_uuid (`session_uuid`),
    INDEX idx_user_id (`user_id`),
    INDEX idx_is_deleted (`is_deleted`),
    INDEX idx_is_recycle (`is_recycle`),
    INDEX idx_status (`status`),
    INDEX idx_is_system_session (`is_system_session`),
    INDEX idx_is_top (`is_top`, `top_at`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='AI对话会话主表';

-- 2. AI对话系统提示词消息表（为SpringAi创建的新表，弥补不足）
CREATE TABLE IF NOT EXISTS `ai_chat_session_system_prompt`
(
    `id`            BIGINT NOT NULL COMMENT '主键ID（手动填入雪花ID，非自增）',
    `session_id`    BIGINT NOT NULL COMMENT '关联的会话ID（关联ai_chat_session.session_id）',
    `user_id`       BIGINT   DEFAULT 0 COMMENT '归属用户ID（0=匿名，>0=登录用户，与主表一致）',
    `serial_number` INT    NOT NULL COMMENT '序列号（同一user_id+session_id下从1开始递增）',
    `system_prompt` TEXT   NOT NULL COMMENT '系统提示词文本（核心字段，存储完整的AI系统指令）',
    `create_time`   DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间（首次生成提示词的时间）',
    `update_time`   DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间戳（提示词修改时自动更新）',
    PRIMARY KEY (`id`),
    -- 核心联合唯一索引：保证同一用户+同一会话下序列号唯一，避免重复
    UNIQUE INDEX uk_user_session_serial (`user_id`, `session_id`, `serial_number`),
    -- 辅助索引：按会话ID查询所有提示词（含序列号排序）
    INDEX idx_session_id (`session_id`),
    -- 辅助索引：按用户ID筛选
    INDEX idx_user_id (`user_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='AI对话会话-系统提示词表（支持同一会话多版本提示词，按序列号递增）';

-- 3. AI对话消息表（核心表，建表幂等：IF NOT EXISTS 避免重复创建报错）
CREATE TABLE IF NOT EXISTS `ai_chat_message`
(
    `message_id`      BIGINT  NOT NULL COMMENT '消息主键（雪花ID）',
    `session_id`      BIGINT  NOT NULL COMMENT '会话ID（关联新对话）',
    `parent_msg_id`   BIGINT   DEFAULT NULL COMMENT '父消息ID：AI回复指向对应的用户消息ID，用户消息为null',
    `task_id`         VARCHAR(64) NOT NULL COMMENT 'AI流式任务唯一ID(UUID)', -- 🔥 新增必填字段
    `user_id`         BIGINT   DEFAULT 0 COMMENT '归属用户ID',
    `role`            TINYINT NOT NULL COMMENT '1=用户，2=AI助手，3=SpringBoot系统',
    `content`         TEXT    NOT NULL COMMENT '消息原始内容',
    `summary_content` TEXT     DEFAULT NULL COMMENT '消息摘要/总结文本（滑动窗口专用，非必填）',
    `split_content`   TEXT     DEFAULT NULL COMMENT '消息切分后的JSON文本（该消息被切分成多条段落，可为null）',
    `is_temp`         TINYINT NOT NULL DEFAULT 0 COMMENT '是否临时消息 0=否 1=是',
    `sort_timestamp`  BIGINT  NOT NULL COMMENT '排序专用时间戳（消息完成瞬间毫秒级，前端排序依据）',
    `create_time`     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    -- 主键索引（原有）
    PRIMARY KEY (`message_id`) USING BTREE,
    -- 原有索引1：加速会话维度的消息列表查询
    INDEX idx_session_id (`session_id`) USING BTREE,
    -- 原有索引2：加速父消息关联的反查
    INDEX idx_parent_msg_id (`parent_msg_id`) USING BTREE,
    -- 新增索引：加速 task_id 维度查询
    INDEX idx_task_id (`task_id`) USING BTREE,
    -- 原有索引3：加速查询「用户最新聊天时间」
    INDEX idx_user_create_time (`user_id`, `create_time` DESC) USING BTREE,
    -- 新增索引4：适配「会话+用户+role=1+时间升序」查询
    INDEX idx_session_user_role_create_time (`session_id`, `user_id`, `role`, `create_time` ASC) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='AI消息表（显式关联版）';

-- AI基本性格配置表：控制AI底层性格+强度等级，替代原AI心情配置表
CREATE TABLE IF NOT EXISTS `ai_personality_config`
(
    `id`                        BIGINT   NOT NULL COMMENT '主键ID（雪花ID，手动生成）',
    `user_id`                   BIGINT            DEFAULT NULL COMMENT '归属用户ID（Integer类型，null=所有用户通用，非null=该用户专属性格）',
    `ai_personality_code`       TINYINT  NOT NULL DEFAULT 1 COMMENT 'AI基本性格编码（关联AiPersonalityEnum枚举code，默认中性（1））',
    `personality_strength_code` TINYINT  NOT NULL DEFAULT 1 COMMENT '性格强度编码（关联AiPersonalityStrengthEnum枚举code，默认1级：基础强度）',
    `create_time`               datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`               datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_valid`                  TINYINT  NOT NULL DEFAULT 1 COMMENT '是否有效（1=有效，0=无效）',
    `is_deleted`                TINYINT  NOT NULL DEFAULT 0 COMMENT '删除状态（0=未删除，1=已删除）',
    PRIMARY KEY (`id`),
    -- 核心索引：仅按用户维度查询（适配“一个用户一个AI”）
    UNIQUE KEY `uk_user_id` (`user_id`) COMMENT '用户唯一索引（保证一个用户仅1套性格配置）',
    KEY `idx_create_time` (`create_time`) COMMENT '按创建时间排序索引'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='AI基本性格配置表（控制AI底层性格类型+强度等级，一个用户仅生效1套性格配置）';

-- AI心情配置表：存储AI在不同场景下的说话风格（心情+强度）
CREATE TABLE IF NOT EXISTS `ai_mood_config`
(
    `id`               BIGINT   NOT NULL COMMENT '主键ID（雪花ID，手动生成）',
    `user_id`          BIGINT            DEFAULT NULL COMMENT '归属用户ID（Integer类型，null=所有用户通用，非null=该用户专属配置）',
    `ai_mood_code`     TINYINT  NOT NULL DEFAULT 1 COMMENT 'AI心情编码（关联AiMoodEnum枚举code，默认无心情（1））',
    `ai_strength_code` TINYINT  NOT NULL DEFAULT 1 COMMENT 'AI强度编码（关联AiMoodStrengthEnum枚举code，默认1级）',
    `create_time`      datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`      datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_valid`         TINYINT  NOT NULL DEFAULT 1 COMMENT '是否有效（1=有效，0=无效）',
    `is_deleted`       TINYINT  NOT NULL DEFAULT 0 COMMENT '删除状态（0=未删除，1=已删除）',
    PRIMARY KEY (`id`),
    -- 核心索引：仅按用户维度查询
    KEY `idx_user_id` (`user_id`) COMMENT '用户索引（核心查询索引）',
    KEY `idx_create_time` (`create_time`) COMMENT '按创建时间排序索引'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='AI心情配置表（控制AI说话风格和强度，一个用户可配置多个心情）';

-- AI情绪实时状态表：存储用户级AI动态情绪值（喜爱值/活跃度），配套ai_mood_config主基调表使用
CREATE TABLE IF NOT EXISTS `ai_emotion_real_state`
(
    `id`             BIGINT   NOT NULL COMMENT '主键ID（雪花ID，手动生成）',
    `user_id`        BIGINT   NOT NULL COMMENT '归属用户ID（Integer类型，非空，关联base_user_info的user_id）',
    `like_value`     BIGINT   NOT NULL DEFAULT 0 COMMENT '喜爱值（情绪状态机核心值：-100=极度愤怒，0=冷静（初始值），100=极度喜爱）',
    `activity_value` TINYINT  NOT NULL DEFAULT 50 COMMENT '活跃度（0=最低/沉默，50=中等/正常，100=最高/亢奋，控制AI回复的积极程度）',
    `familiarity`    TINYINT  NOT NULL DEFAULT 0 COMMENT '熟悉度（0=完全陌生，100=极度熟悉，反映AI对用户的了解程度）',
    `create_time`    datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '状态创建时间（用户首次触发情绪时生成）',
    `update_time`    datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '状态更新时间（每次情绪值变化自动更新）',
    `is_valid`       TINYINT  NOT NULL DEFAULT 1 COMMENT '是否有效（1=有效，0=无效/用户注销）',
    `is_deleted`     TINYINT  NOT NULL DEFAULT 0 COMMENT '删除状态（0=未删除，1=已删除）',
    PRIMARY KEY (`id`),
    -- 核心索引：用户唯一索引
    UNIQUE KEY `uk_user_id` (`user_id`) COMMENT '用户唯一索引（保证一个用户只有一条实时情绪状态）',
    -- 辅助索引
    KEY `idx_update_time` (`update_time`) COMMENT '按状态更新时间索引',
    -- 活跃度专用索引
    KEY `idx_activity_update` (`activity_value`, `update_time`) COMMENT '按活跃度+更新时间索引，适配活跃度=100的查询',
    -- 【新增】喜爱度专用索引
    KEY `idx_is_deleted_like_update_user` (`is_deleted`, `like_value`, `update_time`, `user_id`) COMMENT '喜爱度>0查询专用覆盖索引'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='AI情绪实时状态表（动态存储喜爱值/活跃度，约束AI情绪变化，避免突变）';

-- AI情绪变动流水表：记录AI情绪值的每一次波动明细（关联base_behavior）
CREATE TABLE IF NOT EXISTS `ai_emotion_log_detail`
(
    `id`            BIGINT      NOT NULL COMMENT '雪花ID（主键）',
    `user_id`       BIGINT      NOT NULL COMMENT '用户ID',
    `behavior_id`   INT         NOT NULL COMMENT '触发变动的行为ID',
    `emotion_type`  VARCHAR(20) NOT NULL COMMENT '情绪类型',
    `change_value`  SMALLINT    NOT NULL COMMENT '变动分值',
    `value_after`   BIGINT      NOT NULL COMMENT '变动后分值',
    `change_reason` VARCHAR(255)         DEFAULT NULL COMMENT '变动原因',
    `behavior_time` BIGINT      NOT NULL COMMENT '行为时间戳(毫秒)',
    `create_time`   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`), -- 雪花ID做主键，天然唯一
    INDEX `idx_user_emotion` (`user_id`, `emotion_type`),
    INDEX `idx_create_time` (`create_time`),
    INDEX `idx_behavior_time` (`behavior_time`)
    -- 已删除错误的唯一键 uk_user_behavior_type
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT = 'AI情绪变动流水表';

-- 1. AI行为字典表（原子化存储所有AI行为定义）
CREATE TABLE IF NOT EXISTS `base_ai_behavior`
(
    `behavior_id`   int         NOT NULL AUTO_INCREMENT COMMENT 'AI行为ID（主键）',
    `behavior_code` varchar(50) NOT NULL COMMENT 'AI行为编码（如AI_ACTIVE_SEND_MSG）',
    `behavior_name` varchar(30) NOT NULL COMMENT 'AI行为名称（中文描述）',
    `create_time`   datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`behavior_id`),
    UNIQUE KEY `uk_ai_behavior_code` (`behavior_code`) COMMENT 'AI行为编码唯一'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT = 'AI行为字典表';

-- 2. AI行为分值配置表（AI行为对应的情绪分值）
CREATE TABLE IF NOT EXISTS `ai_behavior_score`
(
    `id`            int         NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `behavior_code` varchar(50) NOT NULL COMMENT '关联AI行为编码',
    `score_type`    varchar(30) NOT NULL COMMENT '分值类型：LIKE/ACTIVITY/FAMILIARITY',
    `score_val`     tinyint     NULL COMMENT '变动分值（-100~100）',
    `create_time`   datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `uk_ai_behavior_score_type` (`behavior_code`, `score_type`) COMMENT 'AI行为+分值类型唯一'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT = 'AI行为分值配置表';

-- AI微服务-角色表（存储AI角色及人设配置）
CREATE TABLE IF NOT EXISTS `ai_role`
(
    `role_id`        int          NOT NULL AUTO_INCREMENT COMMENT '角色ID（主键）',
    `role_code`      varchar(50)  NOT NULL COMMENT '角色编码（唯一标识，如AFTER_SALE、BIRTHDAY_AI）',
    `role_desc`      varchar(255) COMMENT '角色基础描述',
    `persona_core`   text         NOT NULL COMMENT '人设核心定义',
    `persona_tone`   varchar(50)  DEFAULT 'normal' COMMENT '人设语气风格',
    `avatar_path`    varchar(255) DEFAULT '' NULL COMMENT '角色头像路径',
    `create_user_id` bigint       NOT NULL COMMENT '【新增】角色创建人ID（归属用户）',
    `role_status`    tinyint(1)   NOT NULL DEFAULT 0 COMMENT '【新增】角色状态 0-草稿 1-已发布',
    `visible_scope`  tinyint(1)   NOT NULL DEFAULT 0 COMMENT '【新增】可见范围 0-私有(仅自己) 1-公开(所有人)',
    `create_time`    datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`role_id`),
    UNIQUE KEY `uk_role_code` (`role_code`),
    KEY `idx_create_user` (`create_user_id`) COMMENT '创建人索引'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT = 'AI微服务-角色表';

CREATE TABLE IF NOT EXISTS `simulate_ai_event_prompt`
(
    `id`            int          NOT NULL AUTO_INCREMENT COMMENT '主键',
    `location_code` varchar(50)  NOT NULL COMMENT '地点编码（与事件服务一致：HOME/SCHOOL）',
    `event_code`    varchar(50)  NOT NULL COMMENT '事件编码（与事件服务一致：SLEEP/CLASS）',
    `ai_event_desc` varchar(512) NOT NULL COMMENT 'AI拟人化事件描述（提示词核心）',
    `status`        tinyint      NOT NULL DEFAULT 1 COMMENT '状态：1-启用 0-禁用',
    `create_time`   datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    `update_time`   datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP() ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_location_event` (`location_code`, `event_code`) COMMENT '唯一索引：一个地点+事件对应一条AI话术'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='AI微服务-事件话术配置表（拟人化提示词映射）';

-- AI微服务-用户配置表（单配置项：主动聊天模式）
CREATE TABLE IF NOT EXISTS `ai_config`
(
    `config_id`        int        NOT NULL AUTO_INCREMENT COMMENT '配置ID（主键）',
    `user_id`          int        NOT NULL COMMENT '用户ID（唯一对应用户）',
    `active_chat_mode` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '主动聊天模式 0-关闭(默认) 1-开启',
    `split_ai_message` TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'AI消息切分 0-不切分(默认) 1-切分',
    `create_time`      datetime   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`      datetime   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`config_id`),
    UNIQUE KEY `uk_user_id` (`user_id`) COMMENT '一个用户仅一条配置'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT = 'AI微服务-用户配置表（存储用户个性化AI配置）';

-- AI微服务-用户与角色绑定表（多对多：用户使用的角色）
CREATE TABLE IF NOT EXISTS `user_ai_role_bind`
(
    `id`          bigint     NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`     bigint     NOT NULL COMMENT '用户ID',
    `role_id`     int        NOT NULL COMMENT 'AI角色ID',
    `create_time` datetime   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '绑定时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_role` (`user_id`,`role_id`) COMMENT '防止重复绑定',
    KEY `idx_user_id` (`user_id`),
    KEY `idx_role_id` (`role_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT = 'AI微服务-用户与角色绑定表(多对多)';

-- 1. 用户行为字典表（移除所有 user_ 前缀）
CREATE TABLE IF NOT EXISTS `base_user_behavior`
(
    `behavior_id`    int         NOT NULL AUTO_INCREMENT COMMENT '行为ID（主键）',
    `behavior_code`  varchar(50) NOT NULL COMMENT '行为编码（对应BehaviorEnum的code，如CHAT_SEND_MSG、LIKE_AI_REPLY）',
    `behavior_name`  varchar(30) NOT NULL COMMENT '行为名称（中文描述，如：发送聊天消息、点赞AI回复）',
    `create_time`    datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`behavior_id`),
    UNIQUE KEY `uk_behavior_code` (`behavior_code`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT = '用户行为字典表（原子化存储所有用户行为定义）';

-- 2. 用户行为分值配置表（无需修改，保持原样）
CREATE TABLE IF NOT EXISTS `user_behavior_score`
(
    id              int         NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    behavior_code   varchar(50) NOT NULL COMMENT '关联用户行为编码',
    score_type      varchar(30) NOT NULL COMMENT '分值类型：LIKE-喜爱值,ACTIVITY-活跃度,FAMILIARITY-熟悉度',
    score_val       tinyint     NULL COMMENT '变动分值（可正可负，NULL表示不配置）',
    create_time     datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `uk_behavior_score_type` (behavior_code, score_type) COMMENT '行为+分值类型唯一约束',
    CONSTRAINT `ck_score_val` CHECK (`score_val` BETWEEN -100 AND 100)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT = '用户行为分值配置表（多维度分值独立配置）';

-- 3. 用户特征分值表（按用户维度存储，移除session_id）
CREATE TABLE IF NOT EXISTS `user_feature_score`
(
    `id`             bigint   NOT NULL COMMENT '雪花ID（主键）',
    `user_id`        int      NOT NULL COMMENT '用户ID（关联业务维度用户ID）',
    `activity_score` tinyint  NOT NULL DEFAULT 0 COMMENT '用户活跃度分值（0-100，默认0）',
    `favor_score`    tinyint  NOT NULL DEFAULT 0 COMMENT '用户对AI喜爱度分值（-100到100，默认0）',
    `familiar_score` tinyint  NOT NULL DEFAULT 0 COMMENT 'AI对用户熟悉度分值（0-100，默认0）',
    `create_time`    datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`    datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    -- 调整唯一索引：仅用户ID唯一（一个用户一套特征分值）
    UNIQUE KEY `uk_user_id` (`user_id`),
    -- 保留用户ID索引（核心查询维度）
    INDEX `idx_user_id` (`user_id`),
    -- 新增：复合索引（活跃度+更新时间），优化「活跃度=0」的查询，支持时间筛选
    INDEX `idx_activity_update` (`activity_score`, `update_time`),
    -- 分值范围校验（保留）
    CONSTRAINT `CK_ACTIVITY_SCORE` CHECK (`activity_score` BETWEEN 0 AND 100),
    CONSTRAINT `CK_FAVOR_SCORE` CHECK (`favor_score` BETWEEN -100 AND 100),
    CONSTRAINT `CK_FAMILIAR_SCORE` CHECK (`familiar_score` BETWEEN 0 AND 100)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT = '用户特征分值表（按【用户】维度存储活跃度/喜爱度/熟悉度）';

-- 4. 行为加分明细表（移除session_id，按用户维度记录）
CREATE TABLE IF NOT EXISTS `user_behavior_score_detail`
(
    `id`            bigint      NOT NULL COMMENT '雪花ID（主键）',
    `user_id`       int         NOT NULL COMMENT '用户ID（冗余，优化查询）',
    `behavior_id`   int         NOT NULL COMMENT '关联base_user_behavior的行为ID（业务层保证数据一致性）',
    `feature_type`  varchar(20) NOT NULL COMMENT '加分特征类型（ACTIVITY-活跃度/FAVOR-喜爱度/FAMILIAR-熟悉度）',
    `add_score`     tinyint     NOT NULL COMMENT '本次加分值（可正可负，范围-100~100）',
    `score_after`   tinyint     NOT NULL COMMENT '加分后特征分值（-100~100，便于回溯）',
    `behavior_time` bigint      NOT NULL COMMENT '行为发生时间戳（毫秒，精准记录行为时间）',
    `create_time`   datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    -- 主键保留
    PRIMARY KEY (`id`),
    -- 调整索引：仅保留用户维度核心索引
    INDEX `idx_user_id` (`user_id`),             -- 按用户查询（核心维度）
    INDEX `idx_behavior_id` (`behavior_id`),     -- 按行为类型查询
    INDEX `idx_feature_type` (`feature_type`),   -- 按特征类型筛选
    INDEX `idx_behavior_time` (`behavior_time`), -- 按时间排序/筛选
    -- 保留分值范围校验
    CONSTRAINT `ck_add_score` CHECK (`add_score` BETWEEN -100 AND 100),
    CONSTRAINT `ck_score_after` CHECK (`score_after` BETWEEN -100 AND 100)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT = '用户行为加分明细表（按【用户】维度记录每一次行为的特征加分）';

-- ====================== 0. AI Token类型配置表（核心解耦：多厂商+输入输出区分） ======================
-- 核心作用：统一管理所有AI厂商的输入/输出Token类型，新增厂商/类型只需加配置，不改表结构
CREATE TABLE IF NOT EXISTS `ai_token_type`
(
    `type_id`           bigint      NOT NULL AUTO_INCREMENT COMMENT 'Token类型ID（主键）',
    `vendor`            varchar(30) NOT NULL COMMENT '厂商编码 DEEPSEEK-深度求索 DOUBAO-火山豆包 OPENAI-GPT',
    `token_side`        tinyint     NOT NULL COMMENT 'Token方向 1-输入(提示词) 2-输出(回答)',
    `type_code`         varchar(50) NOT NULL COMMENT '类型唯一编码 例：DEEPSEEK_INPUT、DEEPSEEK_OUTPUT',
    `type_name`         varchar(50) NOT NULL COMMENT '类型名称 例：DeepSeek输入Token、DeepSeek输出Token',
    `price_per_million` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '每百万Token价格（元）',
    `status`            tinyint     NOT NULL DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
    `create_time`       datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`type_id`),
    UNIQUE KEY `uk_type_code` (`type_code`) COMMENT '类型编码唯一',
    INDEX `idx_vendor` (`vendor`) COMMENT '厂商查询索引'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT = 'AI微服务-Token类型配置表（多厂商+输入输出解耦）';

-- ====================== 1. AI用户Token余额表（按类型存储：输入/输出/多厂商） ======================
-- 核心作用：记录用户【每种Token】的消耗数量，精准区分计费
CREATE TABLE IF NOT EXISTS `ai_user_token_balance`
(
    `balance_id`         bigint      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`            int         NOT NULL COMMENT '用户ID',
    `type_id`            bigint      NOT NULL COMMENT 'Token类型ID（关联ai_token_type）',
    `total_consumed`     bigint      NOT NULL DEFAULT 0 COMMENT '累计消耗Token总量（统计对账）',
    `create_time`        datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`        datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`balance_id`),
    UNIQUE KEY `uk_user_type` (`user_id`, `type_id`) COMMENT '一个用户+一种类型仅一条记录',
    INDEX `idx_type_id` (`type_id`) COMMENT '类型查询索引'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT = 'AI微服务-用户Token余额表（仅统计累计消耗）';

-- ====================== 2. AI用户Token变动流水表（手动雪花ID，无自增） ======================
-- 核心作用：不可篡改流水账，记录每一笔Token消耗行为，审计/对账必备
CREATE TABLE IF NOT EXISTS `ai_user_token_record`
(
    `record_id`          bigint      NOT NULL COMMENT '流水ID（主键，手动雪花ID）',
    `user_id`            int         NOT NULL COMMENT '用户ID',
    `type_id`            bigint      NOT NULL COMMENT 'Token类型ID',
    `change_type`        tinyint     NOT NULL COMMENT '变动类型 1-消耗扣减 2-充值增加 3-赠送增加',
    `change_amount`      bigint      NOT NULL COMMENT '变动数量（正数）',
    `change_way`         varchar(50) NOT NULL COMMENT '变动方式 USER_CHAT/AI_REPLY/RECHARGE/GIFT',
    `before_consumed`    bigint      NOT NULL COMMENT '变动前累计消耗',
    `after_consumed`     bigint      NOT NULL COMMENT '变动后累计消耗',
    `remark`             varchar(255) DEFAULT NULL COMMENT '备注',
    `create_time`        datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录时间',
    PRIMARY KEY (`record_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_user_type` (`user_id`, `type_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT = 'AI微服务-用户Token流水表（消耗审计对账）';

-- ====================== AI用户统一算力积分表（极简版） ======================
CREATE TABLE IF NOT EXISTS `ai_user_point_balance`
(
    `id`            bigint      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`       int         NOT NULL COMMENT '用户ID',
    `total_point`   bigint      NOT NULL DEFAULT 0 COMMENT '总可用算力积分（唯一可消耗余额）',
    `create_time`   datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`) COMMENT '一个用户仅一条记录'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT = 'AI微服务-用户统一算力积分表（唯一可消耗资产）';

-- ====================== AI用户算力积分变动流水表（手动雪花ID，无自增） ======================
CREATE TABLE IF NOT EXISTS `ai_user_point_record`
(
    `record_id`          bigint      NOT NULL COMMENT '流水ID（主键，手动雪花ID）',
    `user_id`            int         NOT NULL COMMENT '用户ID',
    `change_type`        tinyint     NOT NULL COMMENT '变动类型 1-消耗扣减 2-充值增加 3-赠送增加',
    `change_amount`      bigint      NOT NULL COMMENT '变动积分数量（正数）',
    `change_way`         varchar(50) NOT NULL COMMENT '变动方式 USER_CHAT/AI_REPLY/RECHARGE/GIFT',

    -- 🔥 修正：这里是【余额】，不是累计消耗！
    `before_point`       bigint      NOT NULL COMMENT '变动前可用积分余额',
    `after_point`        bigint      NOT NULL COMMENT '变动后可用积分余额',

    `remark`             varchar(255) DEFAULT NULL COMMENT '备注（如：DeepSeek输出消耗/充值赠送）',
    `create_time`        datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录时间',
    PRIMARY KEY (`record_id`),
    INDEX `idx_user_id` (`user_id`) COMMENT '用户ID查询索引'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT = 'AI微服务-用户积分流水表（余额变动审计对账）';

-- 1. AI对话关键词表（核心表，建表幂等：IF NOT EXISTS 避免重复创建报错）
CREATE TABLE IF NOT EXISTS `ai_chat_keyword`
(
    `keyword_id`      BIGINT      NOT NULL COMMENT '关键词主键（雪花ID，唯一标识单条关键词）',
    `session_id`      BIGINT      NOT NULL COMMENT '会话ID（关联ai_chat_session.session_id，冗余字段）',
    `msg_id`          BIGINT      NOT NULL COMMENT '消息ID（关联ai_chat_message.id，当前关键词所属消息主键）',
    `parent_msg_id`   BIGINT   DEFAULT NULL COMMENT '父消息ID（关联ai_chat_message.parent_msg_id：AI回复关键词指向对应用户消息ID，用户消息关键词为null）',
    `user_id`         BIGINT   DEFAULT 0 COMMENT '归属用户ID', -- 新增：对齐消息表设计，DEFAULT 0（匿名用户）
    `keyword_content` varchar(64) NOT NULL COMMENT '提取的单个核心关键词（如“Redis缓存穿透”）',
    `keyword_sort`    TINYINT  DEFAULT 0 COMMENT '关键词排序（0=第1个，1=第2个...，体现优先级）',
    `create_time`     datetime DEFAULT CURRENT_TIMESTAMP COMMENT '关键词提取时间',
    PRIMARY KEY (`keyword_id`) USING BTREE,
    -- 核心索引1：加速「单消息」维度的关键词查询（如查某条消息的所有关键词）
    INDEX idx_msg_id (`msg_id`) USING BTREE,
    -- 核心索引2：加速「会话」维度的关键词查询（如查某会话的所有关键词）
    INDEX idx_session_id (`session_id`) USING BTREE,
    -- 核心索引3：加速「父消息」关联查询（如查某用户消息对应的AI回复关键词）
    INDEX idx_parent_msg_id (`parent_msg_id`) USING BTREE,
    -- 核心索引4：加速关键词内容的模糊检索（如搜“Redis”找到所有相关关键词）
    INDEX idx_keyword_content (`keyword_content`) USING BTREE,
    -- 新增索引5：加速「用户+关键词」的核心查询（如查用户123所有包含“Redis”的关键词）
    INDEX idx_user_keyword (`user_id`, `keyword_content`) USING BTREE,
    -- 新增索引6：加速「用户+会话」的关键词查询（如查用户123在会话10001的所有关键词）
    INDEX idx_user_session (`user_id`, `session_id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='AI对话关键词表（存储每条消息提取的语义关键词，显式关联父子消息）';

# DROP TABLE ai_mood_config;
# DROP TABLE ai_chat_session;
# DROP TABLE ai_chat_message;
# DROP TABLE ai_emotion_real_state;
# DROP TABLE ai_personality_config;
#
# DROP TABLE user_behavior_score_detail;
# DROP TABLE base_user_behavior;
# DROP TABLE user_feature_score;


CREATE TABLE IF NOT EXISTS ai_recharge_order
(
    id            BIGINT PRIMARY KEY COMMENT '雪花ID（充值订单ID）',
    user_id       INTEGER        NOT NULL COMMENT '充值用户ID',
    amount        DECIMAL(16, 2) NOT NULL COMMENT '充值金额（元）',
    point         BIGINT         NOT NULL COMMENT '充值到账的AI积分', -- 🔥 改为BIGINT 对应Java Long
    business_type TINYINT        NOT NULL COMMENT '1-充值',
    pay_type      TINYINT        NULL COMMENT '1-微信 2-支付宝',
    status        TINYINT        NOT NULL COMMENT '0-待支付 1-支付成功 2-支付失败 3-过期',
    create_time   DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time   DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    pay_time      DATETIME       NULL COMMENT '支付完成时间',
    remark        VARCHAR(255)   NULL COMMENT '充值备注',
    INDEX idx_user_id (user_id),
    INDEX idx_create_time (create_time),
    INDEX idx_status (status)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT 'AI聊天积分充值订单表';