package com.yucairoad.dto;

import lombok.Data;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
public class EventTemplate implements Serializable {

    private static final long serialVersionUID = 1L;

    private String eventId;

    private String eventType;

    private String title;

    private String description;

    private List<EventChoice> choices;

    private TriggerCondition condition;

    private int weight;
}
