package com.xunqin.vo;

import com.xunqin.entity.VolunteerActivityReport;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class VolunteerActivityReportVO extends VolunteerActivityReport {

    private static final long serialVersionUID = 1L;

    private String volunteerName;
    private String volunteerAvatar;
    private String activityTitle;
    private String activityType;
    private String statusName;
    private String reviewerName;

    public VolunteerActivityReportVO() {
    }

    public VolunteerActivityReportVO(VolunteerActivityReport report,
                                     String volunteerName, String volunteerAvatar,
                                     String activityTitle, String activityType) {
        this.setId(report.getId());
        this.setActivityId(report.getActivityId());
        this.setParticipantId(report.getParticipantId());
        this.setVolunteerId(report.getVolunteerId());
        this.setTitle(report.getTitle());
        this.setContent(report.getContent());
        this.setWorkContent(report.getWorkContent());
        this.setWorkResults(report.getWorkResults());
        this.setProblemsEncountered(report.getProblemsEncountered());
        this.setSuggestions(report.getSuggestions());
        this.setPhotos(report.getPhotos());
        this.setAttachments(report.getAttachments());
        this.setStatus(report.getStatus());
        this.setReviewRemark(report.getReviewRemark());
        this.setReviewTime(report.getReviewTime());
        this.setReviewerId(report.getReviewerId());
        this.setCreateTime(report.getCreateTime());
        this.setUpdateTime(report.getUpdateTime());
        this.volunteerName = volunteerName;
        this.volunteerAvatar = volunteerAvatar;
        this.activityTitle = activityTitle;
        this.activityType = activityType;
    }
}
