package com.yucairoad.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("school")
public class School implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long saveId;

    private String name;

    private String type;

    private String level;

    private Integer reputation;

    private BigDecimal funds;

    private Integer studentCount;

    private Integer teacherCount;

    private String teachingPolicy;

    private String examStyle;

    private Integer totalYears;

    private String achievements;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
