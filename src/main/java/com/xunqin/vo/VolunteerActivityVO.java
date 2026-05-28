package com.xunqin.vo;

import com.xunqin.entity.VolunteerActivity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class VolunteerActivityVO extends VolunteerActivity {

    private static final long serialVersionUID = 1L;

    private String publisherName;
    private String publisherAvatar;
    private String typeName;
    private String statusName;
    private Boolean hasParticipated;
    private Integer participantStatus;
    private Long participantId;
    private Boolean canReport;
    private Integer participantCount;
    private BigDecimal myWorkHours;

    public VolunteerActivityVO() {
    }

    public VolunteerActivityVO(VolunteerActivity activity, String publisherName, String publisherAvatar) {
        this.setId(activity.getId());
        this.setTitle(activity.getTitle());
        this.setType(activity.getType());
        this.setDescription(activity.getDescription());
        this.setContent(activity.getContent());
        this.setLocation(activity.getLocation());
        this.setLocationLat(activity.getLocationLat());
        this.setLocationLng(activity.getLocationLng());
        this.setStartTime(activity.getStartTime());
        this.setEndTime(activity.getEndTime());
        this.setMaxParticipants(activity.getMaxParticipants());
        this.setCurrentParticipants(activity.getCurrentParticipants());
        this.setRequiredSkills(activity.getRequiredSkills());
        this.setContactName(activity.getContactName());
        this.setContactPhone(activity.getContactPhone());
        this.setContactEmail(activity.getContactEmail());
        this.setCoverImage(activity.getCoverImage());
        this.setAttachments(activity.getAttachments());
        this.setPublisherId(activity.getPublisherId());
        this.setStatus(activity.getStatus());
        this.setViewCount(activity.getViewCount());
        this.setCreateTime(activity.getCreateTime());
        this.setUpdateTime(activity.getUpdateTime());
        this.publisherName = publisherName;
        this.publisherAvatar = publisherAvatar;
    }
}
