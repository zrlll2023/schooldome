package com.yucairoad.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("building")
public class Building implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long schoolId;

    private String type;

    private Integer level;

    private Integer capacity;

    private BigDecimal monthlyCost;

    private BigDecimal buildCost;

    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
