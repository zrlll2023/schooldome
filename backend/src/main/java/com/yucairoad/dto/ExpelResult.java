package com.yucairoad.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class ExpelResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long studentId;

    private String studentName;

    private Integer newReputation;

    private Integer newStudentCount;

    private String message;
}
