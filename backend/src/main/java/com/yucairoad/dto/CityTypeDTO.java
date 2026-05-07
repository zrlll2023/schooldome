package com.yucairoad.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class CityTypeDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String type;
    private String displayName;
    private double studentQualityBonus;
    private double competitionFactor;
    private double costMultiplier;
    private int reputationBase;
    private boolean policySupport;
    private long openCost;
    private String[] availableCities;
    private String description;
}
