package com.yucairoad.enums;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public enum CampusLevel {

    SMALL("小型", 5, new BigDecimal("1000000")),
    MEDIUM("中型", 10, new BigDecimal("3000000")),
    LARGE("大型", 15, new BigDecimal("8000000")),
    EXTRA_LARGE("超大", 20, null);

    private final String displayName;
    private final int maxBuildingCount;
    private final BigDecimal expandCost;

    CampusLevel(String displayName, int maxBuildingCount, BigDecimal expandCost) {
        this.displayName = displayName;
        this.maxBuildingCount = maxBuildingCount;
        this.expandCost = expandCost;
    }

    public CampusLevel nextLevel() {
        if (this == EXTRA_LARGE) {
            throw new IllegalStateException("已达到最大校园等级");
        }
        return values()[this.ordinal() + 1];
    }

    public boolean canExpand() {
        return this != EXTRA_LARGE;
    }
}
