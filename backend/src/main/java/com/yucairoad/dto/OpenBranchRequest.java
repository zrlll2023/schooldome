package com.yucairoad.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class OpenBranchRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private String cityType;
    private String name;
}
