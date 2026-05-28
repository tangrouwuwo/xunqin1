package com.xunqin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("volunteer_activity_participant")
public class VolunteerActivityParticipant implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long activityId;

    private Long volunteerId;

    private Integer status;

    private String applyReason;

    private String rejectReason;

    private LocalDateTime checkInTime;

    private LocalDateTime checkOutTime;

    private BigDecimal workHours;

    private String adminRemark;

    private Integer isReported;

    @TableLogic
    private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
