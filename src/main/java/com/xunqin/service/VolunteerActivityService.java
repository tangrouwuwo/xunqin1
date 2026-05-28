package com.xunqin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xunqin.entity.VolunteerActivity;
import com.xunqin.entity.VolunteerActivityProgress;
import com.xunqin.vo.VolunteerActivityVO;
import com.xunqin.vo.VolunteerActivityParticipantVO;
import com.xunqin.vo.VolunteerActivityReportVO;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

public interface VolunteerActivityService {

    // 上传封面图片
    String uploadCoverImage(MultipartFile coverImage);
    
    String uploadPhotos(MultipartFile[] photos);

    // ==================== 管理员功能 ====================

    VolunteerActivity createActivity(Long publisherId, String title, String type, String description,
                                     String content, String location, Double locationLat, Double locationLng,
                                     LocalDateTime startTime, LocalDateTime endTime, Integer maxParticipants,
                                     String requiredSkills, String contactName, String contactPhone,
                                     String contactEmail, String coverImage, String attachments);

    VolunteerActivity updateActivity(Long activityId, Long publisherId, String title, String type,
                                     String description, String content, String location, Double locationLat,
                                     Double locationLng, LocalDateTime startTime, LocalDateTime endTime,
                                     Integer maxParticipants, String requiredSkills, String contactName,
                                     String contactPhone, String contactEmail, String coverImage, String attachments);

    void deleteActivity(Long activityId, Long publisherId);

    void publishActivity(Long activityId, Long publisherId);

    void cancelActivity(Long activityId, Long publisherId);

    void startActivity(Long activityId, Long publisherId);

    void endActivity(Long activityId, Long publisherId);

    Page<VolunteerActivityVO> getAdminActivities(Long publisherId, Integer status, String type,
                                                  Integer pageNum, Integer pageSize);

    VolunteerActivityVO getAdminActivityDetail(Long activityId, Long publisherId);

    // ==================== 志愿者功能 ====================

    Page<VolunteerActivityVO> getActivities(Long volunteerId, Integer status, String type,
                                            String keyword, Integer pageNum, Integer pageSize);

    VolunteerActivityVO getActivityDetail(Long activityId, Long volunteerId);

    void joinActivity(Long activityId, Long volunteerId, String applyReason);

    void quitActivity(Long activityId, Long volunteerId);

    void volunteerCheckin(Long activityId, Long volunteerId);

    Page<VolunteerActivityVO> getMyActivities(Long volunteerId, Integer status, Integer pageNum, Integer pageSize);

    // ==================== 参与管理功能 ====================

    Page<VolunteerActivityParticipantVO> getActivityParticipants(Long activityId, Long adminId,
                                                                  Integer status, Integer pageNum, Integer pageSize);

    void approveParticipant(Long participantId, Long adminId, String adminRemark);

    void rejectParticipant(Long participantId, Long adminId, String rejectReason);

    void checkInParticipant(Long participantId, Long adminId);

    void checkOutParticipant(Long participantId, Long adminId, Double workHours);

    // ==================== 报告功能 ====================

    void submitReport(Long activityId, Long volunteerId, String title, String content,
                      String workContent, String workResults, String problemsEncountered,
                      String suggestions, String photos, String attachments);

    Page<VolunteerActivityReportVO> getActivityReports(Long activityId, Long adminId,
                                                        Integer status, Integer pageNum, Integer pageSize);

    void approveReport(Long reportId, Long adminId, String reviewRemark);

    void rejectReport(Long reportId, Long adminId, String reviewRemark);

    VolunteerActivityReportVO getMyReport(Long activityId, Long volunteerId);
    
    VolunteerActivityReportVO getReportDetail(Long reportId, Long adminId);

    // ==================== 进度功能 ====================

    VolunteerActivityProgress addProgress(Long activityId, Long publisherId, String title,
                                          String content, String progressType, Integer isImportant);

    List<VolunteerActivityProgress> getActivityProgress(Long activityId);

    void deleteProgress(Long progressId, Long publisherId);

    // ==================== 统计功能 ====================

    long countByStatus(Integer status);

    long countParticipantsByActivity(Long activityId);

    long countReportsByStatus(Integer status);
}
