package com.yucairoad.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("game_save")
public class GameSave implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String saveName;

    private Integer currentYear;

    private Integer currentMonth;

    private String gameState;

    private Integer isActive;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
