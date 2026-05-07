package com.yucairoad.dto;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class EventDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String eventId;

    private String eventType;

    private String title;

    private String description;

    private List<EventChoiceDTO> choices;

    private Integer playerChoice;

    private Map<String, Object> result;

    private String status;

    private boolean urgent;

    private Integer triggerYear;

    private Integer triggerMonth;

    private LocalDateTime createdAt;
}
