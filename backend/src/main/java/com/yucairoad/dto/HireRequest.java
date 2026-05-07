package com.yucairoad.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class HireRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private String hireType;

    private String targetLevel;
}
