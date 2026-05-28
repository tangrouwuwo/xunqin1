package com.xunqin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("success_case")
public class SuccessCase implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long seekerId;

    private Long missingPersonId;

    private String title;

    private String story;

    private String reunionDate;

    private String reunionLocation;

    private String photos;

    private String videos;

    private String tags;

    private Integer viewCount;

    private Integer status;

    private String approvalRemark;

    private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
