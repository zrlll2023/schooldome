package com.yucairoad.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("event_log")
public class EventLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long saveId;

    private String eventType;

    private String eventTitle;

    private String eventDescription;

    private String choices;

    private Integer playerChoice;

    private String result;

    private Integer triggerYear;

    private Integer triggerMonth;

    private LocalDateTime createdAt;
}
