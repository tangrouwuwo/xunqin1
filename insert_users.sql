-- 插入管理员账号
INSERT INTO `user` (`username`, `password`, `nickname`, `role`, `status`, `is_verified`) 
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '系统管理员', 'ADMIN', 1, 1);

-- 插入测试用户
INSERT INTO `user` (`username`, `password`, `nickname`, `role`, `status`, `is_verified`) 
VALUES ('seeker1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '张寻亲', 'SEEKER', 1, 0);

INSERT INTO `user` (`username`, `password`, `nickname`, `role`, `status`, `is_verified`) 
VALUES ('volunteer1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '王志愿', 'VOLUNTEER', 1, 0);

INSERT INTO `user` (`username`, `password`, `nickname`, `role`, `status`, `is_verified`) 
VALUES ('clue1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '线索人', 'CLUE_PROVIDER', 1, 0);

-- 验证插入结果
SELECT COUNT(*) FROM user;