package com.yucairoad.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yucairoad.common.BusinessException;
import com.yucairoad.dto.GroupStatusDTO;
import com.yucairoad.entity.BranchSchool;
import com.yucairoad.entity.GameSave;
import com.yucairoad.enums.CityType;
import com.yucairoad.enums.GroupLevel;
import com.yucairoad.mapper.BranchSchoolMapper;
import com.yucairoad.mapper.GameSaveMapper;
import com.yucairoad.service.GroupService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class GroupServiceImpl implements GroupService {

    private final BranchSchoolMapper branchSchoolMapper;
    private final GameSaveMapper gameSaveMapper;

    public GroupServiceImpl(BranchSchoolMapper branchSchoolMapper,
                            GameSaveMapper gameSaveMapper) {
        this.branchSchoolMapper = branchSchoolMapper;
        this.gameSaveMapper = gameSaveMapper;
    }

    @Override
    public GroupStatusDTO getGroupStatus(Long saveId) {
        validateSaveExists(saveId);

        LambdaQueryWrapper<BranchSchool> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BranchSchool::getSaveId, saveId)
               .ne(BranchSchool::getStatus, "已关闭");

        List<BranchSchool> allBranches = branchSchoolMapper.selectList(wrapper);

        long activeCount = allBranches.stream()
                .filter(b -> "运营中".equals(b.getStatus()))
                .count();

        GroupLevel currentLevel = GroupLevel.fromBranchCount((int) allBranches.size());

        BigDecimal groupReputation = calculateGroupReputation(allBranches);
        BigDecimal annualRemittance = calculateAnnualRemittance(allBranches);
        Map<String, Long> distribution = calculateDistribution(allBranches);

        GroupStatusDTO status = new GroupStatusDTO();
        status.setGroupLevel(currentLevel != null ? currentLevel.name() : "NONE");
        status.setGroupLevelDisplay(currentLevel != null ? currentLevel.getDisplayName() : "未成团");
        status.setTotalBranches(allBranches.size());
        status.setActiveBranches((int) activeCount);
        status.setGroupReputation(groupReputation);
        status.setAnnualRemittance(annualRemittance);
        status.setBenefits(currentLevel != null ? currentLevel.getBenefits() : List.of());
        status.setBranchDistribution(distribution);
        status.setNextLevel(buildNextLevelInfo(currentLevel, allBranches.size()));

        return status;
    }

    private BigDecimal calculateGroupReputation(List<BranchSchool> branches) {
        int totalRep = 0;
        for (BranchSchool branch : branches) {
            totalRep += branch.getReputation() != null ? branch.getReputation() : 0;
        }
        return BigDecimal.valueOf(totalRep);
    }

    private BigDecimal calculateAnnualRemittance(List<BranchSchool> branches) {
        BigDecimal total = BigDecimal.ZERO;
        for (BranchSchool branch : branches) {
            if (branch.getTotalRemittance() != null) {
                total = total.add(branch.getTotalRemittance());
            }
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    private Map<String, Long> calculateDistribution(List<BranchSchool> branches) {
        Map<String, Long> distribution = new HashMap<>();
        for (CityType type : CityType.values()) {
            long count = branches.stream()
                    .filter(b -> type.name().equals(b.getCityType()))
                    .count();
            if (count > 0) {
                distribution.put(type.getDisplayName(), count);
            }
        }
        return distribution;
    }

    private GroupStatusDTO.NextLevelInfo buildNextLevelInfo(GroupLevel currentLevel, int currentCount) {
        GroupStatusDTO.NextLevelInfo nextInfo = new GroupStatusDTO.NextLevelInfo();

        if (currentLevel == null) {
            GroupLevel firstLevel = GroupLevel.EDUCATION_ALLIANCE;
            nextInfo.setLevel(firstLevel.name());
            nextInfo.setLevelDisplay(firstLevel.getDisplayName());
            nextInfo.setRequiredBranches(firstLevel.getRequiredBranches());
            nextInfo.setCurrentBranches(currentCount);
            nextInfo.setCanUpgrade(false);
            nextInfo.setBranchesNeeded(firstLevel.getRequiredBranches() - currentCount);
        } else {
            GroupLevel nextLevel = currentLevel.getNextLevel();
            if (nextLevel != null) {
                nextInfo.setLevel(nextLevel.name());
                nextInfo.setLevelDisplay(nextLevel.getDisplayName());
                nextInfo.setRequiredBranches(nextLevel.getRequiredBranches());
                nextInfo.setCurrentBranches(currentCount);
                nextInfo.setCanUpgrade(currentCount >= nextLevel.getRequiredBranches());
                nextInfo.setBranchesNeeded(Math.max(0, nextLevel.getRequiredBranches() - currentCount));
            } else {
                nextInfo.setLevel("MAX");
                nextInfo.setLevelDisplay("已达最高等级");
                nextInfo.setRequiredBranches(currentCount);
                nextInfo.setCurrentBranches(currentCount);
                nextInfo.setCanUpgrade(false);
                nextInfo.setBranchesNeeded(0);
            }
        }

        return nextInfo;
    }

    private void validateSaveExists(Long saveId) {
        GameSave save = gameSaveMapper.selectById(saveId);
        if (save == null) {
            throw new BusinessException("存档不存在");
        }
    }
}
