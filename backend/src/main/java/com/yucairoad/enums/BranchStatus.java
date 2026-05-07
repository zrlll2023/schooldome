package com.yucairoad.enums;

import lombok.Getter;

@Getter
public enum BranchStatus {

    CONSTRUCTING("建设中"),
    OPERATING("运营中"),
    CLOSED("已关闭");

    private final String displayName;

    BranchStatus(String displayName) {
        this.displayName = displayName;
    }

    public static BranchStatus fromName(String name) {
        for (BranchStatus status : values()) {
            if (status.name().equals(name)) {
                return status;
            }
        }
        throw new IllegalArgumentException("无效的分校状态: " + name);
    }
}
