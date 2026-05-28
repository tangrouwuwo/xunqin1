package com.xunqin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("missing_person_change_log")
public class MissingPersonChangeLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long missingPersonId;

    private Long seekerId;

    private String fieldName;

    private String oldValue;

    private String newValue;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}