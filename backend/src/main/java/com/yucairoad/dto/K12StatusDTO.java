package com.yucairoad.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class K12StatusDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<StageInfo> stages;

    private Boolean isComplete;

    private BuildProgressDTO buildProgress;

    private PipelineDTO pipelineSummary;

    private SynergyDTO synergySummary;

    @Data
    public static class StageInfo implements Serializable {
        private static final long serialVersionUID = 1L;
        private String type;
        private String status;
        private String name;
        private Integer students;
        private Integer teachers;
        private String level;
    }
}
