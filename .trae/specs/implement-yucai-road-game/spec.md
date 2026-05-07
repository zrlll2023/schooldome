# 《育才之路》游戏开发 Spec

## Why
基于PRD文档实现《育才之路》文字类校园模拟经营Web游戏,提供完整的前后端功能,让玩家体验教育集团创始人的角色,从高中起步逐步建立K12教育体系。

## What Changes
- 创建完整的前后端项目结构(Vue 3 + Spring Boot)
- 实现6大核心系统：招生与升学、教学管理、校园建设、事件叙事、K12一体化、分校集团
- 建立完整的数据库设计和API接口
- 实现游戏核心引擎(时间推进、状态管理、数值计算)
- **BREAKING**: 这是一个全新项目,无现有代码

## Impact
- Affected specs: 无(新项目)
- Affected code: 全新代码库

## ADDED Requirements

### Requirement: 项目基础架构
The system SHALL provide a complete project structure with Vue 3 frontend and Spring Boot backend.

#### Scenario: 项目初始化成功
- **WHEN** 开发环境准备完成
- **THEN** 前端项目可运行在 http://localhost:5173
- **AND** 后端项目可运行在 http://localhost:8080
- **AND** 数据库连接正常

### Requirement: 用户认证系统
The system SHALL provide user registration and JWT-based authentication.

#### Scenario: 用户注册登录
- **WHEN** 用户填写用户名密码进行注册
- **THEN** 系统创建用户并返回JWT token
- **AND** 后续请求使用token进行认证

### Requirement: 存档管理系统
The system SHALL support game save/load functionality with multiple save slots.

#### Scenario: 存档操作
- **WHEN** 用户创建/加载/删除存档
- **THEN** 存档数据正确持久化到数据库
- **AND** 支持最多10个存档位

### Requirement: 游戏核心引擎
The system SHALL provide game time advancement and state management.

#### Scenario: 时间推进
- **WHEN** 玩家点击"下个月"
- **THEN** 游戏时间推进一个月
- **AND** 触发月度结算、事件生成、数值更新
- **AND** 支持加速机制(1x/2x/5x/10x)

### Requirement: 招生与升学系统
The system SHALL implement enrollment and examination mechanics.

#### Scenario: 招生执行
- **WHEN** 玩家设置招生政策并执行招生
- **THEN** 根据声望、学费档位、招生标准计算生源质量
- **AND** 生成对应等级的学生(S/A/B/C/D)

#### Scenario: 考试结算
- **WHEN** 到达考试时间点(月考/期末/中考/高考)
- **THEN** 计算学生成绩并影响学校声望
- **AND** 生成升学结果

### Requirement: 教学管理系统
The system SHALL provide teaching policy configuration and teacher/student management.

#### Scenario: 教学政策设定
- **WHEN** 玩家调整教学风格、作业量等5个维度
- **THEN** 政策影响学生成绩成长、压力值、素质发展

#### Scenario: 教师招聘培养
- **WHEN** 玩家花费资金招聘或培养教师
- **THEN** 教师队伍更新,教学能力影响学生成绩

### Requirement: 校园建设系统
The system SHALL allow building construction and campus expansion.

#### Scenario: 建筑建造升级
- **WHEN** 玩家选择建筑类型和等级进行建设
- **THEN** 扣除相应资金,建筑进入建设中状态
- **AND** 建成后提升学生容量或提供加成效果

### Requirement: 经济系统
The system SHALL implement income/expense calculation and financial balance.

#### Scenario: 月度经济结算
- **WHEN** 每月结算时
- **THEN** 计算学费收入、政府拨款、工资支出、维护费用
- **AND** 更新学校资金余额

### Requirement: 事件与叙事系统
The system SHALL generate dynamic events based on game state.

#### Scenario: 事件触发处理
- **WHEN** 游戏时间推进触发事件
- **THEN** 显示事件弹窗并提供选项
- **AND** 玩家选择后按概率加权计算结果

### Requirement: K12一体化系统
The system SHALL support multi-stage education (primary/junior/senior high).

#### Scenario: 学段建设与输送
- **WHEN** 玩家达到声望要求并建设新学段
- **THEN** 解锁K12人才输送链
- **AND** 直升学生获得适应期和成绩加成

### Requirement: 分校与集团系统
The system SHALL allow branch school opening and education group management.

#### Scenario: 分校开设运营
- **WHEN** 玩家满足条件开设分校
- **THEN** 分校独立运营并向总校上缴利润
- **AND** 可选择不同管理模式

### Requirement: 总览界面
The system SHALL display school status dashboard with key metrics.

#### Scenario: 查看总览
- **WHEN** 玩家进入总览页面
- **THEN** 显示声望、资金、学生数、教师数、学业均分等核心指标
- **AND** 显示学年进度和近期事件

## MODIFIED Requirements
无(新项目)

## REMOVED Requirements
无(新项目)
