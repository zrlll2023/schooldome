package com.yucairoad.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class CampusStatsDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String campusLevel;
    private int currentBuildingCount;
    private int maxBuildingCount;
    private int totalStudentCapacity;
    private double academicBonusPercent;
    private double physicalBonusPercent;
    private double scienceBonusPercent;
    private BigDecimal totalMonthlyMaintenance;
    private BigDecimal expandCost;
    private boolean canExpand;
}
