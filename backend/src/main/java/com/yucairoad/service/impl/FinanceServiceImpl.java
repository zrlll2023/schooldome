package com.yucairoad.service.impl;

import com.yucairoad.common.BusinessException;
import com.yucairoad.dto.FinancialReport;
import com.yucairoad.dto.GameState;
import com.yucairoad.entity.BranchSchool;
import com.yucairoad.entity.Building;
import com.yucairoad.entity.EventLog;
import com.yucairoad.entity.GameSave;
import com.yucairoad.entity.Teacher;
import com.yucairoad.mapper.BranchSchoolMapper;
import com.yucairoad.mapper.BuildingMapper;
import com.yucairoad.mapper.EventLogMapper;
import com.yucairoad.mapper.GameSaveMapper;
import com.yucairoad.mapper.SchoolMapper;
import com.yucairoad.mapper.StudentMapper;
import com.yucairoad.mapper.TeacherMapper;
import com.yucairoad.service.FinanceService;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
public class FinanceServiceImpl implements FinanceService {

    private static final BigDecimal BASE_GOVERNMENT_GRANT = new BigDecimal("500000");
    private static final int MONTHS_PER_YEAR = 12;
    private static final BigDecimal PERFORMANCE_MAX_ANNUAL = new BigDecimal("2000000");
    private static final BigDecimal STUDENT_SUBSIDY_PER_STUDENT = new BigDecimal("500");
    private static final BigDecimal LOAN_ANNUAL_RATE = new BigDecimal("0.08");

    private static final BigDecimal TUITION_LOW = new BigDecimal("3000");
    private static final BigDecimal TUITION_MEDIUM = new BigDecimal("6000");
    private static final BigDecimal TUITION_HIGH = new BigDecimal("12000");
    private static final BigDecimal TUITION_VERY_HIGH = new BigDecimal("20000");

    private static final BigDecimal DONATION_MIN = new BigDecimal("10000");
    private static final BigDecimal DONATION_MAX = new BigDecimal("500000");
    private static final BigDecimal COOPERATION_MIN = new BigDecimal("50000");
    private static final BigDecimal COOPERATION_MAX = new BigDecimal("200000");
    private static final BigDecimal BRANCH_REMITTANCE_RATE = new BigDecimal("0.20");

    private final GameSaveMapper gameSaveMapper;
    private final SchoolMapper schoolMapper;
    private final StudentMapper studentMapper;
    private final TeacherMapper teacherMapper;
    private final BuildingMapper buildingMapper;
    private final BranchSchoolMapper branchSchoolMapper;
    private final EventLogMapper eventLogMapper;
    private final ObjectMapper objectMapper;

    public FinanceServiceImpl(GameSaveMapper gameSaveMapper,
                              SchoolMapper schoolMapper,
                              StudentMapper studentMapper,
                              TeacherMapper teacherMapper,
                              BuildingMapper buildingMapper,
                              BranchSchoolMapper branchSchoolMapper,
                              EventLogMapper eventLogMapper) {
        this.gameSaveMapper = gameSaveMapper;
        this.schoolMapper = schoolMapper;
        this.studentMapper = studentMapper;
        this.teacherMapper = teacherMapper;
        this.buildingMapper = buildingMapper;
        this.branchSchoolMapper = branchSchoolMapper;
        this.eventLogMapper = eventLogMapper;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public FinancialReport calculateMonthlyIncome(Long saveId) {
        FinancialReport report = new FinancialReport();

        GameState state = loadGameState(saveId);
        Long schoolId = getSchoolId(state);

        BigDecimal tuitionIncome = calculateTuitionIncome(schoolId, state);
        report.getIncomeBreakdown().setTuition(tuitionIncome);

        BigDecimal governmentGrant = calculateGovernmentGrant(state);
        report.getIncomeBreakdown().setGovernmentGrant(governmentGrant);

        BigDecimal donation = tryTriggerDonation(schoolId, state);
        report.getIncomeBreakdown().setDonation(donation);

        BigDecimal cooperation = tryTriggerCooperation(state);
        report.getIncomeBreakdown().setCooperation(cooperation);

        BigDecimal branchRemittance = calculateBranchRemittance(saveId);
        report.getIncomeBreakdown().setBranchRemittance(branchRemittance);

        BigDecimal totalIncome = tuitionIncome.add(governmentGrant)
                .add(donation).add(cooperation).add(branchRemittance);
        report.setMonthlyIncome(totalIncome.setScale(2, RoundingMode.HALF_UP));

        return report;
    }

    @Override
    public FinancialReport calculateMonthlyExpense(Long saveId) {
        FinancialReport report = new FinancialReport();

        GameState state = loadGameState(saveId);
        Long schoolId = getSchoolId(state);

        BigDecimal teacherSalary = calculateTeacherSalary(schoolId, state);
        report.getExpenseBreakdown().setTeacherSalary(teacherSalary);

        BigDecimal studentSubsidy = calculateStudentSubsidy(schoolId, state);
        report.getExpenseBreakdown().setStudentSubsidy(studentSubsidy);

        BigDecimal buildingMaintenance = calculateBuildingMaintenance(schoolId, state);
        report.getExpenseBreakdown().setBuildingMaintenance(buildingMaintenance);

        BigDecimal activities = BigDecimal.ZERO;
        report.getExpenseBreakdown().setActivities(activities);

        BigDecimal loanInterest = calculateLoanInterest(state);
        report.getExpenseBreakdown().setLoanInterest(loanInterest);

        BigDecimal totalExpense = teacherSalary.add(studentSubsidy)
                .add(buildingMaintenance).add(activities).add(loanInterest);
        report.setMonthlyExpense(totalExpense.setScale(2, RoundingMode.HALF_UP));

        return report;
    }

    @Override
    public FinancialReport getFinancialReport(Long saveId) {
        FinancialReport incomeReport = calculateMonthlyIncome(saveId);
        FinancialReport expenseReport = calculateMonthlyExpense(saveId);

        FinancialReport report = new FinancialReport();
        report.setMonthlyIncome(incomeReport.getMonthlyIncome());
        report.setMonthlyExpense(expenseReport.getMonthlyExpense());
        report.setIncomeBreakdown(incomeReport.getIncomeBreakdown());
        report.setExpenseBreakdown(expenseReport.getExpenseBreakdown());

        BigDecimal netProfit = incomeReport.getMonthlyIncome().subtract(expenseReport.getMonthlyExpense());
        report.setNetProfit(netProfit.setScale(2, RoundingMode.HALF_UP));

        GameState state = loadGameState(saveId);
        BigDecimal currentBalance = state.getFunds() != null ? state.getFunds() : BigDecimal.ZERO;
        report.setBalance(currentBalance.setScale(2, RoundingMode.HALF_UP));

        return report;
    }

    @Override
    public FinancialReport processMonthlySettlement(Long saveId) {
        FinancialReport report = getFinancialReport(saveId);

        GameState state = loadGameState(saveId);
        BigDecimal currentFunds = state.getFunds() != null ? state.getFunds() : BigDecimal.ZERO;
        BigDecimal newFunds = currentFunds.add(report.getNetProfit());
        state.setFunds(newFunds.setScale(2, RoundingMode.HALF_UP));
        persistGameState(saveId, state);

        report.setBalance(newFunds.setScale(2, RoundingMode.HALF_UP));

        if (newFunds.compareTo(BigDecimal.ZERO) < 0) {
            recordFundWarningEvent(saveId, newFunds);
        }

        return report;
    }

    private BigDecimal calculateTuitionIncome(Long schoolId, GameState state) {
        int studentCount = getStudentCount(schoolId, state);
        if (studentCount <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal perStudentTuition = getTuitionPerStudent(state);
        return perStudentTuition.multiply(BigDecimal.valueOf(studentCount))
                .divide(BigDecimal.valueOf(MONTHS_PER_YEAR), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal getTuitionPerStudent(GameState state) {
        if (state == null || state.getEnrollmentPolicy() == null ||
                state.getEnrollmentPolicy().getTuitionLevel() == null) {
            return TUITION_MEDIUM;
        }
        String level = state.getEnrollmentPolicy().getTuitionLevel();
        return switch (level) {
            case "低" -> TUITION_LOW;
            case "中" -> TUITION_MEDIUM;
            case "高" -> TUITION_HIGH;
            case "极高" -> TUITION_VERY_HIGH;
            default -> TUITION_MEDIUM;
        };
    }

    private BigDecimal calculateGovernmentGrant(GameState state) {
        BigDecimal baseGrant = BASE_GOVERNMENT_GRANT.divide(
                BigDecimal.valueOf(MONTHS_PER_YEAR), 2, RoundingMode.HALF_UP);

        double lastExamAvg = getLastExamAverageScore(state);
        BigDecimal performanceBonus = BigDecimal.valueOf(lastExamAvg * 1000)
                .divide(BigDecimal.valueOf(MONTHS_PER_YEAR), 2, RoundingMode.HALF_UP);
        BigDecimal maxMonthlyPerformance = PERFORMANCE_MAX_ANNUAL.divide(
                BigDecimal.valueOf(MONTHS_PER_YEAR), 2, RoundingMode.HALF_UP);
        performanceBonus = performanceBonus.min(maxMonthlyPerformance);

        return baseGrant.add(performanceBonus);
    }

    private BigDecimal tryTriggerDonation(Long schoolId, GameState state) {
        if (state == null || state.getSchool() == null) {
            return BigDecimal.ZERO;
        }

        Random random = new Random(System.currentTimeMillis());
        double donationChance = 0.05 + (state.getReputation() != null ? state.getReputation() / 500.0 : 0);
        donationChance = Math.min(donationChance, 0.3);

        if (random.nextDouble() > donationChance) {
            return BigDecimal.ZERO;
        }

        long minAmount = DONATION_MIN.longValue();
        long maxAmount = DONATION_MAX.longValue();
        long amount = minAmount + (long)(random.nextDouble() * (maxAmount - minAmount));
        return BigDecimal.valueOf(amount);
    }

    private BigDecimal tryTriggerCooperation(GameState state) {
        if (state == null || state.getReputation() == null) {
            return BigDecimal.ZERO;
        }

        Random random = new Random(System.currentTimeMillis() + 1);
        double coopChance = Math.min(state.getReputation() / 200.0, 0.15);

        if (random.nextDouble() > coopChance) {
            return BigDecimal.ZERO;
        }

        long minAmount = COOPERATION_MIN.longValue();
        long maxAmount = COOPERATION_MAX.longValue();
        long amount = minAmount + (long)(random.nextDouble() * (maxAmount - minAmount));
        return BigDecimal.valueOf(amount);
    }

    private BigDecimal calculateBranchRemittance(Long saveId) {
        List<BranchSchool> branches = branchSchoolMapper.selectList(
                com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper.<BranchSchool>wrapper ->
                        wrapper.eq(BranchSchool::getSaveId, saveId)
                                .eq(BranchSchool::getStatus, "ACTIVE")
        );

        if (branches == null || branches.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal totalRemittance = BigDecimal.ZERO;
        for (BranchSchool branch : branches) {
            if (branch.getAnnualProfit() != null && branch.getAnnualProfit().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal remittance = branch.getAnnualProfit()
                        .multiply(BRANCH_REMITTANCE_RATE)
                        .divide(BigDecimal.valueOf(MONTHS_PER_YEAR), 2, RoundingMode.HALF_UP);
                totalRemittance = totalRemittance.add(remittance);
            }
        }
        return totalRemittance;
    }

    private BigDecimal calculateTeacherSalary(Long schoolId, GameState state) {
        BigDecimal totalSalary = BigDecimal.ZERO;

        if (state != null && state.getSchool() != null && state.getSchool().getTeachers() != null) {
            for (GameState.TeacherInfo teacher : state.getSchool().getTeachers()) {
                if (teacher.getSalary() != null) {
                    totalSalary = totalSalary.add(teacher.getSalary());
                }
            }
        } else if (schoolId != null) {
            List<Teacher> teachers = teacherMapper.selectList(
                    com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper.<Teacher>wrapper ->
                            wrapper.eq(Teacher::getSchoolId, schoolId)
            );
            for (Teacher teacher : teachers) {
                if (teacher.getSalary() != null) {
                    totalSalary = totalSalary.add(teacher.getSalary());
                }
            }
        }

        return totalSalary;
    }

    private BigDecimal calculateStudentSubsidy(Long schoolId, GameState state) {
        int studentCount = getStudentCount(schoolId, state);
        return STUDENT_SUBSIDY_PER_STUDENT.multiply(BigDecimal.valueOf(Math.max(0, studentCount)));
    }

    private BigDecimal calculateBuildingMaintenance(Long schoolId, GameState state) {
        BigDecimal maintenance = BigDecimal.ZERO;

        if (state != null && state.getSchool() != null && state.getSchool().getBuildings() != null) {
            for (GameState.BuildingInfo building : state.getSchool().getBuildings()) {
                if (building.getMonthlyCost() != null) {
                    maintenance = maintenance.add(building.getMonthlyCost());
                }
            }
        } else if (schoolId != null) {
            List<Building> buildings = buildingMapper.selectList(
                    com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper.<Building>wrapper ->
                            wrapper.eq(Building::getSchoolId, schoolId)
            );
            for (Building building : buildings) {
                if (building.getMonthlyCost() != null) {
                    maintenance = maintenance.add(building.getMonthlyCost());
                }
            }
        }

        return maintenance;
    }

    private BigDecimal calculateLoanInterest(GameState state) {
        if (state == null || state.getFunds() == null || state.getFunds().compareTo(BigDecimal.ZERO) >= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal loanPrincipal = state.getFunds().abs();
        return loanPrincipal
                .multiply(LOAN_ANNUAL_RATE)
                .divide(BigDecimal.valueOf(MONTHS_PER_YEAR), 2, RoundingMode.HALF_UP);
    }

    private void recordFundWarningEvent(Long saveId, BigDecimal funds) {
        EventLog event = new EventLog();
        event.setSaveId(saveId);
        event.setEventType("FINANCIAL_WARNING");
        event.setEventTitle("资金不足警告");
        event.setEventDescription(String.format("学校资金已不足！当前余额: %.2f元，请尽快采取措施增加收入或减少支出", funds.doubleValue()));
        event.setResult("WARNING");
        event.setTriggerYear(LocalDateTime.now().getYear());
        event.setTriggerMonth(LocalDateTime.now().getMonthValue());
        event.setCreatedAt(LocalDateTime.now());
        eventLogMapper.insert(event);
    }

    private int getStudentCount(Long schoolId, GameState state) {
        if (state != null && state.getStudentCount() != null) {
            return state.getStudentCount();
        }
        if (schoolId != null) {
            Long count = studentMapper.selectCount(
                    com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper.<Student>wrapper ->
                            wrapper.eq(Student::getSchoolId, schoolId)
                            .eq(Student::getStatus, "ENROLLED")
            );
            return count.intValue();
        }
        return 0;
    }

    private Long getSchoolId(GameState state) {
        if (state != null && state.getSchool() != null) {
            return null;
        }
        return null;
    }

    private double getLastExamAverageScore(GameState state) {
        if (state == null || state.getStatistics() == null || state.getStatistics().getAvgAcademic() == null) {
            return 450.0;
        }
        return state.getStatistics().getAvgAcademic();
    }

    private GameState loadGameState(Long saveId) {
        GameSave save = gameSaveMapper.selectById(saveId);
        if (save == null) {
            throw new BusinessException("存档不存在");
        }
        String gameStateJson = save.getGameState();
        if (gameStateJson == null || gameStateJson.isBlank()) {
            return createDefaultState();
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
            GameSave update = new GameSave();
            update.setId(saveId);
            update.setGameState(json);
            update.setUpdatedAt(LocalDateTime.now());
            gameSaveMapper.updateById(update);
        } catch (JsonProcessingException e) {
            throw new BusinessException("存档数据保存失败");
        }
    }

    private GameState createDefaultState() {
        GameState state = new GameState();
        state.setFunds(new BigDecimal("2000000"));
        state.setReputation(0);
        state.setStudentCount(0);
        state.setTeacherCount(0);
        return state;
    }
}
