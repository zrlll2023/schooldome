package com.yucairoad.enums;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum CityType {

    TIER1("一线城市", 0.2, 1.5, 2.0, 15, false,
            new String[]{"北京", "上海", "广州", "深圳"}),
    TIER2("二线城市", 0.1, 1.2, 1.3, 10, false,
            new String[]{"成都", "杭州", "武汉", "南京", "重庆", "西安", "苏州", "天津"}),
    TIER3("三线城市", 0.0, 1.0, 1.0, 5, false,
            new String[]{"长沙", "郑州", "济南", "青岛", "大连", "宁波", "厦门", "福州"}),
    COUNTY("县城", -0.1, 0.7, 0.6, 3, true,
            new String[]{"县城A", "县城B", "县城C", "县城D", "县城E", "县城F"});

    private final String displayName;
    private final double studentQualityBonus;
    private final double competitionFactor;
    private final double costMultiplier;
    private final int reputationBase;
    private final boolean policySupport;
    private final String[] availableCities;

    CityType(String displayName, double studentQualityBonus, double competitionFactor,
             double costMultiplier, int reputationBase, boolean policySupport, String[] availableCities) {
        this.displayName = displayName;
        this.studentQualityBonus = studentQualityBonus;
        this.competitionFactor = competitionFactor;
        this.costMultiplier = costMultiplier;
        this.reputationBase = reputationBase;
        this.policySupport = policySupport;
        this.availableCities = availableCities;
    }

    public static CityType fromName(String name) {
        return Arrays.stream(CityType.values())
                .filter(t -> t.name().equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("无效的城市类型: " + name));
    }

    public String getRandomCity() {
        int index = (int) (Math.random() * availableCities.length);
        return availableCities[index];
    }

    public long getOpenCost() {
        if (this == COUNTY) {
            return 3000000L;
        }
        return (long) (5000000 * costMultiplier);
    }
}
