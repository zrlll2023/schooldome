package com.yucairoad.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class BuildProgressDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String stageType;

    private Integer startYear;

    private Integer startMonth;

    private Integer totalMonths;

    private Integer elapsedMonths;

    private Integer progressPercent;

    private BigDecimal buildCost;

    private String status;

    private Long schoolId;
}
