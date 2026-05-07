package com.yucairoad.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class StudentInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String name;

    private String grade;

    private String gradeLevel;

    private BigDecimal academicScore;

    private BigDecimal qualityScore;

    private BigDecimal healthScore;
}
