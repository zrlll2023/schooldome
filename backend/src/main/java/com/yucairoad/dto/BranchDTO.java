package com.yucairoad.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class BranchDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String cityType;
    private String cityTypeDisplay;
    private String managementMode;
    private String managementModeDisplay;
    private Integer studentCount;
    private BigDecimal annualProfit;
    private Integer qualityRating;
    private Integer reputationContribution;
    private Integer operatingYears;
    private String status;
    private String upgradeLevel;
    private LocalDateTime createdAt;
}
