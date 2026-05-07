package com.yucairoad.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class ExamHistoryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long examId;

    private String examType;

    private BigDecimal score;

    private Integer rank;

    private Integer examYear;

    private String studentName;
}
