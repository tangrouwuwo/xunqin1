package com.xunqin.vo;

import com.xunqin.entity.VolunteerActivityParticipant;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class VolunteerActivityParticipantVO extends VolunteerActivityParticipant {

    private static final long serialVersionUID = 1L;

    private String volunteerName;
    private String volunteerAvatar;
    private String volunteerPhone;
    private String volunteerEmail;
    private String activityTitle;
    private String activityType;
    private String statusName;
    private Boolean hasReport;

    public VolunteerActivityParticipantVO() {
    }

    public VolunteerActivityParticipantVO(VolunteerActivityParticipant participant,
                                          String volunteerName, String volunteerAvatar,
                                          String activityTitle, String activityType) {
        this.setId(participant.getId());
        this.setActivityId(participant.getActivityId());
        this.setVolunteerId(participant.getVolunteerId());
        this.setStatus(participant.getStatus());
        this.setApplyReason(participant.getApplyReason());
        this.setRejectReason(participant.getRejectReason());
        this.setCheckInTime(participant.getCheckInTime());
        this.setCheckOutTime(participant.getCheckOutTime());
        this.setWorkHours(participant.getWorkHours());
        this.setAdminRemark(participant.getAdminRemark());
        this.setIsReported(participant.getIsReported());
        this.setCreateTime(participant.getCreateTime());
        this.setUpdateTime(participant.getUpdateTime());
        this.volunteerName = volunteerName;
        this.volunteerAvatar = volunteerAvatar;
        this.activityTitle = activityTitle;
        this.activityType = activityType;
    }
}
