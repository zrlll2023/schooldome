package com.yucairoad.enums;

import lombok.Getter;

@Getter
public enum BranchUpgradeLevel {

    NEW_BRANCH("新分校", 0, 0),
    STABLE("稳定期", 3, 5),
    MATURE("成熟期", 5, 20),
    MODEL_SCHOOL("示范校", 10, 15);

    private final String displayName;
    private final int requiredYears;
    private final int reputationBonusPerYear;

    BranchUpgradeLevel(String displayName, int requiredYears, int reputationBonusPerYear) {
        this.displayName = displayName;
        this.requiredYears = requiredYears;
        this.reputationBonusPerYear = reputationBonusPerYear;
    }

    public static BranchUpgradeLevel fromYears(int operatingYears) {
        if (operatingYears >= MODEL_SCHOOL.requiredYears) return MODEL_SCHOOL;
        if (operatingYears >= MATURE.requiredYears) return MATURE;
        if (operatingYears >= STABLE.requiredYears) return STABLE;
        return NEW_BRANCH;
    }

    public boolean canOpenSubBranches() {
        return this == MODEL_SCHOOL;
    }
}
