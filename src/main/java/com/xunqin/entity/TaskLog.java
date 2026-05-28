package com.xunqin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("task_log")
public class TaskLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long taskId;

    private Long volunteerId;

    private String logType;

    private String content;

    private String attachments;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
