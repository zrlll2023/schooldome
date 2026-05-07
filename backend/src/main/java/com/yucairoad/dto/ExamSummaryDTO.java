package com.yucairoad.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class ExamSummaryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private BigDecimal avgScore;

    private BigDecimal maxScore;

    private BigDecimal minScore;

    private BigDecimal passRate;

    private BigDecimal excellenceRate;

    private Integer totalStudents;

    private String examType;
}
