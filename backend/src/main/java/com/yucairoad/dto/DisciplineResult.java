package com.yucairoad.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;

@Data
public class DisciplineResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long studentId;

    private String studentName;

    private BigDecimal newAcademicScore;

    private BigDecimal newHealthScore;

    private String disciplineReason;

    private String message;
}
