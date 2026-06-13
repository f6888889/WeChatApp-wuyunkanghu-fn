-- 银舞沐心数据库初始化脚本
CREATE DATABASE IF NOT EXISTS yinwu_muxin CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE yinwu_muxin;

-- 用户表
DROP TABLE IF EXISTS users;
CREATE TABLE users (
    id VARCHAR(50) PRIMARY KEY,
    open_id VARCHAR(100) UNIQUE,
    nickname VARCHAR(100),
    avatar VARCHAR(500),
    gender VARCHAR(20),
    age INT,
    phone VARCHAR(20),
    emergency_contact_name VARCHAR(100),
    emergency_contact_phone VARCHAR(20),
    emergency_contact_enabled BOOLEAN DEFAULT FALSE,
    health_profile JSON,
    learning_progress JSON,
    favorites JSON,
    recent_searches JSON,
    learning_history JSON,
    created_at DATE,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 课程表
DROP TABLE IF EXISTS courses;
CREATE TABLE courses (
    id VARCHAR(50) PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    difficulty VARCHAR(20),
    cover_image VARCHAR(500),
    video_url VARCHAR(500),
    suitable_for JSON,
    benefits JSON,
    steps JSON,
    created_at DATE,
    category VARCHAR(50),
    like_count INT DEFAULT 0,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 健康预警表
DROP TABLE IF EXISTS health_alerts;
CREATE TABLE health_alerts (
    id VARCHAR(50) PRIMARY KEY,
    user_id VARCHAR(50),
    type VARCHAR(20),
    level VARCHAR(20),
    title VARCHAR(200),
    content TEXT,
    data_snapshot JSON,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 健康提醒表
DROP TABLE IF EXISTS health_reminders;
CREATE TABLE health_reminders (
    id VARCHAR(50) PRIMARY KEY,
    type VARCHAR(20),
    title VARCHAR(200),
    content TEXT,
    time_slot VARCHAR(20),
    icon VARCHAR(50),
    priority VARCHAR(20),
    category VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 商城物品表
DROP TABLE IF EXISTS shop_items;
CREATE TABLE shop_items (
    id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    points INT,
    image VARCHAR(500),
    category VARCHAR(50),
    stock INT,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 消息表
DROP TABLE IF EXISTS messages;
CREATE TABLE messages (
    id VARCHAR(50) PRIMARY KEY,
    user_id VARCHAR(50),
    type VARCHAR(20),
    title VARCHAR(200),
    content TEXT,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 学习记录表
DROP TABLE IF EXISTS learning_records;
CREATE TABLE learning_records (
    id VARCHAR(50) PRIMARY KEY,
    user_id VARCHAR(50),
    course_id VARCHAR(50),
    course_title VARCHAR(200),
    cover_image VARCHAR(500),
    duration INT,
    completed BOOLEAN DEFAULT FALSE,
    completed_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE
);

-- 插入用户数据
INSERT INTO users (id, open_id, nickname, avatar, gender, age, phone, emergency_contact_name, emergency_contact_phone, emergency_contact_enabled, health_profile, learning_progress, favorites, recent_searches, learning_history, created_at) VALUES
('user_001', NULL, '王奶奶', 'https://picsum.photos/seed/avatar1/200/200', 'female', 68, '138****5678', '王小明', '139****5678', TRUE, '{"hasHypertension":true,"hasDiabetes":false,"hasArthritis":true,"hasHeartDisease":false,"mobilityLevel":"some_limitation"}', '{"totalCourses":8,"completedCourses":3,"totalMinutes":156,"continuousDays":7,"onlineMinutes":0}', '["course_001","course_004","course_006"]', '["膝盖不好","腰疼","肩颈酸痛"]', '[]', '2024-06-01'),
('user_002', NULL, '李爷爷', 'https://picsum.photos/seed/avatar2/200/200', 'male', 72, '139****1234', '李小红', '138****1234', TRUE, '{"hasHypertension":true,"hasDiabetes":true,"hasArthritis":false,"hasHeartDisease":false,"mobilityLevel":"good"}', '{"totalCourses":8,"completedCourses":5,"totalMinutes":280,"continuousDays":15,"onlineMinutes":0}', '["course_002","course_005","course_008"]', '["血压高","糖尿病","太极"]', '[]', '2024-05-15'),
('user_003', NULL, '张阿姨', 'https://picsum.photos/seed/avatar3/200/200', 'female', 62, '136****8765', '张小华', '137****8765', TRUE, '{"hasHypertension":false,"hasDiabetes":false,"hasArthritis":false,"hasHeartDisease":false,"mobilityLevel":"excellent"}', '{"totalCourses":8,"completedCourses":7,"totalMinutes":420,"continuousDays":30,"onlineMinutes":0}', '["course_003","course_005","course_008"]', '["新疆舞","民族舞","广场舞"]', '[]', '2024-04-20'),
('user_d27dcbe5', 'oJFlS3fAduMHE4qnvNJtMl5XgZE4', '五个核桃', 'http://tmp/hWeQeL888xTG73847918c9bca7f5dba7c4d621ec3b4c.jpeg', NULL, 0, NULL, NULL, NULL, FALSE, NULL, '{"totalCourses":0,"completedCourses":0,"totalMinutes":27,"continuousDays":1,"onlineMinutes":68}', '["course_005"]', '["一加一等于几","一加二等于几","膝盖不舒服","新疆舞","腰疼","膝盖不好"]', '[{"courseId":"course_005","courseTitle":"荷塘月色","coverImage":"https://picsum.photos/seed/dance5/400/300","duration":15,"completedAt":"2026-04-27T10:30:00Z"}]', '2024-03-15');

-- 插入课程数据
INSERT INTO courses (id, title, description, difficulty, cover_image, video_url, suitable_for, benefits, steps, created_at, category, like_count) VALUES
('course_001', '山水情歌', '在优美的山水情歌中翩翩起舞，动作舒缓流畅，仿佛置身大自然。适合喜欢抒情曲风的舞者。', 'easy', 'https://java-chashaobao-ai.oss-cn-beijing.aliyuncs.com/%E5%9B%BD%E5%88%9B%E5%9B%BE%E7%89%87/%E5%B1%B1%E6%B0%B4%E6%83%85%E6%AD%8C%E5%9B%BE%E7%89%87.png', 'https://java-chashaobao-ai.oss-cn-beijing.aliyuncs.com/%E5%9B%BD%E5%88%9B/1%E5%B1%B1%E6%B0%B4%E6%83%85%E6%AD%8C.mp4', '["膝盖不适","关节炎","术后恢复"]', '["保护膝盖","增加润滑","减少疼痛"]', '[{"title":"热身准备","duration":2,"description":"轻轻活动关节"},{"title":"基础动作","duration":8,"description":"缓慢抬腿、轻柔摆动"},{"title":"放松整理","duration":5,"description":"深呼吸、轻轻拍打腿部"}]', '2024-03-15', '膝盖保健', 0),
('course_002', '美丽的草原我的家', '跟随悠扬的草原旋律，舒展双臂，仿佛置身辽阔草原。动作优美大方，提升气质。', 'medium', 'https://java-chashaobao-ai.oss-cn-beijing.aliyuncs.com/%E5%9B%BD%E5%88%9B%E5%9B%BE%E7%89%87/%E7%BE%8E%E4%B8%BD%E7%9A%84%E8%8D%89%E5%8E%9F%E6%88%91%E7%9A%84%E5%AE%B6%E5%9B%BE%E7%89%87.png', 'https://java-chashaobao-ai.oss-cn-beijing.aliyuncs.com/%E5%9B%BD%E5%88%9B/2%E7%BE%8E%E4%B8%BD%E7%9A%84%E8%8D%89%E5%8E%9F%E6%88%91%E7%9A%84%E5%AE%B6.mp4', '["肩颈酸痛","颈椎不适","长期低头"]', '["缓解酸痛","增加灵活度","改善血液循环"]', '[{"title":"颈部运动","duration":3,"description":"缓慢转头、点头"},{"title":"肩部运动","duration":5,"description":"耸肩、绕肩、画圈"},{"title":"整理放松","duration":4,"description":"深呼吸、轻轻摇晃"}]', '2024-03-18', '肩颈保健', 0),
('course_003', '中老年健身操', '经典健身操，动作简单易学，配合节奏明快的音乐，强身健体，活力满满。', 'medium', 'https://java-chashaobao-ai.oss-cn-beijing.aliyuncs.com/%E5%9B%BD%E5%88%9B%E5%9B%BE%E7%89%87/%E4%B8%AD%E8%80%81%E5%B9%B4%E5%81%A5%E8%BA%AB%E6%93%8D%E5%9B%BE%E7%89%87.png', 'https://java-chashaobao-ai.oss-cn-beijing.aliyuncs.com/%E5%9B%BD%E5%88%9B/3%E4%B8%AD%E8%80%81%E5%B9%B4%E5%81%A5%E8%BA%AB%E6%93%8D.mp4', '["手脚灵活","喜欢音乐","想要学舞蹈基础"]', '["锻炼协调性","愉悦心情","增加社交乐趣"]', '[{"title":"基本手位","duration":5,"description":"学习新疆舞基本手型"},{"title":"动脖子","duration":8,"description":"传说中的移颈动脖子"},{"title":"简单组合","duration":7,"description":"配合脚步的简单组合"}]', '2024-04-01', '舞蹈基础', 0),
('course_004', '醉酒的蝴蝶', '热门广场舞曲目，蝴蝶般的优美舞姿，让你成为广场上的焦点。节奏欢快，动作有趣。', 'easy', 'https://java-chashaobao-ai.oss-cn-beijing.aliyuncs.com/%E5%9B%BD%E5%88%9B%E5%9B%BE%E7%89%87/%E9%86%89%E9%85%92%E7%9A%84%E8%9D%B4%E8%9D%B6%E5%9B%BE%E7%89%87.png', 'https://java-chashaobao-ai.oss-cn-beijing.aliyuncs.com/%E5%9B%BD%E5%88%9B/4%E9%86%89%E9%85%92%E7%9A%84%E8%9D%B4%E8%9D%B6.mp4', '["腿脚不便","站立困难","高血压患者"]', '["平缓血压","修身养性","改善平衡"]', '[{"title":"起势","duration":2,"description":"调整呼吸、放松身心"},{"title":"八式练习","duration":12,"description":"懒扎衣、单鞭、提手上势..."},{"title":"收势","duration":4,"description":"整理气息、缓缓收功"}]', '2024-04-10', '血压保健', 0),
('course_005', '荷塘月色', '如诗如画的荷塘月色，舞姿优雅柔美。在月光下翩翩起舞，感受宁静与美好。', 'easy', 'https://java-chashaobao-ai.oss-cn-beijing.aliyuncs.com/%E5%9B%BD%E5%88%9B%E5%9B%BE%E7%89%87/%E8%8D%B7%E5%A1%98%E6%9C%88%E8%89%B2%E5%9B%BE%E7%89%87.png', 'https://java-chashaobao-ai.oss-cn-beijing.aliyuncs.com/%E5%9B%BD%E5%88%9B/5%E8%8D%B7%E5%A1%98%E6%9C%88%E8%89%B2.mp4', '["初学者","想要社交","喜欢热闹"]', '["锻炼身体","结识朋友","跟上潮流"]', '[{"title":"基本步伐","duration":4,"description":"学会简单脚步"},{"title":"手臂动作","duration":4,"description":"跟上节奏挥手"},{"title":"完整跟跳","duration":2,"description":"配合音乐完整练习"}]', '2024-05-01', '社交舞蹈', 0),
('course_006', '健康操一分钟', '短短一分钟，快速激活全身。适合时间紧张时练习，随时随地动起来。', 'easy', 'https://java-chashaobao-ai.oss-cn-beijing.aliyuncs.com/%E5%9B%BD%E5%88%9B%E5%9B%BE%E7%89%87/%E5%81%A5%E5%BA%B7%E6%93%8D%E4%B8%80%E5%88%86%E9%92%9F%E5%9B%BE%E7%89%87.png', 'https://java-chashaobao-ai.oss-cn-beijing.aliyuncs.com/%E5%9B%BD%E5%88%9B/6%E5%81%A5%E5%BA%B7%E6%93%8D%E4%B8%80%E5%88%86%E9%92%9F.mp4', '["腰椎不适","腰间盘突出","久坐老人"]', '["缓解腰痛","加强腰部肌肉","改善姿势"]', '[{"title":"伸展运动","duration":2,"description":"轻轻伸展腰背"},{"title":"核心训练","duration":3,"description":"增强核心力量"},{"title":"整理放松","duration":1,"description":"轻轻拍打腰部"}]', '2024-05-15', '腰椎保健', 0),
('course_007', '新疆舞基础', '学习新疆舞的基本动作，热情奔放，节奏明快，感受西域风情。', 'medium', 'https://java-chashaobao-ai.oss-cn-beijing.aliyuncs.com/%E5%9B%BD%E5%88%9B%E5%9B%BE%E7%89%87/%E6%96%B0%E7%96%86%E8%88%9E%E5%9B%BE%E7%89%87.png', 'https://java-chashaobao-ai.oss-cn-beijing.aliyuncs.com/%E5%9B%BD%E5%88%9B/7%E6%96%B0%E7%96%86%E8%88%9E%E5%9F%BA%E7%A1%80.mp4', '["手脚灵活","喜欢节奏感","想要学新疆舞"]', '["锻炼协调性","增强节奏感","提升气质"]', '[{"title":"基本手型","duration":3,"description":"学习新疆舞手型"},{"title":"移颈技巧","duration":5,"description":"传说中的动脖子"},{"title":"步伐组合","duration":5,"description":"脚步与手部配合"}]', '2024-06-01', '民族舞', 0),
('course_008', '舒缓瑜伽', '轻柔的瑜伽动作，配合舒缓的音乐，帮助放松身心，缓解压力。', 'easy', 'https://java-chashaobao-ai.oss-cn-beijing.aliyuncs.com/%E5%9B%BD%E5%88%9B%E5%9B%BE%E7%89%87/%E8%88%92%E7%BC%96%E7%91%B0%E4%BC%A4%E5%9B%BE%E7%89%87.png', 'https://java-chashaobao-ai.oss-cn-beijing.aliyuncs.com/%E5%9B%BD%E5%88%9B/8%E8%88%92%E7%BC%96%E7%91%B0%E4%BC%A4.mp4', '["压力大","睡眠不好","想要放松"]', '["缓解压力","改善睡眠","放松身心"]', '[{"title":"呼吸调整","duration":3,"description":"深呼吸放松"},{"title":"简单体式","duration":8,"description":"轻柔的瑜伽体式"},{"title":"冥想休息","duration":4,"description":"全身放松"}]', '2024-06-15', '放松疗愈', 0);

-- 插入健康提醒数据
INSERT INTO health_reminders (id, type, title, content, time_slot, icon, priority, category) VALUES
('health_001', 'reminder', '该喝水啦', '王奶奶，来跳广场舞之前记得喝杯温水哦~', 'morning', 'water', 'normal', NULL),
('health_002', 'reminder', '该动一动了', '坐了两个小时了，起来活动活动筋骨吧！', 'afternoon', 'activity', 'high', NULL),
('health_003', 'reminder', '服药提醒', '王奶奶，您的降压药吃了吗？别忘了哦~', 'evening', 'pill', 'high', NULL),
('health_004', 'tip', '今日知识', '跳舞前热身5分钟，可以减少关节损伤哦！', NULL, 'lightbulb', 'normal', 'health'),
('health_005', 'tip', '饮食建议', '跳舞前不要吃太饱，半流食最容易消化~', NULL, 'apple', 'normal', 'nutrition'),
('health_006', 'tip', '心理关怀', '今天心情好吗？来跳个欢快的舞蹈吧！', NULL, 'heart', 'normal', 'mental');

-- 插入商城物品数据
INSERT INTO shop_items (id, name, description, points, image, category, stock, active) VALUES
('item_001', '养生茶礼包', '精选枸杞菊花茶，适合日常养生饮用', 100, 'https://picsum.photos/seed/shop1/400/300', '养生', 50, TRUE),
('item_002', '太极扇', '精美太极扇，适合太极练习使用', 200, 'https://picsum.photos/seed/shop2/400/300', '运动', 30, TRUE),
('item_003', '广场舞音响', '便携式蓝牙音响，广场舞必备神器', 500, 'https://picsum.photos/seed/shop3/400/300', '运动', 20, TRUE),
('item_004', '健康食谱电子书', '老年人专属健康食谱，营养均衡搭配', 50, 'https://picsum.photos/seed/shop4/400/300', '养生', 999, TRUE),
('item_005', '舞蹈练功服', '舒适透气练功服，跳舞更自在', 300, 'https://picsum.photos/seed/shop5/400/300', '运动', 40, TRUE),
('item_006', '按摩锤', '经络按摩锤，缓解身体疲劳', 150, 'https://picsum.photos/seed/shop6/400/300', '养生', 60, TRUE),
('item_007', '舞蹈扇子', '精美舞蹈扇子，翩翩起舞更优美', 180, 'https://picsum.photos/seed/shop7/400/300', '运动', 35, TRUE),
('item_008', '健康手环', '记录每日步数和心率，关注健康', 400, 'https://picsum.photos/seed/shop8/400/300', '科技', 25, TRUE);

COMMIT;