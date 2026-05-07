package com.yucairoad.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class BuildingDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String type;
    private String typeName;
    private Integer level;
    private Integer maxLevel;
    private Integer capacity;
    private BigDecimal monthlyCost;
    private BigDecimal buildCost;
    private String status;
    private LocalDateTime completeTime;
    private LocalDateTime createdAt;
}
