package com.yucairoad.enums;

import lombok.Getter;

import java.math.BigDecimal;
import java.util.Arrays;

@Getter
public enum BuildingType {

    TEACHING_BUILDING("教学楼", 5, 200, new BigDecimal("100000"), new BigDecimal("5000")),
    DORMITORY("宿舍楼", 5, 100, new BigDecimal("80000"), new BigDecimal("3000")),
    LIBRARY("图书馆", 5, 0, new BigDecimal("150000"), new BigDecimal("4000")),
    GYMNASIUM("体育馆", 3, 0, new BigDecimal("200000"), new BigDecimal("6000")),
    LAB_BUILDING("实验楼", 3, 0, new BigDecimal("250000"), new BigDecimal("8000"));

    private final String displayName;
    private final int maxLevel;
    private final int capacityPerLevel;
    private final BigDecimal baseBuildCost;
    private final BigDecimal monthlyCostPerLevel;

    BuildingType(String displayName, int maxLevel, int capacityPerLevel,
                 BigDecimal baseBuildCost, BigDecimal monthlyCostPerLevel) {
        this.displayName = displayName;
        this.maxLevel = maxLevel;
        this.capacityPerLevel = capacityPerLevel;
        this.baseBuildCost = baseBuildCost;
        this.monthlyCostPerLevel = monthlyCostPerLevel;
    }

    public static BuildingType fromName(String name) {
        return Arrays.stream(BuildingType.values())
                .filter(t -> t.name().equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("无效的建筑类型: " + name));
    }

    public boolean isMultipleAllowed() {
        return this == TEACHING_BUILDING;
    }
}
