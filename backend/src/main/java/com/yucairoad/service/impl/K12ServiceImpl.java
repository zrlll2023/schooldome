package com.yucairoad.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yucairoad.common.BusinessException;
import com.yucairoad.dto.*;
import com.yucairoad.entity.Building;
import com.yucairoad.entity.GameSave;
import com.yucairoad.entity.School;
import com.yucairoad.entity.Student;
import com.yucairoad.entity.Teacher;
import com.yucairoad.mapper.BuildingMapper;
import com.yucairoad.mapper.GameSaveMapper;
import com.yucairoad.mapper.SchoolMapper;
import com.yucairoad.mapper.StudentMapper;
import com.yucairoad.mapper.TeacherMapper;
import com.yucairoad.service.GameSaveService;
import com.yucairoad.service.K12Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class K12ServiceImpl implements K12Service {

    private static final int JUNIOR_BUILD_COST = 2000000;
    private static final int PRIMARY_BUILD_COST = 3000000;
    private static final int JUNIOR_REPUTATION_REQUIRED = 1000;
    private static final int PRIMARY_REPUTATION_REQUIRED = 4000;
    private static final int BUILD_DURATION_MONTHS = 6;
    private static final double PRIMARY_TO_JUNIOR_BASE_RATE = 0.8;
    private static final double JUNIOR_TO_SENIOR_BASE_RATE = 0.7;
    private static final int K12_GAOKAO_BONUS_YEARS = 5;

    private final GameSaveMapper gameSaveMapper;
    private final GameSaveService gameSaveService;
    private final SchoolMapper schoolMapper;
    private final StudentMapper studentMapper;
    private final TeacherMapper teacherMapper;
    private final BuildingMapper buildingMapper;
    private final ObjectMapper objectMapper;

    public K12ServiceImpl(GameSaveMapper gameSaveMapper,
                          GameSaveService gameSaveService,
                          SchoolMapper schoolMapper,
                          StudentMapper studentMapper,
                          TeacherMapper teacherMapper,
                          BuildingMapper buildingMapper) {
        this.gameSaveMapper = gameSaveMapper;
        this.gameSaveService = gameSaveService;
        this.schoolMapper = schoolMapper;
        this.studentMapper = studentMapper;
        this.teacherMapper = teacherMapper;
        this.buildingMapper = buildingMapper;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public K12StatusDTO getK12Status(Long saveId) {
        validateSaveExists(saveId);
        GameState state = loadGameState(saveId);
        GameState.K12System k12System = getOrCreateK12System(state);

        K12StatusDTO dto = new K12StatusDTO();
        List<K12StatusDTO.StageInfo> stages = new ArrayList<>();

        if (k12System.getHasSenior()) {
            stages.add(buildStageInfo(k12System.getSeniorSchool(), "SENIOR", "OPERATING"));
        }
        if (k12System.getHasJunior()) {
            String status = k12System.getJuniorBuildProgress() != null ? "BUILDING" : "OPERATING";
            stages.add(buildStageInfo(k12System.getJuniorSchool(), "JUNIOR", status));
        } else {
            stages.add(createEmptyStageInfo("JUNIOR", "NOT_BUILT"));
        }
        if (k12System.getHasPrimary()) {
            String status = k12System.getPrimaryBuildProgress() != null ? "BUILDING" : "OPERATING";
            stages.add(buildStageInfo(k12System.getPrimarySchool(), "PRIMARY", status));
        } else {
            stages.add(createEmptyStageInfo("PRIMARY", "NOT_BUILT"));
        }

        dto.setStages(stages);
        dto.setIsComplete(k12System.getHasPrimary() && k12System.getHasJunior() && k12System.getHasSenior());
        dto.setPipelineSummary(getPipeline(saveId));
        dto.setSynergySummary(getSynergy(saveId));

        BuildProgressDTO activeProgress = getActiveBuildProgress(k12System);
        dto.setBuildProgress(activeProgress);

        return dto;
    }

    @Override
    @Transactional
    public BuildProgressDTO buildStage(Long saveId, String stage) {
        GameState state = loadGameState(saveId);
        GameState.K12System k12System = getOrCreateK12System(state);

        String normalizedStage = stage.toUpperCase();
        boolean isPrimary = "PRIMARY".equals(normalizedStage);
        boolean isJunior = "JUNIOR".equals(normalizedStage);

        if (!isPrimary && !isJunior) {
            throw new BusinessException("无效的学段类型，仅支持PRIMARY或JUNIOR");
        }

        int requiredReputation = isPrimary ? PRIMARY_REPUTATION_REQUIRED : JUNIOR_REPUTATION_REQUIRED;
        BigDecimal buildCost = isPrimary ? new BigDecimal(PRIMARY_BUILD_COST) : new BigDecimal(JUNIOR_BUILD_COST);
        boolean alreadyOwned = isPrimary ? Boolean.TRUE.equals(k12System.getHasPrimary()) : Boolean.TRUE.equals(k12System.getHasJunior());

        if (alreadyOwned) {
            throw new BusinessException((isPrimary ? "小学" : "初中") + "已存在，无需重复建设");
        }

        Integer reputation = state.getReputation() != null ? state.getReputation() : 0;
        if (reputation < requiredReputation) {
            throw new BusinessException("声望不足，建设" + (isPrimary ? "小学" : "初中") + "需要声望达到" + requiredReputation +
                    "(当前:" + reputation + ")");
        }

        BigDecimal funds = state.getFunds() != null ? state.getFunds() : BigDecimal.ZERO;
        if (funds.compareTo(buildCost) < 0) {
            throw new BusinessException("资金不足，建设" + (isPrimary ? "小学" : "初中") + "需要" +
                    buildCost.divide(new BigDecimal("10000"), 2, RoundingMode.HALF_UP) + "万" +
                    "(当前:" + funds.divide(new BigDecimal("10000"), 2, RoundingMode.HALF_UP) + "万)");
        }

        state.setFunds(funds.subtract(buildCost));

        Map<String, Object> progressMap = new HashMap<>();
        progressMap.put("startYear", state.getCurrentYear());
        progressMap.put("startMonth", state.getCurrentMonth());
        progressMap.put("totalMonths", BUILD_DURATION_MONTHS);
        progressMap.put("elapsedMonths", 0);
        progressMap.put("status", "BUILDING");

        if (isPrimary) {
            k12System.setPrimaryBuildProgress(progressMap);
        } else {
            k12System.setJuniorBuildProgress(progressMap);
        }

        state.setK12System(k12System);
        persistGameState(saveId, state);

        return convertToBuildProgressDTO(normalizedStage, progressMap, buildCost);
    }

    @Override
    public PipelineDTO getPipeline(Long saveId) {
        validateSaveExists(saveId);
        GameState state = loadGameState(saveId);
        GameState.K12System k12System = getOrCreateK12System(state);

        PipelineDTO dto = new PipelineDTO();

        PipelineDTO.PipelineData primaryToJunior = calculatePipelineData(
                saveId, k12System, "PRIMARY", "JUNIOR",
                k12System.getPipelineData().getPrimaryToJuniorRate(),
                PRIMARY_TO_JUNIOR_BASE_RATE, k12System.getPipelineData().getPrimaryGraduates()
        );
        dto.setPrimaryToJunior(primaryToJunior);

        PipelineDTO.PipelineData juniorToSenior = calculatePipelineData(
                saveId, k12System, "JUNIOR", "SENIOR",
                k12System.getPipelineData().getJuniorToSeniorRate(),
                JUNIOR_TO_SENIOR_BASE_RATE, k12System.getPipelineData().getJuniorGraduates()
        );
        dto.setJuniorToSenior(juniorToSenior);

        PipelineDTO.K12Statistics stats = calculateK12Statistics(saveId, k12System);
        dto.setK12Statistics(stats);

        return dto;
    }

    @Override
    public SynergyDTO getSynergy(Long saveId) {
        validateSaveExists(saveId);
        GameState state = loadGameState(saveId);
        GameState.K12System k12System = getOrCreateK12System(state);

        SynergyDTO dto = new SynergyDTO();
        List<String> activeSynergies = new ArrayList<>();
        List<String> inactiveSynergies = new ArrayList<>();
        Map<String, SynergyDTO.SynergyEffect> effects = new LinkedHashMap<>();

        boolean hasTwoStages = (Boolean.TRUE.equals(k12System.getHasPrimary()) && Boolean.TRUE.equals(k12System.getHasJunior())) ||
                (Boolean.TRUE.equals(k12System.getHasJunior()) && Boolean.TRUE.equals(k12System.getHasSenior()));
        boolean hasAllThree = Boolean.TRUE.equals(k12System.getHasPrimary()) &&
                Boolean.TRUE.equals(k12System.getHasJunior()) &&
                Boolean.TRUE.equals(k12System.getHasSenior());

        SynergyDTO.SynergyEffect resourceSharing = new SynergyDTO.SynergyEffect();
        resourceSharing.setActive(hasTwoStages);
        resourceSharing.setCondition("拥有2个以上学段");
        resourceSharing.setBenefit("图书馆、体育馆等设施多学段共享,维护费-20%");
        resourceSharing.setMonthlySavings(hasTwoStages ? new BigDecimal("5000") : BigDecimal.ZERO);
        effects.put("resourceSharing", resourceSharing);
        if (hasTwoStages) activeSynergies.add("resourceSharing"); else inactiveSynergies.add("resourceSharing");

        SynergyDTO.SynergyEffect teacherMobility = new SynergyDTO.SynergyEffect();
        teacherMobility.setActive(hasTwoStages);
        teacherMobility.setCondition("拥有2个以上学段");
        teacherMobility.setBenefit("可在学段间调配教师,特级教师全校共享");
        teacherMobility.setEffect(hasTwoStages ? "特级教师对全校所有学段生效" : null);
        effects.put("teacherMobility", teacherMobility);
        if (hasTwoStages) activeSynergies.add("teacherMobility"); else inactiveSynergies.add("teacherMobility");

        SynergyDTO.SynergyEffect brandBonus = new SynergyDTO.SynergyEffect();
        brandBonus.setActive(hasAllThree);
        brandBonus.setCondition("K12体系完整(拥有小学+初中+高中)");
        brandBonus.setBenefit("招生时'直升名额'吸引优质生源,S级概率+5%");
        effects.put("brandBonus", brandBonus);
        if (hasAllThree) activeSynergies.add("brandBonus"); else inactiveSynergies.add("brandBonus");

        int consecutiveYears = k12System.getConsecutiveExcellenceYears() != null ? k12System.getConsecutiveExcellenceYears() : 0;
        boolean districtUnlocked = consecutiveYears >= K12_GAOKAO_BONUS_YEARS;
        SynergyDTO.SynergyEffect districtEffect = new SynergyDTO.SynergyEffect();
        districtEffect.setActive(districtUnlocked);
        districtEffect.setCondition("连续5年高考成绩优秀(平均分>600)");
        districtEffect.setBenefit("周边区域生源质量自动+10%,解锁'金牌学区'称号");
        districtEffect.setYearsRemaining(districtUnlocked ? null : Math.max(0, K12_GAOKAO_BONUS_YEARS - consecutiveYears));
        effects.put("districtEffect", districtEffect);
        if (districtUnlocked) activeSynergies.add("districtEffect"); else inactiveSynergies.add("districtEffect");

        dto.setActiveSynergies(activeSynergies);
        dto.setInactiveSynergies(inactiveSynergies);
        dto.setEffects(effects);

        return dto;
    }

    @Override
    @Transactional
    public void updateBuildProgress(Long saveId, String stageType) {
        GameState state = loadGameState(saveId);
        GameState.K12System k12System = getOrCreateK12System(state);

        boolean isPrimary = "PRIMARY".equalsIgnoreCase(stageType);
        Map<String, Object> progress = isPrimary ? k12System.getPrimaryBuildProgress() : k12System.getJuniorBuildProgress();

        if (progress == null || !"BUILDING".equals(progress.get("status"))) {
            return;
        }

        int elapsed = ((Number) progress.getOrDefault("elapsedMonths", 0)).intValue() + 1;
        progress.put("elapsedMonths", elapsed);

        int totalMonths = ((Number) progress.getOrDefault("totalMonths", BUILD_DURATION_MONTHS)).intValue();
        int percent = Math.min(100, (elapsed * 100) / totalMonths);

        if (elapsed >= totalMonths) {
            progress.put("status", "COMPLETED");
            activateStage(saveId, state, k12System, stageType);
        }

        state.setK12System(k12System);
        persistGameState(saveId, state);
    }

    @Override
    @Transactional
    public void processPipeline(Long saveId) {
        GameState state = loadGameState(saveId);
        GameState.K12System k12System = getOrCreateK12System(state);

        GameState.PipelineData pipelineData = k12System.getPipelineData();

        if (Boolean.TRUE.equals(k12System.getHasPrimary()) && Boolean.TRUE.equals(k12System.getHasJunior())) {
            processStageTransfer(saveId, k12System, "PRIMARY", "JUNIOR", pipelineData, PRIMARY_TO_JUNIOR_BASE_RATE);
        }

        if (Boolean.TRUE.equals(k12System.getHasJunior()) && Boolean.TRUE.equals(k12System.getHasSenior())) {
            processStageTransfer(saveId, k12System, "JUNIOR", "SENIOR", pipelineData, JUNIOR_TO_SENIOR_BASE_RATE);
        }

        updateFullTrainedCount(saveId, k12System);
        state.setK12System(k12System);
        persistGameState(saveId, state);
    }

    @Override
    public void updateSynergyEffects(Long saveId) {
        GameState state = loadGameState(saveId);
        GameState.K12System k12System = getOrCreateK12System(state);

        GameState.SynergyEffects synergyEffects = k12System.getSynergyEffects();
        if (synergyEffects == null) {
            synergyEffects = new GameState.SynergyEffects();
        }

        boolean hasTwoStages = (Boolean.TRUE.equals(k12System.getHasPrimary()) && Boolean.TRUE.equals(k12System.getHasJunior())) ||
                (Boolean.TRUE.equals(k12System.getHasJunior()) && Boolean.TRUE.equals(k12System.getHasSenior()));
        boolean hasAllThree = Boolean.TRUE.equals(k12System.getHasPrimary()) &&
                Boolean.TRUE.equals(k12System.getHasJunior()) &&
                Boolean.TRUE.equals(k12System.getHasSenior());

        synergyEffects.setResourceSharing(hasTwoStages);
        synergyEffects.setTeacherMobility(hasTwoStages);
        synergyEffects.setBrandBonus(hasAllThree);

        k12System.setSynergyEffects(synergyEffects);
        state.setK12System(k12System);
        persistGameState(saveId, state);
    }

    private void activateStage(Long saveId, GameState state, GameState.K12System k12System, String stageType) {
        boolean isPrimary = "PRIMARY".equalsIgnoreCase(stageType);
        String schoolName = isPrimary ? "育才小学" : "育才初中";
        String schoolType = isPrimary ? "PRIMARY" : "JUNIOR";

        School newSchool = new School();
        newSchool.setSaveId(saveId);
        newSchool.setName(schoolName);
        newSchool.setType(schoolType);
        newSchool.setLevel(isPrimary ? "区重点" : "市重点");
        newSchool.setReputation(0);
        newSchool.setFunds(new BigDecimal("500000"));
        newSchool.setStudentCount(0);
        newSchool.setTeacherCount(5);
        newSchool.setTotalYears(0);
        newSchool.setCreatedAt(LocalDateTime.now());
        newSchool.setUpdatedAt(LocalDateTime.now());
        schoolMapper.insert(newSchool);

        for (int i = 1; i <= 5; i++) {
            Teacher teacher = new Teacher();
            teacher.setSchoolId(newSchool.getId());
            teacher.setName("教师" + (isPrimary ? "小" : "初") + i);
            teacher.setLevel("二级教师");
            teacher.setTeachingAbility(55 + (int)(Math.random() * 15));
            teacher.setMoralLevel(60 + (int)(Math.random() * 20));
            teacher.setSalary(new BigDecimal("6000"));
            teacher.setExperience(3 + (int)(Math.random() * 10));
            teacher.setHireYear(state.getCurrentYear());
            teacher.setCreatedAt(LocalDateTime.now());
            teacher.setUpdatedAt(LocalDateTime.now());
            teacherMapper.insert(teacher);
        }

        Building teachingBuilding = new Building();
        teachingBuilding.setSchoolId(newSchool.getId());
        teachingBuilding.setType("教学楼");
        teachingBuilding.setLevel(1);
        teachingBuilding.setCapacity(300);
        teachingBuilding.setMonthlyCost(new BigDecimal("2000"));
        teachingBuilding.setBuildCost(new BigDecimal("100000"));
        teachingBuilding.setStatus("运营中");
        teachingBuilding.setCreatedAt(LocalDateTime.now());
        teachingBuilding.setUpdatedAt(LocalDateTime.now());
        buildingMapper.insert(teachingBuilding);

        Building dormitory = new Building();
        dormitory.setSchoolId(newSchool.getId());
        dormitory.setType("宿舍楼");
        dormitory.setLevel(1);
        dormitory.setCapacity(150);
        dormitory.setMonthlyCost(new BigDecimal("1200"));
        dormitory.setBuildCost(new BigDecimal("80000"));
        dormitory.setStatus("运营中");
        dormitory.setCreatedAt(LocalDateTime.now());
        dormitory.setUpdatedAt(LocalDateTime.now());
        buildingMapper.insert(dormitory);

        GameState.SchoolInfo schoolInfo = new GameState.SchoolInfo();
        schoolInfo.setName(schoolName);
        schoolInfo.setType(isPrimary ? "小学" : "初中");
        schoolInfo.setLevel(isPrimary ? "区重点" : "市重点");
        schoolInfo.setTotalYears(0);

        if (isPrimary) {
            k12System.setHasPrimary(true);
            k12System.setPrimarySchool(schoolInfo);
            k12System.setPrimaryBuildProgress(null);
        } else {
            k12System.setHasJunior(true);
            k12System.setJuniorSchool(schoolInfo);
            k12System.setJuniorBuildProgress(null);
        }
    }

    private PipelineDTO.PipelineData calculatePipelineData(Long saveId, GameState.K12System k12System,
                                                           String fromStage, String toStage,
                                                           Double currentRate, double baseRate, Integer totalGraduates) {
        PipelineDTO.PipelineData data = new PipelineDTO.PipelineData();
        int graduates = totalGraduates != null ? totalGraduates : 0;
        data.setTotalGraduates(graduates);

        double rate = currentRate != null ? currentRate : baseRate;
        data.setTransferRate(rate);

        double transferCount = graduates * rate;
        data.setTransferCount(transferCount);
        data.setExternalCount(graduates - transferCount);

        int repGain = (int)Math.round((graduates - transferCount) * 0.1);
        data.setReputationGain(repGain);
        data.setBonusDescription("直升学生适应期-50%,成绩+10%");

        return data;
    }

    private PipelineDTO.K12Statistics calculateK12Statistics(Long saveId, GameState.K12System k12System) {
        PipelineDTO.K12Statistics stats = new PipelineDTO.K12Statistics();
        stats.setFullTrainedStudents(k12System.getPipelineData().getK12FullTrainedStudents() != null ?
                k12System.getPipelineData().getK12FullTrainedStudents() : 0);

        int currentK12 = countCurrentK12Students(saveId);
        stats.setCurrentK12Students(currentK12);
        stats.setGaokaoBonusRate(new BigDecimal("0.05"));

        int consecutiveYears = k12System.getConsecutiveExcellenceYears() != null ?
                k12System.getConsecutiveExcellenceYears() : 0;
        stats.setConsecutiveExcellenceYears(consecutiveYears);

        return stats;
    }

    private int countCurrentK12Students(Long saveId) {
        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Student::getIsK12Student, 1)
               .eq(Student::getStatus, "在校");
        return Math.toIntExact(studentMapper.selectCount(wrapper));
    }

    private void processStageTransfer(Long saveId, GameState.K12System k12System,
                                       String fromStage, String toStage,
                                       GameState.PipelineData pipelineData, double baseRate) {
        Long fromSchoolId = "PRIMARY".equals(fromStage) ?
                getSchoolIdByType(saveId, "PRIMARY") : getSchoolIdByType(saveId, "JUNIOR");
        if (fromSchoolId == null) return;

        String targetGrade = "PRIMARY".equals(fromStage) ? "初一" : "高一";

        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Student::getSchoolId, fromSchoolId)
               .eq(Student::getStatus, "在校")
               .and(w -> w.like(Student::getGrade, "六年级").or()
                       .like(Student::getGrade, "初三"));
        List<Student> graduatingStudents = studentMapper.selectList(wrapper);

        if (graduatingStudents.isEmpty()) return;

        int totalGraduates = graduatingStudents.size();
        double qualityScore = calculateTeachingQuality(saveId, fromSchoolId);
        double transferRate = Math.min(0.95, qualityScore * baseRate);

        Random rng = new Random(2024L + fromSchoolId);
        Collections.shuffle(graduatingStudents, rng);

        int transferCount = (int)Math.round(totalGraduates * transferRate);
        Long toSchoolId = "JUNIOR".equals(toStage) ? getSchoolIdByType(saveId, "JUNIOR") :
                getSchoolIdByType(saveId, "SENIOR");
        if (toSchoolId == null) return;

        int transferred = 0;
        for (Student student : graduatingStudents) {
            if (transferred < transferCount) {
                student.setIsK12Student(1);
                student.setFromSchoolId(fromSchoolId);
                student.setSchoolId(toSchoolId);
                student.setGrade(targetGrade);
                BigDecimal currentScore = student.getAcademicScore() != null ? student.getAcademicScore() : new BigDecimal("60");
                student.setAcademicScore(currentScore.add(new BigDecimal("10")));
                studentMapper.updateById(student);
                transferred++;
            } else {
                student.setStatus("毕业");
                studentMapper.updateById(student);
            }
        }

        int externalCount = totalGraduates - transferred;
        int reputationGain = (int)Math.round(externalCount * 0.1);

        if ("PRIMARY".equals(fromStage)) {
            pipelineData.setPrimaryGraduates(totalGraduates);
            pipelineData.setPrimaryToJuniorRate(transferRate);
        } else {
            pipelineData.setJuniorGraduates(totalGraduates);
            pipelineData.setJuniorToSeniorRate(transferRate);
        }

        GameState state = loadGameState(saveId);
        state.setReputation((state.getReputation() != null ? state.getReputation() : 0) + reputationGain);
        persistGameState(saveId, state);
    }

    private double calculateTeachingQuality(Long saveId, Long schoolId) {
        LambdaQueryWrapper<Teacher> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Teacher::getSchoolId, schoolId);
        List<Teacher> teachers = teacherMapper.selectList(wrapper);
        if (teachers.isEmpty()) return 0.5;

        double avgAbility = teachers.stream()
                .mapToInt(t -> t.getTeachingAbility() != null ? t.getTeachingAbility() : 50)
                .average()
                .orElse(60.0);

        return avgAbility / 100.0;
    }

    private Long getSchoolIdByType(Long saveId, String type) {
        LambdaQueryWrapper<School> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(School::getSaveId, saveId).eq(School::getType, type);
        School school = schoolMapper.selectOne(wrapper);
        return school != null ? school.getId() : null;
    }

    private void updateFullTrainedCount(Long saveId, GameState.K12System k12System) {
        Long primarySchoolId = getSchoolIdByType(saveId, "PRIMARY");
        Long seniorSchoolId = getSchoolIdByType(saveId, "SENIOR");
        if (primarySchoolId == null || seniorSchoolId == null) return;

        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Student::getSchoolId, seniorSchoolId)
               .eq(Student::getFromSchoolId, primarySchoolId)
               .eq(Student::getIsK12Student, 1)
               .eq(Student::getStatus, "在校");
        long fullTrained = studentMapper.selectCount(wrapper);
        k12System.getPipelineData().setK12FullTrainedStudents((int)fullTrained);
    }

    private K12StatusDTO.StageInfo buildStageInfo(GameState.SchoolInfo school, String type, String status) {
        K12StatusDTO.StageInfo info = new K12StatusDTO.StageInfo();
        info.setType(type);
        info.setStatus(status);
        if (school != null) {
            info.setName(school.getName());
            info.setLevel(school.getLevel());
        } else {
            info.setName("SENIOR".equals(type) ? "育才高中" : ("JUNIOR".equals(type) ? "育才初中" : "育才小学"));
            info.setLevel("");
        }
        info.setStudents(0);
        info.setTeachers(0);
        return info;
    }

    private K12StatusDTO.StageInfo createEmptyStageInfo(String type, String status) {
        K12StatusDTO.StageInfo info = new K12StatusDTO.StageInfo();
        info.setType(type);
        info.setStatus(status);
        info.setName("NOT_BUILT".equals(status) ? ("PRIMARY".equals(type) ? "育才小学" : "育才初中") : "");
        info.setStudents(0);
        info.setTeachers(0);
        info.setLevel("");
        return info;
    }

    private BuildProgressDTO getActiveBuildProgress(GameState.K12System k12System) {
        Map<String, Object> primaryProgress = k12System.getPrimaryBuildProgress();
        if (primaryProgress != null && "BUILDING".equals(primaryProgress.get("status"))) {
            return convertToBuildProgressDTO("PRIMARY", primaryProgress, new BigDecimal(PRIMARY_BUILD_COST));
        }
        Map<String, Object> juniorProgress = k12System.getJuniorBuildProgress();
        if (juniorProgress != null && "BUILDING".equals(juniorProgress.get("status"))) {
            return convertToBuildProgressDTO("JUNIOR", juniorProgress, new BigDecimal(JUNIOR_BUILD_COST));
        }
        return null;
    }

    private BuildProgressDTO convertToBuildProgressDTO(String stageType, Map<String, Object> progress, BigDecimal buildCost) {
        BuildProgressDTO dto = new BuildProgressDTO();
        dto.setStageType(stageType);
        dto.setStartYear(((Number) progress.getOrDefault("startYear", 0)).intValue());
        dto.setStartMonth(((Number) progress.getOrDefault("startMonth", 0)).intValue());
        dto.setTotalMonths(((Number) progress.getOrDefault("totalMonths", BUILD_DURATION_MONTHS)).intValue());
        dto.setElapsedMonths(((Number) progress.getOrDefault("elapsedMonths", 0)).intValue());

        int total = dto.getTotalMonths();
        int elapsed = dto.getElapsedMonths();
        dto.setProgressPercent(total > 0 ? Math.min(100, (elapsed * 100) / total) : 0);

        dto.setBuildCost(buildCost);
        dto.setStatus((String) progress.getOrDefault("status", "UNKNOWN"));
        return dto;
    }

    private GameState.K12System getOrCreateK12System(GameState state) {
        if (state.getK12System() == null) {
            GameState.K12System k12System = new GameState.K12System();
            k12System.setHasSenior(true);
            k12System.setHasJunior(false);
            k12System.setHasPrimary(false);

            GameState.SchoolInfo seniorInfo = new GameState.SchoolInfo();
            seniorInfo.setName("育才高中");
            seniorInfo.setType("高中");
            seniorInfo.setLevel("普通");
            seniorInfo.setTotalYears(0);
            k12System.setSeniorSchool(seniorInfo);

            GameState.PipelineData pipelineData = new GameState.PipelineData();
            pipelineData.setPrimaryToJuniorRate(0.0);
            pipelineData.setJuniorToSeniorRate(0.0);
            pipelineData.setPrimaryGraduates(0);
            pipelineData.setJuniorGraduates(0);
            pipelineData.setK12FullTrainedStudents(0);
            k12System.setPipelineData(pipelineData);

            GameState.SynergyEffects synergyEffects = new GameState.SynergyEffects();
            synergyEffects.setResourceSharing(false);
            synergyEffects.setTeacherMobility(false);
            synergyEffects.setBrandBonus(false);
            synergyEffects.setDistrictEffect(false);
            k12System.setSynergyEffects(synergyEffects);

            k12System.setConsecutiveExcellenceYears(0);
            state.setK12System(k12System);
        }
        return state.getK12System();
    }

    private GameState loadGameState(Long saveId) {
        GameSave save = gameSaveMapper.selectById(saveId);
        if (save == null) {
            throw new BusinessException("存档不存在");
        }
        String gameStateJson = save.getGameState();
        if (gameStateJson == null || gameStateJson.isBlank()) {
            throw new BusinessException("存档数据为空");
        }
        try {
            return objectMapper.readValue(gameStateJson, new TypeReference<GameState>() {});
        } catch (JsonProcessingException e) {
            throw new BusinessException("存档数据解析失败: " + e.getMessage());
        }
    }

    private void persistGameState(Long saveId, GameState state) {
        try {
            String json = objectMapper.writeValueAsString(state);
            gameSaveService.updateSave(null, saveId, json);
        } catch (JsonProcessingException e) {
            throw new BusinessException("存档数据保存失败: " + e.getMessage());
        }
    }

    private void validateSaveExists(Long saveId) {
        GameSave save = gameSaveMapper.selectById(saveId);
        if (save == null) {
            throw new BusinessException("存档不存在");
        }
    }
}
