package com.yucairoad.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("alumni")
public class Alumni implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long saveId;

    private String studentName;

    private Integer graduationYear;

    private String graduationSchool;

    private String achievementType;

    private String achievementLevel;

    private BigDecimal donationAmount;

    private Integer reputationContribution;

    private LocalDateTime createdAt;
}
