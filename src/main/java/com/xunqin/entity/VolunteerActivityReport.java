package com.xunqin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("volunteer_activity_report")
public class VolunteerActivityReport implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long activityId;

    private Long participantId;

    private Long volunteerId;

    private String title;

    private String content;

    private String workContent;

    private String workResults;

    private String problemsEncountered;

    private String suggestions;

    private String photos;

    private String attachments;

    private Integer status;

    private String reviewRemark;

    private LocalDateTime reviewTime;

    private Long reviewerId;

    @TableLogic
    private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
