-- =====================================================
-- Get Jobs MySQL 数据库初始化脚本
-- 版本: 1.0.0
-- 说明: 从 SQLite 迁移到 MySQL 的完整建表脚本
-- 包含: 现有业务表 + 商业化新增表
-- =====================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `get_jobs` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `get_jobs`;

-- =====================================================
-- 第一部分：现有业务表（从 SQLite 迁移）
-- =====================================================

-- 1. AI 配置表
DROP TABLE IF EXISTS `ai`;
CREATE TABLE `ai` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `introduce` TEXT COMMENT '技能介绍',
    `prompt` TEXT COMMENT 'AI提示词',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI配置表';

-- 2. Boss 黑名单表
DROP TABLE IF EXISTS `boss_blacklist`;
CREATE TABLE `boss_blacklist` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `type` VARCHAR(20) NOT NULL COMMENT '类型: company(公司), recruiter(招聘者), job(职位)',
    `value` VARCHAR(200) NOT NULL COMMENT '黑名单值',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_type` (`type`),
    INDEX `idx_value` (`value`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Boss黑名单表';

-- 3. 通用配置表
DROP TABLE IF EXISTS `config`;
CREATE TABLE `config` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `config_key` VARCHAR(100) NOT NULL COMMENT '配置键',
    `config_value` TEXT COMMENT '配置值',
    `config_type` VARCHAR(50) NOT NULL DEFAULT 'string' COMMENT '配置类型',
    `category` VARCHAR(50) NOT NULL DEFAULT 'general' COMMENT '分类',
    `description` TEXT COMMENT '描述',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通用配置表';

-- 4. Cookie 管理表
DROP TABLE IF EXISTS `cookie`;
CREATE TABLE `cookie` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `platform` VARCHAR(50) NOT NULL COMMENT '平台名称 (boss/zhilian/job51/liepin)',
    `cookie_value` TEXT NOT NULL COMMENT 'Cookie值',
    `remark` TEXT COMMENT '备注',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_platform` (`platform`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Cookie管理表';

-- 5. Boss 配置表
DROP TABLE IF EXISTS `boss_config`;
CREATE TABLE `boss_config` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `debugger` TINYINT DEFAULT 0 COMMENT '调试模式 (1=开启, 0=关闭)',
    `wait_time` INT DEFAULT 10 COMMENT '页面操作等待时间(秒)',
    `keywords` VARCHAR(500) COMMENT '搜索关键词',
    `city_code` VARCHAR(200) COMMENT '城市代码',
    `industry` VARCHAR(200) COMMENT '行业',
    `job_type` VARCHAR(50) COMMENT '职位类型',
    `experience` VARCHAR(50) COMMENT '工作经验',
    `degree` VARCHAR(200) COMMENT '学历要求',
    `salary` VARCHAR(50) COMMENT '薪资区间',
    `scale` VARCHAR(200) COMMENT '公司规模',
    `stage` VARCHAR(200) COMMENT '融资阶段',
    `say_hi` TEXT COMMENT '默认打招呼语',
    `expected_salary_min` INT COMMENT '期望薪资下限',
    `expected_salary_max` INT COMMENT '期望薪资上限',
    `enable_ai` TINYINT DEFAULT 1 COMMENT '是否启用AI生成打招呼 (1=启用, 0=关闭)',
    `send_img_resume` TINYINT DEFAULT 0 COMMENT '是否发送图片简历 (1=启用, 0=关闭)',
    `filter_dead_hr` TINYINT DEFAULT 1 COMMENT '是否过滤不在线HR (1=启用, 0=关闭)',
    `dead_status` VARCHAR(200) COMMENT 'HR不在线状态列表',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Boss直聘配置表';

-- 6. Boss 职位数据表
DROP TABLE IF EXISTS `boss_data`;
CREATE TABLE `boss_data` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `encrypt_id` VARCHAR(100) COMMENT '加密职位ID',
    `encrypt_user_id` VARCHAR(100) COMMENT '加密用户ID',
    `company_name` TEXT COMMENT '公司名称',
    `job_name` TEXT COMMENT '职位名称',
    `salary` VARCHAR(100) COMMENT '薪资',
    `location` VARCHAR(100) COMMENT '地点',
    `experience` VARCHAR(100) COMMENT '经验要求',
    `degree` VARCHAR(100) COMMENT '学历要求',
    `hr_name` VARCHAR(100) COMMENT 'HR名称',
    `hr_position` VARCHAR(100) COMMENT 'HR职位',
    `hr_active_status` VARCHAR(50) COMMENT 'HR在线状态',
    `delivery_status` VARCHAR(20) DEFAULT '未投递' COMMENT '投递状态: 未投递/已投递/已过滤/投递失败',
    `job_description` TEXT COMMENT '职位描述',
    `job_url` VARCHAR(500) COMMENT '职位URL',
    `recruitment_status` VARCHAR(50) COMMENT '招聘状态',
    `company_address` VARCHAR(300) COMMENT '公司地址',
    `industry` VARCHAR(200) COMMENT '行业',
    `introduce` TEXT COMMENT '公司介绍',
    `financing_stage` VARCHAR(100) COMMENT '融资阶段',
    `company_scale` VARCHAR(100) COMMENT '公司规模',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_encrypt_id` (`encrypt_id`),
    INDEX `idx_delivery_status` (`delivery_status`),
    INDEX `idx_company_name` (`company_name`(100))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Boss职位数据表';

-- 7. Boss 选项表
DROP TABLE IF EXISTS `boss_option`;
CREATE TABLE `boss_option` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `type` VARCHAR(50) NOT NULL COMMENT '选项类型: city, industry, experience, jobType, salary, degree, scale, stage',
    `name` VARCHAR(100) NOT NULL COMMENT '选项名称',
    `code` VARCHAR(100) NOT NULL COMMENT '选项代码',
    `sort_order` INT DEFAULT 0 COMMENT '显示排序',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Boss选项表';

-- 8. Boss 行业表
DROP TABLE IF EXISTS `boss_industry`;
CREATE TABLE `boss_industry` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name` VARCHAR(200) COMMENT '行业名称',
    `code` INT COMMENT '行业代码',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Boss行业表';

-- 9. 智联配置表
DROP TABLE IF EXISTS `zhilian_config`;
CREATE TABLE `zhilian_config` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `keywords` VARCHAR(500) COMMENT '搜索关键词',
    `city_code` VARCHAR(200) COMMENT '城市代码',
    `salary` VARCHAR(50) COMMENT '薪资范围',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='智联招聘配置表';

-- 10. 智联职位数据表
DROP TABLE IF EXISTS `zhilian_data`;
CREATE TABLE `zhilian_data` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `job_id` VARCHAR(64) COMMENT '职位ID',
    `job_title` VARCHAR(200) COMMENT '职位名称',
    `job_link` VARCHAR(300) COMMENT '职位链接',
    `salary` VARCHAR(100) COMMENT '薪资',
    `location` VARCHAR(100) COMMENT '工作地点',
    `experience` VARCHAR(100) COMMENT '经验要求',
    `degree` VARCHAR(100) COMMENT '学历要求',
    `company_name` VARCHAR(200) COMMENT '公司名称',
    `delivery_status` VARCHAR(20) DEFAULT '未投递' COMMENT '投递状态',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_job_id` (`job_id`),
    INDEX `idx_delivery_status` (`delivery_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='智联职位数据表';

-- 11. 智联选项表
DROP TABLE IF EXISTS `zhilian_option`;
CREATE TABLE `zhilian_option` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `type` VARCHAR(50) COMMENT '选项类型',
    `name` VARCHAR(100) COMMENT '选项名称',
    `code` VARCHAR(100) COMMENT '选项代码',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='智联选项表';

-- 12. 51Job 配置表
DROP TABLE IF EXISTS `job51_config`;
CREATE TABLE `job51_config` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `keywords` VARCHAR(500) COMMENT '搜索关键词',
    `job_area` VARCHAR(200) COMMENT '工作地区',
    `salary` VARCHAR(200) COMMENT '薪资范围',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='51Job配置表';

-- 13. 51Job 职位数据表
DROP TABLE IF EXISTS `job51_data`;
CREATE TABLE `job51_data` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `job_id` BIGINT COMMENT '职位ID',
    `job_title` VARCHAR(200) COMMENT '职位名称',
    `job_link` VARCHAR(300) COMMENT '职位链接',
    `job_salary_text` VARCHAR(100) COMMENT '薪资文本',
    `job_area` VARCHAR(100) COMMENT '工作地区',
    `job_edu_req` VARCHAR(50) COMMENT '学历要求',
    `job_exp_req` VARCHAR(50) COMMENT '经验要求',
    `job_publish_time` VARCHAR(50) COMMENT '发布时间',
    `comp_id` BIGINT COMMENT '公司ID',
    `comp_name` VARCHAR(200) COMMENT '公司名称',
    `comp_industry` VARCHAR(100) COMMENT '行业',
    `comp_scale` VARCHAR(50) COMMENT '公司规模',
    `hr_id` VARCHAR(64) COMMENT 'HR ID',
    `hr_name` VARCHAR(50) COMMENT 'HR名称',
    `hr_title` VARCHAR(100) COMMENT 'HR职位',
    `delivered` TINYINT DEFAULT 0 COMMENT '是否已投递 (0=未投递, 1=已投递)',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_job_id` (`job_id`),
    INDEX `idx_delivered` (`delivered`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='51Job职位数据表';

-- 14. 51Job 选项表
DROP TABLE IF EXISTS `job51_option`;
CREATE TABLE `job51_option` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `type` VARCHAR(50) COMMENT '选项类型',
    `name` VARCHAR(100) COMMENT '选项名称',
    `code` VARCHAR(100) COMMENT '选项代码',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='51Job选项表';

-- 15. 猎聘配置表
DROP TABLE IF EXISTS `liepin_config`;
CREATE TABLE `liepin_config` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `keywords` VARCHAR(500) COMMENT '搜索关键词',
    `city` VARCHAR(200) COMMENT '城市',
    `salary_code` VARCHAR(200) COMMENT '薪资代码',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='猎聘配置表';

-- 16. 猎聘职位数据表
DROP TABLE IF EXISTS `liepin_data`;
CREATE TABLE `liepin_data` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `job_id` BIGINT COMMENT '职位ID',
    `job_title` VARCHAR(200) COMMENT '职位名称',
    `job_link` VARCHAR(300) COMMENT '职位链接',
    `job_salary_text` VARCHAR(100) COMMENT '薪资文本',
    `job_area` VARCHAR(100) COMMENT '工作地区',
    `job_edu_req` VARCHAR(50) COMMENT '学历要求',
    `job_exp_req` VARCHAR(50) COMMENT '经验要求',
    `job_publish_time` VARCHAR(50) COMMENT '发布时间',
    `comp_id` BIGINT COMMENT '公司ID',
    `comp_name` VARCHAR(200) COMMENT '公司名称',
    `comp_industry` VARCHAR(100) COMMENT '行业',
    `comp_scale` VARCHAR(50) COMMENT '公司规模',
    `hr_id` VARCHAR(64) COMMENT 'HR ID',
    `hr_name` VARCHAR(50) COMMENT 'HR名称',
    `hr_title` VARCHAR(100) COMMENT 'HR职位',
    `hr_im_id` VARCHAR(64) COMMENT 'HR IM ID',
    `delivered` TINYINT DEFAULT 0 COMMENT '是否已投递 (0=未投递, 1=已投递)',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_job_id` (`job_id`),
    INDEX `idx_delivered` (`delivered`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='猎聘职位数据表';

-- 17. 猎聘选项表
DROP TABLE IF EXISTS `liepin_option`;
CREATE TABLE `liepin_option` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `type` VARCHAR(50) NOT NULL COMMENT '选项类型',
    `name` VARCHAR(100) NOT NULL COMMENT '选项名称',
    `code` VARCHAR(100) NOT NULL COMMENT '选项代码',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='猎聘选项表';

-- 18. 搜索预设表
DROP TABLE IF EXISTS `search_preset`;
CREATE TABLE `search_preset` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name` VARCHAR(200) COMMENT '预设名称',
    `keywords` TEXT COMMENT '搜索关键词 (逗号分隔)',
    `city` VARCHAR(100) COMMENT '城市名称 (跨平台统一)',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='搜索预设表';

-- 19. 通用选项表
DROP TABLE IF EXISTS `common_option`;
CREATE TABLE `common_option` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `type` VARCHAR(50) NOT NULL COMMENT '选项类型',
    `label` VARCHAR(200) NOT NULL COMMENT '显示标签',
    `value` TEXT NOT NULL COMMENT '选项值',
    `sort_order` INT DEFAULT 0 COMMENT '排序顺序',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通用选项表';

-- =====================================================
-- 第二部分：商业化新增表
-- =====================================================

-- 20. 用户表
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `phone` VARCHAR(20) NOT NULL COMMENT '手机号',
    `password_hash` VARCHAR(255) COMMENT '密码哈希',
    `nickname` VARCHAR(100) COMMENT '昵称',
    `status` TINYINT DEFAULT 1 COMMENT '状态 (1=正常, 0=禁用)',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 21. 用户设备表
DROP TABLE IF EXISTS `user_device`;
CREATE TABLE `user_device` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `device_fingerprint` VARCHAR(128) NOT NULL COMMENT '设备指纹',
    `device_name` VARCHAR(100) COMMENT '设备名称',
    `last_login_at` DATETIME COMMENT '最后登录时间',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_fingerprint` (`device_fingerprint`),
    UNIQUE INDEX `uk_user_device` (`user_id`, `device_fingerprint`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户设备表';

-- 22. 用户额度表
DROP TABLE IF EXISTS `user_balance`;
CREATE TABLE `user_balance` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `apply_count` INT DEFAULT 0 COMMENT '自动投递剩余额度',
    `ai_match_count` INT DEFAULT 0 COMMENT 'AI匹配分析剩余次数',
    `ai_greet_count` INT DEFAULT 0 COMMENT 'AI打招呼剩余次数',
    `report_count` INT DEFAULT 0 COMMENT '数据报告剩余次数',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户额度表';

-- 23. 充值码表
DROP TABLE IF EXISTS `recharge_code`;
CREATE TABLE `recharge_code` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `code` VARCHAR(32) NOT NULL COMMENT '充值码 (如: GJ2025-XXXX-XXXX-XXXX)',
    `type` TINYINT NOT NULL COMMENT '套餐类型 (1:标准包 2:求职包 3:无限包)',
    `credits_json` JSON COMMENT '额度信息 {"apply":100,"ai_match":50,"ai_greet":30,"report":1}',
    `status` TINYINT DEFAULT 0 COMMENT '状态 (0:未激活 1:已激活 2:已过期 3:已冻结)',
    `activated_by` BIGINT COMMENT '激活用户ID',
    `activated_at` DATETIME COMMENT '激活时间',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '生成时间',
    `expire_at` DATETIME COMMENT '过期时间',
    `batch_no` VARCHAR(20) COMMENT '批次号',
    `remark` VARCHAR(200) COMMENT '备注',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_code` (`code`),
    INDEX `idx_status` (`status`),
    INDEX `idx_batch_no` (`batch_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='充值码表';

-- 24. 消费记录表
DROP TABLE IF EXISTS `consumption_log`;
CREATE TABLE `consumption_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `type` VARCHAR(30) NOT NULL COMMENT '消费类型 (apply:投递, ai_match:AI分析, ai_greet:AI打招呼, report:报告)',
    `amount` INT DEFAULT 1 COMMENT '消耗额度',
    `platform` VARCHAR(50) COMMENT '招聘平台 (boss/liepin/job51/zhilian)',
    `job_id` VARCHAR(100) COMMENT '职位ID',
    `job_name` VARCHAR(200) COMMENT '职位名称',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '消费时间',
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_type` (`type`),
    INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消费记录表';

-- 25. 充值记录表
DROP TABLE IF EXISTS `recharge_log`;
CREATE TABLE `recharge_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `code_id` BIGINT NOT NULL COMMENT '充值码ID',
    `type` TINYINT NOT NULL COMMENT '套餐类型',
    `credits_json` JSON COMMENT '充值的额度详情',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '充值时间',
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_code_id` (`code_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='充值记录表';

-- 26. 用户配置表
DROP TABLE IF EXISTS `user_config`;
CREATE TABLE `user_config` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `platform` VARCHAR(50) NOT NULL COMMENT '平台 (boss/liepin/job51/zhilian)',
    `config_json` JSON COMMENT '配置内容',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_user_platform` (`user_id`, `platform`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户配置表';
