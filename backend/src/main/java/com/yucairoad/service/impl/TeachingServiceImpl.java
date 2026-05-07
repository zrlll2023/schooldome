package com.yucairoad.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.yucairoad.common.BusinessException;
import com.yucairoad.dto.*;
import com.yucairoad.entity.EventLog;
import com.yucairoad.entity.GameSave;
import com.yucairoad.entity.School;
import com.yucairoad.entity.Student;
import com.yucairoad.entity.Teacher;
import com.yucairoad.mapper.EventLogMapper;
import com.yucairoad.mapper.GameSaveMapper;
import com.yucairoad.mapper.SchoolMapper;
import com.yucairoad.mapper.StudentMapper;
import com.yucairoad.mapper.TeacherMapper;
import com.yucairoad.service.TeachingService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class TeachingServiceImpl implements TeachingService {

    private static final String[] LEVELS = {"初级", "二级", "一级", "高级", "特级"};
    private static final String[] SPECIALTIES = {"文科", "理科", "综合"};
    private static final String[] SURNAMES = {"张", "王", "李", "刘", "陈", "杨", "黄", "赵", "周", "吴"};
    private static final String[] GIVEN_NAMES = {"伟", "芳", "娜", "敏", "静", "强", "磊", "军", "洋", "勇", "艳", "杰", "涛", "明", "超", "秀英", "丽", "桂英", "玲", "飞"};

    private static final int[] LEVEL_EXP_THRESHOLDS = {100, 300, 600, 1000};
    private static final int[][] TEACHING_ABILITY_RANGE = {{25, 39}, {40, 54}, {55, 69}, {70, 84}, {85, 100}};
    private static final BigDecimal[] LEVEL_SALARIES = {
        new BigDecimal("5000"),
        new BigDecimal("7000"),
        new BigDecimal("10000"),
        new BigDecimal("15000"),
        new BigDecimal("20000")
    };

    private static final int HIRE_NORMAL_COST = 5000;
    private static final int HIRE_HEADHUNTER_COST = 50000;
    private static final int TRAIN_REGULAR_COST = 5000;
    private static final int TRAIN_EXTERNAL_COST = 20000;
    private static final int TRAIN_INTERNAL_COST = 2500;
    private static final int FOCUS_STUDENT_COST = 1000;

    private final GameSaveMapper gameSaveMapper;
    private final TeacherMapper teacherMapper;
    private final StudentMapper studentMapper;
    private final SchoolMapper schoolMapper;
    private final EventLogMapper eventLogMapper;
    private final ObjectMapper objectMapper;

    public TeachingServiceImpl(GameSaveMapper gameSaveMapper,
                               TeacherMapper teacherMapper,
                               StudentMapper studentMapper,
                               SchoolMapper schoolMapper,
                               EventLogMapper eventLogMapper) {
        this.gameSaveMapper = gameSaveMapper;
        this.teacherMapper = teacherMapper;
        this.studentMapper = studentMapper;
        this.schoolMapper = schoolMapper;
        this.eventLogMapper = eventLogMapper;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public TeachingPolicy getTeachingPolicy(Long saveId) {
        ObjectNode gameState = loadGameState(saveId);
        if (gameState.has("teachingPolicy")) {
            try {
                return objectMapper.treeToValue(gameState.get("teachingPolicy"), TeachingPolicy.class);
            } catch (JsonProcessingException e) {
                return getDefaultTeachingPolicy();
            }
        }
        return getDefaultTeachingPolicy();
    }

    @Override
    public TeachingPolicy updateTeachingPolicy(Long saveId, TeachingPolicy policy) {
        validateTeachingPolicy(policy);
        ObjectNode gameState = loadGameState(saveId);
        try {
            gameState.set("teachingPolicy", objectMapper.valueToTree(policy));
            persistGameState(saveId, gameState);
        } catch (IllegalArgumentException e) {
            throw new BusinessException("教学政策保存失败");
        }
        return policy;
    }

    @Override
    public Page<TeacherInfo> getTeachers(Long saveId, int page, int size) {
        Long schoolId = getSchoolIdBySaveId(saveId);
        LambdaQueryWrapper<Teacher> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Teacher::getSchoolId, schoolId)
               .orderByDesc(Teacher::getTeachingAbility);

        Page<Teacher> teacherPage = teacherMapper.selectPage(new Page<>(page, size), wrapper);

        Page<TeacherInfo> resultPage = new Page<>(teacherPage.getCurrent(), teacherPage.getSize(), teacherPage.getTotal());
        List<TeacherInfo> teacherInfos = new ArrayList<>();
        for (Teacher teacher : teacherPage.getRecords()) {
            teacherInfos.add(convertToTeacherInfo(teacher));
        }
        resultPage.setRecords(teacherInfos);
        return resultPage;
    }

    @Override
    public TeacherInfo hireTeacher(Long saveId, HireRequest request) {
        String hireType = request.getHireType();
        if (!"NORMAL".equals(hireType) && !"HEADHUNTER".equals(hireType)) {
            throw new BusinessException("无效的招聘类型，仅支持 NORMAL/HEADHUNTER");
        }

        Long schoolId = getSchoolIdBySaveId(saveId);
        School school = schoolMapper.selectById(schoolId);
        if (school == null) {
            throw new BusinessException("学校信息不存在");
        }

        int cost = "HEADHUNTER".equals(hireType) ? HIRE_HEADHUNTER_COST : HIRE_NORMAL_COST;
        BigDecimal currentFunds = school.getFunds();
        if (currentFunds.compareTo(BigDecimal.valueOf(cost)) < 0) {
            throw new BusinessException("资金不足，招聘需要 " + cost + " 元");
        }

        int levelIndex;
        if ("HEADHUNTER".equals(hireType)) {
            levelIndex = resolveTargetLevel(request.getTargetLevel());
            if (Math.random() > 0.8) {
                levelIndex = Math.max(0, levelIndex - 1);
            }
        } else {
            levelIndex = generateLevelByReputation(school.getReputation());
        }

        Teacher teacher = generateTeacher(schoolId, levelIndex);
        teacherMapper.insert(teacher);

        school.setFunds(currentFunds.subtract(BigDecimal.valueOf(cost)));
        school.setTeacherCount((school.getTeacherCount() == null ? 0 : school.getTeacherCount()) + 1);
        schoolMapper.updateById(school);

        return convertToTeacherInfo(teacher);
    }

    @Override
    public TeacherInfo trainTeacher(Long saveId, Long teacherId, TrainRequest request) {
        String trainType = request.getTrainType();
        if (!"REGULAR".equals(trainType) && !"EXTERNAL".equals(trainType) && !"INTERNAL".equals(trainType)) {
            throw new BusinessException("无效的培训类型，仅支持 REGULAR/EXTERNAL/INTERNAL");
        }

        Teacher teacher = teacherMapper.selectById(teacherId);
        if (teacher == null) {
            throw new BusinessException("教师不存在");
        }

        Long schoolId = getSchoolIdBySaveId(saveId);
        School school = schoolMapper.selectById(schoolId);
        if (school == null) {
            throw new BusinessException("学校信息不存在");
        }

        int cost;
        int baseExpMin, baseExpMax;
        double efficiency;

        switch (trainType) {
            case "REGULAR":
                cost = TRAIN_REGULAR_COST;
                baseExpMin = 50;
                baseExpMax = 100;
                efficiency = 1.0;
                break;
            case "EXTERNAL":
                cost = TRAIN_EXTERNAL_COST;
                baseExpMin = 100;
                baseExpMax = 200;
                efficiency = 0.7;
                break;
            case "INTERNAL":
            default:
                cost = TRAIN_INTERNAL_COST;
                baseExpMin = 30;
                baseExpMax = 60;
                efficiency = 1.3;
                break;
        }

        if (school.getFunds().compareTo(BigDecimal.valueOf(cost)) < 0) {
            throw new BusinessException("资金不足，培训需要 " + cost + " 元");
        }

        int expGain = (int) ((baseExpMin + (int) (Math.random() * (baseExpMax - baseExpMin + 1))) * efficiency);
        int currentExp = teacher.getExperience() == null ? 0 : teacher.getExperience();
        int newExp = currentExp + expGain;
        teacher.setExperience(newExp);

        int currentLevelIndex = getLevelIndex(teacher.getLevel());
        if (currentLevelIndex < LEVEL_EXP_THRESHOLDS.length - 1 && newExp >= LEVEL_EXP_THRESHOLDS[currentLevelIndex]) {
            String newLevel = LEVELS[currentLevelIndex + 1];
            teacher.setLevel(newLevel);
            int newAbilityMin = TEACHING_ABILITY_RANGE[currentLevelIndex + 1][0];
            int newAbilityRange = TEACHING_ABILITY_RANGE[currentLevelIndex + 1][1] - TEACHING_ABILITY_RANGE[currentLevelIndex + 1][0];
            teacher.setTeachingAbility(newAbilityMin + (int) (Math.random() * (newAbilityRange + 1)));
            teacher.setSalary(LEVEL_SALARIES[currentLevelIndex + 1]);
        }

        teacherMapper.updateById(teacher);

        school.setFunds(school.getFunds().subtract(BigDecimal.valueOf(cost)));
        schoolMapper.updateById(school);

        return convertToTeacherInfo(teacher);
    }

    @Override
    public void dismissTeacher(Long saveId, Long teacherId) {
        Teacher teacher = teacherMapper.selectById(teacherId);
        if (teacher == null) {
            throw new BusinessException("教师不存在");
        }

        Long schoolId = getSchoolIdBySaveId(saveId);
        School school = schoolMapper.selectById(schoolId);
        if (school == null) {
            throw new BusinessException("学校信息不存在");
        }

        teacherMapper.deleteById(teacherId);

        int currentCount = school.getTeacherCount() == null ? 0 : school.getTeacherCount();
        school.setTeacherCount(Math.max(0, currentCount - 1));

        String level = teacher.getLevel();
        int currentReputation = school.getReputation() == null ? 0 : school.getReputation();
        if ("特级".equals(level)) {
            school.setReputation(currentReputation - 10);
        } else if ("高级".equals(level)) {
            school.setReputation(currentReputation - 5);
        }

        schoolMapper.updateById(school);
    }

    @Override
    public Page<StudentInfo> getStudents(Long saveId, int page, int size, String grade) {
        Long schoolId = getSchoolIdBySaveId(saveId);
        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Student::getSchoolId, schoolId);
        if (grade != null && !grade.isBlank()) {
            wrapper.eq(Student::getGrade, grade);
        }
        wrapper.orderByAsc(Student::getGrade);

        Page<Student> studentPage = studentMapper.selectPage(new Page<>(page, size), wrapper);

        Page<StudentInfo> resultPage = new Page<>(studentPage.getCurrent(), studentPage.getSize(), studentPage.getTotal());
        List<StudentInfo> studentInfos = new ArrayList<>();
        for (Student student : studentPage.getRecords()) {
            studentInfos.add(convertToStudentInfo(student));
        }
        resultPage.setRecords(studentInfos);
        return resultPage;
    }

    @Override
    public void focusStudent(Long saveId, Long studentId) {
        Student student = studentMapper.selectById(studentId);
        if (student == null) {
            throw new BusinessException("学生不存在");
        }

        BigDecimal academicScore = student.getAcademicScore() != null ? student.getAcademicScore() : BigDecimal.ZERO;
        BigDecimal healthScore = student.getHealthScore() != null ? student.getHealthScore() : BigDecimal.ZERO;

        boolean canFocus = academicScore.compareTo(new BigDecimal("30")) < 0
                        || healthScore.compareTo(new BigDecimal("20")) < 0;
        if (!canFocus) {
            throw new BusinessException("该学生不满足重点关注条件（学业<30或身心<20）");
        }

        Long schoolId = getSchoolIdBySaveId(saveId);
        School school = schoolMapper.selectById(schoolId);
        if (school == null) {
            throw new BusinessException("学校信息不存在");
        }

        if (school.getFunds().compareTo(BigDecimal.valueOf(FOCUS_STUDENT_COST)) < 0) {
            throw new BusinessException("资金不足，重点关注需要 " + FOCUS_STUDENT_COST + " 元/人/月");
        }

        int boost = 5 + (int) (Math.random() * 6);

        BigDecimal newAcademic = academicScore.add(BigDecimal.valueOf(boost)).min(new BigDecimal("100"));
        BigDecimal newHealth = healthScore.add(BigDecimal.valueOf(boost)).min(new BigDecimal("100"));
        student.setAcademicScore(newAcademic);
        student.setQualityScore(
            student.getQualityScore() != null
                ? student.getQualityScore().add(BigDecimal.valueOf(boost)).min(new BigDecimal("100"))
                : BigDecimal.valueOf(boost)
        );
        student.setHealthScore(newHealth);
        studentMapper.updateById(student);

        school.setFunds(school.getFunds().subtract(BigDecimal.valueOf(FOCUS_STUDENT_COST)));
        schoolMapper.updateById(school);
    }

    @Override
    public TeachingPrediction getPrediction(Long saveId) {
        TeachingPolicy policy = getTeachingPolicy(saveId);
        Long schoolId = getSchoolIdBySaveId(saveId);

        LambdaQueryWrapper<Teacher> teacherWrapper = new LambdaQueryWrapper<>();
        teacherWrapper.eq(Teacher::getSchoolId, schoolId);
        List<Teacher> teachers = teacherMapper.selectList(teacherWrapper);

        double avgTeacherAbility = 50.0;
        if (teachers != null && !teachers.isEmpty()) {
            double sum = 0;
            for (Teacher t : teachers) {
                sum += t.getTeachingAbility() != null ? t.getTeachingAbility() : 50;
            }
            avgTeacherAbility = sum / teachers.size();
        }

        double facilityBonus = calculateFacilityBonus(saveId);

        double styleScoreWeight = getStyleScoreWeight(policy.getTeachingStyle());
        double styleQualityWeight = getStyleQualityWeight(policy.getTeachingStyle());

        double homeworkBonus = getHomeworkBonus(policy.getHomeworkLoad());

        double weekendScoreEffect = getWeekendScoreEffect(policy.getWeekendArrangement());

        double predictedExamScore = avgTeacherAbility * facilityBonus * styleScoreWeight
                * homeworkBonus * weekendScoreEffect * 0.65 + 30;
        predictedExamScore = Math.min(100, Math.max(0, predictedExamScore));

        double pressureBase = 40.0;
        double pressureFromHomework = getHomeworkPressure(policy.getHomeworkLoad());
        double pressureFromWeekend = getWeekendPressure(policy.getWeekendArrangement());
        double pressureIndex = pressureBase + pressureFromHomework + pressureFromWeekend;
        pressureIndex = Math.min(100, Math.max(0, pressureIndex));

        double qualityBase = 50.0;
        double qualityFromExtracurricular = getExtracurricularQuality(policy.getExtracurricular());
        double qualityFromStyle = styleQualityWeight * 15;
        double qualityIndex = qualityBase + qualityFromExtracurricular + qualityFromStyle;
        qualityIndex = Math.min(100, Math.max(0, qualityIndex));

        TeachingPrediction prediction = new TeachingPrediction();
        prediction.setPredictedExamScore(Math.round(predictedExamScore * 10.0) / 10.0);
        prediction.setPressureIndex(Math.round(pressureIndex * 10.0) / 10.0);
        prediction.setQualityIndex(Math.round(qualityIndex * 10.0) / 10.0);
        return prediction;
    }

    private ObjectNode loadGameState(Long saveId) {
        GameSave save = gameSaveMapper.selectById(saveId);
        if (save == null) {
            throw new BusinessException("存档不存在");
        }
        String gameStateJson = save.getGameState();
        if (gameStateJson == null || gameStateJson.isBlank()) {
            return objectMapper.createObjectNode();
        }
        try {
            return objectMapper.readValue(gameStateJson, ObjectNode.class);
        } catch (JsonProcessingException e) {
            return objectMapper.createObjectNode();
        }
    }

    private void persistGameState(Long saveId, ObjectNode gameState) {
        try {
            String json = objectMapper.writeValueAsString(gameState);
            GameSave save = gameSaveMapper.selectById(saveId);
            if (save != null) {
                save.setGameState(json);
                save.setUpdatedAt(LocalDateTime.now());
                gameSaveMapper.updateById(save);
            }
        } catch (JsonProcessingException e) {
            throw new BusinessException("游戏状态保存失败");
        }
    }

    private TeachingPolicy getDefaultTeachingPolicy() {
        TeachingPolicy policy = new TeachingPolicy();
        policy.setTeachingStyle("BALANCED");
        policy.setHomeworkLoad("MODERATE");
        policy.setWeekendArrangement("HALF_DAY");
        policy.setExtracurricular("MODERATE");
        policy.setCompetitionTraining("MODERATE");
        return policy;
    }

    private void validateTeachingPolicy(TeachingPolicy policy) {
        Set<String> validStyles = Set.of("EXAM", "QUALITY", "BALANCED");
        Set<String> validLoads = Set.of("LIGHT", "MODERATE", "HEAVY");
        Set<String> validWeekends = Set.of("REST", "HALF_DAY", "FULL_DAY");
        Set<String> validExtras = Set.of("SIMPLE", "MODERATE", "RICH");
        Set<String> validCompetitions = Set.of("NONE", "MODERATE", "INTENSIVE");

        if (policy.getTeachingStyle() == null || !validStyles.contains(policy.getTeachingStyle())) {
            throw new BusinessException("无效的教学风格，仅支持 EXAM/QUALITY/BALANCED");
        }
        if (policy.getHomeworkLoad() == null || !validLoads.contains(policy.getHomeworkLoad())) {
            throw new BusinessException("无效的作业量设置，仅支持 LIGHT/MODERATE/HEAVY");
        }
        if (policy.getWeekendArrangement() == null || !validWeekends.contains(policy.getWeekendArrangement())) {
            throw new BusinessException("无效的周末安排，仅支持 REST/HALF_DAY/FULL_DAY");
        }
        if (policy.getExtracurricular() == null || !validExtras.contains(policy.getExtracurricular())) {
            throw new BusinessException("无效的课外活动设置，仅支持 SIMPLE/MODERATE/RICH");
        }
        if (policy.getCompetitionTraining() == null || !validCompetitions.contains(policy.getCompetitionTraining())) {
            throw new BusinessException("无效的竞赛培训设置，仅支持 NONE/MODERATE/INTENSIVE");
        }
    }

    private Long getSchoolIdBySaveId(Long saveId) {
        LambdaQueryWrapper<School> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(School::getSaveId, saveId);
        School school = schoolMapper.selectOne(wrapper);
        if (school == null) {
            throw new BusinessException("未找到关联的学校信息");
        }
        return school.getId();
    }

    private TeacherInfo convertToTeacherInfo(Teacher teacher) {
        TeacherInfo info = new TeacherInfo();
        info.setId(teacher.getId());
        info.setName(teacher.getName());
        info.setLevel(teacher.getLevel());
        info.setTeachingAbility(teacher.getTeachingAbility());
        info.setMoralLevel(teacher.getMoralLevel());
        info.setSpecialty(teacher.getSpecialty());
        info.setSalary(teacher.getSalary());
        info.setExperience(teacher.getExperience());
        info.setHireYear(teacher.getHireYear());
        return info;
    }

    private StudentInfo convertToStudentInfo(Student student) {
        StudentInfo info = new StudentInfo();
        info.setId(student.getId());
        info.setName(student.getName());
        info.setGrade(student.getGrade());
        info.setGradeLevel(student.getGradeLevel());
        info.setAcademicScore(student.getAcademicScore());
        info.setQualityScore(student.getQualityScore());
        info.setHealthScore(student.getHealthScore());
        return info;
    }

    private Teacher generateTeacher(Long schoolId, int levelIndex) {
        Teacher teacher = new Teacher();
        teacher.setSchoolId(schoolId);
        teacher.setName(generateRandomName());
        teacher.setLevel(LEVELS[levelIndex]);
        int abilityMin = TEACHING_ABILITY_RANGE[levelIndex][0];
        int abilityRange = TEACHING_ABILITY_RANGE[levelIndex][1] - TEACHING_ABILITY_RANGE[levelIndex][0];
        teacher.setTeachingAbility(abilityMin + (int) (Math.random() * (abilityRange + 1)));
        teacher.setMoralLevel(60 + (int) (Math.random() * 36));
        teacher.setSpecialty(SPECIALTIES[(int) (Math.random() * SPECIALTIES.length)]);
        teacher.setSalary(LEVEL_SALARIES[levelIndex]);
        teacher.setExperience(0);
        teacher.setHireYear(LocalDate.now().getYear());
        teacher.setCreatedAt(LocalDateTime.now());
        teacher.setUpdatedAt(LocalDateTime.now());
        return teacher;
    }

    private String generateRandomName() {
        String surname = SURNAMES[(int) (Math.random() * SURNAMES.length)];
        String givenName = GIVEN_NAMES[(int) (Math.random() * GIVEN_NAMES.length)];
        return surname + givenName;
    }

    private int generateLevelByReputation(Integer reputation) {
        int rep = reputation == null ? 0 : reputation;
        double rand = Math.random() * 100;
        if (rep >= 80) {
            if (rand < 15) return 4;
            if (rand < 35) return 3;
            if (rand < 65) return 2;
            if (rand < 88) return 1;
            return 0;
        } else if (rep >= 50) {
            if (rand < 5) return 4;
            if (rand < 18) return 3;
            if (rand < 45) return 2;
            if (rand < 78) return 1;
            return 0;
        } else if (rep >= 20) {
            if (rand < 2) return 4;
            if (rand < 10) return 3;
            if (rand < 35) return 2;
            if (rand < 70) return 1;
            return 0;
        } else {
            if (rand < 1) return 4;
            if (rand < 5) return 3;
            if (rand < 22) return 2;
            if (rand < 58) return 1;
            return 0;
        }
    }

    private int resolveTargetLevel(String targetLevel) {
        if (targetLevel == null) return 2;
        switch (targetLevel.toUpperCase()) {
            case "特级": return 4;
            case "高级": return 3;
            case "一级": return 2;
            case "二级": return 1;
            case "初级": return 0;
            default: return 2;
        }
    }

    private int getLevelIndex(String level) {
        for (int i = 0; i < LEVELS.length; i++) {
            if (LEVELS[i].equals(level)) return i;
        }
        return 0;
    }

    private double calculateFacilityBonus(Long saveId) {
        ObjectNode gameState = loadGameState(saveId);
        double bonus = 1.0;
        if (gameState.has("school") && gameState.get("school").has("buildings")) {
            try {
                var buildings = objectMapper.treeToValue(gameState.get("school").get("buildings"),
                    new TypeReference<List<Map<String, Object>>>() {});
                if (buildings != null) {
                    for (Map<String, Object> b : buildings) {
                        Integer lvl = b.get("level") != null ? ((Number) b.get("level")).intValue() : 1;
                        bonus += (lvl - 1) * 0.05;
                    }
                }
            } catch (JsonProcessingException e) {
                bonus = 1.0;
            }
        }
        return Math.min(bonus, 1.5);
    }

    private double getStyleScoreWeight(String style) {
        if (style == null) return 1.0;
        switch (style) {
            case "EXAM": return 1.3;
            case "QUALITY": return 0.7;
            case "BALANCED":
            default: return 1.0;
        }
    }

    private double getStyleQualityWeight(String style) {
        if (style == null) return 1.0;
        switch (style) {
            case "EXAM": return 0.7;
            case "QUALITY": return 1.3;
            case "BALANCED":
            default: return 1.0;
        }
    }

    private double getHomeworkBonus(String load) {
        if (load == null) return 1.1;
        switch (load) {
            case "LIGHT": return 1.10;
            case "MODERATE": return 1.20;
            case "HEAVY": return 1.30;
            default: return 1.1;
        }
    }

    private double getWeekendScoreEffect(String arrangement) {
        if (arrangement == null) return 1.0;
        switch (arrangement) {
            case "REST": return 0.95;
            case "HALF_DAY": return 1.0;
            case "FULL_DAY": return 1.10;
            default: return 1.0;
        }
    }

    private double getHomeworkPressure(String load) {
        if (load == null) return 10.0;
        switch (load) {
            case "LIGHT": return 10.0;
            case "MODERATE": return 20.0;
            case "HEAVY": return 40.0;
            default: return 10.0;
        }
    }

    private double getWeekendPressure(String arrangement) {
        if (arrangement == null) return 0.0;
        switch (arrangement) {
            case "REST": return -20.0;
            case "HALF_DAY": return 0.0;
            case "FULL_DAY": return 15.0;
            default: return 0.0;
        }
    }

    private double getExtracurricularQuality(String extracurricular) {
        if (extracurricular == null) return 7.5;
        switch (extracurricular) {
            case "SIMPLE": return 15.0;
            case "MODERATE": return 7.5;
            case "RICH": return 0.0;
            default: return 7.5;
        }
    }

    @Override
    public DisciplineResult disciplineStudent(Long saveId, Long studentId, DisciplineRequest request) {
        Student student = studentMapper.selectById(studentId);
        if (student == null) {
            throw new BusinessException("学生不存在");
        }

        Long schoolId = getSchoolIdBySaveId(saveId);

        LambdaQueryWrapper<EventLog> eventWrapper = new LambdaQueryWrapper<>();
        eventWrapper.eq(EventLog::getSaveId, saveId)
                   .eq(EventLog::getEventType, "STUDENT_VIOLATION")
                   .eq(EventLog::getResult, String.valueOf(studentId));
        Long violationCount = eventLogMapper.selectCount(eventWrapper);
        if (violationCount == null || violationCount == 0) {
            throw new BusinessException("该学生未触发违纪事件，无法进行纪律处分");
        }

        String reason = request != null && request.getReason() != null ? request.getReason() : "违纪行为";

        BigDecimal currentAcademic = student.getAcademicScore() != null ? student.getAcademicScore() : BigDecimal.valueOf(60);
        BigDecimal currentHealth = student.getHealthScore() != null ? student.getHealthScore() : BigDecimal.valueOf(80);

        BigDecimal newAcademic = currentAcademic.subtract(BigDecimal.TEN).max(BigDecimal.ZERO);
        BigDecimal newHealth = currentHealth.subtract(new BigDecimal("20")).max(BigDecimal.ZERO);
        student.setAcademicScore(newAcademic);
        student.setHealthScore(newHealth);
        studentMapper.updateById(student);

        EventLog disciplineLog = new EventLog();
        disciplineLog.setSaveId(saveId);
        disciplineLog.setEventType("DISCIPLINE");
        disciplineLog.setEventTitle("纪律处分 - " + student.getName());
        disciplineLog.setEventDescription("对" + student.getName() + "执行纪律处分，原因：" + reason);
        disciplineLog.setResult("{\"studentId\":" + studentId + ",\"academicChange\":-10,\"healthChange\":-20}");
        disciplineLog.setTriggerYear(LocalDate.now().getYear());
        disciplineLog.setTriggerMonth(LocalDate.now().getMonthValue());
        disciplineLog.setCreatedAt(LocalDateTime.now());
        eventLogMapper.insert(disciplineLog);

        School school = schoolMapper.selectById(schoolId);
        if (school != null) {
            LambdaQueryWrapper<Student> allStudentsWrapper = new LambdaQueryWrapper<>();
            allStudentsWrapper.eq(Student::getSchoolId, schoolId);
            List<Student> allStudents = studentMapper.selectList(allStudentsWrapper);
            for (Student s : allStudents) {
                if (!s.getId().equals(studentId)) {
                    BigDecimal sHealth = s.getHealthScore() != null ? s.getHealthScore() : BigDecimal.valueOf(80);
                    s.setHealthScore(sHealth.add(BigDecimal.ONE).min(new BigDecimal("100")));
                    studentMapper.updateById(s);
                }
            }
        }

        DisciplineResult result = new DisciplineResult();
        result.setStudentId(studentId);
        result.setStudentName(student.getName());
        result.setNewAcademicScore(newAcademic);
        result.setNewHealthScore(newHealth);
        result.setDisciplineReason(reason);
        result.setMessage("已对学生" + student.getName() + "执行纪律处分，学业-10，身心-20。全校其他学生身心+1（震慑效果）");
        return result;
    }

    @Override
    public ExpelResult expelStudent(Long saveId, Long studentId) {
        Student student = studentMapper.selectById(studentId);
        if (student == null) {
            throw new BusinessException("学生不存在");
        }

        BigDecimal academicScore = student.getAcademicScore() != null ? student.getAcademicScore() : BigDecimal.valueOf(60);
        if (academicScore.compareTo(new BigDecimal("20")) >= 0) {
            throw new BusinessException("该学生学业成绩不低于20分，不满足劝退条件（需academicScore < 20）");
        }

        Long schoolId = getSchoolIdBySaveId(saveId);
        int consecutiveMonthsBelow30 = checkConsecutiveLowAcademic(studentId, saveId);
        if (consecutiveMonthsBelow30 < 2) {
            throw new BusinessException("该学生连续" + consecutiveMonthsBelow30 + "个月学业低于30分，不满足劝退条件（需连续2个月<30）");
        }

        student.setStatus("劝退");
        studentMapper.updateById(student);

        School school = schoolMapper.selectById(schoolId);
        if (school != null) {
            int currentReputation = school.getReputation() != null ? school.getReputation() : 0;
            school.setReputation(currentReputation - 10);
            int currentStudentCount = school.getStudentCount() != null ? school.getStudentCount() : 0;
            school.setStudentCount(Math.max(0, currentStudentCount - 1));
            schoolMapper.updateById(school);
        }

        ExpelResult result = new ExpelResult();
        result.setStudentId(studentId);
        result.setStudentName(student.getName());
        result.setNewReputation(school != null ? school.getReputation() : 0);
        result.setNewStudentCount(school != null ? school.getStudentCount() : 0);
        result.setMessage("已将学生" + student.getName() + "劝退转学，学校声望-10，学生数-1");
        return result;
    }

    private int checkConsecutiveLowAcademic(Long studentId, Long saveId) {
        LambdaQueryWrapper<EventLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EventLog::getSaveId, saveId)
               .eq(EventLog::getEventType, "ACADEMIC_UPDATE")
               .like(EventLog::getResult, "\"studentId\":" + studentId)
               .orderByDesc(EventLog::getCreatedAt);
        List<EventLog> logs = eventLogMapper.selectList(wrapper);

        int consecutiveCount = 0;
        for (EventLog log : logs) {
            try {
                Map<String, Object> resultMap = objectMapper.readValue(log.getResult(), new TypeReference<Map<String, Object>>() {});
                Double score = resultMap.get("academicScore") != null ?
                    ((Number) resultMap.get("academicScore")).doubleValue() : null;
                if (score != null && score < 30) {
                    consecutiveCount++;
                } else {
                    break;
                }
            } catch (Exception e) {
                break;
            }
        }
        return consecutiveCount;
    }

    @Override
    public Page<StudentInfo> getAtRiskStudents(Long saveId, int page, int size) {
        Long schoolId = getSchoolIdBySaveId(saveId);

        List<Student> atRiskList = new ArrayList<>();
        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Student::getSchoolId, schoolId);
        List<Student> allStudents = studentMapper.selectList(wrapper);

        for (Student student : allStudents) {
            BigDecimal academic = student.getAcademicScore() != null ? student.getAcademicScore() : BigDecimal.valueOf(60);
            BigDecimal health = student.getHealthScore() != null ? student.getHealthScore() : BigDecimal.valueOf(80);
            if (academic.compareTo(new BigDecimal("30")) < 0 || health.compareTo(new BigDecimal("20")) < 0) {
                atRiskList.add(student);
            }
        }

        int start = (page - 1) * size;
        int end = Math.min(start + size, atRiskList.size());
        List<Student> pageList = start < atRiskList.size() ? atRiskList.subList(start, end) : new ArrayList<>();

        Page<StudentInfo> resultPage = new Page<>(page, size, atRiskList.size());
        List<StudentInfo> studentInfos = new ArrayList<>();
        for (Student student : pageList) {
            studentInfos.add(convertToStudentInfo(student));
        }
        resultPage.setRecords(studentInfos);
        return resultPage;
    }

    @Override
    public StudentStatisticsDTO getStudentStatistics(Long saveId) {
        Long schoolId = getSchoolIdBySaveId(saveId);

        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Student::getSchoolId, schoolId);
        List<Student> students = studentMapper.selectList(wrapper);

        StudentStatisticsDTO stats = new StudentStatisticsDTO();
        stats.setTotalStudents((long) students.size());

        if (!students.isEmpty()) {
            double sumAcademic = 0, sumQuality = 0, sumHealth = 0;
            Map<String, Integer> gradeDistribution = new HashMap<>();
            gradeDistribution.put("S", 0);
            gradeDistribution.put("A", 0);
            gradeDistribution.put("B", 0);
            gradeDistribution.put("C", 0);
            gradeDistribution.put("D", 0);
            int atRiskCount = 0;

            for (Student student : students) {
                BigDecimal academic = student.getAcademicScore() != null ? student.getAcademicScore() : BigDecimal.valueOf(60);
                BigDecimal quality = student.getQualityScore() != null ? student.getQualityScore() : BigDecimal.valueOf(60);
                BigDecimal health = student.getHealthScore() != null ? student.getHealthScore() : BigDecimal.valueOf(80);

                sumAcademic += academic.doubleValue();
                sumQuality += quality.doubleValue();
                sumHealth += health.doubleValue();

                String level = student.getGradeLevel();
                if (level != null && gradeDistribution.containsKey(level)) {
                    gradeDistribution.merge(level, 1, Integer::sum);
                } else {
                    gradeDistribution.merge("B", 1, Integer::sum);
                }

                if (academic.compareTo(new BigDecimal("30")) < 0 || health.compareTo(new BigDecimal("20")) < 0) {
                    atRiskCount++;
                }
            }

            stats.setAvgAcademic(Math.round(sumAcademic / students.size() * 10.0) / 10.0);
            stats.setAvgQuality(Math.round(sumQuality / students.size() * 10.0) / 10.0);
            stats.setAvgHealth(Math.round(sumHealth / students.size() * 10.0) / 10.0);
            stats.setGradeDistribution(gradeDistribution);
            stats.setAtRiskCount(atRiskCount);
            stats.setExcellentCount(gradeDistribution.getOrDefault("S", 0));
        } else {
            stats.setAvgAcademic(0.0);
            stats.setAvgQuality(0.0);
            stats.setAvgHealth(0.0);
            stats.setGradeDistribution(Map.of("S", 0, "A", 0, "B", 0, "C", 0, "D", 0));
            stats.setAtRiskCount(0);
            stats.setExcellentCount(0);
        }

        return stats;
    }
}
