package com.yucairoad.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class RankingDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer rank;

    private String studentName;

    private BigDecimal score;

    private String grade;
}
