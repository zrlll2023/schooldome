package com.yucairoad.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class EnrollmentResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer totalEnrolled;

    private Map<String, Integer> qualityBreakdown;

    private BigDecimal tuitionIncome;

    private List<StudentInfo> students;

    @Data
    public static class StudentInfo implements Serializable {
        private static final long serialVersionUID = 1L;
        private Long id;
        private String name;
        String gradeLevel;
        BigDecimal academicScore;
        BigDecimal qualityScore;
        BigDecimal healthScore;
    }
}
