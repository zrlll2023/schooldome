package com.yucairoad.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
public enum GroupLevel {

    EDUCATION_ALLIANCE("教育联盟", 3,
            List.of("统一招聘:招聘费用-20%", "教师流通:可在分校间调配教师")),
    EDUCATION_GROUP("教育集团", 5,
            List.of("统一招聘:招聘费用-20%", "教师流通:可在分校间调配教师",
                    "集团品牌加成:+10%所有分校声望", "统一考试:对比各校成绩")),
    EDUCATION_EMPIRE("教育帝国", 10,
            List.of("统一招聘:招聘费用-20%", "教师流通:可在分校间调配教师",
                    "集团品牌加成:+10%所有分校声望", "统一考试:对比各校成绩",
                    "全国排名:参与全国学校排名", "政策影响力:触发特殊事件", "国际交流前置:解锁国际交流"));

    private final String displayName;
    private final int requiredBranches;
    private final List<String> benefits;

    GroupLevel(String displayName, int requiredBranches, List<String> benefits) {
        this.displayName = displayName;
        this.requiredBranches = requiredBranches;
        this.benefits = benefits;
    }

    public static GroupLevel fromBranchCount(int branchCount) {
        if (branchCount >= EDUCATION_EMPIRE.requiredBranches) {
            return EDUCATION_EMPIRE;
        } else if (branchCount >= EDUCATION_GROUP.requiredBranches) {
            return EDUCATION_GROUP;
        } else if (branchCount >= EDUCATION_ALLIANCE.requiredBranches) {
            return EDUCATION_ALLIANCE;
        }
        return null;
    }

    public static GroupLevel fromName(String name) {
        return Arrays.stream(GroupLevel.values())
                .filter(g -> g.name().equals(name))
                .findFirst()
                .orElse(null);
    }

    public GroupLevel getNextLevel() {
        if (this == EDUCATION_ALLIANCE) return EDUCATION_GROUP;
        if (this == EDUCATION_GROUP) return EDUCATION_EMPIRE;
        return null;
    }
}
