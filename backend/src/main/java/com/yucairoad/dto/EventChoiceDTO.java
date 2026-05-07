package com.yucairoad.dto;

import lombok.Data;
import java.io.Serializable;
import java.util.Map;

@Data
public class EventChoiceDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private int choiceId;

    private String text;

    private int cost;

    private double baseSuccessRate;
}
