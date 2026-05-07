package com.yucairoad.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yucairoad.common.BusinessException;
import com.yucairoad.dto.*;
import com.yucairoad.entity.EventLog;
import com.yucairoad.entity.School;
import com.yucairoad.entity.Student;
import com.yucairoad.entity.Teacher;
import com.yucairoad.mapper.EventLogMapper;
import com.yucairoad.mapper.SchoolMapper;
import com.yucairoad.mapper.StudentMapper;
import com.yucairoad.mapper.TeacherMapper;
import com.yucairoad.service.EventEngineService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class EventEngineServiceImpl implements EventEngineService {

    private final EventLogMapper eventLogMapper;
    private final SchoolMapper schoolMapper;
    private final StudentMapper studentMapper;
    private final TeacherMapper teacherMapper;
    private final ObjectMapper objectMapper;
    private final List<EventTemplate> templateLibrary;

    public EventEngineServiceImpl(EventLogMapper eventLogMapper,
                                  SchoolMapper schoolMapper,
                                  StudentMapper studentMapper,
                                  TeacherMapper teacherMapper) {
        this.eventLogMapper = eventLogMapper;
        this.schoolMapper = schoolMapper;
        this.studentMapper = studentMapper;
        this.teacherMapper = teacherMapper;
        this.objectMapper = new ObjectMapper();
        this.templateLibrary = initializeTemplateLibrary();
    }

    @Override
    public List<EventDTO> generateMonthlyEvents(Long saveId) {
        School school = getSchoolBySaveId(saveId);
        if (school == null) {
            return new ArrayList<>();
        }

        int schoolLevel = calculateSchoolLevel(school);
        int poolSize = 2 + schoolLevel / 3;
        poolSize = Math.min(poolSize, 6);

        Map<String, List<String>> candidatePool = generateCandidatePool(school, saveId);

        List<EventDTO> generatedEvents = new ArrayList<>();
        Random random = new Random(System.currentTimeMillis());

        for (Map.Entry<String, List<String>> entry : candidatePool.entrySet()) {
            List<String> candidates = entry.getValue();
            for (String candidate : candidates) {
                EventTemplate template = findTemplateByTitle(candidate);
                if (template != null && random.nextDouble() < 0.5 && generatedEvents.size() < Math.min(2, poolSize)) {
                    EventDTO eventDTO = createEventFromTemplate(template, saveId);
                    generatedEvents.add(eventDTO);
                    saveEventToDatabase(eventDTO, saveId);
                }
            }
            if (generatedEvents.size() >= Math.min(2, poolSize)) {
                break;
            }
        }

        checkAndGenerateUrgentEvents(school, saveId, generatedEvents);

        return generatedEvents;
    }

    @Override
    public List<EventDTO> getActiveEvents(Long saveId) {
        LambdaQueryWrapper<EventLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EventLog::getSaveId, saveId)
               .isNull(EventLog::getPlayerChoice)
               .orderByDesc(EventLog::getCreatedAt);
        List<EventLog> pendingEvents = eventLogMapper.selectList(wrapper);

        List<EventDTO> result = new ArrayList<>();
        for (EventLog log : pendingEvents) {
            result.add(convertToEventDTO(log));
        }
        return result;
    }

    @Override
    public EventResultDTO processEventChoice(Long eventId, int choiceId, Long saveId) {
        EventLog eventLog = eventLogMapper.selectById(eventId);
        if (eventLog == null) {
            throw new BusinessException("事件不存在");
        }
        if (!eventLog.getSaveId().equals(saveId)) {
            throw new BusinessException("无权操作此事件");
        }
        if (eventLog.getPlayerChoice() != null) {
            throw new BusinessException("该事件已处理");
        }

        String eventType = eventLog.getEventType();
        EventTemplate template = findTemplateByEventId(eventType + "_" + eventId);
        if (template == null) {
            template = findTemplateByTitle(eventLog.getEventTitle());
        }

        if (template == null) {
            throw new BusinessException("未找到对应的事件模板");
        }

        EventChoice selectedChoice = null;
        for (EventChoice choice : template.getChoices()) {
            if (choice.getChoiceId() == choiceId) {
                selectedChoice = choice;
                break;
            }
        }

        if (selectedChoice == null) {
            throw new BusinessException("无效的选择ID");
        }

        School school = getSchoolBySaveId(saveId);
        double actualRate = calculateActualSuccessRate(selectedChoice.getBaseSuccessRate(), eventType, school);

        long seed = (eventId + saveId * 1000L + LocalDate.now().getMonthValue());
        Random random = new Random(seed);
        double roll = random.nextDouble();

        boolean success = roll < actualRate;

        Map<String, Object> effects = success ? selectedChoice.getSuccessResult() : selectedChoice.getFailResult();
        applyEffects(effects, school, saveId);

        eventLog.setPlayerChoice(choiceId);
        try {
            eventLog.setResult(objectMapper.writeValueAsString(Map.of(
                "success", success,
                "actualSuccessRate", actualRate,
                "effects", effects != null ? effects : new HashMap<>()
            )));
        } catch (JsonProcessingException e) {
            eventLog.setResult("{\"success\":" + success + "}");
        }
        eventLogMapper.updateById(eventLog);

        EventResultDTO result = new EventResultDTO();
        result.setSuccess(success);
        result.setChoiceId(choiceId);
        result.setActualSuccessRate(Math.round(actualRate * 100.0) / 100.0);
        result.setEffects(effects);
        result.setMessage(success ? "决策执行成功！" : "决策执行失败...");

        if (success && random.nextDouble() < 0.1) {
            generateChainEvent(school, saveId, eventLog);
        }

        return result;
    }

    @Override
    public Page<EventDTO> getEventHistory(Long saveId, int page, int size, String type) {
        LambdaQueryWrapper<EventLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EventLog::getSaveId, saveId)
               .isNotNull(EventLog::getPlayerChoice)
               .orderByDesc(EventLog::getCreatedAt);
        if (type != null && !type.isBlank()) {
            wrapper.eq(EventLog::getEventType, type);
        }

        Page<EventLog> logPage = eventLogMapper.selectPage(new Page<>(page, size), wrapper);

        Page<EventDTO> resultPage = new Page<>(logPage.getCurrent(), logPage.getSize(), logPage.getTotal());
        List<EventDTO> eventDTOs = new ArrayList<>();
        for (EventLog log : logPage.getRecords()) {
            eventDTOs.add(convertToEventDTO(log));
        }
        resultPage.setRecords(eventDTOs);
        return resultPage;
    }

    @Override
    public List<EventTemplateDTO> getEventTemplates(String type) {
        List<EventTemplateDTO> result = new ArrayList<>();
        for (EventTemplate template : templateLibrary) {
            if (type == null || type.isBlank() || type.equals(template.getEventType())) {
                EventTemplateDTO dto = convertToTemplateDTO(template);
                result.add(dto);
            }
        }
        return result;
    }

    private List<EventTemplate> initializeTemplateLibrary() {
        List<EventTemplate> templates = new ArrayList<>();

        templates.add(createStudentPressureEvent());
        templates.add(createTeacherResignationEvent());
        templates.add(createEducationInspectionEvent());
        templates.add(createCampusCrowdingEvent());
        templates.add(createLibraryRenovationEvent());
        templates.add(createMediaInterviewEvent());
        templates.add(createExamMobilizationEvent());
        templates.add(createExamAnalysisEvent());
        templates.add(createAlumniDonationEvent());
        templates.add(createAlumniVisitEvent());
        templates.add(createStudentAchievementEvent());
        templates.add(createTeacherTrainingEvent());
        templates.add(createFacilityBreakdownEvent());
        templates.add(createParentComplaintEvent());
        templates.add(createFinancialCrisisEvent());
        templates.add(createEnrollmentSurgeEvent());
        templates.add(createCompetitionVictoryEvent());
        templates.add(createStudentIllnessEvent());
        templates.add(createPolicyChangeEvent());
        templates.add(createCommunitySupportEvent());

        return templates;
    }

    private EventTemplate createStudentPressureEvent() {
        EventTemplate template = new EventTemplate();
        template.setEventId("STUDENT_PRESSURE");
        template.setEventType("STUDENT");
        template.setTitle("学生反映学习压力过大");
        template.setDescription("近期多名学生反映作业量大、休息时间不足，部分学生出现焦虑情绪");

        List<EventChoice> choices = new ArrayList<>();

        EventChoice choiceA = new EventChoice();
        choiceA.setChoiceId(1);
        choiceA.setText("减轻作业量，增加活动时间");
        choiceA.setCost(0);
        choiceA.setBaseSuccessRate(0.70);
        choiceA.setSuccessResult(Map.of(
            "qualityScore", 10,
            "healthScore", 15,
            "message", "学生压力得到缓解"
        ));
        choiceA.setFailResult(Map.of(
            "academicScore", -5,
            "message", "成绩出现下滑"
        ));

        EventChoice choiceB = new EventChoice();
        choiceB.setChoiceId(2);
        choiceB.setText("开展心理咨询");
        choiceB.setCost(50000);
        choiceB.setBaseSuccessRate(0.85);
        choiceB.setSuccessResult(Map.of(
            "healthScore", 20,
            "funds", -50000,
            "message", "心理健康状况改善"
        ));
        choiceB.setFailResult(Map.of(
            "funds", -60000,
            "message", "效果不佳，资金浪费"
        ));

        EventChoice choiceC = new EventChoice();
        choiceC.setChoiceId(3);
        choiceC.setText("维持现状，加强督促");
        choiceC.setCost(0);
        choiceC.setBaseSuccessRate(0.90);
        choiceC.setSuccessResult(Map.of(
            "academicScore", 5,
            "message", "成绩有所提升"
        ));
        choiceC.setFailResult(Map.of(
            "healthScore", -10,
            "reputation", -5,
            "message", "学生身心状况恶化"
        ));

        choices.add(choiceA);
        choices.add(choiceB);
        choices.add(choiceC);
        template.setChoices(choices);
        template.setWeight(15);
        return template;
    }

    private EventTemplate createTeacherResignationEvent() {
        EventTemplate template = new EventTemplate();
        template.setEventId("TEACHER_RESIGNATION");
        template.setEventType("TEACHER");
        template.setTitle("骨干教师提出离职");
        template.setDescription("因薪资待遇问题，王老师考虑跳槽到私立学校");

        List<EventChoice> choices = new ArrayList<>();

        EventChoice choiceA = new EventChoice();
        choiceA.setChoiceId(1);
        choiceA.setText("大幅涨薪挽留");
        choiceA.setCost(20000);
        choiceA.setBaseSuccessRate(0.80);
        choiceA.setSuccessResult(Map.of(
            "teacherMorale", 10,
            "funds", -20000,
            "message", "成功挽留骨干教师"
        ));
        choiceA.setFailResult(Map.of(
            "funds", -40000,
            "teacherCount", -1,
            "message", "未能挽留，损失双倍费用"
        ));

        EventChoice choiceB = new EventChoice();
        choiceB.setChoiceId(2);
        choiceB.setText("情感挽留+适度加薪");
        choiceB.setCost(8000);
        choiceB.setBaseSuccessRate(0.60);
        choiceB.setSuccessResult(Map.of(
            "funds", -8000,
            "message", "成功挽留教师"
        ));
        choiceB.setFailResult(Map.of(
            "teacherCount", -1,
            "reputation", -5,
            "message", "教师离职"
        ));

        EventChoice choiceC = new EventChoice();
        choiceC.setChoiceId(3);
        choiceC.setText("同意离职，公开招聘");
        choiceC.setCost(0);
        choiceC.setBaseSuccessRate(1.0);
        choiceC.setSuccessResult(Map.of(
            "reputation", -5,
            "teacherCount", -1,
            "message", "已启动招聘流程"
        ));
        choiceC.setFailResult(Map.of());

        choices.add(choiceA);
        choices.add(choiceB);
        choices.add(choiceC);
        template.setChoices(choices);
        template.setWeight(12);
        return template;
    }

    private EventTemplate createEducationInspectionEvent() {
        EventTemplate template = new EventTemplate();
        template.setEventId("SOCIAL_INSPECTION");
        template.setEventType("SOCIAL");
        template.setTitle("教育局即将来校视察");
        template.setDescription("下周教育局领导将莅临指导工作");

        List<EventChoice> choices = new ArrayList<>();

        EventChoice choiceA = new EventChoice();
        choiceA.setChoiceId(1);
        choiceA.setText("充分准备，展示亮点");
        choiceA.setCost(30000);
        choiceA.setBaseSuccessRate(0.75);
        choiceA.setSuccessResult(Map.of(
            "reputation", 30,
            "funds", 50000,
            "message", "视察圆满成功，获得奖励"
        ));
        choiceA.setFailResult(Map.of(
            "reputation", -10,
            "funds", -30000,
            "message", "准备不足，评价一般"
        ));

        EventChoice choiceB = new EventChoice();
        choiceB.setChoiceId(2);
        choiceB.setText("常规应对");
        choiceB.setCost(5000);
        choiceB.setBaseSuccessRate(0.50);
        choiceB.setSuccessResult(Map.of(
            "reputation", 10,
            "message", "顺利通过视察"
        ));
        choiceB.setFailResult(Map.of(
            "reputation", -15,
            "message", "视察结果不理想"
        ));

        EventChoice choiceC = new EventChoice();
        choiceC.setChoiceId(3);
        choiceC.setText("主动汇报困难，申请支持");
        choiceC.setCost(10000);
        choiceC.setBaseSuccessRate(0.60);
        choiceC.setSuccessResult(Map.of(
            "reputation", 5,
            "funds", 100000,
            "message", "获得政府专项拨款"
        ));
        choiceC.setFailResult(Map.of(
            "reputation", -5,
            "funds", -10000,
            "message", "申请未获批准"
        ));

        choices.add(choiceA);
        choices.add(choiceB);
        choices.add(choiceC);
        template.setChoices(choices);
        template.setWeight(10);
        return template;
    }

    private EventTemplate createCampusCrowdingEvent() {
        EventTemplate template = new EventTemplate();
        template.setEventId("CAMPUS_CROWDING");
        template.setEventType("CAMPUS");
        template.setTitle("教学楼容量不足需扩建");
        template.setDescription("学生人数持续增长，现有教学楼已接近满负荷运转");

        List<EventChoice> choices = new ArrayList<>();

        EventChoice choiceA = new EventChoice();
        choiceA.setChoiceId(1);
        choiceA.setText("立即扩建教学楼");
        choiceA.setCost(500000);
        choiceA.setBaseSuccessRate(0.85);
        choiceA.setSuccessResult(Map.of(
            "capacity", 300,
            "funds", -500000,
            "reputation", 10,
            "message", "扩建完成，容量提升"
        ));
        choiceA.setFailResult(Map.of(
            "funds", -250000,
            "message", "扩建工程延期"
        ));

        EventChoice choiceB = new EventChoice();
        choiceB.setChoiceId(2);
        choiceB.setText("调整排课，分时段使用");
        choiceB.setCost(10000);
        choiceB.setBaseSuccessRate(0.70);
        choiceB.setSuccessResult(Map.of(
            "funds", -10000,
            "message", "临时方案有效缓解拥挤"
        ));
        choiceB.setFailResult(Map.of(
            "qualityScore", -5,
            "healthScore", -5,
            "message", "师生怨声载道"
        ));

        EventChoice choiceC = new EventChoice();
        choiceC.setChoiceId(3);
        choiceC.setText("暂时维持现状");
        choiceC.setCost(0);
        choiceC.setBaseSuccessRate(0.50);
        choiceC.setSuccessResult(Map.of(
            "message", "勉强维持正常教学"
        ));
        choiceC.setFailResult(Map.of(
            "reputation", -10,
            "healthScore", -10,
            "message": "拥挤问题严重影响教学质量"
        ));

        choices.add(choiceA);
        choices.add(choiceB);
        choices.add(choiceC);
        template.setChoices(choices);
        template.setWeight(8);
        return template;
    }

    private EventTemplate createLibraryRenovationEvent() {
        EventTemplate template = new EventTemplate();
        template.setEventId("CAMPUS_LIBRARY");
        template.setEventType("CAMPUS");
        template.setTitle("图书馆翻新完成");
        template.setDescription("经过数月施工，学校图书馆翻新工程已完工");

        List<EventChoice> choices = new ArrayList<>();

        EventChoice choiceA = new EventChoice();
        choiceA.setChoiceId(1);
        choiceA.setText("举办开馆仪式");
        choiceA.setCost(20000);
        choiceA.setBaseSuccessRate(0.80);
        choiceA.setSuccessResult(Map.of(
            "reputation", 15,
            "qualityScore", 8,
            "funds", -20000,
            "message": "仪式圆满成功"
        ));
        choiceA.setFailResult(Map.of(
            "funds", -20000,
            "message": "反响平平"
        ));

        EventChoice choiceB = new EventChoice();
        choiceB.setChoiceId(2);
        choiceB.setText("直接投入使用");
        choiceB.setCost(0);
        choiceB.setBaseSuccessRate(1.0);
        choiceB.setSuccessResult(Map.of(
            "qualityScore", 5,
            "message": "新图书馆投入使用"
        ));
        choiceB.setFailResult(Map.of());

        choices.add(choiceA);
        choices.add(choiceB);
        template.setChoices(choices);
        template.setWeight(6);
        return template;
    }

    private EventTemplate createMediaInterviewEvent() {
        EventTemplate template = new EventTemplate();
        template.setEventId("SOCIAL_MEDIA");
        template.setEventType("SOCIAL");
        template.setTitle("媒体采访邀请");
        template.getDescription();
        template.setDescription("省电视台希望来校采访报道办学特色");

        List<EventChoice> choices = new ArrayList<>();

        EventChoice choiceA = new EventChoice();
        choiceA.setChoiceId(1);
        choiceA.setText("积极接受采访");
        choiceA.setCost(15000);
        choiceA.setBaseSuccessRate(0.70);
        choiceA.setSuccessResult(Map.of(
            "reputation", 25,
            "funds", -15000,
            "message": "正面报道大幅提升知名度"
        ));
        choiceA.setFailResult(Map.of(
            "reputation", -8,
            "funds", -15000,
            "message": "报道角度不利"
        ));

        EventChoice choiceB = new EventChoice();
        choiceB.setChoiceId(2);
        choiceB.setText("谨慎应对，提供书面材料");
        choiceB.setCost(3000);
        choiceB.setBaseSuccessRate(0.85);
        choiceB.setSuccessResult(Map.of(
            "reputation", 10,
            "funds", -3000,
            "message": "稳妥完成采访"
        ));
        choiceB.setFailResult(Map.of(
            "funds", -3000,
            "message": "媒体兴趣不高"
        ));

        EventChoice choiceC = new EventChoice();
        choiceC.setChoiceId(3);
        choiceC.setText("婉拒采访");
        choiceC.setCost(0);
        choiceC.setBaseSuccessRate(1.0);
        choiceC.setSuccessResult(Map.of(
            "message": "保持低调"
        ));
        choiceC.setFailResult(Map.of());

        choices.add(choiceA);
        choices.add(choiceB);
        choices.add(choiceC);
        template.setChoices(choices);
        template.setWeight(7);
        return template;
    }

    private EventTemplate createExamMobilizationEvent() {
        EventTemplate template = new EventTemplate();
        template.setEventId("EXAM_MOBILIZATION");
        template.setEventType("EXAM");
        template.setTitle("高考倒计时动员");
        template.setDescription("高考临近，需要组织考前动员活动");

        List<EventChoice> choices = new ArrayList<>();

        EventChoice choiceA = new EventChoice();
        choiceA.setChoiceId(1);
        choiceA.setText("大型动员大会+心理辅导");
        choiceA.setCost(30000);
        choiceA.setBaseSuccessRate(0.75);
        choiceA.setSuccessResult(Map.of(
            "academicScore", 8,
            "healthScore", 10,
            "funds", -30000,
            "message": "士气高昂迎考"
        ));
        choiceA.setFailResult(Map.of(
            "funds", -30000,
            "healthScore", -5,
            "message": "过度紧张适得其反"
        ));

        EventChoice choiceB = new EventChoice();
        choiceB.setChoiceId(2);
        choiceB.setText("班级小规模动员");
        choiceB.setCost(5000);
        choiceB.setBaseSuccessRate(0.65);
        choiceB.setSuccessResult(Map.of(
            "academicScore", 5,
            "funds", -5000,
            "message": "平稳备考"
        ));
        choiceB.setFailResult(Map.of(
            "funds", -5000,
            "message": "效果有限"
        ));

        EventChoice choiceC = new EventChoice();
        choiceC.setChoiceId(3);
        choiceC.setText("维持日常教学节奏");
        choiceC.setCost(0);
        choiceC.setBaseSuccessRate(0.55);
        choiceC.setSuccessResult(Map.of(
            "message": "按部就班备考"
        ));
        choiceC.setFailResult(Map.of(
            "academicScore", -3,
            "message": "缺乏冲刺动力"
        ));

        choices.add(choiceA);
        choices.add(choiceB);
        choices.add(choiceC);
        template.setChoices(choices);
        template.setWeight(9);
        return template;
    }

    private EventTemplate createExamAnalysisEvent() {
        EventTemplate template = new EventTemplate();
        template.setEventId("EXAM_ANALYSIS");
        template.setEventType("EXAM");
        template.setTitle("期末考试安排与分析");
        template.setDescription("期末考试即将来临，需要安排考试并进行成绩分析");

        List<EventChoice> choices = new ArrayList<>();

        EventChoice choiceA = new EventChoice();
        choiceA.setChoiceId(1);
        choiceA.setText("全面模拟考试+详细分析");
        choiceA.setCost(20000);
        choiceA.setBaseSuccessRate(0.78);
        choiceA.setSuccessResult(Map.of(
            "academicScore", 10,
            "funds", -20000,
            "message": "考试成绩显著提升"
        ));
        choiceA.setFailResult(Map.of(
            "funds", -20000,
            "pressure", 15,
            "message": "学生压力过大"
        ));

        EventChoice choiceB = new EventChoice();
        choiceB.setChoiceId(2);
        choiceB.setText("常规考试+重点分析");
        choiceB.setCost(8000);
        choiceB.setBaseSuccessRate(0.68);
        choiceB.setSuccessResult(Map.of(
            "academicScore", 6,
            "funds", -8000,
            "message": "考试顺利完成"
        ));
        choiceB.setFailResult(Map.of(
            "funds", -8000,
            "message": "分析不够深入"
        ));

        choices.add(choiceA);
        choices.add(choiceB);
        template.setChoices(choices);
        template.setWeight(9);
        return template;
    }

    private EventTemplate createAlumniDonationEvent() {
        EventTemplate template = new EventTemplate();
        template.setEventId("ALUMNI_DONATION");
        template.setEventType("ALUMNI");
        template.setTitle("校友荣归母校");
        template.setDescription("知名校友回访母校，表达捐赠意向");

        List<EventChoice> choices = new ArrayList<>();

        EventChoice choiceA = new EventChoice();
        choiceA.setChoiceId(1);
        choiceA.setText("隆重接待，争取大额捐赠");
        choiceA.setCost(40000);
        choiceA.setBaseSuccessRate(0.65);
        choiceA.setSuccessResult(Map.of(
            "funds", 500000,
            "reputation", 20,
            "cost", -40000,
            "message": "获得大额捐赠！"
        ));
        choiceA.setFailResult(Map.of(
            "funds", -40000,
            "message": "捐赠金额低于预期"
        ));

        EventChoice choiceB = new EventChoice();
        choiceB.setChoiceId(2);
        choiceB.setText("热情接待，量力而行");
        choiceB.setCost(10000);
        choiceB.setBaseSuccessRate(0.80);
        choiceB.setSuccessResult(Map.of(
            "funds", 100000,
            "reputation", 10,
            "cost", -10000,
            "message": "获得适量捐赠"
        ));
        choiceB.setFailResult(Map.of(
            "funds", -10000,
            "message": "校友表示以后再考虑"
        ));

        choices.add(choiceA);
        choices.add(choiceB);
        template.setChoices(choices);
        template.setWeight(5);
        return template;
    }

    private EventTemplate createAlumniVisitEvent() {
        EventTemplate template = new EventTemplate();
        template.setEventId("ALUMNI_VISIT");
        template.setEventType("ALUMNI");
        template.setTitle("优秀校友分享会");
        template.setDescription("多位优秀校友愿意返校分享经验");

        List<EventChoice> choices = new ArrayList<>();

        EventChoice choiceA = new EventChoice();
        choiceA.setChoiceId(1);
        choiceA.setText("组织大型分享会");
        choiceA.setCost(15000);
        choiceA.setBaseSuccessRate(0.72);
        choiceA.setSuccessResult(Map.of(
            "qualityScore", 12,
            "academicScore", 5,
            "funds", -15000,
            "message": "学生深受鼓舞"
        ));
        choiceA.setFailResult(Map.of(
            "funds", -15000,
            "message": "参与度不高"
        ));

        EventChoice choiceB = new EventChoice();
        choiceB.setChoiceId(2);
        choiceB.setText("班级巡回分享");
        choiceB.setCost(3000);
        choiceB.setBaseSuccessRate(0.85);
        choiceB.setSuccessResult(Map.of(
            "qualityScore", 7,
            "academicScore", 3,
            "funds", -3000,
            "message": "分享效果良好"
        ));
        choiceB.setFailResult(Map.of(
            "funds", -3000,
            "message": "效果一般"
        ));

        choices.add(choiceA);
        choices.add(choiceB);
        template.setChoices(choices);
        template.setWeight(4);
        return template;
    }

    private EventTemplate createStudentAchievementEvent() {
        EventTemplate template = new EventTemplate();
        template.setEventId("STUDENT_ACHIEVEMENT");
        template.setEventType("STUDENT");
        template.setTitle("学生在竞赛中获奖");
        template.setDescription("我校学生在省级学科竞赛中获得优异成绩");

        List<EventChoice> choices = new ArrayList<>();

        EventChoice choiceA = new EventChoice();
        choiceA.setChoiceId(1);
        choiceA.setText("全校表彰+奖金激励");
        choiceA.setCost(30000);
        choiceA.setBaseSuccessRate(0.88);
        choiceA.setSuccessResult(Map.of(
            "reputation", 20,
            "academicScore", 8,
            "funds", -30000,
            "message": "全校学习热情高涨"
        ));
        choiceA.setFailResult(Map.of(
            "funds", -30000,
            "message": "效果尚可"
        ));

        EventChoice choiceB = new EventChoice();
        choiceB.setChoiceId(2);
        choiceB.setText("简单表扬");
        choiceB.setCost(0);
        choiceB.setBaseSuccessRate(0.95);
        choiceB.setSuccessResult(Map.of(
            "reputation", 8,
            "academicScore", 3,
            "message": "给予肯定和鼓励"
        ));
        choiceB.setFailResult(Map.of(
            "message": "反响平平"
        ));

        choices.add(choiceA);
        choices.add(choiceB);
        template.setChoices(choices);
        template.setWeight(8);
        return template;
    }

    private EventTemplate createTeacherTrainingEvent() {
        EventTemplate template = new EventTemplate();
        template.setEventId("TEACHER_TRAINING");
        template.setEventType("TEACHER");
        template.setTitle("教师培训机会");
        template.setDescription("省教育厅组织骨干教师培训，我校有推荐名额");

        List<EventChoice> choices = new ArrayList<>();

        EventChoice choiceA = new EventChoice();
        choiceA.setChoiceId(1);
        choiceA.setText("选派多名教师参加");
        choiceA.setCost(60000);
        choiceA.setBaseSuccessRate(0.75);
        choiceA.setSuccessResult(Map.of(
            "teacherAbility", 15,
            "teacherMorale", 10,
            "funds", -60000,
            "message": "教师队伍整体提升"
        ));
        choiceA.setFailResult(Map.of(
            "funds", -60000,
            "message": "培训效果一般"
        ));

        EventChoice choiceB = new EventChoice();
        choiceB.setChoiceId(2);
        choiceB.setText("选派少量骨干教师");
        choiceB.setCost(20000);
        choiceB.setBaseSuccessRate(0.85);
        choiceB.setSuccessResult(Map.of(
            "teacherAbility", 8,
            "funds", -20000,
            "message": "骨干能力提升"
        ));
        choiceB.setFailResult(Map.of(
            "funds", -20000,
            "message": "收获有限"
        ));

        choices.add(choiceA);
        choices.add(choiceB);
        template.setChoices(choices);
        template.setWeight(6);
        return template;
    }

    private EventTemplate createFacilityBreakdownEvent() {
        EventTemplate template = new EventTemplate();
        template.setEventId("CAMPUS_BREAKDOWN");
        template.setEventType("CAMPUS");
        template.setTitle("设施故障");
        template.setDescription("实验室设备出现故障，影响实验教学");

        List<EventChoice> choices = new ArrayList<>();

        EventChoice choiceA = new EventChoice();
        choiceA.setChoiceId(1);
        choiceA.setText("立即维修更换");
        choiceA.setCost(80000);
        choiceA.setBaseSuccessRate(0.90);
        choiceA.setSuccessResult(Map.of(
            "funds", -80000,
            "academicScore", 5,
            "message": "快速恢复正常教学"
        ));
        choiceA.setFailResult(Map.of(
            "funds", -80000,
            "academicScore", -3,
            "message": "维修时间较长"
        ));

        EventChoice choiceB = new EventChoice();
        choiceB.setChoiceId(2);
        choiceB.setText("临时调配设备");
        choiceB.setCost(15000);
        choiceB.setBaseSuccessRate(0.65);
        choiceB.setSuccessResult(Map.of(
            "funds", -15000,
            "message": "临时解决"
        ));
        choiceB.setFailResult(Map.of(
            "funds", -15000,
            "academicScore", -8,
            "qualityScore", -5,
            "message": "严重影响了教学进度"
        ));

        choices.add(choiceA);
        choices.add(choiceB);
        template.setChoices(choices);
        template.setWeight(7);
        return template;
    }

    private EventTemplate createParentComplaintEvent() {
        EventTemplate template = new EventTemplate();
        template.setEventId("STUDENT_PARENT_COMPLAINT");
        template.setEventType("STUDENT");
        template.setTitle("家长投诉");
        template.setDescription("部分家长对学校管理方式提出投诉");

        List<EventChoice> choices = new ArrayList<>();

        EventChoice choiceA = new EventChoice();
        choiceA.setChoiceId(1);
        choiceA.setText("积极沟通整改");
        choiceA.setCost(10000);
        choiceA.setBaseSuccessRate(0.78);
        choiceA.setSuccessResult(Map.of(
            "reputation", 12,
            "funds", -10000,
            "message": "家长满意度提升"
        ));
        choiceA.setFailResult(Map.of(
            "funds", -10000,
            "reputation", -5,
            "message": "部分家长仍不满意"
        ));

        EventChoice choiceB = new EventChoice();
        choiceB.setChoiceId(2);
        choiceB.setText("正式回应解释");
        choiceB.setCost(2000);
        choiceB.setBaseSuccessRate(0.60);
        choiceB.setSuccessResult(Map.of(
            "funds", -2000,
            "message": "事态平息"
        ));
        choiceB.setFailResult(Map.of(
            "reputation", -10,
            "funds", -2000,
            "message": "舆论发酵"
        ));

        choices.add(choiceA);
        choices.add(choiceB);
        template.setChoices(choices);
        template.setWeight(8);
        return template;
    }

    private EventTemplate createFinancialCrisisEvent() {
        EventTemplate template = new EventTemplate();
        template.setEventId("SYSTEM_FINANCIAL");
        template.setEventType("SYSTEM");
        template.setTitle("资金紧张预警");
        template.setDescription("学校运营资金已低于安全线，需要采取措施");

        List<EventChoice> choices = new ArrayList<>();

        EventChoice choiceA = new EventChoice();
        choiceA.setChoiceId(1);
        choiceA.setText("申请银行贷款");
        choiceA.setCost(0);
        choiceA.setBaseSuccessRate(0.65);
        choiceA.setSuccessResult(Map.of(
            "funds", 300000,
            "message": "获得贷款支持"
        ));
        choiceA.setFailResult(Map.of(
            "reputation", -15,
            "message": "贷款申请被拒"
        ));

        EventChoice choiceB = new EventChoice();
        choiceB.setChoiceId(2);
        choiceB.setText("压缩开支");
        choiceB.setCost(0);
        choiceB.setBaseSuccessRate(0.85);
        choiceB.setSuccessResult(Map.of(
            "teacherMorale", -10,
            "healthScore", -8,
            "message": "度过难关但士气受挫"
        ));
        choiceB.setFailResult(Map.of(
            "reputation", -20,
            "teacherMorale", -15,
            "message": "教职工大量流失"
        ));

        EventChoice choiceC = new EventChoice();
        choiceC.setChoiceId(3);
        choiceC.setText("寻求校友捐助");
        choiceC.setCost(5000);
        choiceC.setBaseSuccessRate(0.50);
        choiceC.setSuccessResult(Map.of(
            "funds", 150000,
            "funds", -5000,
            "message": "获得校友资助"
        ));
        choiceC.setFailResult(Map.of(
            "funds", -5000,
            "message": "筹款效果不佳"
        ));

        choices.add(choiceA);
        choices.add(choiceB);
        choices.add(choiceC);
        template.setChoices(choices);
        template.setWeight(4);
        template.setCondition(new TriggerCondition());
        return template;
    }

    private EventTemplate createEnrollmentSurgeEvent() {
        EventTemplate template = new EventTemplate();
        template.setEventId("STUDENT_ENROLLMENT_SURGE");
        template.setEventType("STUDENT");
        template.setTitle("招生火爆");
        template.setDescription("今年报名人数远超预期，生源质量大幅提升");

        List<EventChoice> choices = new ArrayList<>();

        EventChoice choiceA = new EventChoice();
        choiceA.setChoiceId(1);
        choiceA.setText("扩大招生规模");
        choiceA.setCost(100000);
        choiceA.setBaseSuccessRate(0.70);
        choiceA.setSuccessResult(Map.of(
            "studentCount", 100,
            "reputation", 15,
            "funds", -100000,
            "message": "优质生源大量涌入"
        ));
        choiceA.setFailResult(Map.of(
            "funds", -100000,
            "healthScore", -10,
            "message": "资源紧张影响质量"
        ));

        EventChoice choiceB = new EventChoice();
        choiceB.setChoiceId(2);
        choiceB.setText("严格控制招生质量");
        choiceB.setCost(20000);
        choiceB.setBaseSuccessRate(0.82);
        choiceB.setSuccessResult(Map.of(
            "studentCount", 30,
            "academicScore", 10,
            "reputation", 10,
            "funds", -20000,
            "message": "精英化办学路线"
        ));
        choiceB.setFailResult(Map.of(
            "funds", -20000,
            "message": "错失发展良机"
        ));

        choices.add(choiceA);
        choices.add(choiceB);
        template.setChoices(choices);
        template.setWeight(6);
        return template;
    }

    private EventTemplate createCompetitionVictoryEvent() {
        EventTemplate template = new EventTemplate();
        template.setEventId("STUDENT_COMPETITION_WIN");
        template.setEventType("STUDENT");
        template.setTitle("竞赛重大突破");
        template.setDescription("我校学生在全国奥赛中斩获金牌！");

        List<EventChoice> choices = new ArrayList<>();

        EventChoice choiceA = new EventChoice();
        choiceA.setChoiceId(1);
        choiceA.setText("高调宣传庆祝");
        choiceA.setCost(50000);
        choiceA.setBaseSuccessRate(0.80);
        choiceA.setSuccessResult(Map.of(
            "reputation", 35,
            "academicScore", 12,
            "funds", -50000,
            "message": "名声大噪！"
        ));
        choiceA.setFailResult(Map.of(
            "funds", -50000,
            "reputation", 10,
            "message": "宣传效果一般"
        ));

        EventChoice choiceB = new EventChoice();
        choiceB.setChoiceId(2);
        choiceB.setText("低调表彰");
        choiceB.setCost(5000);
        choiceB.setBaseSuccessRate(0.92);
        choiceB.setSuccessResult(Map.of(
            "reputation", 18,
            "academicScore", 6,
            "funds", -5000,
            "message": "稳步提升声誉"
        ));
        choiceB.setFailResult(Map.of(
            "funds", -5000,
            "message": "内部庆祝"
        ));

        choices.add(choiceA);
        choices.add(choiceB);
        template.setChoices(choices);
        template.setWeight(5);
        return template;
    }

    private EventTemplate createStudentIllnessEvent() {
        EventTemplate template = new EventTemplate();
        template.setEventId("STUDENT_ILLNESS");
        template.setEventType("STUDENT");
        template.setTitle("传染病疫情");
        template.setDescription("校园内出现传染性疾病病例");

        List<EventChoice> choices = new ArrayList<>();

        EventChoice choiceA = new EventChoice();
        choiceA.setChoiceId(1);
        choiceA.setText("立即停课消毒");
        choiceA.setCost(50000);
        choiceA.setBaseSuccessRate(0.88);
        choiceA.setSuccessResult(Map.of(
            "healthScore", 15,
            "funds", -50000,
            "academicScore", -5,
            "message": "疫情迅速控制"
        ));
        choiceA.setFailResult(Map.of(
            "funds", -50000,
            "healthScore", -10,
            "reputation", -10,
            "message": "疫情影响扩大"
        ));

        EventChoice choiceB = new EventChoice();
        choiceB.setChoiceId(2);
        choiceB.setText("隔离观察+加强防控");
        choiceB.setCost(20000);
        choiceB.setBaseSuccessRate(0.65);
        choiceB.setSuccessResult(Map.of(
            "funds", -20000,
            "healthScore", 5,
            "message": "基本控制"
        ));
        choiceB.setFailResult(Map.of(
            "funds", -20000,
            "healthScore", -15,
            "academicScore", -10,
            "reputation", -15,
            "message": "疫情扩散，多人感染"
        ));

        choices.add(choiceA);
        choices.add(choiceB);
        template.setChoices(choices);
        template.setWeight(6);
        return template;
    }

    private EventTemplate createPolicyChangeEvent() {
        EventTemplate template = new EventTemplate();
        template.setEventId("SOCIAL_POLICY_CHANGE");
        template.setEventType("SOCIAL");
        template.setTitle("教育政策调整");
        template.setDescription("教育部发布新的教育改革政策");

        List<EventChoice> choices = new ArrayList<>();

        EventChoice choiceA = new EventChoice();
        choiceA.setChoiceId(1);
        choiceA.setText("积极响应政策");
        choiceA.setCost(80000);
        choiceA.setBaseSuccessRate(0.72);
        choiceA.setSuccessResult(Map.of(
            "reputation", 25,
            "funds", -80000,
            "qualityScore", 10,
            "message": "成为政策示范校"
        ));
        choiceA.setFailResult(Map.of(
            "funds", -80000,
            "message": "转型阵痛期"
        ));

        EventChoice choiceB = new EventChoice();
        choiceB.setChoiceId(2);
        choiceB.setText("观望等待");
        choiceB.setCost(0);
        choiceB.setBaseSuccessRate(0.60);
        choiceB.setSuccessResult(Map.of(
            "message": "稳中求进"
        ));
        choiceB.setFailResult(Map.of(
            "reputation", -12,
            "message": "错过政策红利"
        ));

        choices.add(choiceA);
        choices.add(choiceB);
        template.setChoices(choices);
        template.setWeight(7);
        return template;
    }

    private EventTemplate createCommunitySupportEvent() {
        EventTemplate template = new EventTemplate();
        template.setEventId("SOCIAL_COMMUNITY");
        template.setEventType("SOCIAL");
        template.setTitle("社区合作项目");
        template.setDescription("周边社区提出与学校开展合作项目");

        List<EventChoice> choices = new ArrayList<>();

        EventChoice choiceA = new EventChoice();
        choiceA.setChoiceId(1);
        choiceA.setText("深度参与合作");
        choiceA.setCost(25000);
        choiceA.setBaseSuccessRate(0.75);
        choiceA.setSuccessResult(Map.of(
            "reputation", 15,
            "qualityScore", 8,
            "funds", -25000,
            "message": "社区关系和谐"
        ));
        choiceA.setFailResult(Map.of(
            "funds", -25000,
            "message": "合作进展缓慢"
        ));

        EventChoice choiceB = new EventChoice();
        choiceB.setChoiceId(2);
        choiceB.setText("象征性参与");
        choiceB.setCost(3000);
        choiceB.setBaseSuccessRate(0.88);
        choiceB.setSuccessResult(Map.of(
            "reputation", 5,
            "funds", -3000,
            "message": "维护良好关系"
        ));
        choiceB.setFailResult(Map.of(
            "funds", -3000,
            "message": "效果有限"
        ));

        choices.add(choiceA);
        choices.add(choiceB);
        template.setChoices(choices);
        template.setWeight(5);
        return template;
    }

    private Map<String, List<String>> generateCandidatePool(School school, Long saveId) {
        Map<String, List<String>> pool = new HashMap<>();
        pool.put("STUDENT", new ArrayList<>());
        pool.put("TEACHER", new ArrayList<>());
        pool.put("CAMPUS", new ArrayList<>());
        pool.put("SOCIAL", new ArrayList<>());
        pool.put("EXAM", new ArrayList<>());
        pool.put("ALUMNI", new ArrayList<>());

        int currentMonth = LocalDate.now().getMonthValue();

        LambdaQueryWrapper<Student> studentWrapper = new LambdaQueryWrapper<>();
        studentWrapper.eq(Student::getSchoolId, school.getId());
        List<Student> students = studentMapper.selectList(studentWrapper);

        double sampleRate = 0.05 + Math.random() * 0.10;
        int sampleSize = Math.max(1, (int) (students.size() * sampleRate));

        Collections.shuffle(students);
        List<Student> sampledStudents = students.subList(0, Math.min(sampleSize, students.size()));

        boolean hasHealthIssue = false;
        boolean hasAcademicIssue = false;
        for (Student s : sampledStudents) {
            BigDecimal health = s.getHealthScore() != null ? s.getHealthScore() : BigDecimal.valueOf(80);
            BigDecimal academic = s.getAcademicScore() != null ? s.getAcademicScore() : BigDecimal.valueOf(60);
            if (health.compareTo(new BigDecimal("30")) < 0) {
                hasHealthIssue = true;
            }
            if (academic.compareTo(new BigDecimal("20")) < 0) {
                hasAcademicIssue = true;
            }
        }

        if (hasHealthIssue) {
            pool.get("STUDENT").add("学生反映学习压力过大");
        }
        if (hasAcademicIssue) {
            pool.get("STUDENT").add("学生出现厌学情绪");
        }
        if (Math.random() < 0.08) {
            pool.get("STUDENT").add("学生在竞赛中获奖");
        }

        LambdaQueryWrapper<Teacher> teacherWrapper = new LambdaQueryWrapper<>();
        teacherWrapper.eq(Teacher::getSchoolId, school.getId());
        List<Teacher> teachers = teacherMapper.selectList(teacherWrapper);

        for (Teacher t : teachers) {
            Integer moral = t.getMoralLevel() != null ? t.getMoralLevel() : 70;
            if (moral < 50) {
                pool.get("TEACHER").add("骨干教师提出离职");
                break;
            }
        }

        int superTeacherCount = 0;
        for (Teacher t : teachers) {
            if ("特级".equals(t.getLevel())) {
                superTeacherCount++;
                if (Math.random() < 0.05) {
                    pool.get("TEACHER").add("特级教师提出改革方案");
                    break;
                }
            }
        }

        if (superTeacherCount > 0 && Math.random() < 0.05) {
            pool.get("TEACHER").add("教师培训机会");
        }

        int studentCount = school.getStudentCount() != null ? school.getStudentCount() : 0;
        int capacity = 500;
        if (studentCount > capacity * 0.9) {
            pool.get("CAMPUS").add("教学楼容量不足需扩建");
        }

        if (Math.random() < 0.06) {
            pool.get("CAMPUS").add("图书馆翻新完成");
        }
        if (Math.random() < 0.05) {
            pool.get("CAMPUS").add("设施故障");
        }

        int reputation = school.getReputation() != null ? school.getReputation() : 0;
        if (reputation >= 100 && reputation < 500 && Math.random() < 0.08) {
            pool.get("SOCIAL").add("市教育局视察");
        } else if (reputation >= 500 && reputation < 1000 && Math.random() < 0.08) {
            pool.get("SOCIAL").add("媒体采访邀请");
        } else if (reputation >= 1000 && Math.random() < 0.08) {
            pool.get("SOCIAL").add("省教育厅关注");
        }

        if (currentMonth == 6 || currentMonth == 1 || currentMonth == 7) {
            if (currentMonth == 6) {
                pool.get("EXAM").add("高考倒计时动员");
            } else {
                pool.get("EXAM").add("期末考试安排与分析");
            }
        }

        double alumniProbability = 0.02 + Math.min(reputation / 1000.0, 0.08);
        if (Math.random() < alumniProbability) {
            if (Math.random() < 0.5) {
                pool.get("ALUMNI").add("校友荣归母校");
            } else {
                pool.get("ALUMNI").add("优秀校友分享会");
            }
        }

        if (Math.random() < 0.04) {
            pool.get("STUDENT").add("家长投诉");
        }
        if (Math.random() < 0.03) {
            pool.get("SYSTEM").add("资金紧张预警");
        }

        return pool;
    }

    private void checkAndGenerateUrgentEvents(School school, Long saveId, List<EventDTO> existingEvents) {
        BigDecimal funds = school.getFunds() != null ? school.getFunds() : BigDecimal.ZERO;
        if (funds.compareTo(BigDecimal.valueOf(100000)) < 0) {
            boolean hasFinancialEvent = false;
            for (EventDTO evt : existingEvents) {
                if ("SYSTEM_FINANCIAL".equals(evt.getEventId())) {
                    hasFinancialEvent = true;
                    break;
                }
            }
            if (!hasFinancialEvent) {
                EventTemplate template = findTemplateByEventId("SYSTEM_FINANCIAL");
                if (template != null) {
                    EventDTO urgentEvent = createEventFromTemplate(template, saveId);
                    urgentEvent.setUrgent(true);
                    existingEvents.add(urgentEvent);
                    saveEventToDatabase(urgentEvent, saveId);
                }
            }
        }
    }

    private EventDTO createEventFromTemplate(EventTemplate template, Long saveId) {
        EventDTO dto = new EventDTO();
        dto.setEventId(template.getEventId());
        dto.setEventType(template.getEventType());
        dto.setTitle(template.getTitle());
        dto.setDescription(template.getDescription());
        dto.setStatus("PENDING");
        dto.setUrgent(false);
        dto.setTriggerYear(LocalDate.now().getYear());
        dto.setTriggerMonth(LocalDate.now().getMonthValue());
        dto.setCreatedAt(LocalDateTime.now());

        List<EventChoiceDTO> choiceDTOs = new ArrayList<>();
        if (template.getChoices() != null) {
            for (EventChoice choice : template.getChoices()) {
                EventChoiceDTO choiceDTO = new EventChoiceDTO();
                choiceDTO.setChoiceId(choice.getChoiceId());
                choiceDTO.setText(choice.getText());
                choiceDTO.setCost(choice.getCost());
                choiceDTO.setBaseSuccessRate(choice.getBaseSuccessRate());
                choiceDTOs.add(choiceDTO);
            }
        }
        dto.setChoices(choiceDTOs);

        return dto;
    }

    private void saveEventToDatabase(EventDTO eventDTO, Long saveId) {
        EventLog eventLog = new EventLog();
        eventLog.setSaveId(saveId);
        eventLog.setEventType(eventDTO.getEventType());
        eventLog.setEventTitle(eventDTO.getTitle());
        eventLog.setEventDescription(eventDTO.getDescription());
        try {
            eventLog.setChoices(objectMapper.writeValueAsString(eventDTO.getChoices()));
        } catch (JsonProcessingException e) {
            eventLog.setChoices("[]");
        }
        eventLog.setTriggerYear(eventDTO.getTriggerYear());
        eventLog.setTriggerMonth(eventDTO.getTriggerMonth());
        eventLog.setCreatedAt(LocalDateTime.now());
        eventLogMapper.insert(eventLog);
        eventDTO.setId(eventLog.getId());
    }

    private EventTemplate findTemplateByTitle(String title) {
        for (EventTemplate template : templateLibrary) {
            if (title.equals(template.getTitle())) {
                return template;
            }
        }
        return null;
    }

    private EventTemplate findTemplateByEventId(String eventId) {
        for (EventTemplate template : templateLibrary) {
            if (eventId.equals(template.getEventId())) {
                return template;
            }
        }
        return null;
    }

    private double calculateActualSuccessRate(double baseRate, String eventType, School school) {
        double bonus = 0.0;

        LambdaQueryWrapper<Teacher> teacherWrapper = new LambdaQueryWrapper<>();
        if (school != null) {
            teacherWrapper.eq(Teacher::getSchoolId, school.getId());
        }
        List<Teacher> teachers = teacherMapper.selectList(teacherWrapper);

        double avgMoral = 70.0;
        if (teachers != null && !teachers.isEmpty()) {
            double sum = 0;
            for (Teacher t : teachers) {
                sum += t.getMoralLevel() != null ? t.getMoralLevel() : 70;
            }
            avgMoral = sum / teachers.size();
        }

        if (avgMoral > 80 && ("STUDENT".equals(eventType) || "DISCIPLINE".equals(eventType))) {
            bonus += 0.10;
        }

        if (school != null) {
            int reputation = school.getReputation() != null ? school.getReputation() : 0;
            if (reputation > 1000 && "SOCIAL".equals(eventType)) {
                bonus += 0.10;
            }

            BigDecimal funds = school.getFunds() != null ? school.getFunds() : BigDecimal.ZERO;
            if (funds.compareTo(new BigDecimal("5000000")) > 0) {
                bonus += 0.10;
            }
        }

        if ("CAMPUS".equals(eventType)) {
            bonus += 0.05;
        }

        return Math.min(1.0, Math.max(0.0, baseRate + bonus));
    }

    private void applyEffects(Map<String, Object> effects, School school, Long saveId) {
        if (effects == null || school == null) {
            return;
        }

        for (Map.Entry<String, Object> entry : effects.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value instanceof Number) {
                Number numValue = (Number) value;
                double change = numValue.doubleValue();

                switch (key) {
                    case "reputation":
                        int currentRep = school.getReputation() != null ? school.getReputation() : 0;
                        school.setReputation(currentRep + (int) change);
                        break;
                    case "funds":
                        BigDecimal currentFunds = school.getFunds() != null ? school.getFunds() : BigDecimal.ZERO;
                        school.setFunds(currentFunds.add(BigDecimal.valueOf(change)));
                        break;
                    case "studentCount":
                        int currentStudents = school.getStudentCount() != null ? school.getStudentCount() : 0;
                        school.setStudentCount(Math.max(0, currentStudents + (int) change));
                        break;
                    case "teacherCount":
                        int currentTeachers = school.getTeacherCount() != null ? school.getTeacherCount() : 0;
                        school.setTeacherCount(Math.max(0, currentTeachers + (int) change));
                        break;
                    default:
                        break;
                }
            }
        }

        schoolMapper.updateById(school);
    }

    private void generateChainEvent(School school, Long saveId, EventLog sourceEvent) {
        String chainEventTitle = "";
        if ("ALUMNI_DONATION".equals(sourceEvent.getEventType())) {
            chainEventTitle = "后续捐赠意向";
        } else if ("SOCIAL_INSPECTION".equals(sourceEvent.getEventType())) {
            chainEventTitle = "复查通知";
        } else {
            return;
        }

        EventLog chainEvent = new EventLog();
        chainEvent.setSaveId(saveId);
        chainEvent.setEventType("CHAIN_EVENT");
        chainEvent.setEventTitle(chainEventTitle);
        chainEvent.setEventDescription("连锁反应：" + sourceEvent.getEventTitle() + " 触发了后续事件");
        chainEvent.setStatus("PENDING");
        chainEvent.setTriggerYear(LocalDate.now().getYear());
        chainEvent.setTriggerMonth(LocalDate.now().getMonthValue());
        chainEvent.setCreatedAt(LocalDateTime.now());
        eventLogMapper.insert(chainEvent);
    }

    private EventDTO convertToEventDTO(EventLog log) {
        EventDTO dto = new EventDTO();
        dto.setId(log.getId());
        dto.setEventId(log.getEventType());
        dto.setEventType(log.getEventType());
        dto.setTitle(log.getEventTitle());
        dto.setDescription(log.getEventDescription());
        dto.setPlayerChoice(log.getPlayerChoice());
        dto.setTriggerYear(log.getTriggerYear());
        dto.setTriggerMonth(log.getTriggerMonth());
        dto.setCreatedAt(log.getCreatedAt());
        dto.setStatus(log.getPlayerChoice() != null ? "RESOLVED" : "PENDING");

        if (log.getChoices() != null && !log.getChoices().isBlank()) {
            try {
                List<EventChoiceDTO> choices = objectMapper.readValue(log.getChoices(),
                    new com.fasterxml.jackson.core.type.TypeReference<List<EventChoiceDTO>>() {});
                dto.setChoices(choices);
            } catch (JsonProcessingException e) {
                dto.setChoices(new ArrayList<>());
            }
        }

        if (log.getResult() != null && !log.getResult().isBlank()) {
            try {
                Map<String, Object> resultMap = objectMapper.readValue(log.getResult(),
                    new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
                dto.setResult(resultMap);
            } catch (JsonProcessingException e) {
                dto.setResult(Map.of("raw", log.getResult()));
            }
        }

        return dto;
    }

    private EventTemplateDTO convertToTemplateDTO(EventTemplate template) {
        EventTemplateDTO dto = new EventTemplateDTO();
        dto.setEventId(template.getEventId());
        dto.setEventType(template.getEventType());
        dto.setTitle(template.getTitle());
        dto.setDescription(template.getDescription());
        dto.setWeight(template.getWeight());

        List<EventChoiceDTO> choiceDTOs = new ArrayList<>();
        if (template.getChoices() != null) {
            for (EventChoice choice : template.getChoices()) {
                EventChoiceDTO choiceDTO = new EventChoiceDTO();
                choiceDTO.setChoiceId(choice.getChoiceId());
                choiceDTO.setText(choice.getText());
                choiceDTO.setCost(choice.getCost());
                choiceDTO.setBaseSuccessRate(choice.getBaseSuccessRate());
                choiceDTOs.add(choiceDTO);
            }
        }
        dto.setChoices(choiceDTOs);

        return dto;
    }

    private School getSchoolBySaveId(Long saveId) {
        LambdaQueryWrapper<School> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(School::getSaveId, saveId);
        return schoolMapper.selectOne(wrapper);
    }

    private int calculateSchoolLevel(School school) {
        if (school == null) return 1;
        int reputation = school.getReputation() != null ? school.getReputation() : 0;
        if (reputation >= 2000) return 12;
        if (reputation >= 1500) return 10;
        if (reputation >= 1000) return 8;
        if (reputation >= 500) return 6;
        if (reputation >= 200) return 4;
        if (reputation >= 100) return 3;
        if (reputation >= 50) return 2;
        return 1;
    }
}
