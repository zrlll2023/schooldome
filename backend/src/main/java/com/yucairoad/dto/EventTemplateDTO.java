package com.yucairoad.dto;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

@Data
public class EventTemplateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String eventId;

    private String eventType;

    private String title;

    private String description;

    private List<EventChoiceDTO> choices;

    private int weight;
}
