package com.yucairoad.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class ScoreResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long studentId;

    private String studentName;

    private BigDecimal basePotential;

    private BigDecimal teacherBonus;

    private BigDecimal facilityBonus;

    private BigDecimal courseRationality;

    private BigDecimal effortCoefficient;

    private BigDecimal randomFactor;

    private BigDecimal finalScore;

    public ScoreResult() {
        this.basePotential = BigDecimal.ZERO;
        this.teacherBonus = BigDecimal.ZERO;
        this.facilityBonus = BigDecimal.ZERO;
        this.courseRationality = BigDecimal.ONE;
        this.effortCoefficient = BigDecimal.ONE;
        this.randomFactor = BigDecimal.ONE;
        this.finalScore = BigDecimal.ZERO;
    }
}
