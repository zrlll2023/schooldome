package com.yucairoad.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class ExamTriggerResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private String examType;

    private String message;

    private Integer totalStudents;

    private BigDecimal avgScore;

    private Integer reputationChange;

    private BigDecimal fundChange;

    private List<ExamResultDTO> results;

    private ExamSummaryDTO summary;
}
