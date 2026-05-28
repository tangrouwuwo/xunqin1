-- ============================================================
-- 寻亲信息测试数据
-- 执行方式：
--   1. 在 Navicat / DBeaver / MySQL 命令行中执行此文件
--   2. 或在程序启动前，执行 source 命令：
--      source D:/寻亲网站升级版/src/main/resources/sql/test_data_missing_persons.sql
-- ============================================================

-- 插入寻亲信息测试数据（超过一页，pageSize=12）
-- 使用 seeker_id=2（张寻亲）
-- 数据库实际存在的寻亲者：id=2(张寻亲), id=7(陈翔)
-- status=1 表示已审核通过，is_deleted=0 表示未删除
-- missing_location 格式：省|市|详细地址

INSERT INTO `missing_person` (`seeker_id`, `title`, `name`, `gender`, `age_at_missing`, `missing_date`, `missing_location`, `description`, `photos`, `contact_name`, `contact_phone`, `status`, `is_deleted`) VALUES 
(2, '寻找儿子张小明', '张小明', '男', 5, '2020-05-15', '北京市|朝阳区|朝阳公园附近', '身高约110cm，穿着红色T恤，蓝色短裤，说话带北方口音', NULL, '张三', '13800138001', 1, 0);

INSERT INTO `missing_person` (`seeker_id`, `title`, `name`, `gender`, `age_at_missing`, `missing_date`, `missing_location`, `description`, `photos`, `contact_name`, `contact_phone`, `status`, `is_deleted`) VALUES 
(2, '紧急寻找女儿李小红', '李小红', '女', 8, '2019-08-20', '上海市|浦东新区|陆家嘴地铁站', '扎着马尾辫，穿着粉色连衣裙，背着粉色书包', NULL, '李四', '13800138002', 1, 0);

INSERT INTO `missing_person` (`seeker_id`, `title`, `name`, `gender`, `age_at_missing`, `missing_date`, `missing_location`, `description`, `photos`, `contact_name`, `contact_phone`, `status`, `is_deleted`) VALUES 
(2, '寻找儿子王大力', '王大力', '男', 10, '2018-03-10', '广东省|广州市|天河区天河城', '身高约140cm，体型偏胖，戴黑框眼镜，喜欢穿运动服', NULL, '王五', '13800138003', 1, 0);

INSERT INTO `missing_person` (`seeker_id`, `title`, `name`, `gender`, `age_at_missing`, `missing_date`, `missing_location`, `description`, `photos`, `contact_name`, `contact_phone`, `status`, `is_deleted`) VALUES 
(2, '寻找父亲赵建国', '赵建国', '男', 65, '2022-01-10', '河北省|石家庄市|长安区博物馆附近', '身高172cm，花白短发，穿深蓝色羽绒服，黑色裤子，有老年痴呆症', NULL, '赵明', '13900139001', 1, 0);

INSERT INTO `missing_person` (`seeker_id`, `title`, `name`, `gender`, `age_at_missing`, `missing_date`, `missing_location`, `description`, `photos`, `contact_name`, `contact_phone`, `status`, `is_deleted`) VALUES 
(2, '寻找母亲刘秀英', '刘秀英', '女', 58, '2021-06-15', '山东省|济南市|历下区泉城广场', '身高160cm，齐耳短发，穿暗红色外套，黑色长裤，右手有烫伤疤痕', NULL, '刘伟', '13900139002', 1, 0);

INSERT INTO `missing_person` (`seeker_id`, `title`, `name`, `gender`, `age_at_missing`, `missing_date`, `missing_location`, `description`, `photos`, `contact_name`, `contact_phone`, `status`, `is_deleted`) VALUES 
(2, '寻找妻子陈小梅', '陈小梅', '女', 32, '2023-11-08', '四川省|成都市|武侯区科华北路', '身高163cm，长发，穿白色羽绒服，牛仔裤，戴银色耳环', NULL, '陈刚', '13900139003', 1, 0);

INSERT INTO `missing_person` (`seeker_id`, `title`, `name`, `gender`, `age_at_missing`, `missing_date`, `missing_location`, `description`, `photos`, `contact_name`, `contact_phone`, `status`, `is_deleted`) VALUES 
(2, '寻找弟弟孙浩然', '孙浩然', '男', 16, '2023-09-20', '河南省|郑州市|二七区火车站', '身高178cm，短发，穿黑色卫衣，灰色运动裤，背黑色双肩包', NULL, '孙蕾', '13900139004', 1, 0);

INSERT INTO `missing_person` (`seeker_id`, `title`, `name`, `gender`, `age_at_missing`, `missing_date`, `missing_location`, `description`, `photos`, `contact_name`, `contact_phone`, `status`, `is_deleted`) VALUES 
(2, '寻找爷爷周德福', '周德福', '男', 72, '2022-08-05', '江苏省|南京市|鼓楼区湖南路', '身高168cm，白发，驼背，穿灰色中山装，拄拐杖，说本地话', NULL, '周强', '13900139005', 1, 0);

INSERT INTO `missing_person` (`seeker_id`, `title`, `name`, `gender`, `age_at_missing`, `missing_date`, `missing_location`, `description`, `photos`, `contact_name`, `contact_phone`, `status`, `is_deleted`) VALUES 
(2, '寻找女儿吴雨桐', '吴雨桐', '女', 13, '2024-02-14', '浙江省|杭州市|西湖区断桥附近', '身高156cm，齐肩长发，穿粉色卫衣，白色百褶裙，戴红色发卡', NULL, '吴刚', '13900139006', 1, 0);

INSERT INTO `missing_person` (`seeker_id`, `title`, `name`, `gender`, `age_at_missing`, `missing_date`, `missing_location`, `description`, `photos`, `contact_name`, `contact_phone`, `status`, `is_deleted`) VALUES 
(2, '寻找哥哥郑志远', '郑志远', '男', 28, '2024-05-01', '广东省|深圳市|南山区科技园', '身高175cm，戴眼镜，穿浅蓝色衬衫，深色西裤，背电脑包', NULL, '郑雅', '13900139007', 1, 0);

INSERT INTO `missing_person` (`seeker_id`, `title`, `name`, `gender`, `age_at_missing`, `missing_date`, `missing_location`, `description`, `photos`, `contact_name`, `contact_phone`, `status`, `is_deleted`) VALUES 
(2, '寻找奶奶黄玉珍', '黄玉珍', '女', 68, '2023-12-03', '湖南省|长沙市|岳麓区大学城', '身高155cm，花白短发，穿枣红色棉袄，黑色裤子，说方言', NULL, '黄磊', '13900139008', 1, 0);

INSERT INTO `missing_person` (`seeker_id`, `title`, `name`, `gender`, `age_at_missing`, `missing_date`, `missing_location`, `description`, `photos`, `contact_name`, `contact_phone`, `status`, `is_deleted`) VALUES 
(2, '寻找父亲林国栋', '林国栋', '男', 55, '2023-07-18', '福建省|福州市|鼓楼区三坊七巷', '身高170cm，平头，穿白色T恤，深色短裤，骑电动车出门', NULL, '林峰', '13900139009', 1, 0);

INSERT INTO `missing_person` (`seeker_id`, `title`, `name`, `gender`, `age_at_missing`, `missing_date`, `missing_location`, `description`, `photos`, `contact_name`, `contact_phone`, `status`, `is_deleted`) VALUES 
(2, '寻找儿子何子轩', '何子轩', '男', 7, '2024-03-22', '山东省|青岛市|市南区五四广场', '身高120cm，穿蓝白条纹T恤，卡其色短裤，戴蓝色帽子', NULL, '何伟', '13900139010', 1, 0);

INSERT INTO `missing_person` (`seeker_id`, `title`, `name`, `gender`, `age_at_missing`, `missing_date`, `missing_location`, `description`, `photos`, `contact_name`, `contact_phone`, `status`, `is_deleted`) VALUES 
(2, '寻找姐姐马晓燕', '马晓燕', '女', 25, '2024-06-01', '北京市|海淀区|中关村大街', '身高165cm，长发披肩，穿白色连衣裙，手提棕色挎包', NULL, '马超', '13900139011', 1, 0);

INSERT INTO `missing_person` (`seeker_id`, `title`, `name`, `gender`, `age_at_missing`, `missing_date`, `missing_location`, `description`, `photos`, `contact_name`, `contact_phone`, `status`, `is_deleted`) VALUES 
(2, '寻找母亲张秀兰', '张秀兰', '女', 62, '2024-04-10', '湖北省|武汉市|武昌区户部巷', '身高158cm，短发微卷，穿紫色碎花上衣，黑色裤子，提菜篮', NULL, '张强', '13900139012', 1, 0);

INSERT INTO `missing_person` (`seeker_id`, `title`, `name`, `gender`, `age_at_missing`, `missing_date`, `missing_location`, `description`, `photos`, `contact_name`, `contact_phone`, `status`, `is_deleted`) VALUES 
(2, '寻找弟弟杨天佑', '杨天佑', '男', 19, '2024-08-15', '陕西省|西安市|雁塔区大雁塔广场', '身高180cm，寸头，穿黑色T恤，迷彩短裤，左侧脸颊有颗痣', NULL, '杨雪', '13900139013', 1, 0);

INSERT INTO `missing_person` (`seeker_id`, `title`, `name`, `gender`, `age_at_missing`, `missing_date`, `missing_location`, `description`, `photos`, `contact_name`, `contact_phone`, `status`, `is_deleted`) VALUES 
(2, '寻找女儿唐思琪', '唐思琪', '女', 11, '2023-10-28', '安徽省|合肥市|蜀山区野生动物园', '身高145cm，扎双马尾，穿黄色T恤，蓝色牛仔裙，爱画画', NULL, '唐磊', '13900139014', 1, 0);

INSERT INTO `missing_person` (`seeker_id`, `title`, `name`, `gender`, `age_at_missing`, `missing_date`, `missing_location`, `description`, `photos`, `contact_name`, `contact_phone`, `status`, `is_deleted`) VALUES 
(2, '寻找丈夫徐建华', '徐建华', '男', 48, '2024-09-05', '重庆市|渝中区|解放碑步行街', '身高174cm，平头微胖，穿深蓝色POLO衫，米色长裤，戴手表', NULL, '徐芳', '13900139015', 1, 0);