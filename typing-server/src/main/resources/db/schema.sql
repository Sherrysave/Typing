-- =============================================================================
-- 打字练习平台后端建表脚本（MySQL 8.0）
-- 对标 Monkeytype 后端 dal/*.ts 的 15 个数据域，逐表对应。
-- 可重复执行：先 DROP 再 CREATE。
-- =============================================================================

CREATE DATABASE IF NOT EXISTS typing_db DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_general_ci;
USE typing_db;

-- -----------------------------------------------------------------------------
-- 1. t_user 用户表（对应 dal/user.ts）
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS `t_user`;
CREATE TABLE `t_user` (
  `uid`              VARCHAR(22)  NOT NULL COMMENT '用户ID（短随机串，对标 Monkeytype 的 uid）',
  `name`             VARCHAR(32)  NOT NULL COMMENT '用户名，唯一',
  `email`            VARCHAR(128)          DEFAULT NULL COMMENT '邮箱，唯一',
  `password_hash`    VARCHAR(100) NOT NULL COMMENT 'BCrypt 哈希',
  `bio`              VARCHAR(256)          DEFAULT NULL COMMENT '个人简介',
  `keyboard`         VARCHAR(64)           DEFAULT NULL COMMENT '常用键盘（用于键盘统计）',
  `profile_details`  JSON                  DEFAULT NULL COMMENT '自定义个人页',
  `inventory`        JSON                  DEFAULT NULL COMMENT '徽章等道具',
  `lb_opt_out`       TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '1=不参与排行榜',
  `banned`           TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '1=已封禁',
  `created_at`       BIGINT       NOT NULL COMMENT 'epoch 毫秒',
  `last_login_at`    BIGINT                DEFAULT NULL COMMENT '上次登录时间 epoch 毫秒',
  PRIMARY KEY (`uid`),
  UNIQUE KEY `uk_name`  (`name`),
  UNIQUE KEY `uk_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- -----------------------------------------------------------------------------
-- 2. t_result 成绩表（对应 dal/result.ts）—— 核心表
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS `t_result`;
CREATE TABLE `t_result` (
  `id`                 BIGINT       NOT NULL AUTO_INCREMENT,
  `uid`                VARCHAR(22)  NOT NULL,
  `wpm`                DECIMAL(7,2) NOT NULL COMMENT '前端计算的 WPM',
  `raw_wpm`            DECIMAL(7,2) NOT NULL COMMENT '含错误击键的原始 WPM',
  `acc`                DECIMAL(7,2) NOT NULL COMMENT '准确率 %',
  `consistency`        DECIMAL(7,2) NOT NULL COMMENT '一致性 %',
  `mode`               VARCHAR(16)  NOT NULL COMMENT 'time/words/quote/code/weak',
  `mode2`              VARCHAR(16)  NOT NULL COMMENT '15/30/60/120 或词数',
  `language`           VARCHAR(16)  NOT NULL DEFAULT 'english',
  `punctuation`        TINYINT(1)   NOT NULL DEFAULT 0,
  `numbers`            TINYINT(1)   NOT NULL DEFAULT 0,
  `lazy_mode`          TINYINT(1)   NOT NULL DEFAULT 0,
  `funbox`             VARCHAR(32)           DEFAULT NULL,
  `test_duration`      DECIMAL(6,2) NOT NULL COMMENT '实际用时（秒）',
  `restart_count`      INT          NOT NULL DEFAULT 0,
  `incomplete_tests`   INT          NOT NULL DEFAULT 0,
  `afk_duration`       INT          NOT NULL DEFAULT 0 COMMENT '挂机毫秒数，反作弊依据',
  `bailed_out`         TINYINT(1)   NOT NULL DEFAULT 0,
  `char_stats`         JSON         NOT NULL COMMENT '{correct,incorrect,extra,missed}',
  `key_spacing_stats`  JSON         NOT NULL COMMENT '相邻击键间隔分布',
  `key_duration_stats` JSON         NOT NULL COMMENT '按键按下时长分布',
  `chart_data`         JSON         NOT NULL COMMENT '每秒采样，用于复现结果图表',
  `flags`              JSON                  DEFAULT NULL COMMENT '反作弊命中项',
  `tags`               JSON                  DEFAULT NULL,
  `is_pb`              TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否个人最好成绩',
  `validity`           TINYINT      NOT NULL DEFAULT 1 COMMENT '1有效 0待审 -1拒绝',
  `client_timestamp`   BIGINT       NOT NULL COMMENT '客户端完成时间，用于排序与防重放',
  `server_timestamp`   BIGINT       NOT NULL COMMENT '服务端落库时间',
  PRIMARY KEY (`id`),
  KEY `idx_uid_time`  (`uid`, `server_timestamp`),
  KEY `idx_lb`        (`mode`, `mode2`, `language`, `wpm`),
  KEY `idx_dedup`     (`uid`, `client_timestamp`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='打字成绩表';

-- -----------------------------------------------------------------------------
-- 3. t_leaderboard_entry 排行榜冷备表（对应 dal/leaderboards.ts）
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS `t_leaderboard_entry`;
CREATE TABLE `t_leaderboard_entry` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT,
  `mode`        VARCHAR(16)  NOT NULL,
  `mode2`       VARCHAR(16)  NOT NULL,
  `language`    VARCHAR(16)  NOT NULL,
  `uid`         VARCHAR(22)  NOT NULL,
  `result_id`   BIGINT       NOT NULL COMMENT '指向 t_result.id',
  `wpm`         DECIMAL(7,2) NOT NULL,
  `raw_wpm`     DECIMAL(7,2) NOT NULL,
  `acc`         DECIMAL(7,2) NOT NULL,
  `consistency` DECIMAL(7,2) NOT NULL,
  `timestamp`   BIGINT       NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_scope_uid` (`mode`, `mode2`, `language`, `uid`),
  KEY `idx_scope_wpm` (`mode`, `mode2`, `language`, `wpm`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='每人每维度最好成绩（冷备）';

-- -----------------------------------------------------------------------------
-- 4. t_ape_key 个人 API Key 表（对应 dal/ape-keys.ts）
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS `t_ape_key`;
CREATE TABLE `t_ape_key` (
  `id`           VARCHAR(22) NOT NULL,
  `uid`          VARCHAR(22) NOT NULL,
  `name`         VARCHAR(64) NOT NULL COMMENT '用途备注',
  `hash`         CHAR(64)    NOT NULL COMMENT 'SHA-256 摘要，明文仅创建时返回一次',
  `enabled`      TINYINT(1)  NOT NULL DEFAULT 1,
  `created_at`   BIGINT      NOT NULL,
  `last_used_at` BIGINT               DEFAULT NULL,
  `last_used_ip` VARCHAR(45)          DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_uid` (`uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='个人 API Key（第二鉴权通道）';

-- -----------------------------------------------------------------------------
-- 5. t_config 全站配置表（对应 dal/config.ts）
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS `t_config`;
CREATE TABLE `t_config` (
  `key`        VARCHAR(64) NOT NULL COMMENT '配置项键',
  `value`      JSON        NOT NULL COMMENT '配置项值（任意 JSON）',
  `updated_at` BIGINT      NOT NULL COMMENT 'epoch 毫秒',
  PRIMARY KEY (`key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='全站配置';

-- -----------------------------------------------------------------------------
-- 6. t_preset 用户预设表（对应 dal/preset.ts）
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS `t_preset`;
CREATE TABLE `t_preset` (
  `id`         VARCHAR(22) NOT NULL,
  `uid`        VARCHAR(22) NOT NULL,
  `name`       VARCHAR(64) NOT NULL,
  `config`     JSON        NOT NULL COMMENT '预设配置（模式/语言等）',
  `created_at` BIGINT      NOT NULL,
  `updated_at` BIGINT      NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_uid` (`uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户配置预设';

-- -----------------------------------------------------------------------------
-- 7. t_quote 名言库（对应 dal/quote.ts）
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS `t_quote`;
CREATE TABLE `t_quote` (
  `id`          VARCHAR(22)  NOT NULL,
  `language`    VARCHAR(16)  NOT NULL,
  `text`        TEXT         NOT NULL,
  `source`      VARCHAR(128) NOT NULL COMMENT '出处',
  `length`      INT          NOT NULL COMMENT '字符数（冗余列，便于按长度筛选）',
  `approved_by` VARCHAR(22)           DEFAULT NULL,
  `timestamp`   BIGINT       NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_lang_len` (`language`, `length`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='名言库';

-- -----------------------------------------------------------------------------
-- 8. t_new_quote 待审名言（对应 dal/new-quotes.ts）
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS `t_new_quote`;
CREATE TABLE `t_new_quote` (
  `id`        VARCHAR(22)  NOT NULL,
  `uid`       VARCHAR(22)  NOT NULL COMMENT '提交者',
  `language`  VARCHAR(16)  NOT NULL,
  `text`      TEXT         NOT NULL,
  `source`    VARCHAR(128) NOT NULL,
  `status`    TINYINT      NOT NULL DEFAULT 0 COMMENT '0待审 1通过 2驳回',
  `timestamp` BIGINT       NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='待审名言';

-- -----------------------------------------------------------------------------
-- 9. t_quote_rating 名言评分（对应 dal/quote-ratings.ts）
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS `t_quote_rating`;
CREATE TABLE `t_quote_rating` (
  `quote_id`  VARCHAR(22) NOT NULL,
  `uid`       VARCHAR(22) NOT NULL,
  `rating`    TINYINT     NOT NULL COMMENT '1~5',
  `timestamp` BIGINT      NOT NULL,
  PRIMARY KEY (`quote_id`, `uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='名言评分';

-- -----------------------------------------------------------------------------
-- 10. t_report 举报表（对应 dal/report.ts）
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS `t_report`;
CREATE TABLE `t_report` (
  `id`          VARCHAR(22)  NOT NULL,
  `uid`         VARCHAR(22)  NOT NULL COMMENT '举报人',
  `target_uid`  VARCHAR(22)           DEFAULT NULL COMMENT '被举报用户',
  `result_id`   BIGINT                DEFAULT NULL COMMENT '被举报成绩',
  `reason`      VARCHAR(64)  NOT NULL COMMENT '举报原因',
  `description` VARCHAR(512)          DEFAULT NULL,
  `status`      TINYINT      NOT NULL DEFAULT 0 COMMENT '0待处理 1已处理 2驳回',
  `timestamp`   BIGINT       NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`),
  KEY `idx_target` (`target_uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='举报';

-- -----------------------------------------------------------------------------
-- 11. t_psa 站内公告（对应 dal/psa.ts）
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS `t_psa`;
CREATE TABLE `t_psa` (
  `id`        VARCHAR(22)  NOT NULL,
  `message`   TEXT         NOT NULL,
  `sticky`    TINYINT(1)   NOT NULL DEFAULT 0,
  `created_at` BIGINT      NOT NULL,
  `expires_at` BIGINT               DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='站内公告';

-- -----------------------------------------------------------------------------
-- 12. t_blocklist 封禁名单（对应 dal/blocklist.ts）
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS `t_blocklist`;
CREATE TABLE `t_blocklist` (
  `uid`        VARCHAR(22)  NOT NULL,
  `reason`     VARCHAR(256)          DEFAULT NULL,
  `banned_by`  VARCHAR(22)           DEFAULT NULL COMMENT '操作管理员 uid',
  `timestamp`  BIGINT       NOT NULL,
  PRIMARY KEY (`uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='封禁名单';

-- -----------------------------------------------------------------------------
-- 13. t_connection 关注关系（对应 dal/connections.ts）
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS `t_connection`;
CREATE TABLE `t_connection` (
  `uid`            VARCHAR(22) NOT NULL COMMENT '关注者',
  `target_uid`     VARCHAR(22) NOT NULL COMMENT '被关注者',
  `followed_at`    BIGINT      NOT NULL,
  `last_interaction` BIGINT             DEFAULT NULL,
  PRIMARY KEY (`uid`, `target_uid`),
  KEY `idx_target` (`target_uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户关注关系';

-- -----------------------------------------------------------------------------
-- 14. t_admin_uid 管理员名单（对应 dal/admin-uids.ts）
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS `t_admin_uid`;
CREATE TABLE `t_admin_uid` (
  `uid`       VARCHAR(22)  NOT NULL,
  `added_by`  VARCHAR(22)           DEFAULT NULL COMMENT '授权人 uid',
  `timestamp` BIGINT       NOT NULL,
  PRIMARY KEY (`uid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员白名单';

-- -----------------------------------------------------------------------------
-- 15. t_log 操作日志（对应 dal/logs.ts）
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS `t_log`;
CREATE TABLE `t_log` (
  `id`         BIGINT       NOT NULL AUTO_INCREMENT,
  `uid`        VARCHAR(22)           DEFAULT NULL COMMENT '操作人',
  `action`     VARCHAR(64)  NOT NULL COMMENT '动作（如 BAN_USER / APPROVE_QUOTE）',
  `target`     VARCHAR(64)           DEFAULT NULL COMMENT '操作对象标识',
  `detail`     JSON                  DEFAULT NULL COMMENT '附加信息',
  `ip`         VARCHAR(45)           DEFAULT NULL,
  `timestamp`  BIGINT       NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_uid_time` (`uid`, `timestamp`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志';
