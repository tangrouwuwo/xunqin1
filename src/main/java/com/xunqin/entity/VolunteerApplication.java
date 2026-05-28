package com.xunqin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("volunteer_application")
public class VolunteerApplication implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String name;

    private String username;

    private String password;

    private String phone;

    private String email;

    private Integer age;

    private String gender;

    private String location;

    private String skills;

    private String availability;

    private String reason;

    private String experience;

    private Integer status;

    private String reviewComment;

    private Long reviewerId;

    private LocalDateTime reviewTime;

    private Long userId;

    @TableLogic
    private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
