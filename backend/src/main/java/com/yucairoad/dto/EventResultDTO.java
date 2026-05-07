package com.yucairoad.dto;

import lombok.Data;
import java.io.Serializable;
import java.util.Map;

@Data
public class EventResultDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private boolean success;

    private int choiceId;

    private double actualSuccessRate;

    private Map<String, Object> effects;

    private String message;
}
