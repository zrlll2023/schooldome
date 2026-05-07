package com.yucairoad.dto;

import lombok.Data;
import java.io.Serializable;
import java.util.Map;

@Data
public class EventChoice implements Serializable {

    private static final long serialVersionUID = 1L;

    private int choiceId;

    private String text;

    private int cost;

    private double baseSuccessRate;

    private Map<String, Object> successResult;

    private Map<String, Object> failResult;
}
