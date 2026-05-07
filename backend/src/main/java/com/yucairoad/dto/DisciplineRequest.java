package com.yucairoad.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class DisciplineRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private String reason;
}
