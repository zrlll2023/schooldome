package com.yucairoad.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yucairoad.common.BusinessException;
import com.yucairoad.dto.BuildingDTO;
import com.yucairoad.dto.CampusStatsDTO;
import com.yucairoad.dto.GameState;
import com.yucairoad.entity.Building;
import com.yucairoad.entity.GameSave;
import com.yucairoad.enums.BuildingType;
import com.yucairoad.enums.CampusLevel;
import com.yucairoad.mapper.BuildingMapper;
import com.yucairoad.mapper.GameSaveMapper;
import com.yucairoad.service.CampusService;
import com.yucairoad.service.GameSaveService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class CampusServiceImpl implements CampusService {

    private static final String STATUS_CONSTRUCTING = "建设中";
    private static final String STATUS_OPERATING = "运营中";
    private static final String STATUS_DEMOLISHED = "已拆除";
    private static final BigDecimal REFUND_RATE = new BigDecimal("0.30");
    private static final BigDecimal UPGRADE_COST_MULTIPLIER = new BigDecimal("1.5");

    private final BuildingMapper buildingMapper;
    private final GameSaveMapper gameSaveMapper;
    private final GameSaveService gameSaveService;
    private final ObjectMapper objectMapper;

    public CampusServiceImpl(BuildingMapper buildingMapper,
                             GameSaveMapper gameSaveMapper,
                             GameSaveService gameSaveService) {
        this.buildingMapper = buildingMapper;
        this.gameSaveMapper = gameSaveMapper;
        this.gameSaveService = gameSaveService;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public List<BuildingDTO> getBuildings(Long saveId) {
        validateSaveExists(saveId);
        LambdaQueryWrapper<Building> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Building::getSchoolId, saveId)
               .ne(Building::getStatus, STATUS_DEMOLISHED)
               .orderByAsc(Building::getCreatedAt);
        List<Building> buildings = buildingMapper.selectList(wrapper);
        return convertToDTOList(buildings, saveId);
    }

    @Override
    @Transactional
    public BuildingDTO createBuilding(Long saveId, String buildingType, Integer level) {
        GameState state = loadGameState(saveId);
        BuildingType type = BuildingType.fromName(buildingType);

        if (level < 1 || level > type.getMaxLevel()) {
            throw new BusinessException("建筑等级必须在1~" + type.getMaxLevel() + "之间");
        }

        BigDecimal buildCost = type.getBaseBuildCost().multiply(BigDecimal.valueOf(level));
        BigDecimal funds = state.getFunds();
        if (funds.compareTo(buildCost) < 0) {
            throw new BusinessException("资金不足，建造需要" + buildCost.divide(new BigDecimal("10000"), 2, RoundingMode.HALF_UP) + "万");
        }

        CampusLevel campusLevel = getCurrentCampusLevel(saveId);
        int currentBuildingCount = countActiveBuildings(saveId);
        if (currentBuildingCount >= campusLevel.getMaxBuildingCount()) {
            throw new BusinessException("校园建筑数量已达上限(" + campusLevel.getMaxBuildingCount() + "个)，请先扩建校园");
        }

        if (!type.isMultipleAllowed()) {
            long sameTypeCount = countBuildingsByType(saveId, buildingType);
            if (sameTypeCount > 0) {
                throw new BusinessException(type.getDisplayName() + "已存在，同类型建筑最多只能有1个");
            }
        }

        deductFunds(saveId, state, buildCost);

        Building building = new Building();
        building.setSchoolId(saveId);
        building.setType(buildingType);
        building.setLevel(level);
        building.setCapacity(type.getCapacityPerLevel() * level);
        building.setMonthlyCost(type.getMonthlyCostPerLevel().multiply(BigDecimal.valueOf(level)));
        building.setBuildCost(buildCost);
        building.setStatus(STATUS_CONSTRUCTING);
        building.setCreatedAt(LocalDateTime.now());
        building.setUpdatedAt(LocalDateTime.now());
        buildingMapper.insert(building);

        return convertToDTO(building, saveId);
    }

    @Override
    @Transactional
    public BuildingDTO upgradeBuilding(Long saveId, Long buildingId) {
        GameState state = loadGameState(saveId);
        Building building = getActiveBuilding(saveId, buildingId);

        if (!STATUS_OPERATING.equals(building.getStatus())) {
            throw new BusinessException("只有运营中的建筑才能升级");
        }

        BuildingType type = BuildingType.fromName(building.getType());
        int currentLevel = building.getLevel();
        if (currentLevel >= type.getMaxLevel()) {
            throw new BusinessException("建筑已达最高等级" + type.getMaxLevel());
        }

        BigDecimal upgradeCost = type.getBaseBuildCost()
                .multiply(BigDecimal.valueOf(currentLevel))
                .multiply(UPGRADE_COST_MULTIPLIER)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal funds = state.getFunds();
        if (funds.compareTo(upgradeCost) < 0) {
            throw new BusinessException("资金不足，升级需要" + upgradeCost.divide(new BigDecimal("10000"), 2, RoundingMode.HALF_UP) + "万");
        }

        deductFunds(saveId, state, upgradeCost);

        int newLevel = currentLevel + 1;
        building.setLevel(newLevel);
        building.setCapacity(type.getCapacityPerLevel() * newLevel);
        building.setMonthlyCost(type.getMonthlyCostPerLevel().multiply(BigDecimal.valueOf(newLevel)));
        building.setStatus(STATUS_CONSTRUCTING);
        building.setUpdatedAt(LocalDateTime.now());
        buildingMapper.updateById(building);

        return convertToDTO(building, saveId);
    }

    @Override
    @Transactional
    public BuildingDTO demolishBuilding(Long saveId, Long buildingId) {
        GameState state = loadGameState(saveId);
        Building building = getActiveBuilding(saveId, buildingId);

        if (!STATUS_OPERATING.equals(building.getStatus())) {
            throw new BusinessException("只有运营中的建筑才能拆除");
        }

        BuildingType type = BuildingType.fromName(building.getType());
        if (type == BuildingType.TEACHING_BUILDING || type == BuildingType.DORMITORY) {
            int remainingCapacity = calculateRemainingCapacity(saveId, buildingId);
            int studentCount = state.getStudentCount() != null ? state.getStudentCount() : 0;
            if (studentCount > remainingCapacity) {
                throw new BusinessException("拆除后学生容量不足，当前学生" + studentCount + "人，剩余容量" + remainingCapacity + "人");
            }
        }

        BigDecimal refund = building.getBuildCost().multiply(REFUND_RATE).setScale(2, RoundingMode.HALF_UP);
        addFunds(saveId, state, refund);

        building.setStatus(STATUS_DEMOLISHED);
        building.setUpdatedAt(LocalDateTime.now());
        buildingMapper.updateById(building);

        return convertToDTO(building, saveId);
    }

    @Override
    @Transactional
    public CampusStatsDTO expandCampus(Long saveId) {
        GameState state = loadGameState(saveId);
        CampusLevel currentLevel = getCurrentCampusLevel(saveId);

        if (!currentLevel.canExpand()) {
            throw new BusinessException("已达到最大校园等级，无法继续扩建");
        }

        CampusLevel nextLevel = currentLevel.nextLevel();
        BigDecimal expandCost = nextLevel.getExpandCost();

        BigDecimal funds = state.getFunds();
        if (funds.compareTo(expandCost) < 0) {
            throw new BusinessException("资金不足，扩建需要" + expandCost.divide(new BigDecimal("10000"), 2, RoundingMode.HALF_UP) + "万");
        }

        deductFunds(saveId, state, expandCost);

        updateCampusLevelInGameState(saveId, nextLevel);

        return getCampusStats(saveId);
    }

    @Override
    public CampusStatsDTO getCampusStats(Long saveId) {
        validateSaveExists(saveId);
        CampusLevel campusLevel = getCurrentCampusLevel(saveId);
        List<Building> activeBuildings = getActiveBuildings(saveId);

        CampusStatsDTO stats = new CampusStatsDTO();
        stats.setCampusLevel(campusLevel.getDisplayName());
        stats.setCurrentBuildingCount(activeBuildings.size());
        stats.setMaxBuildingCount(campusLevel.getMaxBuildingCount());
        stats.setCanExpand(campusLevel.canExpand());
        if (campusLevel.canExpand()) {
            stats.setExpandCost(campusLevel.nextLevel().getExpandCost());
        } else {
            stats.setExpandCost(BigDecimal.ZERO);
        }

        int totalCapacity = 0;
        double academicBonus = 0;
        double physicalBonus = 0;
        double scienceBonus = 0;
        BigDecimal totalMaintenance = BigDecimal.ZERO;

        for (Building building : activeBuildings) {
            if (STATUS_OPERATING.equals(building.getStatus())) {
                totalCapacity += building.getCapacity() != null ? building.getCapacity() : 0;
                totalMaintenance = totalMaintenance.add(
                        building.getMonthlyCost() != null ? building.getMonthlyCost() : BigDecimal.ZERO
                );

                BuildingType type = BuildingType.fromName(building.getType());
                int level = building.getLevel() != null ? building.getLevel() : 1;
                switch (type) {
                    case LIBRARY:
                        academicBonus += level * 2.0;
                        break;
                    case GYMNASIUM:
                        physicalBonus += level * 3.0;
                        break;
                    case LAB_BUILDING:
                        scienceBonus += level * 5.0;
                        break;
                    default:
                        break;
                }
            }
        }

        stats.setTotalStudentCapacity(totalCapacity);
        stats.setAcademicBonusPercent(academicBonus);
        stats.setPhysicalBonusPercent(physicalBonus);
        stats.setScienceBonusPercent(scienceBonus);
        stats.setTotalMonthlyMaintenance(totalMaintenance);

        return stats;
    }

    @Override
    @Transactional
    public void checkBuildingCompletion(Long saveId) {
        GameState state = loadGameState(saveId);
        int currentMonth = state.getCurrentMonth() != null ? state.getCurrentMonth() : 1;

        LambdaQueryWrapper<Building> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Building::getSchoolId, saveId)
               .eq(Building::getStatus, STATUS_CONSTRUCTING);
        List<Building> constructingBuildings = buildingMapper.selectList(wrapper);

        for (Building building : constructingBuildings) {
            LocalDateTime completeTime = getCompleteTime(building);
            if (completeTime == null || !completeTime.isAfter(LocalDateTime.now())) {
                building.setStatus(STATUS_OPERATING);
                building.setUpdatedAt(LocalDateTime.now());
                buildingMapper.updateById(building);
            }
        }
    }

    private GameState loadGameState(Long saveId) {
        GameSave save = gameSaveMapper.selectById(saveId);
        if (save == null) {
            throw new BusinessException("存档不存在");
        }
        String gameStateJson = save.getGameState();
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

    private void validateSaveExists(Long saveId) {
        GameSave save = gameSaveMapper.selectById(saveId);
        if (save == null) {
            throw new BusinessException("存档不存在");
        }
    }

    private Building getActiveBuilding(Long saveId, Long buildingId) {
        Building building = buildingMapper.selectById(buildingId);
        if (building == null) {
            throw new BusinessException("建筑不存在");
        }
        if (!building.getSchoolId().equals(saveId)) {
            throw new BusinessException("该建筑不属于此学校");
        }
        if (STATUS_DEMOLISHED.equals(building.getStatus())) {
            throw new BusinessException("该建筑已被拆除");
        }
        return building;
    }

    private int countActiveBuildings(Long saveId) {
        LambdaQueryWrapper<Building> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Building::getSchoolId, saveId)
               .ne(Building::getStatus, STATUS_DEMOLISHED);
        return Math.toIntExact(buildingMapper.selectCount(wrapper));
    }

    private long countBuildingsByType(Long saveId, String buildingType) {
        LambdaQueryWrapper<Building> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Building::getSchoolId, saveId)
               .eq(Building::getType, buildingType)
               .ne(Building::getStatus, STATUS_DEMOLISHED);
        return buildingMapper.selectCount(wrapper);
    }

    private List<Building> getActiveBuildings(Long saveId) {
        LambdaQueryWrapper<Building> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Building::getSchoolId, saveId)
               .ne(Building::getStatus, STATUS_DEMOLISHED);
        return buildingMapper.selectList(wrapper);
    }

    private CampusLevel getCurrentCampusLevel(Long saveId) {
        GameState state = loadGameState(saveId);
        String campusLevelStr = "SMALL";
        if (state.getSchool() != null && state.getSchool().getLevel() != null) {
            String level = state.getSchool().getLevel();
            for (CampusLevel cl : CampusLevel.values()) {
                if (cl.getDisplayName().equals(level)) {
                    return cl;
                }
            }
        }
        try {
            return CampusLevel.valueOf(campusLevelStr);
        } catch (IllegalArgumentException e) {
            return CampusLevel.SMALL;
        }
    }

    private void updateCampusLevelInGameState(Long saveId, CampusLevel newLevel) {
        GameState state = loadGameState(saveId);
        if (state.getSchool() == null) {
            throw new BusinessException("学校信息不存在");
        }
        state.getSchool().setLevel(newLevel.getDisplayName());
        persistGameState(saveId, state);
    }

    private int calculateRemainingCapacity(Long saveId, Long excludeBuildingId) {
        List<Building> buildings = getActiveBuildings(saveId);
        int capacity = 0;
        for (Building b : buildings) {
            if (!b.getId().equals(excludeBuildingId) && STATUS_OPERATING.equals(b.getStatus())) {
                BuildingType type = BuildingType.fromName(b.getType());
                if (type == BuildingType.TEACHING_BUILDING || type == BuildingType.DORMITORY) {
                    capacity += b.getCapacity() != null ? b.getCapacity() : 0;
                }
            }
        }
        return capacity;
    }

    private void deductFunds(Long saveId, GameState state, BigDecimal amount) {
        BigDecimal currentFunds = state.getFunds() != null ? state.getFunds() : BigDecimal.ZERO;
        BigDecimal newFunds = currentFunds.subtract(amount);
        if (newFunds.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("资金不足");
        }
        state.setFunds(newFunds);
        persistGameState(saveId, state);
    }

    private void addFunds(Long saveId, GameState state, BigDecimal amount) {
        BigDecimal currentFunds = state.getFunds() != null ? state.getFunds() : BigDecimal.ZERO;
        state.setFunds(currentFunds.add(amount));
        persistGameState(saveId, state);
    }

    private LocalDateTime getCompleteTime(Building building) {
        if (building.getCreatedAt() == null) {
            return null;
        }
        return building.getCreatedAt().plusMonths(1);
    }

    private List<BuildingDTO> convertToDTOList(List<Building> buildings, Long saveId) {
        List<BuildingDTO> dtoList = new ArrayList<>();
        for (Building building : buildings) {
            dtoList.add(convertToDTO(building, saveId));
        }
        return dtoList;
    }

    private BuildingDTO convertToDTO(Building building, Long saveId) {
        BuildingDTO dto = new BuildingDTO();
        dto.setId(building.getId());
        dto.setType(building.getType());
        dto.setLevel(building.getLevel());
        dto.setCapacity(building.getCapacity());
        dto.setMonthlyCost(building.getMonthlyCost());
        dto.setBuildCost(building.getBuildCost());
        dto.setStatus(building.getStatus());
        dto.setCreatedAt(building.getCreatedAt());

        BuildingType type = BuildingType.fromName(building.getType());
        dto.setTypeName(type.getDisplayName());
        dto.setMaxLevel(type.getMaxLevel());

        if (STATUS_CONSTRUCTING.equals(building.getStatus())) {
            dto.setCompleteTime(getCompleteTime(building));
        }

        return dto;
    }
}
