package com.xunqin.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class VolunteerApplicationVO {
    private Long id;
    private String name;
    private String username;
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
    private String reviewerName;
    private LocalDateTime reviewTime;
    private LocalDateTime createTime;
}
