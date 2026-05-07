package com.yucairoad.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class StudentStatisticsDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long totalStudents;

    private Double avgAcademic;

    private Double avgQuality;

    private Double avgHealth;

    private Map<String, Integer> gradeDistribution;

    private Integer atRiskCount;

    private Integer excellentCount;
}
