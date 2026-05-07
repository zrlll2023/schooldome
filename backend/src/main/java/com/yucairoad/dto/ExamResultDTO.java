package com.yucairoad.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class ExamResultDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long studentId;

    private String studentName;

    private BigDecimal score;

    private Integer rank;

    private String examType;

    private Boolean isTopScholar;
}
