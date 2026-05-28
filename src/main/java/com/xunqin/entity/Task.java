package com.xunqin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("task")
public class Task implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long publisherId;

    private Long volunteerId;

    private String title;

    private String type;

    private String description;

    private String location;

    private Double locationLat;

    private Double locationLng;

    private Integer priority;

    private String requiredSkills;

    private LocalDateTime deadline;

    private Integer status;

    private String result;

    private String reviewRemark;

    @TableLogic
    private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
