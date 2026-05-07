package com.yucairoad.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class GameState implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer currentYear;

    private Integer currentMonth;

    private SchoolInfo school;

    private BigDecimal funds;

    private Integer reputation;

    private Integer studentCount;

    private Integer teacherCount;

    private Integer speed;

    private Boolean isPaused;

    private List<GameEvent> events;

    private GameStatistics statistics;

    private EnrollmentPolicy enrollmentPolicy;

    private K12System k12System;

    public GameState() {
        this.events = new ArrayList<>();
        this.statistics = new GameStatistics();
        this.speed = 1;
        this.isPaused = false;
    }

    @Data
    public static class SchoolInfo implements Serializable {
        private static final long serialVersionUID = 1L;
        private String name;
        private String type;
        private String level;
        private Integer totalYears;
        private List<BuildingInfo> buildings;
        private List<TeacherInfo> teachers;

        public SchoolInfo() {
            this.buildings = new ArrayList<>();
            this.teachers = new ArrayList<>();
        }
    }

    @Data
    public static class BuildingInfo implements Serializable {
        private static final long serialVersionUID = 1L;
        private String type;
        private Integer level;
        private Integer capacity;
        private BigDecimal monthlyCost;
    }

    @Data
    public static class TeacherInfo implements Serializable {
        private static final long serialVersionUID = 1L;
        private String name;
        private String level;
        private Integer teachingAbility;
        private BigDecimal salary;
    }

    @Data
    public static class GameEvent implements Serializable {
        private static final long serialVersionUID = 1L;
        private String type;
        private String title;
        private String description;
        private Integer month;
    }

    @Data
    public static class GameStatistics implements Serializable {
        private static final long serialVersionUID = 1L;
        private Double avgAcademic;
        private Double avgQuality;
        private Double avgHealth;
        private Double satisfaction;

        public GameStatistics() {
            this.avgAcademic = 60.0;
            this.avgQuality = 60.0;
            this.avgHealth = 80.0;
            this.satisfaction = 70.0;
        }
    }

    @Data
    public static class K12System implements Serializable {
        private static final long serialVersionUID = 1L;
        private Boolean hasPrimary;
        private Boolean hasJunior;
        private Boolean hasSenior;
        private SchoolInfo primarySchool;
        private SchoolInfo juniorSchool;
        private SchoolInfo seniorSchool;
        private Map<String, Object> primaryBuildProgress;
        private Map<String, Object> juniorBuildProgress;
        private PipelineData pipelineData;
        private SynergyEffects synergyEffects;
        private Integer consecutiveExcellenceYears;
    }

    @Data
    public static class PipelineData implements Serializable {
        private static final long serialVersionUID = 1L;
        private Double primaryToJuniorRate;
        private Double juniorToSeniorRate;
        private Integer primaryGraduates;
        private Integer juniorGraduates;
        private Integer k12FullTrainedStudents;
    }

    @Data
    public static class SynergyEffects implements Serializable {
        private static final long serialVersionUID = 1L;
        private Boolean resourceSharing;
        private Boolean teacherMobility;
        private Boolean brandBonus;
        private Boolean districtEffect;
    }
}
