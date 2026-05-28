package com.xunqin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("clue")
public class Clue implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long missingPersonId;

    private Long providerId;

    private Integer isAnonymous;

    private String content;

    private String contactName;

    private String contactPhone;

    private String contactEmail;

    @TableField(exist = false)
    private String missingPersonName;

    @TableField(exist = false)
    private String submitterName;

    private Integer status;

    private Long handlerId;

    private LocalDateTime handleTime;

    private String handleResult;

    private String handleRemark;

    // 志愿者分配ID（数据库中不存在，暂时注释）
    // private Long assignedVolunteerId;

    @TableLogic
    private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
