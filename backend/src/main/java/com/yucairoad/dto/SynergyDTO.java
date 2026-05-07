package com.yucairoad.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class SynergyDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<String> activeSynergies;

    private List<String> inactiveSynergies;

    private java.util.Map<String, SynergyEffect> effects;

    @Data
    public static class SynergyEffect implements Serializable {
        private static final long serialVersionUID = 1L;
        private Boolean active;
        private String condition;
        private String benefit;
        private BigDecimal monthlySavings;
        private String effect;
        private Integer yearsRemaining;
    }
}
