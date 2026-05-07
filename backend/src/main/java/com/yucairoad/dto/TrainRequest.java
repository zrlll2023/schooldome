package com.yucairoad.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class TrainRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private String trainType;
}
