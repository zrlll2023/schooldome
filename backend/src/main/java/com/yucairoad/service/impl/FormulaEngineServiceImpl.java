package com.yucairoad.service.impl;

import com.yucairoad.dto.FinancialReport;
import com.yucairoad.dto.ScoreResult;
import com.yucairoad.dto.ReputationChange;
import com.yucairoad.dto.EnrollmentPolicy;
import com.yucairoad.dto.TeachingPolicy;
import com.yucairoad.entity.Building;
import com.yucairoad.entity.ExamRecord;
import com.yucairoad.entity.School;
import com.yucairoad.entity.Student;
import com.yucairoad.entity.Teacher;
import com.yucairoad.service.FinanceService;
import com.yucairoad.service.FormulaEngineService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class FormulaEngineServiceImpl implements FormulaEngineService {

    private static final double MIN_TEACHER_BONUS = 0.5;
    private static final double MAX_TEACHER_BONUS = 1.5;
    private static final double MIN_FACILITY_BONUS = 0.8;
    private static final double MAX_FACILITY_BONUS = 1.2;
    private static final double MIN_COURSE_RATIONALITY = 0.7;
    private static final double MAX_COURSE_RATIONALITY = 1.0;
    private static final double RANDOM_STD_DEV = 0.08;
    private static final int SCORE_BASE = 100;

    private static final double PRESSURE_LOW_THRESHOLD = 30.0;
    private static final double PRESSURE_MID_THRESHOLD = 70.0;
    private static final double PRESSURE_MID_PENALTY = -0.2;
    private static final double PRESSURE_HIGH_PENALTY = -0.5;
    private static final double QUALITY_BONUS_PER_10 = 0.02;
    private static final double QUALITY_BONUS_MAX = 0.2;

    private final FinanceService financeService;

    public FormulaEngineServiceImpl(FinanceService financeService) {
        this.financeService = financeService;
    }

    @Override
    public ScoreResult calculateScore(Student student, List<Teacher> teachers, List<Building> buildings, TeachingPolicy policy) {
        ScoreResult result = new ScoreResult();
        result.setStudentId(student.getId());
        result.setStudentName(student.getName());

        double basePotential = getBasePotential(student.getGradeLevel());
        result.setBasePotential(BigDecimal.valueOf(basePotential));

        double teacherBonus = calculateTeacherBonus(teachers);
        teacherBonus = clamp(teacherBonus, MIN_TEACHER_BONUS, MAX_TEACHER_BONUS);
        result.setTeacherBonus(BigDecimal.valueOf(teacherBonus).setScale(4, RoundingMode.HALF_UP));

        double facilityBonus = calculateFacilityBonus(buildings);
        facilityBonus = clamp(facilityBonus, MIN_FACILITY_BONUS, MAX_FACILITY_BONUS);
        result.setFacilityBonus(BigDecimal.valueOf(facilityBonus).setScale(4, RoundingMode.HALF_UP));

        double courseRationality = 1.0;
        courseRationality = clamp(courseRationality, MIN_COURSE_RATIONALITY, MAX_COURSE_RATIONALITY);
        result.setCourseRationality(BigDecimal.valueOf(courseRationality));

        double effortCoefficient = calculateEffortCoefficient(student, policy);
        result.setEffortCoefficient(BigDecimal.valueOf(effortCoefficient).setScale(4, RoundingMode.HALF_UP));

        long seed = (student.getId() != null ? student.getId() : 0L) + getCurrentYearFromContext();
        Random random = new Random(seed);
        double randomFactor = random.nextGaussian() * RANDOM_STD_DEV + 1.0;
        randomFactor = clamp(randomFactor, 0.7, 1.3);
        result.setRandomFactor(BigDecimal.valueOf(randomFactor).setScale(4, RoundingMode.HALF_UP));

        double finalScore = basePotential * SCORE_BASE * teacherBonus * facilityBonus
                * courseRationality * effortCoefficient * randomFactor;
        finalScore = Math.round(finalScore * 10.0) / 10.0;
        finalScore = clamp(finalScore, 0, 150);
        result.setFinalScore(BigDecimal.valueOf(finalScore).setScale(1, RoundingMode.HALF_UP));

        return result;
    }

    @Override
    public ReputationChange calculateReputation(School school, List<ExamRecord> examRecords) {
        ReputationChange change = new ReputationChange();

        if (examRecords != null && !examRecords.isEmpty()) {
            double avgScore = examRecords.stream()
                    .filter(r -> r.getScore() != null)
                    .mapToInt(r -> r.getScore().intValue())
                    .average()
                    .orElse(0);

            if (avgScore > 600) {
                change.getSources().add(new ReputationChange.ReputationSource(
                        "EXAM_SCORE", "高考平均分超过600分", 50));
            } else if (avgScore > 550) {
                change.getSources().add(new ReputationChange.ReputationSource(
                        "EXAM_SCORE", "高考平均分超过550分", 20));
            } else if (avgScore > 500) {
                change.getSources().add(new ReputationChange.ReputationSource(
                        "EXAM_SCORE", "高考平均分超过500分", 10));
            } else if (avgScore > 450) {
                change.getSources().add(new ReputationChange.ReputationSource(
                        "EXAM_SCORE", "高考平均分超过450分", 5));
            } else if (avgScore < 300 && avgScore > 0) {
                change.getSources().add(new ReputationChange.ReputationSource(
                        "EXAM_SCORE", "高考平均分低于300分", -5));
            }
        }

        change.setTotalChange(change.getSources().stream()
                .mapToInt(ReputationChange.ReputationSource::getValue)
                .sum());

        return change;
    }

    @Override
    public FinancialReport calculateFinance(Long saveId) {
        return financeService.getFinancialReport(saveId);
    }

    @Override
    public double calculateEnrollmentQuality(School school, EnrollmentPolicy policy) {
        if (school == null || policy == null) {
            return 50.0;
        }

        double quality = 50.0;

        int reputation = school.getReputation() != null ? school.getReputation() : 0;
        quality += Math.min(reputation / 10.0, 20.0);

        String tuitionLevel = policy.getTuitionLevel();
        if ("高".equals(tuitionLevel) || "极高".equals(tuitionLevel)) {
            quality += 10.0;
        } else if ("低".equals(tuitionLevel)) {
            quality -= 5.0;
        }

        String standard = policy.getEnrollmentStandard();
        if ("严格".equals(standard)) {
            quality += 15.0;
        } else if ("宽松".equals(standard)) {
            quality -= 10.0;
        }

        return clamp(quality, 0, 100);
    }

    @Override
    public List<Student> updateStudentAttributes(List<Student> students, TeachingPolicy policy) {
        if (students == null || students.isEmpty()) {
            return students;
        }

        Random random = new Random(System.currentTimeMillis());
        for (Student student : students) {
            updateSingleStudentAttributes(student, policy, random);
        }
        return students;
    }

    private void updateSingleStudentAttributes(Student student, TeachingPolicy policy, Random random) {
        double academicChange = -2 + random.nextDouble() * 5;
        double qualityChange = -1 + random.nextDouble() * 3;
        double healthChange = -3 + random.nextDouble() * 6;

        if (policy != null) {
            String extracurricular = policy.getExtracurricular();
            if ("RICH".equalsIgnoreCase(extracurricular)) {
                qualityChange += 2;
            } else if ("MODERATE".equalsIgnoreCase(extracurricular)) {
                qualityChange += 1;
            }

            String homeworkLoad = policy.getHomeworkLoad();
            if ("HEAVY".equalsIgnoreCase(homeworkLoad)) {
                healthChange -= 2;
            } else if ("MODERATE".equalsIgnoreCase(homeworkLoad)) {
                healthChange -= 1;
            }

            String weekendArrangement = policy.getWeekendArrangement();
            if ("REST".equalsIgnoreCase(weekendArrangement)) {
                healthChange += 2;
            } else if ("FULL_DAY".equalsIgnoreCase(weekendArrangement)) {
                healthChange -= 1;
            }
        }

        BigDecimal currentAcademic = student.getAcademicScore() != null ?
                student.getAcademicScore() : BigDecimal.valueOf(60);
        student.setAcademicScore(clampBigDecimal(currentAcademic.add(BigDecimal.valueOf(academicChange)), 0, 100));

        BigDecimal currentQuality = student.getQualityScore() != null ?
                student.getQualityScore() : BigDecimal.valueOf(60);
        student.setQualityScore(clampBigDecimal(currentQuality.add(BigDecimal.valueOf(qualityChange)), 0, 100));

        BigDecimal currentHealth = student.getHealthScore() != null ?
                student.getHealthScore() : BigDecimal.valueOf(80);
        student.setHealthScore(clampBigDecimal(currentHealth.add(BigDecimal.valueOf(healthChange)), 0, 100));
    }

    private double getBasePotential(String gradeLevel) {
        if (gradeLevel == null) {
            return 1.0;
        }
        return switch (gradeLevel.toUpperCase()) {
            case "S" -> 1.2;
            case "A" -> 1.1;
            case "B" -> 1.0;
            case "C" -> 0.9;
            case "D" -> 0.8;
            default -> 1.0;
        };
    }

    private double calculateTeacherBonus(List<Teacher> teachers) {
        if (teachers == null || teachers.isEmpty()) {
            return 0.8;
        }
        double avgAbility = teachers.stream()
                .filter(t -> t.getTeachingAbility() != null)
                .mapToInt(Teacher::getTeachingAbility)
                .average()
                .orElse(60.0);
        return avgAbility / 100.0;
    }

    private double calculateFacilityBonus(List<Building> buildings) {
        double bonus = 0.8;
        if (buildings == null || buildings.isEmpty()) {
            return bonus;
        }
        for (Building building : buildings) {
            String type = building.getType();
            Integer level = building.getLevel() != null ? building.getLevel() : 1;
            if ("图书馆".equals(type)) {
                bonus += level * 0.02;
            } else if ("实验楼".equals(type)) {
                bonus += level * 0.05;
            }
        }
        return bonus;
    }

    private double calculateEffortCoefficient(Student student, TeachingPolicy policy) {
        double pressure = calculatePressureValue(student, policy);

        double pressurePenalty;
        if (pressure < PRESSURE_LOW_THRESHOLD) {
            pressurePenalty = 0;
        } else if (pressure <= PRESSURE_MID_THRESHOLD) {
            pressurePenalty = (pressure - PRESSURE_LOW_THRESHOLD) / (PRESSURE_MID_THRESHOLD - PRESSURE_LOW_THRESHOLD)
                    * PRESSURE_MID_PENALTY;
        } else {
            pressurePenalty = PRESSURE_MID_PENALTY + (pressure - PRESSURE_MID_THRESHOLD) / (100 - PRESSURE_MID_THRESHOLD)
                    * (PRESSURE_HIGH_PENALTY - PRESSURE_MID_PENALTY);
        }

        double currentQuality = student.getQualityScore() != null ?
                student.getQualityScore().doubleValue() : 60.0;
        double qualityBonus = Math.min((currentQuality / 10.0) * QUALITY_BONUS_PER_10, QUALITY_BONUS_MAX);

        return (1.0 - pressurePenalty) + qualityBonus;
    }

    private double calculatePressureValue(Student student, TeachingPolicy policy) {
        double pressure = 40.0;

        if (policy != null) {
            String homeworkLoad = policy.getHomeworkLoad();
            if ("HEAVY".equalsIgnoreCase(homeworkLoad)) {
                pressure += 25;
            } else if ("MODERATE".equalsIgnoreCase(homeworkLoad)) {
                pressure += 12;
            } else if ("LIGHT".equalsIgnoreCase(homeworkLoad)) {
                pressure -= 10;
            }

            String weekendArrangement = policy.getWeekendArrangement();
            if ("FULL_DAY".equalsIgnoreCase(weekendArrangement)) {
                pressure += 20;
            } else if ("HALF_DAY".equalsIgnoreCase(weekendArrangement)) {
                pressure += 5;
            } else if ("REST".equalsIgnoreCase(weekendArrangement)) {
                pressure -= 15;
            }
        }

        return clamp(pressure, 0, 100);
    }

    private long getCurrentYearFromContext() {
        return System.currentTimeMillis() / (365L * 24L * 60L * 60L * 1000L);
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private BigDecimal clampBigDecimal(BigDecimal value, int min, int max) {
        return value.max(BigDecimal.valueOf(min)).min(BigDecimal.valueOf(max));
    }
}
