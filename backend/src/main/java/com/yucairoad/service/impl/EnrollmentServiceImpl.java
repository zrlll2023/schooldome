package com.yucairoad.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yucairoad.common.BusinessException;
import com.yucairoad.dto.EnrollmentPolicy;
import com.yucairoad.dto.EnrollmentPreview;
import com.yucairoad.dto.EnrollmentResult;
import com.yucairoad.dto.GameState;
import com.yucairoad.entity.Student;
import com.yucairoad.mapper.GameSaveMapper;
import com.yucairoad.mapper.StudentMapper;
import com.yucairoad.service.EnrollmentService;
import com.yucairoad.service.GameSaveService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
public class EnrollmentServiceImpl implements EnrollmentService {

    private static final Map<String, Integer> SCALE_MAP = Map.of(
            "小", 50,
            "中", 120,
            "大", 250
    );

    private static final Map<String, Double> STANDARD_COEFFICIENT = Map.of(
            "严格", 1.2,
            "普通", 1.0,
            "宽松", 0.7
    );

    private static final Map<String, Double> TUITION_COEFFICIENT = Map.of(
            "极高", 0.8,
            "高", 1.0,
            "中", 1.1,
            "低", 1.2
    );

    private static final Map<String, BigDecimal> TUITION_PER_STUDENT = Map.of(
            "低", new BigDecimal("3000"),
            "中", new BigDecimal("6000"),
            "高", new BigDecimal("12000"),
            "极高", new BigDecimal("20000")
    );

    private static final String[] SURNAMES = {
            "赵", "钱", "孙", "李", "周", "吴", "郑", "王", "冯", "陈",
            "褚", "卫", "蒋", "沈", "韩", "杨", "朱", "秦", "尤", "许",
            "何", "吕", "施", "张", "孔", "曹", "严", "华", "金", "魏",
            "陶", "姜", "戚", "谢", "邹", "喻", "柏", "水", "窦", "章",
            "云", "苏", "潘", "葛", "奚", "范", "彭", "郎", "鲁", "韦",
            "昌", "马", "苗", "凤", "花", "方", "俞", "任", "袁", "柳"
    };

    private static final String[] GIVEN_NAMES = {
            "伟", "芳", "娜", "秀英", "敏", "静", "丽", "强", "磊", "军",
            "洋", "勇", "艳", "杰", "娟", "涛", "明", "超", "秀兰", "霞",
            "平", "刚", "桂英", "文", "玲", "建华", "建国", "建军", "志强", "婷婷"
    };

    private final GameSaveService gameSaveService;
    private final GameSaveMapper gameSaveMapper;
    private final StudentMapper studentMapper;
    private final ObjectMapper objectMapper;

    public EnrollmentServiceImpl(GameSaveService gameSaveService,
                                 GameSaveMapper gameSaveMapper,
                                 StudentMapper studentMapper) {
        this.gameSaveService = gameSaveService;
        this.gameSaveMapper = gameSaveMapper;
        this.studentMapper = studentMapper;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public EnrollmentPolicy saveEnrollmentPolicy(Long saveId, EnrollmentPolicy policy) {
        validatePolicy(policy);
        GameState state = loadGameState(saveId);
        state.setEnrollmentPolicy(policy);
        persistGameState(saveId, state);
        return policy;
    }

    @Override
    public EnrollmentPreview previewEnrollment(Long saveId) {
        GameState state = loadGameState(saveId);
        EnrollmentPolicy policy = state.getEnrollmentPolicy();

        if (policy == null) {
            policy = getDefaultPolicy();
        }

        int reputation = state.getReputation() == null ? 0 : state.getReputation();
        int expectedCount = getExpectedStudentCount(policy);

        Map<String, Double> distribution = calculateQualityDistribution(reputation, policy);
        double avgQuality = calculateAverageQuality(distribution);

        EnrollmentPreview preview = new EnrollmentPreview();
        preview.setExpectedStudentCount(expectedCount);
        preview.setQualityDistribution(distribution);
        preview.setEstimatedAvgQuality(avgQuality);

        return preview;
    }

    @Override
    public EnrollmentResult executeEnrollment(Long saveId) {
        GameState state = loadGameState(saveId);
        int currentMonth = state.getCurrentMonth() == null ? 9 : state.getCurrentMonth();

        if (currentMonth != 9) {
            throw new BusinessException("只能在9月份执行招生操作");
        }

        EnrollmentPolicy policy = state.getEnrollmentPolicy();
        if (policy == null) {
            policy = getDefaultPolicy();
        }

        int reputation = state.getReputation() == null ? 0 : state.getReputation();
        int currentYear = state.getCurrentYear() == null ? 1 : state.getCurrentYear();
        int expectedCount = getExpectedStudentCount(policy);
        Map<String, Double> distribution = calculateQualityDistribution(reputation, policy);
        Random random = createSeededRandom(currentYear);

        List<Student> students = generateStudents(expectedCount, distribution, random, currentYear, false, null);
        Long schoolId = getSchoolId(saveId);

        for (Student student : students) {
            student.setSchoolId(schoolId);
            studentMapper.insert(student);
        }

        updateSchoolStudentCount(schoolId, students.size());

        BigDecimal tuitionIncome = calculateTuitionIncome(students.size(), policy.getTuitionLevel());
        BigDecimal currentFunds = state.getFunds() == null ? BigDecimal.ZERO : state.getFunds();
        state.setFunds(currentFunds.add(tuitionIncome));

        persistGameState(saveId, state);

        EnrollmentResult result = buildEnrollmentResult(students, tuitionIncome);
        result.setTotalEnrolled(students.size());
        return result;
    }

    private void validatePolicy(EnrollmentPolicy policy) {
        if (policy == null) {
            throw new BusinessException("招生政策不能为空");
        }
        Set<String> validTuitionLevels = Set.of("低", "中", "高", "极高");
        Set<String> validScales = Set.of("小", "中", "大");
        Set<String> validStandards = Set.of("宽松", "普通", "严格");

        if (policy.getTuitionLevel() == null || !validTuitionLevels.contains(policy.getTuitionLevel())) {
            throw new BusinessException("无效的学费档位，可选值: 低/中/高/极高");
        }
        if (policy.getEnrollmentScale() == null || !validScales.contains(policy.getEnrollmentScale())) {
            throw new BusinessException("无效的招生规模，可选值: 小/中/大");
        }
        if (policy.getEnrollmentStandard() == null || !validStandards.contains(policy.getEnrollmentStandard())) {
            throw new BusinessException("无效的招生标准，可选值: 宽松/普通/严格");
        }
    }

    private EnrollmentPolicy getDefaultPolicy() {
        EnrollmentPolicy policy = new EnrollmentPolicy();
        policy.setTuitionLevel("中");
        policy.setEnrollmentScale("中");
        policy.setEnrollmentStandard("普通");
        return policy;
    }

    private int getExpectedStudentCount(EnrollmentPolicy policy) {
        String scale = policy.getEnrollmentScale();
        return SCALE_MAP.getOrDefault(scale, 120);
    }

    private Map<String, Double> calculateQualityDistribution(int reputation, EnrollmentPolicy policy) {
        double standardCoeff = STANDARD_COEFFICIENT.getOrDefault(policy.getEnrollmentStandard(), 1.0);
        double tuitionCoeff = TUITION_COEFFICIENT.getOrDefault(policy.getTuitionLevel(), 1.0);

        double baseQuality = reputation * 0.3 + standardCoeff * 40 + tuitionCoeff * 10;

        int reputationBonus = reputation / 100 * 2;
        double sRate = Math.min(5.0 + reputationBonus, 25.0);
        double aRate = Math.min(15.0 + reputationBonus * 1.5, 35.0);
        double bRate = 40.0 - standardCoeff * 5;
        double cRate = 30.0 - standardCoeff * 3;
        double dRate = 10.0 + standardCoeff * 8;

        double total = sRate + aRate + bRate + cRate + dRate;
        sRate = roundToTwoDecimals(sRate / total * 100);
        aRate = roundToTwoDecimals(aRate / total * 100);
        bRate = roundToTwoDecimals(bRate / total * 100);
        cRate = roundToTwoDecimals(cRate / total * 100);
        dRate = roundToTwoDecimals(100.0 - sRate - aRate - bRate - cRate);

        Map<String, Double> distribution = new LinkedHashMap<>();
        distribution.put("S", sRate);
        distribution.put("A", aRate);
        distribution.put("B", bRate);
        distribution.put("C", cRate);
        distribution.put("D", dRate);
        return distribution;
    }

    private double calculateAverageQuality(Map<String, Double> distribution) {
        double avg = 0;
        Map<String, Double> gradeScores = Map.of("S", 95.0, "A", 80.0, "B", 67.0, "C", 52.0, "D", 37.0);
        for (Map.Entry<String, Double> entry : distribution.entrySet()) {
            avg += entry.getValue() / 100.0 * gradeScores.getOrDefault(entry.getKey(), 60.0);
        }
        return roundToTwoDecimals(avg);
    }

    private List<Student> generateStudents(int count, Map<String, Double> distribution, Random random,
                                          int enrolledYear, boolean isK12Student, Long fromSchoolId) {
        List<Student> students = new ArrayList<>();
        List<String> gradeLevels = new ArrayList<>();

        for (Map.Entry<String, Double> entry : distribution.entrySet()) {
            int gradeCount = (int) Math.round(count * entry.getValue() / 100.0);
            for (int i = 0; i < gradeCount; i++) {
                gradeLevels.add(entry.getKey());
            }
        }

        while (gradeLevels.size() < count) {
            gradeLevels.add("B");
        }
        while (gradeLevels.size() > count) {
            gradeLevels.remove(gradeLevels.size() - 1);
        }

        Collections.shuffle(gradeLevels, random);

        for (String gradeLevel : gradeLevels) {
            Student student = new Student();
            student.setName(generateRandomName(random));
            student.setGradeLevel(gradeLevel);
            student.setAcademicScore(generateAcademicScore(gradeLevel, random));
            student.setQualityScore(new BigDecimal(50 + random.nextInt(31)));
            student.setHealthScore(new BigDecimal(70 + random.nextInt(21)));
            student.setStatus("在校");
            student.setEnrolledYear(enrolledYear);
            student.setIsK12Student(isK12Student ? 1 : 0);
            student.setFromSchoolId(fromSchoolId);
            student.setCreatedAt(LocalDateTime.now());
            student.setUpdatedAt(LocalDateTime.now());
            students.add(student);
        }

        return students;
    }

    private BigDecimal generateAcademicScore(String gradeLevel, Random random) {
        int baseMin, baseMax;
        switch (gradeLevel) {
            case "S":
                baseMin = 85;
                baseMax = 100;
                break;
            case "A":
                baseMin = 75;
                baseMax = 84;
                break;
            case "B":
                baseMin = 60;
                baseMax = 74;
                break;
            case "C":
                baseMin = 45;
                baseMax = 59;
                break;
            default:
                baseMin = 30;
                baseMax = 44;
                break;
        }
        int fluctuation = random.nextInt(-5, 6);
        int score = Math.max(0, Math.min(100, baseMin + random.nextInt(baseMax - baseMin + 1) + fluctuation));
        return new BigDecimal(score);
    }

    private String generateRandomName(Random random) {
        String surname = SURNAMES[random.nextInt(SURNAMES.length)];
        String givenName = GIVEN_NAMES[random.nextInt(GIVEN_NAMES.length)];
        return surname + givenName;
    }

    private Random createSeededRandom(int year) {
        long seed = 2024L * 1000 + year;
        return new Random(seed);
    }

    private BigDecimal calculateTuitionIncome(int studentCount, String tuitionLevel) {
        BigDecimal perStudent = TUITION_PER_STUDENT.getOrDefault(tuitionLevel, new BigDecimal("6000"));
        return perStudent.multiply(BigDecimal.valueOf(studentCount));
    }

    private void updateSchoolStudentCount(Long schoolId, int newStudents) {

    }

    private Long getSchoolId(Long saveId) {
        return saveId;
    }

    private EnrollmentResult buildEnrollmentResult(List<Student> students, BigDecimal tuitionIncome) {
        EnrollmentResult result = new EnrollmentResult();
        result.setTuitionIncome(tuitionIncome);

        Map<String, Integer> breakdown = students.stream()
                .collect(Collectors.groupingBy(
                        s -> s.getGradeLevel() != null ? s.getGradeLevel() : "B",
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));
        result.setQualityBreakdown(breakdown);

        List<EnrollmentResult.StudentInfo> studentInfos = students.stream().map(s -> {
            EnrollmentResult.StudentInfo info = new EnrollmentResult.StudentInfo();
            info.setId(s.getId());
            info.setName(s.getName());
            info.setGradeLevel(s.getGradeLevel());
            info.setAcademicScore(s.getAcademicScore());
            info.setQualityScore(s.getQualityScore());
            info.setHealthScore(s.getHealthScore());
            return info;
        }).collect(Collectors.toList());
        result.setStudents(studentInfos);

        return result;
    }

    private GameState loadGameState(Long saveId) {
        var save = gameSaveMapper.selectById(saveId);
        if (save == null) {
            throw new BusinessException("存档不存在");
        }
        String gameStateJson = save.getGameState();
        if (gameStateJson == null || gameStateJson.isBlank()) {
            return createInitialGameState();
        }
        try {
            return objectMapper.readValue(gameStateJson, new TypeReference<GameState>() {});
        } catch (JsonProcessingException e) {
            throw new BusinessException("存档数据解析失败");
        }
    }

    private void persistGameState(Long saveId, GameState state) {
        try {
            String json = objectMapper.writeValueAsString(state);
            gameSaveService.updateSave(null, saveId, json);
        } catch (JsonProcessingException e) {
            throw new BusinessException("存档数据保存失败");
        }
    }

    private GameState createInitialGameState() {
        GameState state = new GameState();
        state.setCurrentYear(1);
        state.setCurrentMonth(9);
        state.setFunds(new BigDecimal("2000000"));
        state.setReputation(0);
        state.setStudentCount(0);
        state.setTeacherCount(0);
        state.setSpeed(1);
        state.setIsPaused(false);
        return state;
    }

    private double roundToTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
