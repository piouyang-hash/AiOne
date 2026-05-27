-- 1. DeepSeek 输入Token
INSERT INTO ai_token_type (vendor, token_side, type_code, type_name, price_per_million, status)
VALUES ('DEEPSEEK', 1, 'DEEPSEEK_INPUT', 'DeepSeek输入Token', 4.00, 1);

-- 2. DeepSeek 输出Token
INSERT INTO ai_token_type (vendor, token_side, type_code, type_name, price_per_million, status)
VALUES ('DEEPSEEK', 2, 'DEEPSEEK_OUTPUT', 'DeepSeek输出Token', 16.00, 1);

-- 3. Kimi 输入Token
INSERT INTO ai_token_type (vendor, token_side, type_code, type_name, price_per_million, status)
VALUES ('KIMI', 1, 'KIMI_INPUT', 'Kimi输入Token', 8.00, 1);

-- 4. Kimi 输出Token
INSERT INTO ai_token_type (vendor, token_side, type_code, type_name, price_per_million, status)
VALUES ('KIMI', 2, 'KIMI_OUTPUT', 'Kimi输出Token', 32.00, 1);


-- 1. 给AI发消息
INSERT INTO base_user_behavior (behavior_code, behavior_name)
VALUES ('CHAT_SEND_MSG', '发送AI聊天消息');

-- 2. 用户离线
INSERT INTO base_user_behavior (behavior_code, behavior_name)
VALUES ('USER_OFFLINE', '用户离线');

-- ====================== CHAT_SEND_MSG 分值配置 ======================
-- 活跃度 +10
INSERT INTO user_behavior_score (behavior_code, score_type, score_val)
VALUES ('CHAT_SEND_MSG', 'ACTIVITY', 10);

-- 喜爱值 0
INSERT INTO user_behavior_score (behavior_code, score_type, score_val)
VALUES ('CHAT_SEND_MSG', 'LIKE', 0);

-- 熟悉度 0
INSERT INTO user_behavior_score (behavior_code, score_type, score_val)
VALUES ('CHAT_SEND_MSG', 'FAMILIARITY', 0);

-- ====================== USER_OFFLINE 分值配置 ======================
-- 活跃度 NULL
INSERT INTO user_behavior_score (behavior_code, score_type, score_val)
VALUES ('USER_OFFLINE', 'ACTIVITY', NULL);

-- 喜爱值 0
INSERT INTO user_behavior_score (behavior_code, score_type, score_val)
VALUES ('USER_OFFLINE', 'LIKE', 0);

-- 熟悉度 0
INSERT INTO user_behavior_score (behavior_code, score_type, score_val)
VALUES ('USER_OFFLINE', 'FAMILIARITY', 0);


-- 1. AI收到用户消息
INSERT INTO base_ai_behavior (behavior_code, behavior_name)
VALUES ('USER_SEND_MSG', 'AI收到用户消息');

-- 2. AI主动发送消息
INSERT INTO base_ai_behavior (behavior_code, behavior_name)
VALUES ('AI_ACTIVE_SEND_MSG', 'AI主动发送消息');

-- 3. AI等待
INSERT INTO base_ai_behavior (behavior_code, behavior_name)
VALUES ('WAIT', 'AI等待');

-- ====================== USER_SEND_MSG 分值配置 ======================
INSERT INTO ai_behavior_score (behavior_code, score_type, score_val) VALUES ('USER_SEND_MSG', 'ACTIVITY', -20);
INSERT INTO ai_behavior_score (behavior_code, score_type, score_val) VALUES ('USER_SEND_MSG', 'LIKE', 1);
INSERT INTO ai_behavior_score (behavior_code, score_type, score_val) VALUES ('USER_SEND_MSG', 'FAMILIARITY', 0);

-- ====================== AI_ACTIVE_SEND_MSG 分值配置 ======================
INSERT INTO ai_behavior_score (behavior_code, score_type, score_val) VALUES ('AI_ACTIVE_SEND_MSG', 'ACTIVITY', -100);
INSERT INTO ai_behavior_score (behavior_code, score_type, score_val) VALUES ('AI_ACTIVE_SEND_MSG', 'LIKE', -1);
INSERT INTO ai_behavior_score (behavior_code, score_type, score_val) VALUES ('AI_ACTIVE_SEND_MSG', 'FAMILIARITY', 0);

-- ====================== WAIT 分值配置 ======================
INSERT INTO ai_behavior_score (behavior_code, score_type, score_val) VALUES ('WAIT', 'ACTIVITY', NULL);
INSERT INTO ai_behavior_score (behavior_code, score_type, score_val) VALUES ('WAIT', 'LIKE', 0);
INSERT INTO ai_behavior_score (behavior_code, score_type, score_val) VALUES ('WAIT', 'FAMILIARITY', 0);


-- 1. 插入 DeepSeek AI 角色
INSERT INTO ai_role (
    role_code,
    role_desc,
    persona_core,
    persona_tone,
    avatar_path,
    create_user_id,
    role_status,
    visible_scope
) VALUES (
             'DEEPSEEK_AI',
             'DeepSeek专业AI助手',
             '专业、严谨、高效的AI助手，擅长解答技术问题、逻辑推理、代码编写，提供精准可靠的回答',
             'normal',
             '/ai_role_deepseek.png',
             1,
             1,
             1
         );

-- 2. 插入 萌牙 AI 角色
INSERT INTO ai_role (
    role_code,
    role_desc,
    persona_core,
    persona_tone,
    avatar_path,
    create_user_id,
    role_status,
    visible_scope
) VALUES (
             'MENGYA_AI',
             '萌牙可爱陪伴AI',
             '温柔可爱、活泼开朗的陪伴型AI，擅长聊天互动、情绪疏导、日常陪伴，语气亲切软萌',
             'normal',
             '/ai_role_mengya.png',
             1,
             1,
             1
         );

INSERT INTO ai_role (role_code, role_desc, persona_core, persona_tone, worldview_setting, avatar_path, create_user_id, role_status, visible_scope)
VALUES (
           'AQNU_MALE_TECH_STUDENT',
           '安庆师范大学在校理工男大学生专属对话角色',
           '你是安庆师范大学在读男大学生，裴阳，主修理工科相关专业，日常热衷于后端开发、Java编程、AI向量检索等技术项目钻研；性格沉稳务实，思维偏理性逻辑，待人真诚随和，和同龄人相处轻松自然；课余除了敲代码做开发练习，也会逛校园、了解安庆本地风土，兼顾学业学习与个人兴趣，聊天既能聊专业技术知识，也能畅谈校园日常、生活琐事',
           'youthful',
           '坐落于安庆本地的公办综合性师范院校，整体办学底蕴深厚，理工类与师范类专业并行发展。校园划分多个生活区与教学区，校内建有十余栋学生宿舍楼，满足全校学生住宿需求；一共设有三座学生食堂，其中二食堂菜品品类丰富、性价比高，日常就餐人流量稳居首位。校园日常课业安排规律，课余常会举办学科竞赛、编程比拼、户外团建、文艺汇演、校园运动赛事等活动。校内绿化覆盖率高，道路楼宇规整，周边毗邻城市生活区，学生课余可就近出行逛街散步，整体校园生活氛围安稳充实。',
           '',
           1,
           1,
           0
       );

INSERT INTO ai_recharge_goods (id, amount, point, name, description, status, remark)
VALUES
    (1000000000000000001, 5,   5,   '5元充值档位',  '基础小额充值，适合体验使用', 1, '基础档位'),
    (1000000000000000002, 10,  10,  '10元充值档位', '性价比入门充值档位', 1, '基础档位'),
    (1000000000000000003, 20,  20,  '20元充值档位', '日常使用推荐档位', 1, '常用档位'),
    (1000000000000000004, 50,  50,  '50元充值档位', '高频使用推荐档位', 1, '中额档位'),
    (1000000000000000005, 100, 100, '100元充值档位','大额实惠充值档位', 1, '大额档位'),
    (1000000000000000006, 200, 200, '200元充值档位','尊享超大额充值档位', 1, '超大额档位');

-- 插入地点表数据
INSERT IGNORE INTO `simulate_location` (`location_code`, `location_desc`)
VALUES
    ('HOME', '家'),
    ('SCHOOL', '学校'),
    ('LIBRARY', '图书馆'),
    ('RESTAURANT', '餐厅'),
    ('SUPERMARKET', '超市'),
    ('PARK', '公园');

-- 插入事件表数据
INSERT IGNORE INTO `simulate_event` (`event_code`, `event_desc`)
VALUES
    ('SLEEP', '睡觉'),
    ('EAT', '吃饭'),
    ('WRITE_CODE', '写代码'),
    ('WALK', '散步'),
    ('PLAY_PIANO', '弹钢琴'),
    ('SHOPPING', '购物'),
    ('ATTEND_CLASS', '上课'),
    ('STUDY', '自习');

-- 模拟游戏-地点事件关联表 插入数据（时长：小时*3600 秒）
INSERT IGNORE INTO `simulate_location_event_relation`
(`location_code`, `event_code`, `event_duration`)
VALUES
    -- 家（HOME）：睡觉8h、写代码4h、弹钢琴4h、吃饭1h
    ('HOME', 'SLEEP', 8 * 3600),
    ('HOME', 'WRITE_CODE', 4 * 3600),
    ('HOME', 'PLAY_PIANO', 4 * 3600),
    ('HOME', 'EAT', 1 * 3600),

    -- 学校（SCHOOL）：上课4h、吃饭1h
    ('SCHOOL', 'ATTEND_CLASS', 4 * 3600),
    ('SCHOOL', 'EAT', 1 * 3600),

    -- 图书馆（LIBRARY）：自习2h
    ('LIBRARY', 'STUDY', 2 * 3600),

    -- 餐厅（RESTAURANT）：吃饭1h
    ('RESTAURANT', 'EAT', 1 * 3600),

    -- 超市（SUPERMARKET）：购物1h
    ('SUPERMARKET', 'SHOPPING', 1 * 3600),

    -- 公园（PARK）：散步1h
    ('PARK', 'WALK', 1 * 3600);

-- 模拟游戏-每日事件安排 版本1（周一）插入数据
-- 执行顺序：睡觉 → 上课 → 学校吃饭 → 上课 → 家吃饭 → 自习 → 写代码
INSERT IGNORE INTO `simulate_event_sequence`
(`roleId`, `version`, `location_code`, `event_code`, `seq_num`)
VALUES
    -- 1. 睡觉（家）
    (6, 1, 'HOME', 'SLEEP', 1),
    -- 2. 上课（学校）
    (6, 1, 'SCHOOL', 'ATTEND_CLASS', 2),
    -- 3. 学校吃饭（餐厅）
    (6, 1, 'SCHOOL', 'EAT', 3),
    -- 4. 上课（学校）
    (6, 1, 'SCHOOL', 'ATTEND_CLASS', 4),
    -- 5. 家吃饭（家）
    (6, 1, 'HOME', 'EAT', 5),
    -- 6. 自习（图书馆）
    (6, 1, 'LIBRARY', 'STUDY', 6),
    -- 7. 写代码（家）
    (6, 1, 'HOME', 'WRITE_CODE', 7);