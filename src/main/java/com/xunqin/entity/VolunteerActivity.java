package com.xunqin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("volunteer_activity")
public class VolunteerActivity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String title;

    private String type;

    private String description;

    private String content;

    private String location;

    private Double locationLat;

    private Double locationLng;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer maxParticipants;

    private Integer currentParticipants;

    private String requiredSkills;

    private String contactName;

    private String contactPhone;

    private String contactEmail;

    private String coverImage;

    private String attachments;

    private Long publisherId;

    private Integer status;

    private Integer viewCount;

    @TableLogic
    private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
