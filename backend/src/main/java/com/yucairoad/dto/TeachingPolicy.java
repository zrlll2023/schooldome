package com.yucairoad.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class TeachingPolicy implements Serializable {

    private static final long serialVersionUID = 1L;

    private String teachingStyle;

    private String homeworkLoad;

    private String weekendArrangement;

    private String extracurricular;

    private String competitionTraining;
}
