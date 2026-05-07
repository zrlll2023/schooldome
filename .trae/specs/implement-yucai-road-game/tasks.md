# Tasks

## 阶段一：基础架构搭建 (M1)

- [x] Task 1: 初始化后端Spring Boot项目
  - [x] 创建Spring Boot 3.2+项目结构
  - [x] 配置pom.xml依赖(Spring Security, MyBatis-Plus, JWT, Redis等)
  - [x] 配置application.yml(数据库、端口、JWT等)
  - [x] 创建全局异常处理和统一响应格式
  - [x] 配置CORS跨域
  - **验证**: 项目可启动,访问http://localhost:8080返回正常响应

- [x] Task 2: 设计并创建数据库表结构
  - [x] 创建sys_user用户表
  - [x] 创建game_save存档表
  - [x] 创建school学校表
  - [x] 创建student学生表
  - [x] create teacher教师表
  - [x] create building建筑表
  - [x] create branch_school分校表
  - [x] create event_log事件记录表
  - [x] create exam_record考试记录表
  - [x] create alumni校友表
  - **验证**: 所有表创建成功,可通过数据库客户端查看

- [x] Task 3: 实现用户认证系统(注册/登录/JWT)
  - [x] 实现User实体类和Mapper
  - [x] 实现注册接口POST /api/v1/auth/register
  - [x] 实现登录接口POST /api/v1/auth/login(JWT生成)
  - [x] 实现JWT过滤器拦截请求
  - [x] 实现获取用户信息接口GET /api/v1/user/profile
  - **验证**: 可成功注册用户,登录获取token,使用token访问受保护接口

- [x] Task 4: 实现存档管理系统
  - [x] 实现Save实体类和Mapper
  - [x] 实现创建存档接口POST /api/v1/save/new
  - [x] 实现获取存档列表GET /api/v1/save/list
  - [x] 实现加载存档GET /api/v1/save/{id}
  - [x] 实现保存存档PUT /api/v1/save/{id}
  - [x] 实现删除存档DELETE /api/v1/save/{id}
  - **验证**: 可创建/加载/保存/删除存档,数据持久化正确

- [x] Task 5: 实现游戏核心引擎骨架
  - [x] 实现GameEngine服务类(时间推进逻辑)
  - [x] 实现advanceMonth()推进一个月
  - [x] 实现获取游戏状态GET /api/v1/game/state
  - [x] 实现推进月份POST /api/v1/game/advance-month
  - [x] 实现设置加速倍率POST /api/v1/game/set-speed
  - [x] 实现暂停功能POST /api/v1/game/pause
  - **验证**: 可推进时间,游戏状态正确更新

- [x] Task 6: 初始化前端Vue 3项目
  - [x] 使用Vite创建Vue 3项目
  - [x] 安装依赖(Pinia, Vue Router, Element Plus, Axios等)
  - [x] 配置项目目录结构(pages, components, stores, api, utils)
  - [x] 配置Axios拦截器(请求/响应拦截,token注入)
  - [x] 配置路由(总览/招生/教学/校园/事件/K12/分校/校史/设置)
  - [x] 创建主布局组件(顶部导航栏+侧边导航+主内容区+底部操作栏)
  - **验证**: 前端项目可运行,页面布局正常显示

## 阶段二：核心系统开发 (M2)

- [x] Task 7: 实现招生与升学系统后端
  - [x] 实现招生政策配置PUT /api/v1/enrollment/policy
  - [x] 实现生源质量预览GET /api/v1/enrollment/preview
  - [x] 实现执行招生POST /api/v1/enrollment/execute(含生源质量计算公式)
  - [x] 实现内部直升机制(小学→初中→高中)
  - **验证**: 招生政策可配置,执行招生后生成对应质量的学生

- [x] Task 8: 实现教学管理系统后端
  - [x] 实现教学政策CRUD GET/PUT /api/v1/teaching/policy
  - [x] 实现教师列表GET /api/v1/teaching/teachers
  - [x] 实现教师招聘POST /api/v1/teaching/teachers/hire(常规/定向猎头)
  - [x] 实现教师培养POST /api/v1/teaching/teachers/{id}/train
  - [x] 实现教师解聘POST /api/v1/teaching/teachers/{id}/dismiss
  - [x] 实现学生列表GET /api/v1/teaching/students
  - [x] 实现重点关注学生POST /api/v1/teaching/students/{id}/focus
  - [x] 实现教学效果预测GET /api/v1/teaching/prediction
  - **验证**: 教学政策可调整,教师招聘培养正常,教学预测数据准确

- [x] Task 9: 实现校园建设系统后端
  - [x] 实现建筑列表GET /api/v1/campus/buildings
  - [x] 实现新建建筑POST /api/v1/campus/buildings
  - [x] 实现升级建筑PUT /api/v1/campus/buildings/{id}/upgrade
  - [x] 实现拆除建筑DELETE /api/v1/campus/buildings/{id}
  - [x] 实现校园扩建POST /api/v1/campus/expand
  - **验证**: 建筑可建造/升级/拆除,校园面积可扩建

- [x] Task 10: 实现经济系统和数值引擎
  - [x] 实现月度收入计算(学费/政府拨款/校友捐赠等)
  - [x] 实现月度支出计算(工资/补贴/维护费等)
  - [x] 实现FormulaEngine数值计算服务
  - [x] 实现成绩计算公式(calculateScore)
  - [x] 实现声望计算公式(calculateReputation)
  - [x] 实现财务状况计算(calculateFinance)
  - **验证**: 月度结算时收支计算正确,资金余额更新准确

- [x] Task 11: 实现总览页面前端
  - [x] 创建总览页面组件
  - [x] 显示核心指标卡片(声望/资金/学生数/教师数)
  - [x] 显示学业/素质/身心均分指标
  - [x] 显示学年进度条
  - [x] 显示近期事件列表
  - [x] 显示当前目标提示
  - [x] 实现底部操作栏(下个月/暂停/设置按钮)
  - **验证**: 总览页面正确展示所有游戏状态信息

## 阶段三：核心系统完善 (M3)

- [x] Task 12: 实现考试系统
  - [x] 实现月考逻辑和结果计算
  - [x] 实现期末考逻辑和结果计算
  - [x] 实现中考/高考逻辑和结果计算
  - [x] 实现小升初逻辑和结果计算
  - [x] 实现升学结果处理(直升/对外升学)
  - [x] 实现考试成绩影响声望
  - [x] 实现获取考试结果GET /api/v1/exam/results
  - [x] 实现获取历史成绩GET /api/v1/exam/history
  - [x] 实现获取排名GET /api/v1/exam/ranking
  - **验证**: 考试时间点自动触发,成绩计算正确,升学流程完整

- [x] Task 13: 完善学生管理和事件引擎v1
  - [x] 实现学生三维属性更新逻辑
  - [x] 实现纪律处分功能
  - [x] 实现劝退转学功能
  - [x] 实现EventEngine事件引擎服务
  - [x] 实现基础事件池(学生事件/教师事件/校园事件)
  - [x] 实现动态事件生成算法
  - [x] 实现概率加权的事件结果计算
  - [x] 实现获取活跃事件GET /api/v1/events/active
  - [x] 实现处理事件POST /api/v1/events/{id}/resolve
  - [x] 实现获取历史事件GET /api/v1/events/history
  - **验证**: 学生管理操作正常,事件按条件触发,选择结果符合概率

- [x] Task 14: 实现主要功能页面前端(招生/教学/校园)
  - [x] 创建招生管理页面(招生政策设置/生源预览/执行招生)
  - [x] 创建教学管理页面(教学政策/教师团队/教学效果预测)
  - [x] 创建校园建设页面(建筑列表/新建/升级/拆除)
  - [x] 实现事件弹窗组件(显示事件描述/选项/成功率/结果)
  - **验证**: 各页面功能完整,交互流畅,数据展示正确

## 阶段四：高级系统开发 (M4-M5)

- [ ] Task 15: 实现K12一体化系统
  - [ ] 实现K12体系状态查询GET /api/v1/k12/status
  - [ ] 实现建设新学段POST /api/v1/k12/build(初中/小学)
  - [ ] 实现人才输送链数据GET /api/v1/k12/pipeline
  - [ ] 实现协同效应计算GET /api/v1/k12/synergy
  - [ ] 实现直升机制(直升率计算/适应期加成/成绩加成)
  - [ ] 实现K12全程培养标记和高考额外加成
  - [ ] 创建K12管理页面前端(学段状态/输送数据/协同效应)
  - **验证**: K12学段可建设,输送链数据准确,协同效应生效

- [ ] Task 16: 实现分校与集团系统
  - [ ] 实现分校列表GET /api/v1/branch/list
  - [ ] 实现开设分校POST /api/v1/branch/open(选址/城市特点)
  - [ ] 实现分校详情GET /api/v1/branch/{id}
  - [ ] 实现更换管理模式PUT /api/v1/branch/{id}/mode
  - [ ] 实现关闭分校DELETE /api/v1/branch/{id}
  - [ ] 实现集团状态查询GET /api/v1/group/status
  - [ ] 实现分校独立运营AI逻辑
  - [ ] 实现利润上缴机制
  - [ ] 创建分校管理页面前端(分校列表/开设/详情/集团状态)
  - **验证**: 分校可开设运营,管理模式可切换,集团等级提升

- [ ] Task 17: 实现校史系统和多结局判定
  - [ ] 实现时间线记录GET /api/v1/history/timeline
  - [ ] 实现校友录管理GET /api/v1/history/alumni
  - [ ] 实现年鉴定成GET /api/v1/history/yearbook/{year}
  - [ ] 实现成就系统GET /api/v1/history/achievements
  - [ ] 实现多结局判定逻辑(教育家/商业大亨/学术泰斗/百年名校/教育改革者/平凡校长)
  - [ ] 创建校史页面前端(时间线/校友录/年鉴/成就)
  - **验证**: 校史数据记录完整,结局判定条件准确

## 阶段五：集成优化 (M6)

- [ ] Task 18: 系统集成测试和优化
  - [ ] 测试完整的游戏循环(招生→教学→考试→结算→扩建)
  - [ ] 测试K12一体化流程(建设学段→人才输送→协同效应)
  - [ ] 测试分校集团流程(开设分校→运营管理→集团升级)
  - [ ] 修复发现的bug
  - [ ] 优化API响应性能
  - [ ] 优化前端页面加载速度
  - **验证**: 核心流程可完整运行,无明显bug,性能达标

# Task Dependencies
- [Task 2] depends on [Task 1]
- [Task 3] depends on [Task 2]
- [Task 4] depends on [Task 2]
- [Task 5] depends on [Task 4]
- [Task 6] depends on [Task 1]
- [Task 7] depends on [Task 5]
- [Task 8] depends on [Task 5]
- [Task 9] depends on [Task 5]
- [Task 10] depends on [Task 7, Task 8, Task 9]
- [Task 11] depends on [Task 6, Task 10]
- [Task 12] depends on [Task 8]
- [Task 13] depends on [Task 8, Task 12]
- [Task 14] depends on [Task 11, Task 13]
- [Task 15] depends on [Task 14]
- [Task 16] depends on [Task 14]
- [Task 17] depends on [Task 16]
- [Task 18] depends on [Task 15, Task 16, Task 17]
