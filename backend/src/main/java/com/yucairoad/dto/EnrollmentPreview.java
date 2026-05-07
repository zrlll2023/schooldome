package com.yucairoad.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

@Data
public class EnrollmentPreview implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer expectedStudentCount;

    private Map<String, Double> qualityDistribution;

    private Double estimatedAvgQuality;
}
