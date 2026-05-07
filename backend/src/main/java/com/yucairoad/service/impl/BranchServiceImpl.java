package com.yucairoad.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yucairoad.common.BusinessException;
import com.yucairoad.dto.*;
import com.yucairoad.entity.BranchSchool;
import com.yucairoad.entity.GameSave;
import com.yucairoad.enums.*;
import com.yucairoad.mapper.BranchSchoolMapper;
import com.yucairoad.mapper.GameSaveMapper;
import com.yucairoad.service.BranchService;
import com.yucairoad.service.GameSaveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BranchServiceImpl implements BranchService {

    private static final int MIN_REPUTATION = 8000;
    private static final long MIN_FUNDS = 5000000L;
    private static final int MAX_BRANCHES = 20;
    private static final int CONSTRUCTION_MONTHS = 12;
    private static final double REMITTANCE_RATE = 0.2;
    private static final BigDecimal TUITION_PER_STUDENT = new BigDecimal("6000");
    private static final BigDecimal TEACHER_SALARY_PER_STUDENT = new BigDecimal("3000");
    private static final BigDecimal MAINTENANCE_BASE = new BigDecimal("50000");
    private static final long COUNTY_OPEN_COST = 3000000L;

    private final BranchSchoolMapper branchSchoolMapper;
    private final GameSaveMapper gameSaveMapper;
    private final GameSaveService gameSaveService;
    private final ObjectMapper objectMapper;

    public BranchServiceImpl(BranchSchoolMapper branchSchoolMapper,
                             GameSaveMapper gameSaveMapper,
                             GameSaveService gameSaveService) {
        this.branchSchoolMapper = branchSchoolMapper;
        this.gameSaveMapper = gameSaveMapper;
        this.gameSaveService = gameSaveService;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public List<BranchDTO> getBranchList(Long saveId) {
        validateSaveExists(saveId);
        LambdaQueryWrapper<BranchSchool> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BranchSchool::getSaveId, saveId)
               .ne(BranchSchool::getStatus, "已关闭")
               .orderByDesc(BranchSchool::getCreatedAt);
        List<BranchSchool> branches = branchSchoolMapper.selectList(wrapper);
        return branches.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public BranchDetailDTO getBranchDetail(Long saveId, Long branchId) {
        validateSaveExists(saveId);
        BranchSchool branch = getBranchByIdAndValidate(branchId, saveId);

        CityType cityType = CityType.fromName(branch.getCityType());
        BranchMode mode = BranchMode.fromName(branch.getManagementMode());
        BranchUpgradeLevel upgradeLevel = BranchUpgradeLevel.fromYears(
                branch.getOperatingYears() != null ? branch.getOperatingYears() : 0);

        BranchDetailDTO detail = new BranchDetailDTO();
        detail.setId(branch.getId());
        detail.setName(branch.getName());
        detail.setCity(branch.getCity());
        detail.setCityType(branch.getCityType());
        detail.setCityTypeDisplay(cityType.getDisplayName());
        detail.setManagementMode(branch.getManagementMode());
        detail.setManagementModeDisplay(mode.getDisplayName());
        detail.setPrincipalAbility(branch.getPrincipalAbility());
        detail.setStudentCount(branch.getStudentCount());
        detail.setAnnualProfit(branch.getAnnualProfit());
        detail.setMonthlyIncome(branch.getMonthlyIncome());
        detail.setMonthlyExpense(branch.getMonthlyExpense());
        detail.setQualityRating(branch.getQualityRating());
        detail.setReputation(branch.getReputation());
        detail.setOperatingYears(branch.getOperatingYears());
        detail.setEstablishedYear(branch.getEstablishedYear());
        detail.setStatus(branch.getStatus());
        detail.setUpgradeLevel(upgradeLevel.getDisplayName());
        detail.setStudentQualityBonus(cityType.getStudentQualityBonus());
        detail.setCompetitionFactor(cityType.getCompetitionFactor());
        detail.setPolicySupport(cityType.isPolicySupport());
        detail.setCreatedAt(branch.getCreatedAt());
        detail.setUpdatedAt(branch.getUpdatedAt());

        int repContribution = calculateReputationContribution(branch, cityType, upgradeLevel);
        detail.setReputationContribution(repContribution);

        detail.setMonthlyHistory(new ArrayList<>());

        return detail;
    }

    @Override
    @Transactional
    public BranchDTO openBranch(Long saveId, String cityTypeStr, String name) {
        GameState state = loadGameState(saveId);
        CityType cityType = CityType.fromName(cityTypeStr);

        validateOpenConditions(state, saveId, cityType);

        long openCost = cityType.getOpenCost();
        deductFunds(saveId, state, BigDecimal.valueOf(openCost));

        String cityName = cityType.getRandomCity();
        String branchName = (name != null && !name.isBlank()) ? name : cityName + "育才中学";

        int principalAbility = 60 + (int) (Math.random() * 31);

        BranchSchool branch = new BranchSchool();
        branch.setSaveId(saveId);
        branch.setName(branchName);
        branch.setCity(cityName);
        branch.setCityType(cityTypeStr);
        branch.setManagementMode(BranchMode.DIRECT.name());
        branch.setPrincipalAbility(principalAbility);
        branch.setStatus(BranchStatus.CONSTRUCTING.getDisplayName());
        branch.setEstablishedYear(state.getCurrentYear());
        branch.setConstructionProgress(0);
        branch.setStudentCount(0);
        branch.setQualityRating(0);
        branch.setReputation(0);
        branch.setOperatingYears(0);
        branch.setAnnualProfit(BigDecimal.ZERO);
        branch.setMonthlyIncome(BigDecimal.ZERO);
        branch.setMonthlyExpense(BigDecimal.ZERO);
        branch.setTotalRemittance(BigDecimal.ZERO);
        branch.setCreatedAt(LocalDateTime.now());
        branch.setUpdatedAt(LocalDateTime.now());

        branchSchoolMapper.insert(branch);

        log.info("成功开设分校: {}, 城市: {}, 类型: {}", branchName, cityName, cityTypeStr);

        return convertToDTO(branch);
    }

    @Override
    @Transactional
    public BranchDTO changeManagementMode(Long saveId, Long branchId, String modeStr) {
        validateSaveExists(saveId);
        BranchSchool branch = getBranchByIdAndValidate(branchId, saveId);

        if (!"运营中".equals(branch.getStatus())) {
            throw new BusinessException("只有运营中的分校才能更换管理模式");
        }

        BranchMode currentMode = BranchMode.fromName(branch.getManagementMode());
        BranchMode targetMode = BranchMode.fromName(modeStr);

        int operatingYears = branch.getOperatingYears() != null ? branch.getOperatingYears() : 0;

        if (!currentMode.canSwitchTo(targetMode, operatingYears)) {
            throw new BusinessException("无法从" + currentMode.getDisplayName() + "切换到" +
                    targetMode.getDisplayName() + ",需要运营满" + currentMode.getMinYearsForSwitch() + "年");
        }

        if (targetMode == BranchMode.LICENSE) {
            GameState state = loadGameState(saveId);
            BigDecimal licenseFee = new BigDecimal("500000");
            if (state.getFunds().compareTo(licenseFee) < 0) {
                throw new BusinessException("资金不足，品牌授权需要支付50万加盟费");
            }
            deductFunds(saveId, state, licenseFee);
        }

        branch.setManagementMode(modeStr);
        branch.setUpdatedAt(LocalDateTime.now());
        branchSchoolMapper.updateById(branch);

        log.info("分校{}管理模式变更为: {}", branch.getName(), modeStr);

        return convertToDTO(branch);
    }

    @Override
    @Transactional
    public BranchDTO closeBranch(Long saveId, Long branchId) {
        validateSaveExists(saveId);
        BranchSchool branch = getBranchByIdAndValidate(branchId, saveId);

        if ("已关闭".equals(branch.getStatus())) {
            throw new BusinessException("该分校已经关闭");
        }

        BigDecimal refund = calculateRefund(branch);

        if (refund.compareTo(BigDecimal.ZERO) > 0) {
            GameState state = loadGameState(saveId);
            addFunds(saveId, state, refund);
        }

        branch.setStatus(BranchStatus.CLOSED.getDisplayName());
        branch.setUpdatedAt(LocalDateTime.now());
        branchSchoolMapper.updateById(branch);

        log.info("分校{}已关闭,返还资金: {}万", branch.getName(),
                refund.divide(new BigDecimal("10000"), 2, RoundingMode.HALF_UP));

        return convertToDTO(branch);
    }

    @Override
    public List<CityTypeDTO> getAvailableCityTypes() {
        List<CityTypeDTO> result = new ArrayList<>();
        for (CityType type : CityType.values()) {
            CityTypeDTO dto = new CityTypeDTO();
            dto.setType(type.name());
            dto.setDisplayName(type.getDisplayName());
            dto.setStudentQualityBonus(type.getStudentQualityBonus());
            dto.setCompetitionFactor(type.getCompetitionFactor());
            dto.setCostMultiplier(type.getCostMultiplier());
            dto.setReputationBase(type.getReputationBase());
            dto.setPolicySupport(type.isPolicySupport());
            dto.setOpenCost(type.getOpenCost());
            dto.setAvailableCities(type.getAvailableCities());
            dto.setDescription(buildDescription(type));
            result.add(dto);
        }
        return result;
    }

    @Override
    @Transactional
    public void updateAllBranches(Long saveId) {
        GameState state = loadGameState(saveId);
        LambdaQueryWrapper<BranchSchool> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BranchSchool::getSaveId, saveId)
               .eq(BranchSchool::getStatus, "运营中");

        List<BranchSchool> activeBranches = branchSchoolMapper.selectList(wrapper);

        for (BranchSchool branch : activeBranches) {
            updateSingleBranch(state, branch);
        }

        persistGameState(saveId, state);
    }

    private void updateSingleBranch(GameState state, BranchSchool branch) {
        CityType cityType = CityType.fromName(branch.getCityType());
        BranchMode mode = BranchMode.fromName(branch.getManagementMode());
        int principalAbility = branch.getPrincipalAbility() != null ? branch.getPrincipalAbility() : 70;

        double quality = calculateBranchQuality(state, principalAbility, cityType, mode);
        int students = calculateBranchStudents(branch, quality, cityType);

        BigDecimal tuitionPerStudentAdjusted = TUITION_PER_STUDENT.multiply(
                BigDecimal.valueOf(cityType.getCompetitionFactor()));

        BigDecimal income = tuitionPerStudentAdjusted.multiply(BigDecimal.valueOf(students));
        BigDecimal teacherCost = TEACHER_SALARY_PER_STUDENT.multiply(BigDecimal.valueOf(students));
        BigDecimal maintenance = MAINTENANCE_BASE.multiply(BigDecimal.valueOf(cityType.getCostMultiplier()));
        BigDecimal expense = teacherCost.add(maintenance);
        BigDecimal profit = income.subtract(expense);

        BigDecimal remittance = profit.multiply(BigDecimal.valueOf(REMITTANCE_RATE));
        BigDecimal currentFunds = state.getFunds();
        state.setFunds(currentFunds.add(remittance));

        BigDecimal totalRemittance = branch.getTotalRemittance() != null ?
                branch.getTotalRemittance() : BigDecimal.ZERO;
        totalRemittance = totalRemittance.add(remittance);

        int qualityRating = (int) Math.round(quality);
        int repChange = Math.max(-5, Math.min(30, qualityRating / 10));
        Integer currentRep = state.getReputation() != null ? state.getReputation() : 0;
        state.setReputation(currentRep + repChange);

        int operatingYears = branch.getOperatingYears() != null ? branch.getOperatingYears() : 0;
        BranchUpgradeLevel upgradeLevel = BranchUpgradeLevel.fromYears(operatingYears);
        int repContribution = calculateReputationContribution(branch, cityType, upgradeLevel);
        int newRep = branch.getReputation() != null ? branch.getReputation() : 0;
        newRep += repContribution + upgradeLevel.getReputationBonusPerYear();

        branch.setStudentCount(students);
        branch.setQualityRating(qualityRating);
        branch.setReputation(newRep);
        branch.setMonthlyIncome(income);
        branch.setMonthlyExpense(expense);
        branch.setAnnualProfit(profit.multiply(new BigDecimal("12")));
        branch.setTotalRemittance(totalRemittance);
        branch.setUpdatedAt(LocalDateTime.now());

        checkBranchUpgrade(branch);

        if (Math.random() < 0.1) {
            generateBranchEvent(branch);
        }

        branchSchoolMapper.updateById(branch);
    }

    private double calculateBranchQuality(GameState state, int principalAbility,
                                          CityType cityType, BranchMode mode) {
        double schoolRep = state.getReputation() != null ? state.getReputation() : 0;
        double quality = schoolRep * 0.3 +
                principalAbility * 0.3 +
                (cityType.getReputationBase() * 10) * 0.2 +
                (50 + Math.random() * 40) * 0.2;

        double variance = mode.getQualityVariance();
        quality = quality * (1 - variance / 2 + Math.random() * variance);

        GroupLevel groupLevel = GroupLevel.fromBranchCount(countActiveBranches(branch.getSaveId()));
        if (groupLevel == GroupLevel.EDUCATION_GROUP || groupLevel == GroupLevel.EDUCATION_EMPIRE) {
            quality *= 1.1;
        }

        return Math.max(0, Math.min(100, quality));
    }

    private int calculateBranchStudents(BranchSchool branch, double quality, CityType cityType) {
        int baseStudents = 200 + (int) (quality * 8);
        baseStudents = (int) (baseStudents * (1 + cityType.getStudentQualityBonus()));

        BranchUpgradeLevel upgradeLevel = BranchUpgradeLevel.fromYears(
                branch.getOperatingYears() != null ? branch.getOperatingYears() : 0);
        if (upgradeLevel == BranchUpgradeLevel.MATURE || upgradeLevel == BranchUpgradeLevel.MODEL_SCHOOL) {
            baseStudents = (int) (baseStudents * 1.2);
        }

        return Math.max(50, Math.min(2000, baseStudents));
    }

    private void checkBranchUpgrade(BranchSchool branch) {
        int operatingYears = branch.getOperatingYears() != null ? branch.getOperatingYears() : 0;
        BranchUpgradeLevel currentLevel = BranchUpgradeLevel.fromYears(operatingYears);

        if ("建设中".equals(branch.getStatus()) && branch.getConstructionProgress() != null) {
            int progress = branch.getConstructionProgress() + 1;
            branch.setConstructionProgress(progress);
            if (progress >= CONSTRUCTION_MONTHS) {
                branch.setStatus("运营中");
                branch.setOperatingYears(0);
                log.info("分校{}建设完成，开始运营", branch.getName());
            }
        } else if ("运营中".equals(branch.getStatus())) {
            branch.setOperatingYears(operatingYears + 1);
        }
    }

    private void generateBranchEvent(BranchSchool branch) {
        log.info("分校{}触发随机事件", branch.getName());
    }

    private int calculateReputationContribution(BranchSchool branch, CityType cityType,
                                               BranchUpgradeLevel upgradeLevel) {
        int base = cityType.getReputationBase();
        if (upgradeLevel == BranchUpgradeLevel.STABLE) {
            base += 5;
        } else if (upgradeLevel == BranchUpgradeLevel.MATURE) {
            base += 10;
        } else if (upgradeLevel == BranchUpgradeLevel.MODEL_SCHOOL) {
            base += 15;
        }
        return base;
    }

    private void validateOpenConditions(GameState state, Long saveId, CityType cityType) {
        Integer reputation = state.getReputation() != null ? state.getReputation() : 0;
        if (reputation < MIN_REPUTATION) {
            throw new BusinessException("声望不足，开设分校需要声望达到" + MIN_REPUTATION +
                    "(全国百强)，当前声望:" + reputation);
        }

        BigDecimal funds = state.getFunds() != null ? state.getFunds() : BigDecimal.ZERO;
        long requiredFunds = cityType.getOpenCost();
        if (funds.compareTo(BigDecimal.valueOf(requiredFunds)) < 0) {
            throw new BusinessException("资金不足，开设分校需要" +
                    (requiredFunds / 10000) + "万，当前资金:" +
                    funds.divide(new BigDecimal("10000"), 2, RoundingMode.HALF_UP) + "万");
        }

        int currentCount = countActiveBranches(saveId);
        if (currentCount >= MAX_BRANCHES) {
            throw new BusinessException("分校数量已达上限(" + MAX_BRANCHES + "所)");
        }
    }

    private int countActiveBranches(Long saveId) {
        LambdaQueryWrapper<BranchSchool> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BranchSchool::getSaveId, saveId)
               .ne(BranchSchool::getStatus, "已关闭");
        return Math.toIntExact(branchSchoolMapper.selectCount(wrapper));
    }

    private BigDecimal calculateRefund(BranchSchool branch) {
        BigDecimal refundRate = new BigDecimal("0.30");
        if (branch.getTotalRemittance() != null) {
            refundRate = refundRate.add(new BigDecimal("0.10"));
        }
        return branch.getAnnualProfit() != null ?
                branch.getAnnualProfit().multiply(refundRate) : BigDecimal.ZERO;
    }

    private BranchDTO convertToDTO(BranchSchool branch) {
        BranchDTO dto = new BranchDTO();
        dto.setId(branch.getId());
        dto.setName(branch.getName());
        dto.setCityType(branch.getCityType());
        try {
            CityType cityType = CityType.fromName(branch.getCityType());
            dto.setCityTypeDisplay(cityType.getDisplayName());
        } catch (Exception e) {
            dto.setCityTypeDisplay(branch.getCityType());
        }
        dto.setManagementMode(branch.getManagementMode());
        try {
            BranchMode mode = BranchMode.fromName(branch.getManagementMode());
            dto.setManagementModeDisplay(mode.getDisplayName());
        } catch (Exception e) {
            dto.setManagementModeDisplay(branch.getManagementMode());
        }
        dto.setStudentCount(branch.getStudentCount());
        dto.setAnnualProfit(branch.getAnnualProfit());
        dto.setQualityRating(branch.getQualityRating());
        dto.setOperatingYears(branch.getOperatingYears());
        dto.setStatus(branch.getStatus());

        int operatingYears = branch.getOperatingYears() != null ? branch.getOperatingYears() : 0;
        BranchUpgradeLevel upgradeLevel = BranchUpgradeLevel.fromYears(operatingYears);
        dto.setUpgradeLevel(upgradeLevel.getDisplayName());

        try {
            CityType cityType = CityType.fromName(branch.getCityType());
            BranchUpgradeLevel level = BranchUpgradeLevel.fromYears(operatingYears);
            dto.setReputationContribution(calculateReputationContribution(branch, cityType, level));
        } catch (Exception e) {
            dto.setReputationContribution(0);
        }

        dto.setCreatedAt(branch.getCreatedAt());
        return dto;
    }

    private String buildDescription(CityType type) {
        StringBuilder desc = new StringBuilder();
        desc.append("生源质量").append(type.getStudentQualityBonus() > 0 ? "+" : "")
                .append((int)(type.getStudentQualityBonus() * 100)).append("%, ");
        desc.append("竞争系数×").append(type.getCompetitionFactor()).append(", ");
        desc.append("成本倍率×").append(type.getCostMultiplier()).append(", ");
        desc.append("基础声望+").append(type.getReputationBase()).append("/年");
        if (type.isPolicySupport()) {
            desc.append(", 政策扶持(+50%拨款)");
        }
        return desc.toString();
    }

    private BranchSchool getBranchByIdAndValidate(Long branchId, Long saveId) {
        BranchSchool branch = branchSchoolMapper.selectById(branchId);
        if (branch == null) {
            throw new BusinessException("分校不存在");
        }
        if (!branch.getSaveId().equals(saveId)) {
            throw new BusinessException("无权操作此分校");
        }
        return branch;
    }

    private void validateSaveExists(Long saveId) {
        GameSave save = gameSaveMapper.selectById(saveId);
        if (save == null) {
            throw new BusinessException("存档不存在");
        }
    }

    private GameState loadGameState(Long saveId) {
        GameSave save = gameSaveMapper.selectById(saveId);
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

    private void deductFunds(Long saveId, GameState state, BigDecimal amount) {
        BigDecimal currentFunds = state.getFunds() != null ? state.getFunds() : BigDecimal.ZERO;
        if (currentFunds.compareTo(amount) < 0) {
            throw new BusinessException("资金不足");
        }
        state.setFunds(currentFunds.subtract(amount));
        persistGameState(saveId, state);
    }

    private void addFunds(Long saveId, GameState state, BigDecimal amount) {
        BigDecimal currentFunds = state.getFunds() != null ? state.getFunds() : BigDecimal.ZERO;
        state.setFunds(currentFunds.add(amount));
        persistGameState(saveId, state);
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
}
