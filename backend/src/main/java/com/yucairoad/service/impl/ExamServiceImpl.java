package com.yucairoad.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yucairoad.common.BusinessException;
import com.yucairoad.dto.*;
import com.yucairoad.entity.*;
import com.yucairoad.mapper.*;
import com.yucairoad.service.ExamService;
import com.yucairoad.service.FormulaEngineService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ExamServiceImpl implements ExamService {

    private static final int GAOKAO_MAX_SCORE = 750;
    private static final double CITY_AVG_SCORE = 70.0;
    private static final int MAX_FUND_REWARD = 2000000;
    private static final int FUND_PER_POINT = 1000;

    private final StudentMapper studentMapper;
    private final TeacherMapper teacherMapper;
    private final BuildingMapper buildingMapper;
    private final SchoolMapper schoolMapper;
    private final ExamRecordMapper examRecordMapper;
    private final AlumniMapper alumniMapper;
    private final EventLogMapper eventLogMapper;
    private final FormulaEngineService formulaEngineService;

    public ExamServiceImpl(StudentMapper studentMapper,
                           TeacherMapper teacherMapper,
                           BuildingMapper buildingMapper,
                           SchoolMapper schoolMapper,
                           ExamRecordMapper examRecordMapper,
                           AlumniMapper alumniMapper,
                           EventLogMapper eventLogMapper,
                           FormulaEngineService formulaEngineService) {
        this.studentMapper = studentMapper;
        this.teacherMapper = teacherMapper;
        this.buildingMapper = buildingMapper;
        this.schoolMapper = schoolMapper;
        this.examRecordMapper = examRecordMapper;
        this.alumniMapper = alumniMapper;
        this.eventLogMapper = eventLogMapper;
        this.formulaEngineService = formulaEngineService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processMonthlyExam(Long saveId) {
        log.info("开始处理月考, saveId: {}", saveId);
        School school = getSchoolBySaveId(saveId);
        List<Student> students = getActiveStudents(saveId);
        if (students.isEmpty()) {
            log.warn("没有在校学生,跳过月考");
            return;
        }

        List<Teacher> teachers = getTeachers(saveId);
        List<Building> buildings = getBuildings(saveId);
        TeachingPolicy policy = getTeachingPolicy(school);

        List<ExamResultDTO> results = new ArrayList<>();
        for (Student student : students) {
            ScoreResult scoreResult = formulaEngineService.calculateScore(student, teachers, buildings, policy);
            BigDecimal examScore = convertToExamScore(scoreResult.getFinalScore(), "MONTHLY");

            adjustStudentAcademicScore(student, examScore, 5);
            studentMapper.updateById(student);

            ExamResultDTO dto = buildExamResultDTO(student, examScore, 0, "MONTHLY", false);
            results.add(dto);

            saveExamRecord(school.getId(), student.getId(), "MONTHLY", examScore, 0, null);
        }

        Collections.sort(results, Comparator.comparing(ExamResultDTO::getScore).reversed());
        assignRanksAndTriggerEvents(results, saveId, school);

        log.info("月考处理完成,参与学生数: {}", students.size());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processFinalExam(Long saveId) {
        log.info("开始处理期末考, saveId: {}", saveId);
        School school = getSchoolBySaveId(saveId);
        List<Student> students = getActiveStudents(saveId);
        if (students.isEmpty()) {
            log.warn("没有在校学生,跳过期末考");
            return;
        }

        List<Teacher> teachers = getTeachers(saveId);
        List<Building> buildings = getBuildings(saveId);
        TeachingPolicy policy = getTeachingPolicy(school);

        List<ExamResultDTO> results = new ArrayList<>();
        for (Student student : students) {
            ScoreResult scoreResult = formulaEngineService.calculateScore(student, teachers, buildings, policy);
            BigDecimal examScore = convertToExamScore(scoreResult.getFinalScore(), "FINAL");

            adjustStudentAcademicScore(student, examScore, 15);
            studentMapper.updateById(student);

            ExamResultDTO dto = buildExamResultDTO(student, examScore, 0, "FINAL", false);
            results.add(dto);

            saveExamRecord(school.getId(), student.getId(), "FINAL", examScore, 0, null);
        }

        Collections.sort(results, Comparator.comparing(ExamResultDTO::getScore).reversed());
        assignRanks(results);

        processGradePromotionOrRetention(students, results, saveId);

        BigDecimal avgScore = calculateAverageScore(results);
        updateReputationForFinalExam(school, avgScore);

        log.info("期末考处理完成,参与学生数: {},平均分: {}", students.size(), avgScore);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processGaokao(Long saveId) {
        log.info("开始处理高考, saveId: {}", saveId);
        School school = getSchoolBySaveId(saveId);
        List<Student> grade12Students = getStudentsByGrade(saveId, "高三");
        if (grade12Students.isEmpty()) {
            log.warn("没有高三学生,跳过高考");
            return;
        }

        List<Teacher> teachers = getTeachers(saveId);
        List<Building> buildings = getBuildings(saveId);
        TeachingPolicy policy = getTeachingPolicy(school);

        List<ExamResultDTO> results = new ArrayList<>();
        boolean hasTopScholar = false;
        int totalReputationChange = 0;

        for (Student student : grade12Students) {
            ScoreResult scoreResult = formulaEngineService.calculateScore(student, teachers, buildings, policy);
            BigDecimal gaokaoScore = convertToGaokaoScore(scoreResult.getFinalScore());

            boolean isTopScholar = gaokaoScore.compareTo(new BigDecimal("700")) > 0;
            if (isTopScholar) {
                hasTopScholar = true;
            }

            int reputationFromStudent = calculateGaokaoReputation(gaokaoScore);
            totalReputationChange += reputationFromStudent;

            String achievementLevel = determineAchievementLevel(gaokaoScore);
            createAlumniRecord(saveId, student, achievementLevel);

            ExamResultDTO dto = buildExamResultDTO(student, gaokaoScore, 0, "GAOKAO", isTopScholar);
            results.add(dto);

            Integer topScholarFlag = isTopScholar ? 1 : 0;
            saveExamRecord(school.getId(), student.getId(), "GAOKAO", gaokaoScore, 0, topScholarFlag);
        }

        Collections.sort(results, Comparator.comparing(ExamResultDTO::getScore).reversed());
        assignRanks(results);

        BigDecimal avgScore = calculateAverageScore(results);
        int bonusReputation = calculateAverageScoreBonus(avgScore);
        totalReputationChange += bonusReputation;

        BigDecimal fundReward = calculateFundReward(avgScore);
        updateSchoolFundsAndReputation(school, fundReward, totalReputationChange);

        updateGraduatedStudentStatus(grade12Students);

        log.info("高考处理完成,参与学生数: {},平均分: {},声望变化: +{},资金奖励: {}",
                grade12Students.size(), avgScore, totalReputationChange, fundReward);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processMiddleSchoolExam(Long saveId) {
        log.info("开始处理中考, saveId: {}", saveId);
        School school = getSchoolBySaveId(saveId);
        List<Student> grade9Students = getStudentsByGrade(saveId, "初三");
        if (grade9Students.isEmpty()) {
            log.warn("没有初三学生,跳过中考");
            return;
        }

        List<Teacher> teachers = getTeachers(saveId);
        List<Building> buildings = getBuildings(saveId);
        TeachingPolicy policy = getTeachingPolicy(school);

        List<ExamResultDTO> results = new ArrayList<>();
        int totalReputationChange = 0;
        Random random = new Random();

        for (Student student : grade9Students) {
            ScoreResult scoreResult = formulaEngineService.calculateScore(student, teachers, buildings, policy);
            BigDecimal examScore = convertToExamScore(scoreResult.getFinalScore(), "MIDDLE_SCHOOL");

            String admissionResult = determineAdmissionResult(examScore, random);
            int reputationFromStudent = calculateMiddleSchoolReputation(admissionResult);
            totalReputationChange += reputationFromStudent;

            ExamResultDTO dto = buildExamResultDTO(student, examScore, 0, "MIDDLE_SCHOOL", false);
            results.add(dto);

            saveExamRecord(school.getId(), student.getId(), "MIDDLE_SCHOOL", examScore, 0, null);
        }

        Collections.sort(results, Comparator.comparing(ExamResultDTO::getScore).reversed());
        assignRanks(results);

        updateSchoolReputation(school, totalReputationChange);
        updateGraduatedStudentStatus(grade9Students);

        log.info("中考处理完成,参与学生数: {},声望变化: +{}", grade9Students.size(), totalReputationChange);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processPrimarySchoolExam(Long saveId) {
        log.info("开始处理小升初考试, saveId: {}", saveId);
        School school = getSchoolBySaveId(saveId);
        List<Student> grade6Students = getStudentsByGrade(saveId, "小学六年级");
        if (grade6Students.isEmpty()) {
            log.warn("没有小学六年级学生,跳过小升初考试");
            return;
        }

        List<Teacher> teachers = getTeachers(saveId);
        List<Building> buildings = getBuildings(saveId);
        TeachingPolicy policy = getTeachingPolicy(school);

        List<ExamResultDTO> results = new ArrayList<>();
        int totalReputationChange = 0;
        Random random = new Random();

        for (Student student : grade6Students) {
            ScoreResult scoreResult = formulaEngineService.calculateScore(student, teachers, buildings, policy);
            BigDecimal examScore = convertToExamScore(scoreResult.getFinalScore(), "PRIMARY_SCHOOL");

            String admissionResult = determineAdmissionResult(examScore, random);
            int reputationFromStudent = calculatePrimarySchoolReputation(admissionResult);
            totalReputationChange += reputationFromStudent;

            ExamResultDTO dto = buildExamResultDTO(student, examScore, 0, "PRIMARY_SCHOOL", false);
            results.add(dto);

            saveExamRecord(school.getId(), student.getId(), "PRIMARY_SCHOOL", examScore, 0, null);
        }

        Collections.sort(results, Comparator.comparing(ExamResultDTO::getScore).reversed());
        assignRanks(results);

        updateSchoolReputation(school, totalReputationChange);
        updateGraduatedStudentStatus(grade6Students);

        log.info("小升初处理完成,参与学生数: {},声望变化: +{}", grade6Students.size(), totalReputationChange);
    }

    @Override
    public List<ExamResultDTO> getExamResults(Long saveId, String examType, Integer year) {
        School school = getSchoolBySaveId(saveId);
        LambdaQueryWrapper<ExamRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExamRecord::getSchoolId, school.getId())
               .eq(ExamRecord::getExamType, examType)
               .eq(year != null, ExamRecord::getExamYear, year)
               .orderByDesc(ExamRecord::getScore);

        List<ExamRecord> records = examRecordMapper.selectList(wrapper);
        return records.stream().map(this::convertToExamResultDTO).collect(Collectors.toList());
    }

    @Override
    public Page<ExamHistoryDTO> getExamHistory(Long saveId, Long studentId, int page, int size) {
        School school = getSchoolBySaveId(saveId);
        Page<ExamRecord> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<ExamRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExamRecord::getSchoolId, school.getId())
               .eq(studentId != null, ExamRecord::getStudentId, studentId)
               .orderByDesc(ExamRecord::getCreatedAt);

        Page<ExamRecord> recordPage = examRecordMapper.selectPage(pageParam, wrapper);
        Page<ExamHistoryDTO> dtoPage = new Page<>(recordPage.getCurrent(), recordPage.getSize(), recordPage.getTotal());

        List<ExamHistoryDTO> dtoList = recordPage.getRecords().stream()
                .map(this::convertToExamHistoryDTO)
                .collect(Collectors.toList());
        dtoPage.setRecords(dtoList);

        return dtoPage;
    }

    @Override
    public List<RankingDTO> getRankings(Long saveId, String examType, Integer year) {
        School school = getSchoolBySaveId(saveId);
        LambdaQueryWrapper<ExamRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExamRecord::getSchoolId, school.getId())
               .eq(examType != null, ExamRecord::getExamType, examType)
               .eq(year != null, ExamRecord::getExamYear, year)
               .isNotNull(ExamRecord::getRank)
               .orderByAsc(ExamRecord::getRank);

        List<ExamRecord> records = examRecordMapper.selectList(wrapper);
        Map<Long, String> studentNameMap = getStudentNameMap(records.stream()
                .map(ExamRecord::getStudentId).collect(Collectors.toSet()));

        return records.stream().map(record -> {
            RankingDTO dto = new RankingDTO();
            dto.setRank(record.getRank());
            dto.setScore(record.getScore());
            dto.setStudentName(studentNameMap.getOrDefault(record.getStudentId(), "未知"));
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public ExamSummaryDTO getExamSummary(Long saveId, String examType, Integer year) {
        School school = getSchoolBySaveId(saveId);
        LambdaQueryWrapper<ExamRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExamRecord::getSchoolId, school.getId())
               .eq(examType != null, ExamRecord::getExamType, examType)
               .eq(year != null, ExamRecord::getExamYear, year);

        List<ExamRecord> records = examRecordMapper.selectList(wrapper);
        ExamSummaryDTO summary = new ExamSummaryDTO();
        summary.setExamType(examType);
        summary.setTotalStudents(records.size());

        if (records.isEmpty()) {
            summary.setAvgScore(BigDecimal.ZERO);
            summary.setMaxScore(BigDecimal.ZERO);
            summary.setMinScore(BigDecimal.ZERO);
            summary.setPassRate(BigDecimal.ZERO);
            summary.setExcellenceRate(BigDecimal.ZERO);
            return summary;
        }

        List<BigDecimal> scores = records.stream()
                .map(ExamRecord::getScore)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        BigDecimal sum = scores.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal avgScore = sum.divide(BigDecimal.valueOf(scores.size()), 2, RoundingMode.HALF_UP);
        summary.setAvgScore(avgScore);
        summary.setMaxScore(scores.stream().max(BigDecimal::compareTo).orElse(BigDecimal.ZERO));
        summary.setMinScore(scores.stream().min(BigDecimal::compareTo).orElse(BigDecimal.ZERO));

        long passCount = scores.stream().filter(s -> s.compareTo(new BigDecimal("60")) >= 0).count();
        long excellentCount = scores.stream().filter(s -> s.compareTo(new BigDecimal("90")) >= 0).count();
        summary.setPassRate(BigDecimal.valueOf(passCount * 100.0 / scores.size()).setScale(2, RoundingMode.HALF_UP));
        summary.setExcellenceRate(BigDecimal.valueOf(excellentCount * 100.0 / scores.size()).setScale(2, RoundingMode.HALF_UP));

        return summary;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExamTriggerResult triggerExam(Long saveId, String examType) {
        log.info("手动触发考试, saveId: {}, examType: {}", saveId, examType);
        ExamTriggerResult result = new ExamTriggerResult();
        result.setExamType(examType);

        switch (examType.toUpperCase()) {
            case "GAOKAO":
                processGaokao(saveId);
                break;
            case "MIDDLE_SCHOOL":
                processMiddleSchoolExam(saveId);
                break;
            case "PRIMARY_SCHOOL":
                processPrimarySchoolExam(saveId);
                break;
            case "FINAL":
                processFinalExam(saveId);
                break;
            case "MONTHLY":
                processMonthlyExam(saveId);
                break;
            default:
                throw new BusinessException("不支持的考试类型: " + examType);
        }

        result.setMessage(examType + "考试执行成功");
        result.setResults(getExamResults(saveId, examType, null));
        result.setSummary(getExamSummary(saveId, examType, null));

        return result;
    }

    private School getSchoolBySaveId(Long saveId) {
        LambdaQueryWrapper<School> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(School::getSaveId, saveId);
        School school = schoolMapper.selectOne(wrapper);
        if (school == null) {
            throw new BusinessException("学校不存在,saveId: " + saveId);
        }
        return school;
    }

    private List<Student> getActiveStudents(Long saveId) {
        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Student::getSchoolId, saveId)
               .ne(Student::getStatus, "GRADUATED")
               .ne(Student::getStatus, "TRANSFERRED");
        return studentMapper.selectList(wrapper);
    }

    private List<Student> getStudentsByGrade(Long saveId, String grade) {
        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Student::getSchoolId, saveId)
               .eq(Student::getGrade, grade)
               .ne(Student::getStatus, "GRADUATED")
               .ne(Student::getStatus, "TRANSFERRED");
        return studentMapper.selectList(wrapper);
    }

    private List<Teacher> getTeachers(Long saveId) {
        LambdaQueryWrapper<Teacher> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Teacher::getSchoolId, saveId);
        return teacherMapper.selectList(wrapper);
    }

    private List<Building> getBuildings(Long saveId) {
        LambdaQueryWrapper<Building> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Building::getSchoolId, saveId);
        return buildingMapper.selectList(wrapper);
    }

    private TeachingPolicy getTeachingPolicy(School school) {
        TeachingPolicy policy = new TeachingPolicy();
        if (school != null && school.getTeachingPolicy() != null) {
            try {
                policy = new com.fasterxml.jackson.databind.ObjectMapper()
                        .readValue(school.getTeachingPolicy(), TeachingPolicy.class);
            } catch (Exception e) {
                log.warn("解析教学政策失败,使用默认配置");
            }
        }
        return policy;
    }

    private BigDecimal convertToExamScore(BigDecimal rawScore, String examType) {
        if (rawScore == null) {
            return BigDecimal.ZERO;
        }
        double score = rawScore.doubleValue();
        if ("GAOKAO".equals(examType)) {
            return convertToGaokaoScore(rawScore);
        }
        score = Math.min(Math.max(score, 0), 150);
        return BigDecimal.valueOf(score).setScale(1, RoundingMode.HALF_UP);
    }

    private BigDecimal convertToGaokaoScore(BigDecimal rawScore) {
        if (rawScore == null) {
            return BigDecimal.ZERO;
        }
        double score = rawScore.doubleValue();
        double gaokaoScore = score * (GAOKAO_MAX_SCORE / 150.0);
        gaokaoScore = Math.min(Math.max(gaokaoScore, 0), GAOKAO_MAX_SCORE);
        return BigDecimal.valueOf(gaokaoScore).setScale(1, RoundingMode.HALF_UP);
    }

    private void adjustStudentAcademicScore(Student student, BigDecimal examScore, int maxAdjustment) {
        BigDecimal currentAcademic = student.getAcademicScore() != null ?
                student.getAcademicScore() : BigDecimal.valueOf(60);
        BigDecimal targetAcademic = examScore.multiply(new BigDecimal("0.667"));
        BigDecimal difference = targetAcademic.subtract(currentAcademic);
        BigDecimal adjustment = difference.multiply(BigDecimal.valueOf(0.3));
        adjustment = adjustment.max(BigDecimal.valueOf(-maxAdjustment))
                              .min(BigDecimal.valueOf(maxAdjustment));
        BigDecimal newAcademic = currentAcademic.add(adjustment);
        student.setAcademicScore(newAcademic.max(BigDecimal.ZERO).min(BigDecimal.valueOf(100)));
    }

    private ExamResultDTO buildExamResultDTO(Student student, BigDecimal score, int rank,
                                              String examType, boolean isTopScholar) {
        ExamResultDTO dto = new ExamResultDTO();
        dto.setStudentId(student.getId());
        dto.setStudentName(student.getName());
        dto.setScore(score);
        dto.setRank(rank);
        dto.setExamType(examType);
        dto.setIsTopScholar(isTopScholar);
        return dto;
    }

    private void saveExamRecord(Long schoolId, Long studentId, String examType,
                                 BigDecimal score, int rank, Integer isTopScholar) {
        ExamRecord record = new ExamRecord();
        record.setSchoolId(schoolId);
        record.setStudentId(studentId);
        record.setExamType(examType);
        record.setScore(score);
        record.setRank(rank);
        record.setExamYear(java.time.Year.now().getValue());
        record.setIsTopScholar(isTopScholar);
        record.setCreatedAt(LocalDateTime.now());
        examRecordMapper.insert(record);
    }

    private void assignRanks(List<ExamResultDTO> results) {
        for (int i = 0; i < results.size(); i++) {
            results.get(i).setRank(i + 1);
        }
    }

    private void assignRanksAndTriggerEvents(List<ExamResultDTO> results, Long saveId, School school) {
        assignRanks(results);

        if (!results.isEmpty()) {
            int topN = Math.min(10, results.size());
            for (int i = 0; i < topN; i++) {
                ExamResultDTO dto = results.get(i);
                saveEventLog(saveId, "EXCELLENT_STUDENT", "优秀学生",
                        dto.getStudentName() + "在月考中获得第" + dto.getRank() + "名");
            }

            int bottomStart = Math.max(0, results.size() - 10);
            for (int i = bottomStart; i < results.size(); i++) {
                ExamResultDTO dto = results.get(i);
                saveEventLog(saveId, "ATTENTION_NEEDED", "需关注学生",
                        dto.getStudentName() + "月考成绩靠后,排名第" + dto.getRank() + "名,需要关注");
            }
        }
    }

    private void saveEventLog(Long saveId, String type, String title, String description) {
        EventLog eventLog = new EventLog();
        eventLog.setSaveId(saveId);
        eventLog.setEventType(type);
        eventLog.setEventTitle(title);
        eventLog.setEventDescription(description);
        eventLog.setCreatedAt(LocalDateTime.now());
        eventLogMapper.insert(eventLog);
    }

    private void processGradePromotionOrRetention(List<Student> students, List<ExamResultDTO> results,
                                                   Long saveId) {
        Map<Long, ExamResultDTO> resultMap = results.stream()
                .collect(Collectors.toMap(ExamResultDTO::getStudentId, dto -> dto));

        for (Student student : students) {
            ExamResultDTO dto = resultMap.get(student.getId());
            if (dto == null || dto.getScore() == null) continue;

            double scorePercent = dto.getScore().doubleValue();

            if (scorePercent < 40) {
                saveEventLog(saveId, "RETENTION_WARNING", "留级警告",
                        student.getName() + "期末考试成绩严重不及格,可能面临留级");
            } else if (scorePercent > 90) {
                if (Math.random() < 0.05) {
                    saveEventLog(saveId, "GRADE_SKIP_OPPORTUNITY", "跳级机会",
                            student.getName() + "表现优异,获得跳级机会");
                }
            }
        }
    }

    private BigDecimal calculateAverageScore(List<ExamResultDTO> results) {
        if (results.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal sum = results.stream()
                .map(ExamResultDTO::getScore)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(BigDecimal.valueOf(results.size()), 2, RoundingMode.HALF_UP);
    }

    private void updateReputationForFinalExam(School school, BigDecimal avgScore) {
        int reputationChange = 0;
        if (avgScore.compareTo(BigDecimal.valueOf(CITY_AVG_SCORE + 10)) > 0) {
            reputationChange = 10;
        } else {
            reputationChange = -5;
        }

        int currentReputation = school.getReputation() != null ? school.getReputation() : 0;
        school.setReputation(currentReputation + reputationChange);
        schoolMapper.updateById(school);
    }

    private int calculateGaokaoReputation(BigDecimal score) {
        double scoreValue = score.doubleValue();
        if (scoreValue > 700) return 50;
        if (scoreValue >= 650) return 50;
        if (scoreValue >= 550) return 20;
        if (scoreValue >= 450) return 10;
        if (scoreValue >= 350) return 5;
        if (scoreValue >= 200) return 2;
        return -5;
    }

    private String determineAchievementLevel(BigDecimal score) {
        double scoreValue = score.doubleValue();
        if (scoreValue > 700) return "状元";
        if (scoreValue >= 650) return "清北";
        if (scoreValue >= 550) return "985";
        if (scoreValue >= 450) return "211";
        if (scoreValue >= 350) return "一本";
        if (scoreValue >= 200) return "二本";
        return "专科";
    }

    private int calculateAverageScoreBonus(BigDecimal avgScore) {
        double avg = avgScore.doubleValue();
        if (avg > 650) return 30;
        if (avg > 600) return 20;
        if (avg > 550) return 10;
        if (avg > 500) return 5;
        if (avg < 400) return -10;
        return 0;
    }

    private BigDecimal calculateFundReward(BigDecimal avgScore) {
        double reward = avgScore.doubleValue() * FUND_PER_POINT;
        return BigDecimal.valueOf(Math.min(reward, MAX_FUND_REWARD)).setScale(2, RoundingMode.HALF_UP);
    }

    private void createAlumniRecord(Long saveId, Student student, String achievementLevel) {
        Alumni alumni = new Alumni();
        alumni.setSaveId(saveId);
        alumni.setStudentName(student.getName());
        alumni.setGraduationYear(java.time.Year.now().getValue());
        alumni.setGraduationSchool(determineGraduationSchool(achievementLevel));
        alumni.setAchievementType("名校");
        alumni.setAchievementLevel(achievementLevel);
        alumni.setDonationAmount(BigDecimal.ZERO);
        alumni.setReputationContribution(calculateAlumniReputation(achievementLevel));
        alumni.setCreatedAt(LocalDateTime.now());
        alumniMapper.insert(alumni);
    }

    private String determineGraduationSchool(String achievementLevel) {
        return switch (achievementLevel) {
            case "状元" -> "清华大学/北京大学";
            case "清北" -> "985高校";
            case "985" -> "985高校";
            case "211" -> "211高校";
            case "一本" -> "一本院校";
            case "二本" -> "二本院校";
            default -> "专科院校";
        };
    }

    private int calculateAlumniReputation(String achievementLevel) {
        return switch (achievementLevel) {
            case "状元" -> 50;
            case "清北" -> 30;
            case "985" -> 20;
            case "211" -> 10;
            case "一本" -> 5;
            case "二本" -> 2;
            default -> 0;
        };
    }

    private void updateSchoolFundsAndReputation(School school, BigDecimal fundReward, int reputationChange) {
        BigDecimal currentFunds = school.getFunds() != null ? school.getFunds() : BigDecimal.ZERO;
        school.setFunds(currentFunds.add(fundReward));

        int currentReputation = school.getReputation() != null ? school.getReputation() : 0;
        school.setReputation(currentReputation + reputationChange);

        schoolMapper.updateById(school);
    }

    private String determineAdmissionResult(BigDecimal score, Random random) {
        double percent = score.doubleValue() / 150.0 * 100;
        double rand = random.nextDouble() * 100;

        if (percent > 90) {
            if (rand < 20) return "重点高中";
            if (rand < 60) return "普通高中";
            if (rand < 90) return "职业高中";
            return "落榜";
        } else if (percent > 75) {
            if (rand < 15) return "重点高中";
            if (rand < 55) return "普通高中";
            if (rand < 85) return "职业高中";
            return "落榜";
        } else if (percent > 60) {
            if (rand < 5) return "重点高中";
            if (rand < 35) return "普通高中";
            if (rand < 75) return "职业高中";
            return "落榜";
        } else {
            if (rand < 25) return "普通高中";
            if (rand < 55) return "职业高中";
            return "落榜";
        }
    }

    private int calculateMiddleSchoolReputation(String admissionResult) {
        return switch (admissionResult) {
            case "重点高中" -> 10;
            case "普通高中" -> 5;
            case "职业高中" -> 2;
            default -> 0;
        };
    }

    private int calculatePrimarySchoolReputation(String admissionResult) {
        return switch (admissionResult) {
            case "重点初中" -> 10;
            case "普通初中" -> 5;
            default -> 2;
        };
    }

    private void updateSchoolReputation(School school, int reputationChange) {
        int currentReputation = school.getReputation() != null ? school.getReputation() : 0;
        school.setReputation(currentReputation + reputationChange);
        schoolMapper.updateById(school);
    }

    private void updateGraduatedStudentStatus(List<Student> students) {
        for (Student student : students) {
            student.setStatus("GRADUATED");
            studentMapper.updateById(student);
        }
    }

    private Map<Long, String> getStudentNameMap(Set<Long> studentIds) {
        if (studentIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Student> students = studentMapper.selectBatchIds(studentIds);
        return students.stream()
                .collect(Collectors.toMap(Student::getId, Student::getName, (a, b) -> a));
    }

    private ExamResultDTO convertToExamResultDTO(ExamRecord record) {
        ExamResultDTO dto = new ExamResultDTO();
        dto.setStudentId(record.getStudentId());
        dto.setScore(record.getScore());
        dto.setRank(record.getRank());
        dto.setExamType(record.getExamType());
        dto.setIsTopScholar(record.getIsTopScholar() != null && record.getIsTopScholar() == 1);

        if (record.getStudentId() != null) {
            Student student = studentMapper.selectById(record.getStudentId());
            if (student != null) {
                dto.setStudentName(student.getName());
            }
        }
        return dto;
    }

    private ExamHistoryDTO convertToExamHistoryDTO(ExamRecord record) {
        ExamHistoryDTO dto = new ExamHistoryDTO();
        dto.setExamId(record.getId());
        dto.setExamType(record.getExamType());
        dto.setScore(record.getScore());
        dto.setRank(record.getRank());
        dto.setExamYear(record.getExamYear());

        if (record.getStudentId() != null) {
            Student student = studentMapper.selectById(record.getStudentId());
            if (student != null) {
                dto.setStudentName(student.getName());
            }
        }
        return dto;
    }
}
