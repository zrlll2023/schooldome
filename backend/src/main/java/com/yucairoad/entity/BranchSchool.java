package com.yucairoad.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("branch_school")
public class BranchSchool implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long saveId;

    private String name;

    private String city;

    private String cityType;

    private String managementMode;

    private Integer principalAbility;

    private Integer reputation;

    private Integer studentCount;

    private BigDecimal annualProfit;

    private BigDecimal monthlyIncome;

    private BigDecimal monthlyExpense;

    private BigDecimal totalRemittance;

    private Integer qualityRating;

    private Integer establishedYear;

    private Integer operatingYears;

    private Integer constructionProgress;

    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
