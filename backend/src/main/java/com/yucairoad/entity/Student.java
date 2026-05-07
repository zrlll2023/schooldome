package com.yucairoad.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("student")
public class Student implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long schoolId;

    private String name;

    private String grade;

    private String gradeLevel;

    private BigDecimal academicScore;

    private BigDecimal qualityScore;

    private BigDecimal healthScore;

    private Integer isK12Student;

    private Long fromSchoolId;

    private Integer enrolledYear;

    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
