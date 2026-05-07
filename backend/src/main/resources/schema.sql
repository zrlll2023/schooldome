CREATE DATABASE IF NOT EXISTS yucai_road DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE yucai_road;

CREATE TABLE sys_user (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码(BCrypt加密)',
    nickname VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    avatar VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

CREATE TABLE game_save (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '存档ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    save_name VARCHAR(100) NOT NULL COMMENT '存档名称',
    current_year INT DEFAULT 1 COMMENT '当前年份',
    current_month INT DEFAULT 9 COMMENT '当前月份(1-12)',
    game_state JSON DEFAULT NULL COMMENT '游戏状态JSON',
    is_active TINYINT DEFAULT 0 COMMENT '是否为当前激活存档(0-否,1-是)',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    CONSTRAINT fk_save_user FOREIGN KEY (user_id) REFERENCES sys_user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='游戏存档表';

CREATE TABLE school (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '学校ID',
    save_id BIGINT NOT NULL COMMENT '存档ID',
    name VARCHAR(100) NOT NULL COMMENT '学校名称',
    type ENUM('PRIMARY','JUNIOR','SENIOR') NOT NULL COMMENT '学校类型(小学/初中/高中)',
    level VARCHAR(20) DEFAULT NULL COMMENT '学校等级',
    reputation INT DEFAULT 0 COMMENT '声誉值',
    funds DECIMAL(15,2) DEFAULT 0.00 COMMENT '资金',
    student_count INT DEFAULT 0 COMMENT '学生数量',
    teacher_count INT DEFAULT 0 COMMENT '教师数量',
    teaching_policy JSON DEFAULT NULL COMMENT '教学策略JSON',
    exam_style VARCHAR(20) DEFAULT NULL COMMENT '考试风格',
    total_years INT DEFAULT 0 COMMENT '办学总年数',
    achievements JSON DEFAULT NULL COMMENT '成就列表JSON',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_save_id (save_id),
    CONSTRAINT fk_school_save FOREIGN KEY (save_id) REFERENCES game_save(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学校表';

CREATE TABLE student (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '学生ID',
    school_id BIGINT NOT NULL COMMENT '学校ID',
    name VARCHAR(50) NOT NULL COMMENT '学生姓名',
    grade VARCHAR(20) DEFAULT NULL COMMENT '年级',
    grade_level ENUM('S','A','B','C','D') DEFAULT 'C' COMMENT '成绩等级',
    academic_score DECIMAL(5,2) DEFAULT 60.00 COMMENT '学业分数',
    quality_score DECIMAL(5,2) DEFAULT 60.00 COMMENT '素质分数',
    health_score DECIMAL(5,2) DEFAULT 60.00 COMMENT '健康分数',
    is_k12_student TINYINT DEFAULT 1 COMMENT '是否K12生源(0-否,1-是)',
    from_school_id BIGINT DEFAULT NULL COMMENT '来源学校ID',
    enrolled_year INT DEFAULT NULL COMMENT '入学年份',
    status ENUM('在校','毕业','转学','劝退') DEFAULT '在校' COMMENT '学生状态',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_school_id (school_id),
    CONSTRAINT fk_student_school FOREIGN KEY (school_id) REFERENCES school(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生表';

CREATE TABLE teacher (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '教师ID',
    school_id BIGINT NOT NULL COMMENT '学校ID',
    name VARCHAR(50) NOT NULL COMMENT '教师姓名',
    level ENUM('特级','高级','一级','二级','初级') DEFAULT '初级' COMMENT '教师职称等级',
    teaching_ability INT DEFAULT 50 COMMENT '教学能力(0-100)',
    moral_level INT DEFAULT 50 COMMENT '师德水平(0-100)',
    specialty ENUM('文科','理科','综合') DEFAULT '综合' COMMENT '学科特长',
    salary DECIMAL(10,2) DEFAULT 0.00 COMMENT '月薪',
    experience INT DEFAULT 0 COMMENT '教龄(年)',
    hire_year INT DEFAULT NULL COMMENT '入职年份',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_school_id (school_id),
    CONSTRAINT fk_teacher_school FOREIGN KEY (school_id) REFERENCES school(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教师表';

CREATE TABLE building (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '建筑ID',
    school_id BIGINT NOT NULL COMMENT '学校ID',
    type ENUM('教学楼','宿舍楼','图书馆','体育馆','实验楼') NOT NULL COMMENT '建筑类型',
    level INT DEFAULT 1 COMMENT '建筑等级',
    capacity INT DEFAULT 0 COMMENT '容纳人数',
    monthly_cost DECIMAL(10,2) DEFAULT 0.00 COMMENT '月维护费用',
    build_cost DECIMAL(15,2) DEFAULT 0.00 COMMENT '建造费用',
    status ENUM('建设中','运营中','拆除') DEFAULT '建设中' COMMENT '建筑状态',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_school_id (school_id),
    CONSTRAINT fk_building_school FOREIGN KEY (school_id) REFERENCES school(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='建筑表';

CREATE TABLE branch_school (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '分校ID',
    save_id BIGINT NOT NULL COMMENT '存档ID',
    name VARCHAR(100) NOT NULL COMMENT '分校名称',
    city_type ENUM('一线','二线','三线','县城') NOT NULL COMMENT '城市类型',
    management_mode ENUM('直营','委托','授权') DEFAULT '直营' COMMENT '管理模式',
    reputation INT DEFAULT 0 COMMENT '声誉值',
    student_count INT DEFAULT 0 COMMENT '学生数量',
    annual_profit DECIMAL(15,2) DEFAULT 0.00 COMMENT '年利润',
    quality_rating INT DEFAULT 0 COMMENT '教学质量评级(0-100)',
    established_year INT DEFAULT NULL COMMENT '建校年份',
    status ENUM('建设中','运营中','关闭') DEFAULT '建设中' COMMENT '分校状态',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_save_id (save_id),
    CONSTRAINT fk_branch_save FOREIGN KEY (save_id) REFERENCES game_save(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分校表';

CREATE TABLE event_log (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '事件记录ID',
    save_id BIGINT NOT NULL COMMENT '存档ID',
    event_type ENUM('随机事件','政策事件','考试事件','招生事件','财务事件','人事事件','建筑事件','校友事件') NOT NULL COMMENT '事件类型',
    event_title VARCHAR(200) NOT NULL COMMENT '事件标题',
    event_description TEXT DEFAULT NULL COMMENT '事件描述',
    choices JSON DEFAULT NULL COMMENT '选项JSON',
    player_choice INT DEFAULT NULL COMMENT '玩家选择(选项序号)',
    result JSON DEFAULT NULL COMMENT '结果JSON',
    trigger_year INT NOT NULL COMMENT '触发年份',
    trigger_month INT NOT NULL COMMENT '触发月份(1-12)',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_save_id (save_id),
    CONSTRAINT fk_event_save FOREIGN KEY (save_id) REFERENCES game_save(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='事件记录表';

CREATE TABLE exam_record (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '考试记录ID',
    school_id BIGINT NOT NULL COMMENT '学校ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    exam_type ENUM('月考','期末','小升初','中考','高考') NOT NULL COMMENT '考试类型',
    score DECIMAL(5,2) DEFAULT 0.00 COMMENT '考试成绩',
    rank INT DEFAULT NULL COMMENT '排名',
    exam_year INT NOT NULL COMMENT '考试年份',
    is_top_scholar TINYINT DEFAULT 0 COMMENT '是否状元(0-否,1-是)',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_school_id (school_id),
    KEY idx_student_id (student_id),
    CONSTRAINT fk_exam_school FOREIGN KEY (school_id) REFERENCES school(id),
    CONSTRAINT fk_exam_student FOREIGN KEY (student_id) REFERENCES student(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考试记录表';

CREATE TABLE alumni (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '校友ID',
    save_id BIGINT NOT NULL COMMENT '存档ID',
    student_name VARCHAR(50) NOT NULL COMMENT '校友姓名',
    graduation_year INT NOT NULL COMMENT '毕业年份',
    graduation_school VARCHAR(100) DEFAULT NULL COMMENT '毕业院校',
    achievement_type ENUM('名校','名企','科研','从政','公益') NOT NULL COMMENT '成就类型',
    achievement_level ENUM('普通','杰出','卓越','传奇') DEFAULT '普通' COMMENT '成就等级',
    donation_amount DECIMAL(15,2) DEFAULT 0.00 COMMENT '捐赠金额',
    reputation_contribution INT DEFAULT 0 COMMENT '声誉贡献值',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_save_id (save_id),
    CONSTRAINT fk_alumni_save FOREIGN KEY (save_id) REFERENCES game_save(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='校友表';
