package com.yucairoad.dto;

import lombok.Data;
import java.io.Serializable;
import java.util.Map;

@Data
public class TriggerCondition implements Serializable {

    private static final long serialVersionUID = 1L;

    private Map<String, Object> conditions;
}
