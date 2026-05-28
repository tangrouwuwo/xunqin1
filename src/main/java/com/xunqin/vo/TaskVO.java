package com.xunqin.vo;

import com.xunqin.entity.Task;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TaskVO {
    private Long id;
    private Long publisherId;
    private String publisherName;
    private String publisherRole;
    private Long volunteerId;
    private String volunteerName;
    private String title;
    private String type;
    private String description;
    private String location;
    private Integer priority;
    private String requiredSkills;
    private LocalDateTime deadline;
    private Integer status;
    private String result;
    private String reviewRemark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public TaskVO(Task task, String publisherName, String publisherRole, String volunteerName) {
        this.id = task.getId();
        this.publisherId = task.getPublisherId();
        this.publisherName = publisherName;
        this.publisherRole = publisherRole;
        this.volunteerId = task.getVolunteerId();
        this.volunteerName = volunteerName;
        this.title = task.getTitle();
        this.type = task.getType();
        this.description = task.getDescription();
        this.location = task.getLocation();
        this.priority = task.getPriority();
        this.requiredSkills = task.getRequiredSkills();
        this.deadline = task.getDeadline();
        this.status = task.getStatus();
        this.result = task.getResult();
        this.reviewRemark = task.getReviewRemark();
        this.createTime = task.getCreateTime();
        this.updateTime = task.getUpdateTime();
    }
}