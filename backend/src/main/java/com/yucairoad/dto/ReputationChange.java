package com.yucairoad.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class ReputationChange implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer totalChange;

    private List<ReputationSource> sources;

    public ReputationChange() {
        this.totalChange = 0;
        this.sources = new ArrayList<>();
    }

    @Data
    public static class ReputationSource implements Serializable {
        private static final long serialVersionUID = 1L;
        private String type;
        private String description;
        private Integer value;

        public ReputationSource(String type, String description, Integer value) {
            this.type = type;
            this.description = description;
            this.value = value;
        }
    }
}
