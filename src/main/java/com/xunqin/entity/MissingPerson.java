package com.xunqin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("missing_person")
public class MissingPerson implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long seekerId;

    private String title;

    private String name;

    private String gender;

    private LocalDate birthDate;

    private Integer ageAtMissing;

    private Integer currentAge;

    private Integer height;

    private Integer weight;

    private String appearance;

    private String clothing;

    private String specialFeatures;

    private LocalDate missingDate;

    private String missingLocation;

    private java.math.BigDecimal missingLocationLat;

    private java.math.BigDecimal missingLocationLng;

    private String missingCause;

    private String description;

    private String photos;

    private String videos;

    private String contactName;

    private String contactPhone;

    private String contactEmail;

    private String reward;

    private Integer status;

    private Integer matchStatus;

    private Integer viewCount;

    private Integer clueCount;

    private String rejectReason;

    @TableLogic
    private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
