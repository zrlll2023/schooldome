package com.yucairoad.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("exam_record")
public class ExamRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long schoolId;

    private Long studentId;

    private String examType;

    private BigDecimal score;

    private Integer rank;

    private Integer examYear;

    private Integer isTopScholar;

    private LocalDateTime createdAt;
}
