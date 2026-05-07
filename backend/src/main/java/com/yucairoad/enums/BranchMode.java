package com.yucairoad.enums;

import lombok.Getter;

@Getter
public enum BranchMode {

    DIRECT("直营管理", 3, 0.05, "总校直接管理，质量可控"),
    DELEGATED("委托管理", 10, 0.10, "任命校长管理，定期汇报"),
    LICENSE("品牌授权", 15, 0.15, "输出品牌和课程");

    private final String displayName;
    private final int minYearsForSwitch;
    private final double qualityVariance;
    private final String description;

    BranchMode(String displayName, int minYearsForSwitch, double qualityVariance, String description) {
        this.displayName = displayName;
        this.minYearsForSwitch = minYearsForSwitch;
        this.qualityVariance = qualityVariance;
        this.description = description;
    }

    public static BranchMode fromName(String name) {
        for (BranchMode mode : values()) {
            if (mode.name().equals(name)) {
                return mode;
            }
        }
        throw new IllegalArgumentException("无效的管理模式: " + name);
    }

    public boolean canSwitchTo(BranchMode targetMode, int operatingYears) {
        if (this == targetMode) {
            return true;
        }
        if (targetMode == DIRECT && this != DIRECT) {
            return false;
        }
        int requiredYears = this.minYearsForSwitch;
        return operatingYears >= requiredYears;
    }
}
