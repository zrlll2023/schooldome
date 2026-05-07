package com.yucairoad.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class TeacherInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String name;

    private String level;

    private Integer teachingAbility;

    private Integer moralLevel;

    private String specialty;

    private BigDecimal salary;

    private Integer experience;

    private Integer hireYear;
}
