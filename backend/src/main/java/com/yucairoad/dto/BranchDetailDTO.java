package com.yucairoad.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class BranchDetailDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String city;
    private String cityType;
    private String cityTypeDisplay;
    private String managementMode;
    private String managementModeDisplay;
    private Integer principalAbility;
    private Integer studentCount;
    private BigDecimal annualProfit;
    private BigDecimal monthlyIncome;
    private BigDecimal monthlyExpense;
    private Integer qualityRating;
    private Integer reputation;
    private Integer reputationContribution;
    private Integer operatingYears;
    private Integer establishedYear;
    private String status;
    private String upgradeLevel;
    private double studentQualityBonus;
    private double competitionFactor;
    private boolean policySupport;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<MonthlyRecord> monthlyHistory;
    private Map<String, Object> upgradeProgress;

    @Data
    public static class MonthlyRecord implements Serializable {
        private static final long serialVersionUID = 1L;
        private int year;
        private int month;
        private int students;
        private BigDecimal profit;
        private int quality;
    }
}
