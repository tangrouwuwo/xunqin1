-- 创建数据库
CREATE DATABASE IF NOT EXISTS xunqin_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE xunqin_db;

-- 用户表
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `password` VARCHAR(255) NOT NULL COMMENT '密码',
  `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
  `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `role` VARCHAR(20) NOT NULL COMMENT '角色：ADMIN/SEEKER/VOLUNTEER/CLUE_PROVIDER',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
  `is_verified` TINYINT DEFAULT 0 COMMENT '是否实名认证：0-未认证 1-已认证',
  `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除：0-未删除 1-已删除',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_email` (`email`),
  KEY `idx_phone` (`phone`),
  KEY `idx_role` (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 寻亲信息表
DROP TABLE IF EXISTS `missing_person`;
CREATE TABLE `missing_person` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `seeker_id` BIGINT NOT NULL COMMENT '寻亲者ID',
  `title` VARCHAR(200) NOT NULL COMMENT '寻亲标题',
  `name` VARCHAR(50) NOT NULL COMMENT '失踪人员姓名',
  `gender` VARCHAR(10) DEFAULT NULL COMMENT '性别',
  `birth_date` DATE DEFAULT NULL COMMENT '出生日期',
  `age_at_missing` INT DEFAULT NULL COMMENT '失踪时年龄',
  `current_age` INT DEFAULT NULL COMMENT '当前年龄',
  `height` INT DEFAULT NULL COMMENT '身高（cm）',
  `weight` INT DEFAULT NULL COMMENT '体重（kg）',
  `appearance` TEXT COMMENT '体貌特征',
  `clothing` VARCHAR(500) DEFAULT NULL COMMENT '衣着描述',
  `special_features` VARCHAR(500) DEFAULT NULL COMMENT '特殊特征',
  `missing_date` DATE DEFAULT NULL COMMENT '失踪日期',
  `missing_location` VARCHAR(255) DEFAULT NULL COMMENT '失踪地点',
  `missing_location_lat` DECIMAL(10,6) DEFAULT NULL COMMENT '失踪地点纬度',
  `missing_location_lng` DECIMAL(10,6) DEFAULT NULL COMMENT '失踪地点经度',
  `missing_cause` VARCHAR(500) DEFAULT NULL COMMENT '失踪原因',
  `description` TEXT COMMENT '详细描述',
  `photos` TEXT COMMENT '照片URL（逗号分隔）',
  `videos` TEXT COMMENT '视频URL（逗号分隔）',
  `contact_name` VARCHAR(50) DEFAULT NULL COMMENT '联系人姓名',
  `contact_phone` VARCHAR(20) DEFAULT NULL COMMENT '联系人电话',
  `contact_email` VARCHAR(100) DEFAULT NULL COMMENT '联系人邮箱',
  `reward` VARCHAR(255) DEFAULT NULL COMMENT '悬赏信息',
  `status` TINYINT DEFAULT 0 COMMENT '审核状态：0-待审核 1-已通过 2-已拒绝',
  `match_status` TINYINT DEFAULT 0 COMMENT '匹配状态：0-未匹配 1-匹配中 2-已匹配',
  `view_count` INT DEFAULT 0 COMMENT '浏览次数',
  `clue_count` INT DEFAULT 0 COMMENT '线索数量',
  `reject_reason` VARCHAR(500) DEFAULT NULL COMMENT '拒绝原因',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除：0-未删除 1-已删除',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_seeker_id` (`seeker_id`),
  KEY `idx_status` (`status`),
  KEY `idx_missing_date` (`missing_date`),
  KEY `idx_missing_location` (`missing_location`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='寻亲信息表';

-- 线索表
DROP TABLE IF EXISTS `clue`;
CREATE TABLE `clue` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `missing_person_id` BIGINT NOT NULL COMMENT '寻亲信息ID',
  `provider_id` BIGINT DEFAULT NULL COMMENT '线索提供者ID（匿名则为NULL）',
  `is_anonymous` TINYINT DEFAULT 0 COMMENT '是否匿名：0-否 1-是',
  `content` TEXT NOT NULL COMMENT '线索内容',
  `contact_name` VARCHAR(50) DEFAULT NULL COMMENT '联系人姓名',
  `contact_phone` VARCHAR(20) DEFAULT NULL COMMENT '联系人电话',
  `contact_email` VARCHAR(100) DEFAULT NULL COMMENT '联系人邮箱',
  `status` TINYINT DEFAULT 0 COMMENT '状态：0-待处理 1-已核实 2-已拒绝',
  `handler_id` BIGINT DEFAULT NULL COMMENT '处理人ID（志愿者）',
  `handle_time` DATETIME DEFAULT NULL COMMENT '处理时间',
  `handle_result` TEXT COMMENT '处理结果',
  `handle_remark` VARCHAR(500) DEFAULT NULL COMMENT '处理备注',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除：0-未删除 1-已删除',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_missing_person_id` (`missing_person_id`),
  KEY `idx_provider_id` (`provider_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='线索表';

-- 任务表
DROP TABLE IF EXISTS `task`;
CREATE TABLE `task` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `publisher_id` BIGINT NOT NULL COMMENT '发布者ID',
  `title` VARCHAR(200) NOT NULL COMMENT '任务标题',
  `type` VARCHAR(50) NOT NULL COMMENT '任务类型：INVESTIGATE/SEARCH/FOLLOW_UP/ANALYZE',
  `description` TEXT COMMENT '任务描述',
  `location` VARCHAR(255) DEFAULT NULL COMMENT '任务地点',
  `location_lat` DECIMAL(10,6) DEFAULT NULL COMMENT '地点纬度',
  `location_lng` DECIMAL(10,6) DEFAULT NULL COMMENT '地点经度',
  `priority` TINYINT DEFAULT 1 COMMENT '优先级：1-低 2-中 3-高',
  `required_skills` VARCHAR(255) DEFAULT NULL COMMENT '所需技能',
  `deadline` DATETIME DEFAULT NULL COMMENT '截止日期',
  `volunteer_id` BIGINT DEFAULT NULL COMMENT '认领志愿者ID',
  `status` TINYINT DEFAULT 0 COMMENT '状态：0-待认领 1-进行中 2-已完成 3-已取消 4-待审核 5-已拒绝',
  `result` TEXT COMMENT '任务结果',
  `review_remark` VARCHAR(500) DEFAULT NULL COMMENT '审核备注（仅拒绝时填写）',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除：0-未删除 1-已删除',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_publisher_id` (`publisher_id`),
  KEY `idx_volunteer_id` (`volunteer_id`),
  KEY `idx_status` (`status`),
  KEY `idx_priority` (`priority`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务表';

-- 任务日志表
DROP TABLE IF EXISTS `task_log`;
CREATE TABLE `task_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `task_id` BIGINT NOT NULL COMMENT '任务ID',
  `volunteer_id` BIGINT NOT NULL COMMENT '志愿者ID',
  `content` TEXT NOT NULL COMMENT '日志内容',
  `attachments` TEXT COMMENT '附件URL（逗号分隔）',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_task_id` (`task_id`),
  KEY `idx_volunteer_id` (`volunteer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务日志表';

-- 社区帖子表
DROP TABLE IF EXISTS `community_post`;
CREATE TABLE `community_post` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` BIGINT NOT NULL COMMENT '发布者ID',
  `type` VARCHAR(50) NOT NULL COMMENT '帖子类型：EXPERIENCE/HELP/DISCUSSION/ANNOUNCEMENT',
  `title` VARCHAR(200) NOT NULL COMMENT '标题',
  `content` TEXT NOT NULL COMMENT '内容',
  `images` TEXT COMMENT '图片URL（逗号分隔）',
  `tags` VARCHAR(255) DEFAULT NULL COMMENT '标签（逗号分隔）',
  `view_count` INT DEFAULT 0 COMMENT '浏览次数',
  `like_count` INT DEFAULT 0 COMMENT '点赞次数',
  `comment_count` INT DEFAULT 0 COMMENT '评论次数',
  `is_top` TINYINT DEFAULT 0 COMMENT '是否置顶：0-否 1-是',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0-待审核 1-已通过 2-已拒绝',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除：0-未删除 1-已删除',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_type` (`type`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='社区帖子表';

-- 评论表
DROP TABLE IF EXISTS `comment`;
CREATE TABLE `comment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `post_id` BIGINT NOT NULL COMMENT '帖子ID',
  `user_id` BIGINT NOT NULL COMMENT '评论者ID',
  `parent_id` BIGINT DEFAULT NULL COMMENT '父评论ID（回复时使用）',
  `content` TEXT NOT NULL COMMENT '评论内容',
  `like_count` INT DEFAULT 0 COMMENT '点赞次数',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除：0-未删除 1-已删除',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_post_id` (`post_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';

-- 点赞表
DROP TABLE IF EXISTS `like_record`;
CREATE TABLE `like_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `target_type` VARCHAR(50) NOT NULL COMMENT '目标类型：POST/COMMENT',
  `target_id` BIGINT NOT NULL COMMENT '目标ID',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除：0-未删除 1-已删除',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_target` (`user_id`, `target_type`, `target_id`, `is_deleted`),
  KEY `idx_target` (`target_type`, `target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='点赞表';

-- 成功案例表
DROP TABLE IF EXISTS `success_case`;
CREATE TABLE `success_case` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `seeker_id` BIGINT NOT NULL COMMENT '寻亲者ID',
  `missing_person_id` BIGINT DEFAULT NULL COMMENT '关联的寻亲信息ID',
  `title` VARCHAR(200) NOT NULL COMMENT '标题',
  `story` TEXT NOT NULL COMMENT '团聚故事',
  `photos` TEXT COMMENT '照片URL（逗号分隔）',
  `videos` TEXT COMMENT '视频URL（逗号分隔）',
  `tags` VARCHAR(255) DEFAULT NULL COMMENT '标签（逗号分隔）',
  `reunion_date` DATE DEFAULT NULL COMMENT '团聚日期',
  `reunion_location` VARCHAR(255) DEFAULT NULL COMMENT '团聚地点',
  `approval_remark` TEXT COMMENT '审核备注',
  `view_count` INT DEFAULT 0 COMMENT '浏览次数',
  `like_count` INT DEFAULT 0 COMMENT '点赞次数',
  `status` TINYINT DEFAULT 0 COMMENT '审核状态：0-待审核 1-已通过 2-已拒绝',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除：0-未删除 1-已删除',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_seeker_id` (`seeker_id`),
  KEY `idx_missing_person_id` (`missing_person_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成功案例表';

-- 管理员通知表
DROP TABLE IF EXISTS `admin_notification`;
CREATE TABLE `admin_notification` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` BIGINT NOT NULL COMMENT '接收用户ID',
  `title` VARCHAR(200) NOT NULL COMMENT '通知标题',
  `content` TEXT NOT NULL COMMENT '通知内容',
  `type` VARCHAR(50) NOT NULL COMMENT '通知类型',
  `target_id` BIGINT DEFAULT NULL COMMENT '关联目标ID',
  `target_type` VARCHAR(50) DEFAULT NULL COMMENT '关联目标类型',
  `priority` TINYINT DEFAULT 1 COMMENT '优先级：1-低 2-中 3-高',
  `is_read` TINYINT DEFAULT 0 COMMENT '是否已读：0-未读 1-已读',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除：0-未删除 1-已删除',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_is_read` (`is_read`),
  KEY `idx_priority` (`priority`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员通知表';

-- 寻亲者通知表
DROP TABLE IF EXISTS `seeker_notification`;
CREATE TABLE `seeker_notification` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` BIGINT NOT NULL COMMENT '接收用户ID',
  `title` VARCHAR(200) NOT NULL COMMENT '通知标题',
  `content` TEXT NOT NULL COMMENT '通知内容',
  `type` VARCHAR(50) NOT NULL COMMENT '通知类型',
  `target_id` BIGINT DEFAULT NULL COMMENT '关联目标ID',
  `target_type` VARCHAR(50) DEFAULT NULL COMMENT '关联目标类型',
  `related_person_id` BIGINT DEFAULT NULL COMMENT '相关寻亲信息ID',
  `is_read` TINYINT DEFAULT 0 COMMENT '是否已读：0-未读 1-已读',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除：0-未删除 1-已删除',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_is_read` (`is_read`),
  KEY `idx_related_person_id` (`related_person_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='寻亲者通知表';

-- 志愿者通知表
DROP TABLE IF EXISTS `volunteer_notification`;
CREATE TABLE `volunteer_notification` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` BIGINT NOT NULL COMMENT '接收用户ID',
  `title` VARCHAR(200) NOT NULL COMMENT '通知标题',
  `content` TEXT NOT NULL COMMENT '通知内容',
  `type` VARCHAR(50) NOT NULL COMMENT '通知类型',
  `target_id` BIGINT DEFAULT NULL COMMENT '关联目标ID',
  `target_type` VARCHAR(50) DEFAULT NULL COMMENT '关联目标类型',
  `task_id` BIGINT DEFAULT NULL COMMENT '相关任务ID',
  `is_read` TINYINT DEFAULT 0 COMMENT '是否已读：0-未读 1-已读',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除：0-未删除 1-已删除',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_is_read` (`is_read`),
  KEY `idx_task_id` (`task_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='志愿者通知表';

-- 线索提供者通知表
DROP TABLE IF EXISTS `clue_provider_notification`;
CREATE TABLE `clue_provider_notification` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` BIGINT DEFAULT NULL COMMENT '接收用户ID（匿名则为NULL）',
  `title` VARCHAR(200) NOT NULL COMMENT '通知标题',
  `content` TEXT NOT NULL COMMENT '通知内容',
  `type` VARCHAR(50) NOT NULL COMMENT '通知类型',
  `target_id` BIGINT DEFAULT NULL COMMENT '关联目标ID',
  `target_type` VARCHAR(50) DEFAULT NULL COMMENT '关联目标类型',
  `clue_id` BIGINT DEFAULT NULL COMMENT '相关线索ID',
  `is_read` TINYINT DEFAULT 0 COMMENT '是否已读：0-未读 1-已读',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除：0-未删除 1-已删除',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_is_read` (`is_read`),
  KEY `idx_clue_id` (`clue_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='线索提供者通知表';

-- 志愿者活动表
DROP TABLE IF EXISTS `volunteer_activity`;
CREATE TABLE `volunteer_activity` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `title` VARCHAR(200) NOT NULL COMMENT '活动标题',
  `type` VARCHAR(50) NOT NULL COMMENT '活动类型',
  `description` TEXT COMMENT '活动描述',
  `content` TEXT COMMENT '活动内容',
  `location` VARCHAR(255) DEFAULT NULL COMMENT '活动地点',
  `location_lat` DECIMAL(10,6) DEFAULT NULL COMMENT '地点纬度',
  `location_lng` DECIMAL(10,6) DEFAULT NULL COMMENT '地点经度',
  `start_time` DATETIME DEFAULT NULL COMMENT '开始时间',
  `end_time` DATETIME DEFAULT NULL COMMENT '结束时间',
  `max_participants` INT DEFAULT NULL COMMENT '最大参与人数',
  `current_participants` INT DEFAULT 0 COMMENT '当前参与人数',
  `required_skills` VARCHAR(255) DEFAULT NULL COMMENT '所需技能',
  `contact_name` VARCHAR(50) DEFAULT NULL COMMENT '联系人姓名',
  `contact_phone` VARCHAR(20) DEFAULT NULL COMMENT '联系人电话',
  `contact_email` VARCHAR(100) DEFAULT NULL COMMENT '联系人邮箱',
  `cover_image` VARCHAR(255) DEFAULT NULL COMMENT '封面图片',
  `attachments` TEXT COMMENT '附件URL（逗号分隔）',
  `publisher_id` BIGINT NOT NULL COMMENT '发布者ID',
  `status` TINYINT DEFAULT 0 COMMENT '状态：0-待审核 1-已通过 2-已拒绝 3-进行中 4-已完成 5-已取消',
  `view_count` INT DEFAULT 0 COMMENT '浏览次数',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除：0-未删除 1-已删除',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_publisher_id` (`publisher_id`),
  KEY `idx_status` (`status`),
  KEY `idx_start_time` (`start_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='志愿者活动表';

-- 志愿者活动参与者表
DROP TABLE IF EXISTS `volunteer_activity_participant`;
CREATE TABLE `volunteer_activity_participant` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `activity_id` BIGINT NOT NULL COMMENT '活动ID',
  `volunteer_id` BIGINT NOT NULL COMMENT '志愿者ID',
  `status` TINYINT DEFAULT 0 COMMENT '状态：0-待审核 1-已通过 2-已拒绝 3-已签到 4-已签退',
  `apply_reason` VARCHAR(500) DEFAULT NULL COMMENT '申请理由',
  `reject_reason` VARCHAR(500) DEFAULT NULL COMMENT '拒绝理由',
  `check_in_time` DATETIME DEFAULT NULL COMMENT '签到时间',
  `check_out_time` DATETIME DEFAULT NULL COMMENT '签退时间',
  `work_hours` DECIMAL(5,2) DEFAULT NULL COMMENT '工作时长',
  `admin_remark` VARCHAR(500) DEFAULT NULL COMMENT '管理员备注',
  `is_reported` TINYINT DEFAULT 0 COMMENT '是否已提交报告：0-未提交 1-已提交',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除：0-未删除 1-已删除',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_activity_id` (`activity_id`),
  KEY `idx_volunteer_id` (`volunteer_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='志愿者活动参与者表';

-- 志愿者活动进度表
DROP TABLE IF EXISTS `volunteer_activity_progress`;
CREATE TABLE `volunteer_activity_progress` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `activity_id` BIGINT NOT NULL COMMENT '活动ID',
  `title` VARCHAR(200) NOT NULL COMMENT '进度标题',
  `content` TEXT COMMENT '进度内容',
  `progress_type` VARCHAR(50) DEFAULT NULL COMMENT '进度类型',
  `publisher_id` BIGINT NOT NULL COMMENT '发布者ID',
  `is_important` TINYINT DEFAULT 0 COMMENT '是否重要：0-否 1-是',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除：0-未删除 1-已删除',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_activity_id` (`activity_id`),
  KEY `idx_publisher_id` (`publisher_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='志愿者活动进度表';

-- 志愿者活动报告表
DROP TABLE IF EXISTS `volunteer_activity_report`;
CREATE TABLE `volunteer_activity_report` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `activity_id` BIGINT NOT NULL COMMENT '活动ID',
  `participant_id` BIGINT NOT NULL COMMENT '参与者ID',
  `volunteer_id` BIGINT NOT NULL COMMENT '志愿者ID',
  `title` VARCHAR(200) NOT NULL COMMENT '报告标题',
  `content` TEXT COMMENT '报告内容',
  `work_content` TEXT COMMENT '工作内容',
  `work_results` TEXT COMMENT '工作成果',
  `problems_encountered` TEXT COMMENT '遇到的问题',
  `suggestions` TEXT COMMENT '建议',
  `photos` TEXT COMMENT '照片URL（逗号分隔）',
  `attachments` TEXT COMMENT '附件URL（逗号分隔）',
  `status` TINYINT DEFAULT 0 COMMENT '状态：0-待审核 1-已通过 2-已拒绝',
  `review_remark` VARCHAR(500) DEFAULT NULL COMMENT '审核备注',
  `review_time` DATETIME DEFAULT NULL COMMENT '审核时间',
  `reviewer_id` BIGINT DEFAULT NULL COMMENT '审核人ID',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除：0-未删除 1-已删除',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_activity_id` (`activity_id`),
  KEY `idx_volunteer_id` (`volunteer_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='志愿者活动报告表';

-- 插入用户数据
INSERT INTO `user` (`username`, `password`, `nickname`, `role`, `status`, `is_verified`) 
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '系统管理员', 'ADMIN', 1, 1);

INSERT INTO `user` (`username`, `password`, `nickname`, `role`, `status`, `is_verified`) 
VALUES ('seeker1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '张寻亲', 'SEEKER', 1, 0);

INSERT INTO `user` (`username`, `password`, `nickname`, `role`, `status`, `is_verified`) 
VALUES ('seeker2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '李寻亲', 'SEEKER', 1, 0);

INSERT INTO `user` (`username`, `password`, `nickname`, `role`, `status`, `is_verified`) 
VALUES ('volunteer1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '王志愿', 'VOLUNTEER', 1, 0);

INSERT INTO `user` (`username`, `password`, `nickname`, `role`, `status`, `is_verified`) 
VALUES ('volunteer2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '赵志愿', 'VOLUNTEER', 1, 0);

INSERT INTO `user` (`username`, `password`, `nickname`, `role`, `status`, `is_verified`) 
VALUES ('clue1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '线索人', 'CLUE_PROVIDER', 1, 0);

-- 插入寻亲信息数据
INSERT INTO `missing_person` (`seeker_id`, `name`, `gender`, `age_at_missing`, `missing_date`, `missing_location`, `description`, `status`) 
VALUES (2, '张小明', '男', 5, '2020-05-15', '北京市朝阳区', '身高约110cm，穿着红色T恤，蓝色短裤', 1);

INSERT INTO `missing_person` (`seeker_id`, `name`, `gender`, `age_at_missing`, `missing_date`, `missing_location`, `description`, `status`) 
VALUES (2, '李小红', '女', 8, '2019-08-20', '上海市浦东新区', '扎着马尾辫，穿着粉色连衣裙', 1);

INSERT INTO `missing_person` (`seeker_id`, `name`, `gender`, `age_at_missing`, `missing_date`, `missing_location`, `description`, `status`) 
VALUES (3, '王大力', '男', 10, '2018-03-10', '广州市天河区', '身高约140cm，有点胖，戴着眼镜', 1);

-- 插入线索数据
INSERT INTO `clue` (`missing_person_id`, `provider_id`, `is_anonymous`, `content`, `status`) 
VALUES (1, 6, 0, '我在北京西站见过一个孩子，长得像张小明', 0);

INSERT INTO `clue` (`missing_person_id`, `provider_id`, `is_anonymous`, `content`, `status`) 
VALUES (2, 6, 1, '上海浦东有个小女孩，特征很像', 0);

-- 插入任务数据
INSERT INTO `task` (`publisher_id`, `title`, `type`, `description`, `priority`, `status`) 
VALUES (1, '调查张小明失踪线索', 'INVESTIGATE', '根据线索前往北京西站调查', 3, 0);

INSERT INTO `task` (`publisher_id`, `title`, `type`, `description`, `priority`, `status`) 
VALUES (1, '走访李小红失踪地点', 'SEARCH', '前往上海浦东新区走访', 2, 0);

-- 插入任务日志数据
INSERT INTO `task_log` (`task_id`, `volunteer_id`, `content`) 
VALUES (1, 4, '已到达北京西站，开始调查');

-- 插入社区帖子数据
INSERT INTO `community_post` (`user_id`, `type`, `title`, `content`, `status`) 
VALUES (2, 'EXPERIENCE', '我的寻亲经历', '分享我的寻亲经历，希望能帮助到更多人', 1);

INSERT INTO `community_post` (`user_id`, `type`, `title`, `content`, `status`) 
VALUES (4, 'DISCUSSION', '如何提高寻亲成功率', '讨论提高寻亲成功率的方法', 1);

-- 插入评论数据
INSERT INTO `comment` (`post_id`, `user_id`, `content`) 
VALUES (1, 4, '加油，一定能找到的！');

INSERT INTO `comment` (`post_id`, `user_id`, `content`) 
VALUES (1, 5, '支持你！');

-- 插入点赞数据
INSERT INTO `like_record` (`user_id`, `target_type`, `target_id`) 
VALUES (4, 'POST', 1);

INSERT INTO `like_record` (`user_id`, `target_type`, `target_id`) 
VALUES (5, 'POST', 1);

-- 插入成功案例数据
INSERT INTO `success_case` (`seeker_id`, `title`, `story`, `status`) 
VALUES (2, '团聚的喜悦', '经过三个月的努力，终于找到了失踪的孩子', 1);

-- 插入通知数据
INSERT INTO `admin_notification` (`user_id`, `title`, `content`, `type`, `priority`) 
VALUES (1, '新用户注册', '有新用户注册需要审核', 'USER_REGISTER', 2);

INSERT INTO `seeker_notification` (`user_id`, `title`, `content`, `type`, `related_person_id`) 
VALUES (2, '新线索提醒', '您的寻亲信息有新线索', 'NEW_CLUE', 1);

INSERT INTO `volunteer_notification` (`user_id`, `title`, `content`, `type`, `task_id`) 
VALUES (4, '新任务提醒', '有新任务需要认领', 'NEW_TASK', 1);

INSERT INTO `clue_provider_notification` (`user_id`, `title`, `content`, `type`, `clue_id`) 
VALUES (6, '线索处理通知', '您提供的线索已被处理', 'CLUE_PROCESSED', 1);

-- 插入志愿者活动数据
INSERT INTO `volunteer_activity` (`publisher_id`, `title`, `type`, `description`, `location`, `start_time`, `end_time`, `max_participants`, `current_participants`, `status`) 
VALUES (1, '北京寻亲志愿者活动', 'SEARCH', '组织志愿者在北京地区进行寻亲宣传和走访', '北京市', '2024-04-01 09:00:00', '2024-04-01 17:00:00', 20, 5, 3);

INSERT INTO `volunteer_activity` (`publisher_id`, `title`, `type`, `description`, `location`, `start_time`, `end_time`, `max_participants`, `current_participants`, `status`) 
VALUES (1, '上海寻亲公益讲座', 'TRAINING', '举办寻亲技巧培训讲座', '上海市', '2024-04-15 14:00:00', '2024-04-15 17:00:00', 50, 10, 1);

-- 插入志愿者活动参与者数据
INSERT INTO `volunteer_activity_participant` (`activity_id`, `volunteer_id`, `status`, `apply_reason`) 
VALUES (1, 4, 1, '我有时间参加，希望能帮助到寻亲家庭');

INSERT INTO `volunteer_activity_participant` (`activity_id`, `volunteer_id`, `status`, `apply_reason`) 
VALUES (1, 5, 0, '我想参加这个活动');

-- 插入志愿者活动进度数据
INSERT INTO `volunteer_activity_progress` (`activity_id`, `title`, `content`, `publisher_id`, `is_important`) 
VALUES (1, '活动筹备完成', '活动场地已确认，物资已准备完毕', 1, 1);

INSERT INTO `volunteer_activity_progress` (`activity_id`, `title`, `content`, `publisher_id`, `is_important`) 
VALUES (1, '志愿者报名情况', '已有5名志愿者报名参加', 1, 0);

-- 插入志愿者活动报告数据
INSERT INTO `volunteer_activity_report` (`activity_id`, `participant_id`, `volunteer_id`, `title`, `content`, `work_content`, `work_results`, `status`) 
VALUES (1, 1, 4, '活动参与报告', '参与了北京寻亲志愿者活动', '负责现场组织和协调', '活动顺利进行，帮助了3个寻亲家庭', 0);

-- AI聊天记录表
DROP TABLE IF EXISTS `ai_chat_message`;
CREATE TABLE `ai_chat_message` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` BIGINT DEFAULT NULL COMMENT '用户ID（匿名则为NULL）',
  `session_id` VARCHAR(100) NOT NULL COMMENT '会话ID，用于标识一次完整的对话',
  `role` VARCHAR(20) NOT NULL COMMENT '角色：user/assistant',
  `content` TEXT NOT NULL COMMENT '消息内容',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除：0-未删除 1-已删除',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_session_id` (`session_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI聊天记录表';

-- 志愿者申请表
DROP TABLE IF EXISTS `volunteer_application`;
CREATE TABLE `volunteer_application` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '申请ID',
  `name` VARCHAR(50) NOT NULL COMMENT '真实姓名',
  `username` VARCHAR(50) NOT NULL COMMENT '申请的用户名',
  `password` VARCHAR(255) NOT NULL COMMENT '密码（加密后）',
  `phone` VARCHAR(20) NOT NULL COMMENT '手机号',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `age` INT DEFAULT NULL COMMENT '年龄',
  `gender` VARCHAR(10) DEFAULT NULL COMMENT '性别',
  `location` VARCHAR(255) DEFAULT NULL COMMENT '所在地',
  `skills` VARCHAR(500) DEFAULT NULL COMMENT '技能（逗号分隔）',
  `availability` VARCHAR(255) DEFAULT NULL COMMENT '可参与时间',
  `reason` TEXT COMMENT '申请理由',
  `experience` TEXT COMMENT '相关经验',
  `status` TINYINT DEFAULT 0 COMMENT '审核状态：0-待审核 1-已通过 2-已拒绝',
  `review_comment` VARCHAR(500) DEFAULT NULL COMMENT '审核意见',
  `reviewer_id` BIGINT DEFAULT NULL COMMENT '审核人ID',
  `review_time` DATETIME DEFAULT NULL COMMENT '审核时间',
  `user_id` BIGINT DEFAULT NULL COMMENT '审核通过后创建的用户ID',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除：0-未删除 1-已删除',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_phone` (`phone`),
  KEY `idx_status` (`status`),
  KEY `idx_reviewer_id` (`reviewer_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='志愿者申请表';

-- ============================================================
-- 数据库迁移：为已有数据库添加 review_remark 列
-- ============================================================
ALTER TABLE `task`
    ADD COLUMN `review_remark` VARCHAR(500) DEFAULT NULL COMMENT '审核备注（仅拒绝时填写）' AFTER `result`,
    MODIFY COLUMN `status` TINYINT DEFAULT 0 COMMENT '状态：0-待认领 1-进行中 2-已完成 3-已取消 4-待审核 5-已拒绝';

-- 寻亲信息变更记录表
DROP TABLE IF EXISTS `missing_person_change_log`;
CREATE TABLE `missing_person_change_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `missing_person_id` BIGINT NOT NULL COMMENT '寻亲信息ID',
  `seeker_id` BIGINT DEFAULT NULL COMMENT '修改者ID',
  `field_name` VARCHAR(100) NOT NULL COMMENT '变更字段名',
  `old_value` TEXT COMMENT '旧值',
  `new_value` TEXT COMMENT '新值',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_missing_person_id` (`missing_person_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='寻亲信息变更记录表';
