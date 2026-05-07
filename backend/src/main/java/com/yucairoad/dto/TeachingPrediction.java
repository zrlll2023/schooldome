package com.yucairoad.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class TeachingPrediction implements Serializable {

    private static final long serialVersionUID = 1L;

    private Double predictedExamScore;

    private Double pressureIndex;

    private Double qualityIndex;
}
