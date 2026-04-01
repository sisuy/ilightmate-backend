-- ================================================================
-- iLightMate 知见光伙伴 — 业务表
-- 基于 RuoYi 框架，所有表前缀 ilm_
-- 执行前请先导入 RuoYi 自带的 ry_20xx.sql 和 quartz.sql
-- ================================================================

-- ════════════════════════════════════════════════
-- 1. Token 计量（服务端权威）
-- ════════════════════════════════════════════════

CREATE TABLE ilm_token_usage (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    user_id     BIGINT       NOT NULL COMMENT 'sys_user.user_id',
    month_key   CHAR(7)      NOT NULL COMMENT 'YYYY-MM',
    used        INT          NOT NULL DEFAULT 0 COMMENT '本月已用 tokens',
    bonus       INT          NOT NULL DEFAULT 0 COMMENT '加购额外 tokens（本月有效）',
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_month (user_id, month_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='月度 Token 使用量（服务端权威）';

CREATE TABLE ilm_dialogue_usage (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    user_id     BIGINT       NOT NULL,
    date_key    DATE         NOT NULL COMMENT 'YYYY-MM-DD',
    count       INT          NOT NULL DEFAULT 0,
    create_time DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_date (user_id, date_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='每日对话计数';

-- ════════════════════════════════════════════════
-- 2. 套餐计划
-- ════════════════════════════════════════════════

CREATE TABLE ilm_combo_plans (
    combo_id            BIGINT        NOT NULL AUTO_INCREMENT,
    combo_name          VARCHAR(50)   NOT NULL COMMENT '体验版/成长版/专业版',
    combo_code          CHAR(1)       NOT NULL COMMENT '0=FREE, 1=GROWTH, 2=PROFESSIONAL',
    combo_type          VARCHAR(20)   NOT NULL COMMENT 'FREE/GROWTH/PROFESSIONAL',
    price               DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    billing_cycle       VARCHAR(20)   DEFAULT 'MONTHLY' COMMENT 'MONTHLY/YEARLY',
    description         TEXT,
    benefits_json       JSON          COMMENT 'ComboBenefitInfo 完整权益 JSON',
    monthly_token_limit INT           NOT NULL DEFAULT 50000,
    daily_dialogue_limit INT          NOT NULL DEFAULT 5 COMMENT '-1=无限',
    trial_days          INT           NOT NULL DEFAULT 0 COMMENT '试用天数',
    region              VARCHAR(20)   DEFAULT 'CN',
    status              CHAR(1)       DEFAULT '1' COMMENT '1=active, 0=inactive',
    order_num           INT           DEFAULT 0,
    remark              VARCHAR(500)  DEFAULT NULL,
    create_time         DATETIME      DEFAULT CURRENT_TIMESTAMP,
    update_time         DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (combo_id),
    UNIQUE KEY uk_code_cycle (combo_code, billing_cycle)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='套餐计划目录';

-- 种子数据：3 层定价
INSERT INTO ilm_combo_plans (combo_name, combo_code, combo_type, price, billing_cycle, monthly_token_limit, daily_dialogue_limit, trial_days, description) VALUES
('体验版', '0', 'FREE',         0.00,    'MONTHLY', 50000,    5,  0,  '50K tokens/月, 2 伙伴, 1 树, 前 3 层分析, 1 种剧场'),
('成长版', '1', 'GROWTH',       19.00,   'MONTHLY', 500000,  -1,  7,  '500K tokens/月, 3→6 伙伴, 3 树, 完整 7 层, 6 剧场, 传承 3 人'),
('成长版', '1', 'GROWTH',      198.00,   'YEARLY',  500000,  -1,  7,  '500K tokens/月, 年付优惠'),
('专业版', '2', 'PROFESSIONAL', 1680.00, 'YEARLY',  3000000, -1, 10,  '3M tokens/月, 6 伙伴, 10 树, 个案管理, PDF 导出, 仅年付');

-- ════════════════════════════════════════════════
-- 3. 用户订阅
-- ════════════════════════════════════════════════

CREATE TABLE ilm_user_subscriptions (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    user_id       BIGINT       NOT NULL,
    combo_id      BIGINT       NOT NULL,
    combo_code    CHAR(1)      NOT NULL,
    combo_name    VARCHAR(50)  NOT NULL,
    combo_type    VARCHAR(20)  NOT NULL,
    daily_consult_limit INT    NOT NULL DEFAULT 5,
    start_time    DATETIME     NOT NULL,
    end_time      DATETIME     NOT NULL,
    status        CHAR(1)      DEFAULT '1' COMMENT '1=active, 0=expired, 2=cancelled',
    create_time   DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_user (user_id),
    KEY idx_end_time (end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户订阅';

-- ════════════════════════════════════════════════
-- 4. 订单
-- ════════════════════════════════════════════════

CREATE TABLE ilm_orders (
    order_id       BIGINT        NOT NULL AUTO_INCREMENT,
    order_no       VARCHAR(32)   NOT NULL COMMENT '订单编号',
    user_id        BIGINT        NOT NULL,
    combo_id       BIGINT        NOT NULL,
    pay_amount     DECIMAL(10,2) NOT NULL,
    order_status   VARCHAR(20)   DEFAULT 'CREATED' COMMENT 'CREATED/PAID/CANCELLED/EXPIRED',
    pay_status     VARCHAR(20)   DEFAULT 'UNPAID' COMMENT 'UNPAID/PAID/REFUNDED',
    pay_type       VARCHAR(20)   DEFAULT NULL COMMENT 'ALIPAY/WECHAT',
    pay_time       DATETIME      DEFAULT NULL,
    start_time     DATETIME      DEFAULT NULL COMMENT '订阅开始时间',
    end_time       DATETIME      DEFAULT NULL COMMENT '订阅结束时间',
    transaction_no VARCHAR(64)   DEFAULT NULL COMMENT '第三方交易号',
    referral_code  VARCHAR(20)   DEFAULT NULL,
    region         VARCHAR(20)   DEFAULT 'CN',
    remark         VARCHAR(500)  DEFAULT NULL,
    create_time    DATETIME      DEFAULT CURRENT_TIMESTAMP,
    update_time    DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (order_id),
    UNIQUE KEY uk_order_no (order_no),
    KEY idx_user (user_id),
    KEY idx_pay_status (pay_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单';

-- ════════════════════════════════════════════════
-- 5. 推荐合作伙伴
-- ════════════════════════════════════════════════

CREATE TABLE ilm_partners (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    partner_type   VARCHAR(20)  NOT NULL COMMENT 'channel/sales_rep/mentor',
    name           VARCHAR(100) NOT NULL,
    phone          VARCHAR(20)  DEFAULT NULL,
    referral_code  VARCHAR(20)  NOT NULL COMMENT 'CH-XXXX / SL-XXXX / MT-XXXX',
    parent_id      BIGINT       DEFAULT NULL COMMENT '上线伙伴 ID（渠道 Tier2）',
    sales_rep_id   BIGINT       DEFAULT NULL COMMENT '对接业务员 ID',
    status         CHAR(1)      DEFAULT '1',
    create_time    DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time    DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_referral_code (referral_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='推荐合作伙伴';

-- ════════════════════════════════════════════════
-- 6. 用户归因
-- ════════════════════════════════════════════════

CREATE TABLE ilm_user_attribution (
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    user_id          BIGINT       NOT NULL,
    entry_type       VARCHAR(20)  NOT NULL COMMENT 'channel/direct_sales/natural/mentor_referral',
    referral_code    VARCHAR(20)  DEFAULT NULL,
    referrer_name    VARCHAR(100) DEFAULT NULL,
    tier1_partner_id BIGINT       DEFAULT NULL,
    tier2_partner_id BIGINT       DEFAULT NULL,
    sales_rep_id     BIGINT       DEFAULT NULL,
    locked           TINYINT(1)   DEFAULT 0 COMMENT '1=首付后锁定',
    locked_at        DATETIME     DEFAULT NULL,
    create_time      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户归因追踪';

-- ════════════════════════════════════════════════
-- 7. 行为事件（匿名化）
-- ════════════════════════════════════════════════

CREATE TABLE ilm_behavioral_events (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    session_id    VARCHAR(64)  DEFAULT NULL,
    user_id       BIGINT       DEFAULT NULL COMMENT '可为 null（匿名）',
    event_type    VARCHAR(50)  NOT NULL,
    companion_id  VARCHAR(20)  DEFAULT NULL,
    payload       JSON         DEFAULT NULL,
    event_time    DATETIME     NOT NULL,
    create_time   DATETIME     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_user_time (user_id, event_time),
    KEY idx_event_type (event_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='行为事件（匿名）';

-- ════════════════════════════════════════════════
-- 8. 扩展 sys_user（添加 iLightMate 业务字段）
-- ════════════════════════════════════════════════

ALTER TABLE sys_user ADD COLUMN invite_code VARCHAR(20) DEFAULT NULL COMMENT '用户自己的邀请码' AFTER phonenumber;
ALTER TABLE sys_user ADD COLUMN sys_language VARCHAR(10) DEFAULT 'zh-CN' COMMENT '语言偏好' AFTER invite_code;

-- ════════════════════════════════════════════════
-- W2+ 用户状态表（建表但 W1 不需要 API）
-- ════════════════════════════════════════════════

CREATE TABLE ilm_explore_scores (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    user_id         BIGINT       NOT NULL,
    dim_andun       INT          NOT NULL DEFAULT 50 COMMENT '安顿 0-100',
    dim_lijie       INT          NOT NULL DEFAULT 50 COMMENT '理解',
    dim_qingxu      INT          NOT NULL DEFAULT 50 COMMENT '情绪流动',
    dim_lianjie     INT          NOT NULL DEFAULT 50 COMMENT '连结',
    dim_xingdong    INT          NOT NULL DEFAULT 50 COMMENT '行动',
    dim_jiena       INT          NOT NULL DEFAULT 50 COMMENT '接纳',
    dim_juecha      INT          NOT NULL DEFAULT 50 COMMENT '觉察',
    create_time     DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='七维探索分数';

CREATE TABLE ilm_diary_entries (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    user_id         BIGINT       NOT NULL,
    content         TEXT         NOT NULL,
    mood            TINYINT      NOT NULL COMMENT '1-5',
    emotions        JSON         COMMENT '["焦虑","希望"]',
    family_mentions JSON         COMMENT '["母亲","父亲"]',
    ai_insight      TEXT         DEFAULT NULL,
    create_time     DATETIME     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='日记';

CREATE TABLE ilm_theater_sessions (
    id                BIGINT       NOT NULL AUTO_INCREMENT,
    user_id           BIGINT       NOT NULL,
    tree_id           VARCHAR(20)  DEFAULT NULL,
    reality_positions JSON         DEFAULT NULL,
    ideal_positions   JSON         DEFAULT NULL,
    patterns          JSON         DEFAULT NULL COMMENT '["parentification","cutoff"]',
    core_insight      TEXT         DEFAULT NULL,
    action_commitment TEXT         DEFAULT NULL,
    radar_before      JSON         DEFAULT NULL,
    radar_after       JSON         DEFAULT NULL,
    create_time       DATETIME     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='剧场记录';

CREATE TABLE ilm_legacy_members (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    user_id         BIGINT       NOT NULL,
    name            VARCHAR(100) NOT NULL,
    role            VARCHAR(50)  NOT NULL COMMENT '父亲/母亲/伴侣/朋友/...',
    tone_style      VARCHAR(50)  DEFAULT NULL COMMENT '温柔/理性/幽默/安静',
    atmosphere      VARCHAR(50)  DEFAULT NULL COMMENT '亲近/平等/尊重',
    catchphrases    JSON         DEFAULT NULL COMMENT '["口头禅1","口头禅2"]',
    create_time     DATETIME     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='传承成员';

CREATE TABLE ilm_emotion_tags (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    user_id         BIGINT       NOT NULL,
    tag             VARCHAR(50)  NOT NULL,
    label           VARCHAR(50)  DEFAULT NULL,
    category        VARCHAR(20)  NOT NULL COMMENT 'positive/negative/neutral/family',
    source          VARCHAR(20)  DEFAULT NULL COMMENT 'ask/companion/diary/explore',
    create_time     DATETIME     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_user (user_id, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='情绪标签';

CREATE TABLE ilm_activities (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    user_id         BIGINT       NOT NULL,
    type            VARCHAR(20)  NOT NULL COMMENT 'ask/explore/mindfield/journey/diary/legacy',
    action          VARCHAR(100) NOT NULL,
    detail          TEXT         DEFAULT NULL,
    create_time     DATETIME     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_user (user_id, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动记录';

CREATE TABLE ilm_dialogue_history (
    id                    BIGINT       NOT NULL AUTO_INCREMENT,
    user_id               BIGINT       NOT NULL,
    companion_id          VARCHAR(20)  NOT NULL,
    conversation_summary  TEXT         DEFAULT NULL,
    key_discoveries       JSON         DEFAULT NULL,
    emotional_trajectory  TEXT         DEFAULT NULL,
    commitments           JSON         DEFAULT NULL,
    message_count         INT          DEFAULT 0,
    duration              INT          DEFAULT 0 COMMENT 'milliseconds',
    create_time           DATETIME     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_user (user_id, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='对话摘要历史';

CREATE TABLE ilm_coach_sessions (
    id                     BIGINT       NOT NULL AUTO_INCREMENT,
    user_id                BIGINT       NOT NULL,
    theme                  VARCHAR(20)  NOT NULL COMMENT 'family/relationship/self/career',
    sub_topic              VARCHAR(100) NOT NULL,
    current_step           INT          DEFAULT 1,
    sub_step               VARCHAR(10)  DEFAULT NULL,
    analysis_data          JSON         DEFAULT NULL,
    insight_data           JSON         DEFAULT NULL,
    selected_companion     VARCHAR(20)  DEFAULT NULL,
    companion_dialogue_data JSON        DEFAULT NULL,
    action_commitment      TEXT         DEFAULT NULL,
    growth_cycle           JSON         DEFAULT NULL,
    radar_before           JSON         DEFAULT NULL,
    completed              TINYINT(1)   DEFAULT 0,
    started_at             DATETIME     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='明场教练会话';

CREATE TABLE ilm_user_consent (
    user_id              BIGINT       NOT NULL,
    basic_service        TINYINT(1)   DEFAULT 0,
    aggregate_insights   TINYINT(1)   DEFAULT 0,
    research_use         TINYINT(1)   DEFAULT 0,
    consent_version      VARCHAR(10)  DEFAULT '1.0',
    consent_timestamp    DATETIME     DEFAULT NULL,
    has_completed        TINYINT(1)   DEFAULT 0,
    create_time          DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time          DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='PIPL 同意状态';

CREATE TABLE ilm_user_onboarding (
    user_id              BIGINT       NOT NULL,
    has_completed        TINYINT(1)   DEFAULT 0,
    role                 VARCHAR(20)  DEFAULT NULL COMMENT 'adult/parent/teen/caregiver',
    gender               VARCHAR(20)  DEFAULT NULL COMMENT 'male/female/undisclosed',
    age_range            VARCHAR(20)  DEFAULT NULL,
    city                 VARCHAR(100) DEFAULT '',
    goals                JSON         DEFAULT NULL COMMENT '["emotion","relationship"]',
    recommended_companion VARCHAR(20) DEFAULT NULL,
    completed_at         DATETIME     DEFAULT NULL,
    create_time          DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time          DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='引导状态';
