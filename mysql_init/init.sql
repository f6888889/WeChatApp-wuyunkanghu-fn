CREATE DATABASE IF NOT EXISTS yinwu_muxin DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE yinwu_muxin;

CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(50) PRIMARY KEY,
    open_id VARCHAR(100),
    nickname VARCHAR(100),
    avatar VARCHAR(500),
    gender VARCHAR(10),
    age INT,
    phone VARCHAR(20),
    created_at VARCHAR(50),
    points INT DEFAULT 0,
    check_in_continuous_days INT DEFAULT 0,
    last_check_in_date VARCHAR(20),
    health_profile JSON,
    learning_progress JSON,
    favorites JSON,
    recent_searches JSON,
    learning_history JSON,
    friends JSON,
    redeemed_items JSON
);

CREATE TABLE IF NOT EXISTS courses (
    id VARCHAR(50) PRIMARY KEY,
    title VARCHAR(200),
    description TEXT,
    difficulty VARCHAR(20),
    cover_image VARCHAR(500),
    video_url VARCHAR(500),
    suitable_for JSON,
    benefits JSON,
    steps JSON,
    created_at VARCHAR(50),
    category VARCHAR(100),
    like_count INT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS health_reminders (
    id VARCHAR(50) PRIMARY KEY,
    type VARCHAR(20),
    title VARCHAR(200),
    content TEXT,
    icon VARCHAR(50),
    time_slot VARCHAR(20)
);

INSERT INTO courses (id, title, description, difficulty, cover_image, video_url, suitable_for, benefits, steps, created_at, category, like_count) VALUES
('course_001', '山水情歌', '在优美的山水情歌中翩翩起舞，动作舒缓流畅，仿佛置身大自然。适合喜欢抒情曲风的舞者。', 'easy', 'https://java-chashaobao-ai.oss-cn-beijing.aliyuncs.com/%E5%9B%BD%E5%88%9B%E5%9B%BE%E7%89%87/%E5%B1%B1%E6%B0%B4%E6%83%85%E6%AD%8C%E5%9B%BE%E7%89%87.png', 'https://java-chashaobao-ai.oss-cn-beijing.aliyuncs.com/%E5%9B%BD%E5%88%9B/1%E5%B1%B1%E6%B0%B4%E6%83%85%E6%AD%8C.mp4', '["膝盖不适", "关节炎", "术后恢复"]', '["保护膝盖", "增加润滑", "减少疼痛"]', '[{"title": "热身准备", "duration": 2, "description": "轻轻活动关节"}, {"title": "基础动作", "duration": 8, "description": "缓慢抬腿、轻柔摆动"}, {"title": "放松整理", "duration": 5, "description": "深呼吸、轻轻拍打腿部"}]', '2024-03-15', '膝盖保健', 0),
('course_002', '美丽的草原我的家', '跟随悠扬的草原旋律，舒展双臂，仿佛置身辽阔草原。动作优美大方，提升气质。', 'medium', 'https://java-chashaobao-ai.oss-cn-beijing.aliyuncs.com/%E5%9B%BD%E5%88%9B%E5%9B%BE%E7%89%87/%E7%BE%8E%E4%B8%BD%E7%9A%84%E8%8D%89%E5%8E%9F%E6%88%91%E7%9A%84%E5%AE%B6%E5%9B%BE%E7%89%87.png', 'https://java-chashaobao-ai.oss-cn-beijing.aliyuncs.com/%E5%9B%BD%E5%88%9B/2%E7%BE%8E%E4%B8%BD%E7%9A%84%E8%8D%89%E5%8E%9F%E6%88%91%E7%9A%84%E5%AE%B6.mp4', '["肩颈酸痛", "颈椎不适", "长期低头"]', '["缓解酸痛", "增加灵活度", "改善血液循环"]', '[{"title": "颈部运动", "duration": 3, "description": "缓慢转头、点头"}, {"title": "肩部运动", "duration": 5, "description": "耸肩、绕肩、画圈"}, {"title": "整理放松", "duration": 4, "description": "深呼吸、轻轻摇晃"}]', '2024-03-18', '肩颈保健', 0),
('course_003', '中老年健身操', '经典健身操，动作简单易学，配合节奏明快的音乐，强身健体，活力满满。', 'medium', 'https://java-chashaobao-ai.oss-cn-beijing.aliyuncs.com/%E5%9B%BD%E5%88%9B%E5%9B%BE%E7%89%87/%E4%B8%AD%E8%80%81%E5%B9%B4%E5%81%A5%E8%BA%AB%E6%93%8D%E5%9B%BE%E7%89%87.png', 'https://java-chashaobao-ai.oss-cn-beijing.aliyuncs.com/%E5%9B%BD%E5%88%9B/3%E4%B8%AD%E8%80%81%E5%B9%B4%E5%81%A5%E8%BA%AB%E6%93%8D.mp4', '["手脚灵活", "喜欢音乐", "想要学舞蹈基础"]', '["锻炼协调性", "愉悦心情", "增加社交乐趣"]', '[{"title": "基本手位", "duration": 5, "description": "学习新疆舞基本手型"}, {"title": "动脖子", "duration": 8, "description": "传说中的移颈动脖子"}, {"title": "简单组合", "duration": 7, "description": "配合脚步的简单组合"}]', '2024-04-01', '舞蹈基础', 0),
('course_004', '醉酒的蝴蝶', '热门广场舞曲目，蝴蝶般的优美舞姿，让你成为广场上的焦点。节奏欢快，动作有趣。', 'easy', 'https://java-chashaobao-ai.oss-cn-beijing.aliyuncs.com/%E5%9B%BD%E5%88%9B%E5%9B%BE%E7%89%87/%E9%86%89%E9%85%92%E7%9A%84%E8%9D%B4%E8%9D%B6%E5%9B%BE%E7%89%87.png', 'https://java-chashaobao-ai.oss-cn-beijing.aliyuncs.com/%E5%9B%BD%E5%88%9B/4%E9%86%89%E9%85%92%E7%9A%84%E8%9D%B4%E8%9D%B6.mp4', '["腿脚不便", "站立困难", "高血压患者"]', '["平缓血压", "修身养性", "改善平衡"]', '[{"title": "起势", "duration": 2, "description": "调整呼吸、放松身心"}, {"title": "八式练习", "duration": 12, "description": "懒扎衣、单鞭、提手上势..."}, {"title": "收势", "duration": 4, "description": "整理气息、缓缓收功"}]', '2024-04-10', '血压保健', 0),
('course_005', '荷塘月色', '如诗如画的荷塘月色，舞姿优雅柔美。在月光下翩翩起舞，感受宁静与美好。', 'easy', 'https://java-chashaobao-ai.oss-cn-beijing.aliyuncs.com/%E5%9B%BD%E5%88%9B%E5%9B%BE%E7%89%87/%E8%8D%B7%E5%A1%98%E6%9C%88%E8%89%B2%E5%9B%BE%E7%89%87.png', 'https://java-chashaobao-ai.oss-cn-beijing.aliyuncs.com/%E5%9B%BD%E5%88%9B/5%E8%8D%B7%E5%A1%98%E6%9C%88%E8%89%B2.mp4', '["初学者", "想要社交", "喜欢热闹"]', '["锻炼身体", "结识朋友", "跟上潮流"]', '[{"title": "基本步伐", "duration": 4, "description": "学会简单脚步"}, {"title": "手臂动作", "duration": 4, "description": "跟上节奏挥手"}, {"title": "完整跟跳", "duration": 2, "description": "配合音乐完整练习"}]', '2024-05-01', '社交舞蹈', 0),
('course_006', '健康操一分钟', '短短一分钟，快速激活全身。适合时间紧张时练习，随时随地动起来。', 'easy', 'https://java-chashaobao-ai.oss-cn-beijing.aliyuncs.com/%E5%9B%BD%E5%88%9B%E5%9B%BE%E7%89%87/%E5%81%A5%E5%BA%B7%E6%93%8D%E4%B8%80%E5%88%86%E9%92%9F%E5%9B%BE%E7%89%87.png', 'https://java-chashaobao-ai.oss-cn-beijing.aliyuncs.com/%E5%9B%BD%E5%88%9B/6%E5%81%A5%E5%BA%B7%E6%93%8D%E4%B8%80%E5%88%86%E9%92%9F.mp4', '["腰椎不适", "腰间盘突出", "久坐老人"]', '["缓解腰痛", "加强腰部肌肉", "改善姿势"]', '[{"title": "热身", "duration": 1, "description": "轻轻转动腰部"}, {"title": "核心训练", "duration": 1, "description": "简单核心动作"}]', '2024-05-10', '腰部保健', 0);

INSERT INTO health_reminders (id, type, title, content, icon, time_slot) VALUES
('reminder_001', 'reminder', '起床喝水', '早上起床后喝一杯温水，补充睡眠时流失的水分', 'water', 'morning'),
('reminder_002', 'reminder', '晨间运动', '适合进行轻柔的舞蹈热身活动，唤醒身体', 'activity', 'morning'),
('reminder_003', 'reminder', '午休伸展', '午餐后进行简单的肩颈放松动作', 'activity', 'afternoon'),
('reminder_004', 'reminder', '傍晚散步', '适合进行户外散步或轻度舞蹈活动', 'activity', 'afternoon'),
('reminder_005', 'reminder', '睡前放松', '进行轻柔的拉伸放松，帮助入睡', 'pill', 'evening'),
('tip_001', 'tip', '舞蹈运动小贴士', '跳舞时注意补充水分，每20分钟喝一次水', 'lightbulb', 'all'),
('tip_002', 'tip', '关节保护', '有关节问题的用户建议选择舒缓类舞蹈课程', 'heart', 'all'),
('tip_003', 'tip', '正确姿势', '保持正确舞姿可以更好保护腰椎和颈椎', 'apple', 'all');