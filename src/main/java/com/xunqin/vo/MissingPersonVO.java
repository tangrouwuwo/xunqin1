package com.xunqin.vo;

import com.xunqin.entity.MissingPerson;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class MissingPersonVO {
    private Long id;
    private Long seekerId;
    private String seekerName;
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
    private BigDecimal missingLocationLat;
    private BigDecimal missingLocationLng;
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
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public MissingPersonVO() {}

    public MissingPersonVO(MissingPerson person, String seekerName) {
        this.id = person.getId();
        this.seekerId = person.getSeekerId();
        this.seekerName = seekerName;
        this.title = person.getTitle();
        this.name = person.getName();
        this.gender = person.getGender();
        this.birthDate = person.getBirthDate();
        this.ageAtMissing = person.getAgeAtMissing();
        this.currentAge = person.getCurrentAge();
        this.height = person.getHeight();
        this.weight = person.getWeight();
        this.appearance = person.getAppearance();
        this.clothing = person.getClothing();
        this.specialFeatures = person.getSpecialFeatures();
        this.missingDate = person.getMissingDate();
        this.missingLocation = person.getMissingLocation();
        this.missingLocationLat = person.getMissingLocationLat();
        this.missingLocationLng = person.getMissingLocationLng();
        this.missingCause = person.getMissingCause();
        this.description = person.getDescription();
        this.photos = person.getPhotos();
        this.videos = person.getVideos();
        this.contactName = person.getContactName();
        this.contactPhone = person.getContactPhone();
        this.contactEmail = person.getContactEmail();
        this.reward = person.getReward();
        this.status = person.getStatus();
        this.matchStatus = person.getMatchStatus();
        this.viewCount = person.getViewCount();
        this.clueCount = person.getClueCount();
        this.rejectReason = person.getRejectReason();
        this.createTime = person.getCreateTime();
        this.updateTime = person.getUpdateTime();
    }
}