package com.yucairoad.enums;

public enum EventType {

    STUDENT("学生事件"),
    TEACHER("教师事件"),
    CAMPUS("校园事件"),
    SOCIAL("社会事件"),
    EXAM("升学事件"),
    ALUMNI("校友事件"),
    GROUP("集团事件"),
    DISCIPLINE("纪律处分"),
    SYSTEM("系统事件");

    private final String description;

    EventType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
