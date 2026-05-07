package com.yucairoad.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class GroupStatusDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String groupLevel;
    private String groupLevelDisplay;
    private int totalBranches;
    private int activeBranches;
    private BigDecimal groupReputation;
    private BigDecimal annualRemittance;
    private List<String> benefits;
    private NextLevelInfo nextLevel;
    private Map<String, Long> branchDistribution;

    @Data
    public static class NextLevelInfo implements Serializable {
        private static final long serialVersionUID = 1L;
        private String level;
        private String levelDisplay;
        private int requiredBranches;
        private int currentBranches;
        private boolean canUpgrade;
        private int branchesNeeded;
    }
}
