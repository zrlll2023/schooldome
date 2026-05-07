package com.yucairoad.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class PipelineDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private PipelineData primaryToJunior;

    private PipelineData juniorToSenior;

    private K12Statistics k12Statistics;

    @Data
    public static class PipelineData implements Serializable {
        private static final long serialVersionUID = 1L;
        private Integer totalGraduates;
        private Double transferCount;
        private Double externalCount;
        private Double transferRate;
        private Integer reputationGain;
        private String bonusDescription;
    }

    @Data
    public static class K12Statistics implements Serializable {
        private static final long serialVersionUID = 1L;
        private Integer fullTrainedStudents;
        private Integer currentK12Students;
        private BigDecimal gaokaoBonusRate;
        private Integer consecutiveExcellenceYears;
    }
}
