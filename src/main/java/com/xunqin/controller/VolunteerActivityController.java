package com.xunqin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xunqin.common.result.Result;
import com.xunqin.entity.VolunteerActivity;
import com.xunqin.entity.VolunteerActivityProgress;
import com.xunqin.service.VolunteerActivityService;
import com.xunqin.vo.VolunteerActivityVO;
import com.xunqin.vo.VolunteerActivityParticipantVO;
import com.xunqin.vo.VolunteerActivityReportVO;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/volunteer-activities")
public class VolunteerActivityController {

    @Autowired
    private VolunteerActivityService volunteerActivityService;

    // ==================== 管理员功能 ====================

    @Data
    public static class CreateActivityRequest {
        @NotBlank(message = "活动标题不能为空")
        private String title;

        @NotBlank(message = "活动类型不能为空")
        private String type;

        private String description;

        @NotBlank(message = "活动内容不能为空")
        private String content;

        private String location;
        private Double locationLat;
        private Double locationLng;

        @NotNull(message = "开始时间不能为空")
        private LocalDateTime startTime;

        @NotNull(message = "结束时间不能为空")
        private LocalDateTime endTime;

        private Integer maxParticipants;
        private String requiredSkills;
        private String contactName;
        private String contactPhone;
        private String contactEmail;
        private String coverImage;
        private String attachments;
    }

    @Data
    public static class UpdateActivityRequest {
        private String title;
        private String type;
        private String description;
        private String content;
        private String location;
        private Double locationLat;
        private Double locationLng;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Integer maxParticipants;
        private String requiredSkills;
        private String contactName;
        private String contactPhone;
        private String contactEmail;
        private String coverImage;
        private String attachments;
    }

    @PostMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<VolunteerActivity> createActivity(Authentication authentication,
                                                    @RequestParam("title") String title,
                                                    @RequestParam("type") String type,
                                                    @RequestParam(value = "description", required = false) String description,
                                                    @RequestParam("content") String content,
                                                    @RequestParam(value = "location", required = false) String location,
                                                    @RequestParam(value = "locationLat", required = false) Double locationLat,
                                                    @RequestParam(value = "locationLng", required = false) Double locationLng,
                                                    @RequestParam("startTime") String startTime,
                                                    @RequestParam("endTime") String endTime,
                                                    @RequestParam(value = "maxParticipants", required = false) Integer maxParticipants,
                                                    @RequestParam(value = "requiredSkills", required = false) String requiredSkills,
                                                    @RequestParam(value = "contactName", required = false) String contactName,
                                                    @RequestParam(value = "contactPhone", required = false) String contactPhone,
                                                    @RequestParam(value = "contactEmail", required = false) String contactEmail,
                                                    @RequestParam(value = "attachments", required = false) String attachments,
                                                    @RequestParam(value = "coverImage", required = false) MultipartFile coverImage) {
        Long userId = Long.parseLong(authentication.getPrincipal().toString());
        
        // 处理时间类型转换
        // 移除毫秒和时区信息，转换为LocalDateTime
        String startDateTimeStr = startTime.replace(".000Z", "");
        String endDateTimeStr = endTime.replace(".000Z", "");
        LocalDateTime startDateTime = LocalDateTime.parse(startDateTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
        LocalDateTime endDateTime = LocalDateTime.parse(endDateTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
        
        // 处理封面图片上传
        String coverImageUrl = null;
        if (coverImage != null && !coverImage.isEmpty()) {
            coverImageUrl = volunteerActivityService.uploadCoverImage(coverImage);
        }
        
        VolunteerActivity activity = volunteerActivityService.createActivity(
            userId, title, type, description,
            content, location, locationLat,
            locationLng, startDateTime, endDateTime,
            maxParticipants, requiredSkills,
            contactName, contactPhone, contactEmail,
            coverImageUrl, attachments
        );
        return Result.success("活动创建成功", activity);
    }

    @PutMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<VolunteerActivity> updateActivity(Authentication authentication,
                                                    @PathVariable Long id,
                                                    @RequestBody UpdateActivityRequest request) {
        Long userId = Long.parseLong(authentication.getPrincipal().toString());
        VolunteerActivity activity = volunteerActivityService.updateActivity(
            id, userId, request.getTitle(), request.getType(), request.getDescription(),
            request.getContent(), request.getLocation(), request.getLocationLat(),
            request.getLocationLng(), request.getStartTime(), request.getEndTime(),
            request.getMaxParticipants(), request.getRequiredSkills(),
            request.getContactName(), request.getContactPhone(), request.getContactEmail(),
            request.getCoverImage(), request.getAttachments()
        );
        return Result.success("活动更新成功", activity);
    }

    @DeleteMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> deleteActivity(Authentication authentication, @PathVariable Long id) {
        Long userId = Long.parseLong(authentication.getPrincipal().toString());
        volunteerActivityService.deleteActivity(id, userId);
        return Result.success("活动删除成功");
    }

    @PostMapping("/admin/{id}/publish")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> publishActivity(Authentication authentication, @PathVariable Long id) {
        Long userId = Long.parseLong(authentication.getPrincipal().toString());
        volunteerActivityService.publishActivity(id, userId);
        return Result.success("活动发布成功");
    }

    @PostMapping("/admin/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> cancelActivity(Authentication authentication, @PathVariable Long id) {
        Long userId = Long.parseLong(authentication.getPrincipal().toString());
        volunteerActivityService.cancelActivity(id, userId);
        return Result.success("活动已取消");
    }

    @PostMapping("/admin/{id}/start")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> startActivity(Authentication authentication, @PathVariable Long id) {
        Long userId = Long.parseLong(authentication.getPrincipal().toString());
        volunteerActivityService.startActivity(id, userId);
        return Result.success("活动已开始");
    }

    @PostMapping("/admin/{id}/end")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> endActivity(Authentication authentication, @PathVariable Long id) {
        Long userId = Long.parseLong(authentication.getPrincipal().toString());
        volunteerActivityService.endActivity(id, userId);
        return Result.success("活动已结束");
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Page<VolunteerActivityVO>> getAdminActivities(
            Authentication authentication,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = Long.parseLong(authentication.getPrincipal().toString());
        Page<VolunteerActivityVO> page = volunteerActivityService.getAdminActivities(
            userId, status, type, pageNum, pageSize);
        return Result.success(page);
    }

    @GetMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<VolunteerActivityVO> getAdminActivityDetail(
            Authentication authentication, @PathVariable Long id) {
        Long userId = Long.parseLong(authentication.getPrincipal().toString());
        VolunteerActivityVO activity = volunteerActivityService.getAdminActivityDetail(id, userId);
        return Result.success(activity);
    }

    // ==================== 志愿者功能 ====================

    @GetMapping
    public Result<Page<VolunteerActivityVO>> getActivities(
            Authentication authentication,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Long volunteerId = null;
        if (authentication != null) {
            volunteerId = Long.parseLong(authentication.getPrincipal().toString());
        }
        Page<VolunteerActivityVO> page = volunteerActivityService.getActivities(
            volunteerId, status, type, keyword, pageNum, pageSize);
        return Result.success(page);
    }

    @GetMapping("/{id}")
    public Result<VolunteerActivityVO> getActivityDetail(
            Authentication authentication, @PathVariable Long id) {
        Long volunteerId = null;
        if (authentication != null) {
            volunteerId = Long.parseLong(authentication.getPrincipal().toString());
        }
        VolunteerActivityVO activity = volunteerActivityService.getActivityDetail(id, volunteerId);
        return Result.success(activity);
    }

    @Data
    public static class JoinActivityRequest {
        private String applyReason;
    }

    @PostMapping("/{id}/join")
    @PreAuthorize("hasAnyRole('VOLUNTEER', 'ADMIN')")
    public Result<?> joinActivity(Authentication authentication,
                                  @PathVariable Long id,
                                  @RequestBody JoinActivityRequest request) {
        Long userId = Long.parseLong(authentication.getPrincipal().toString());
        volunteerActivityService.joinActivity(id, userId, request.getApplyReason());
        return Result.success("报名成功，等待审核");
    }

    @PostMapping("/{id}/quit")
    @PreAuthorize("hasAnyRole('VOLUNTEER', 'ADMIN')")
    public Result<?> quitActivity(Authentication authentication, @PathVariable Long id) {
        Long userId = Long.parseLong(authentication.getPrincipal().toString());
        volunteerActivityService.quitActivity(id, userId);
        return Result.success("已退出活动");
    }

    @PostMapping("/{id}/checkin")
    @PreAuthorize("hasAnyRole('VOLUNTEER', 'ADMIN')")
    public Result<?> volunteerCheckin(Authentication authentication, @PathVariable Long id) {
        Long userId = Long.parseLong(authentication.getPrincipal().toString());
        volunteerActivityService.volunteerCheckin(id, userId);
        return Result.success("签到成功");
    }

    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('VOLUNTEER', 'ADMIN')")
    public Result<Page<VolunteerActivityVO>> getMyActivities(
            Authentication authentication,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = Long.parseLong(authentication.getPrincipal().toString());
        Page<VolunteerActivityVO> page = volunteerActivityService.getMyActivities(
            userId, status, pageNum, pageSize);
        return Result.success(page);
    }

    // ==================== 参与管理功能 ====================

    @GetMapping("/admin/{id}/participants")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Page<VolunteerActivityParticipantVO>> getActivityParticipants(
            Authentication authentication,
            @PathVariable Long id,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = Long.parseLong(authentication.getPrincipal().toString());
        Page<VolunteerActivityParticipantVO> page = volunteerActivityService.getActivityParticipants(
            id, userId, status, pageNum, pageSize);
        return Result.success(page);
    }

    @Data
    public static class ApproveParticipantRequest {
        private String adminRemark;
    }

    @PostMapping("/admin/participants/{participantId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> approveParticipant(Authentication authentication,
                                        @PathVariable Long participantId,
                                        @RequestBody ApproveParticipantRequest request) {
        Long userId = Long.parseLong(authentication.getPrincipal().toString());
        volunteerActivityService.approveParticipant(participantId, userId, request.getAdminRemark());
        return Result.success("已通过申请");
    }

    @Data
    public static class RejectParticipantRequest {
        @NotBlank(message = "拒绝原因不能为空")
        private String rejectReason;
    }

    @PostMapping("/admin/participants/{participantId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> rejectParticipant(Authentication authentication,
                                       @PathVariable Long participantId,
                                       @Valid @RequestBody RejectParticipantRequest request) {
        Long userId = Long.parseLong(authentication.getPrincipal().toString());
        volunteerActivityService.rejectParticipant(participantId, userId, request.getRejectReason());
        return Result.success("已拒绝申请");
    }

    @PostMapping("/admin/participants/{participantId}/checkin")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> checkInParticipant(Authentication authentication, @PathVariable Long participantId) {
        Long userId = Long.parseLong(authentication.getPrincipal().toString());
        volunteerActivityService.checkInParticipant(participantId, userId);
        return Result.success("签到成功");
    }

    @Data
    public static class CheckOutRequest {
        @NotNull(message = "工作时长不能为空")
        private Double workHours;
    }

    @PostMapping("/admin/participants/{participantId}/checkout")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> checkOutParticipant(Authentication authentication,
                                         @PathVariable Long participantId,
                                         @Valid @RequestBody CheckOutRequest request) {
        Long userId = Long.parseLong(authentication.getPrincipal().toString());
        volunteerActivityService.checkOutParticipant(participantId, userId, request.getWorkHours());
        return Result.success("签退成功");
    }

    // ==================== 报告功能 ====================

    @Data
    public static class SubmitReportRequest {
        @NotBlank(message = "报告标题不能为空")
        private String title;

        @NotBlank(message = "报告内容不能为空")
        private String content;

        private String workContent;
        private String workResults;
        private String problemsEncountered;
        private String suggestions;
        private String photos;
        private String attachments;
    }

    @PostMapping("/{id}/report")
    @PreAuthorize("hasAnyRole('VOLUNTEER', 'ADMIN')")
    public Result<?> submitReport(Authentication authentication,
                                  @PathVariable Long id,
                                  @RequestParam("title") String title,
                                  @RequestParam("content") String content,
                                  @RequestParam(required = false) String workContent,
                                  @RequestParam(required = false) String workResults,
                                  @RequestParam(required = false) String problemsEncountered,
                                  @RequestParam(required = false) String suggestions,
                                  @RequestParam(required = false) MultipartFile[] photos,
                                  @RequestParam(required = false) String attachments) {
        try {
            Long userId = Long.parseLong(authentication.getPrincipal().toString());
            
            // 处理图片上传
            String photosUrls = null;
            if (photos != null && photos.length > 0) {
                photosUrls = volunteerActivityService.uploadPhotos(photos);
            }
            
            volunteerActivityService.submitReport(
                id, userId, title, content,
                workContent, workResults,
                problemsEncountered, suggestions,
                photosUrls, attachments
            );
            return Result.success("报告提交成功");
        } catch (Exception e) {
            return Result.error(500, e.getMessage() != null ? e.getMessage() : "提交报告失败");
        }
    }

    @GetMapping("/admin/{id}/reports")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Page<VolunteerActivityReportVO>> getActivityReports(
            Authentication authentication,
            @PathVariable Long id,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = Long.parseLong(authentication.getPrincipal().toString());
        Page<VolunteerActivityReportVO> page = volunteerActivityService.getActivityReports(
            id, userId, status, pageNum, pageSize);
        return Result.success(page);
    }

    @Data
    public static class ReviewReportRequest {
        private String reviewRemark;
    }

    @PostMapping("/admin/reports/{reportId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> approveReport(Authentication authentication,
                                   @PathVariable Long reportId,
                                   @RequestBody ReviewReportRequest request) {
        Long userId = Long.parseLong(authentication.getPrincipal().toString());
        volunteerActivityService.approveReport(reportId, userId, request.getReviewRemark());
        return Result.success("报告已通过");
    }

    @PostMapping("/admin/reports/{reportId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> rejectReport(Authentication authentication,
                                  @PathVariable Long reportId,
                                  @RequestBody ReviewReportRequest request) {
        Long userId = Long.parseLong(authentication.getPrincipal().toString());
        volunteerActivityService.rejectReport(reportId, userId, request.getReviewRemark());
        return Result.success("报告已驳回");
    }

    @GetMapping("/{id}/my-report")
    @PreAuthorize("hasAnyRole('VOLUNTEER', 'ADMIN')")
    public Result<VolunteerActivityReportVO> getMyReport(Authentication authentication, @PathVariable Long id) {
        Long userId = Long.parseLong(authentication.getPrincipal().toString());
        VolunteerActivityReportVO report = volunteerActivityService.getMyReport(id, userId);
        return Result.success(report);
    }
    
    @GetMapping("/admin/reports/{reportId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<VolunteerActivityReportVO> getReportDetail(Authentication authentication, @PathVariable Long reportId) {
        System.out.println("=== 获取报告详情 ===");
        System.out.println("报告ID: " + reportId);
        System.out.println("认证信息: " + authentication);
        System.out.println("用户ID: " + authentication.getPrincipal());
        System.out.println("权限: " + authentication.getAuthorities());
        
        Long userId = Long.parseLong(authentication.getPrincipal().toString());
        VolunteerActivityReportVO report = volunteerActivityService.getReportDetail(reportId, userId);
        return Result.success(report);
    }

    // ==================== 进度功能 ====================

    @Data
    public static class AddProgressRequest {
        @NotBlank(message = "进度标题不能为空")
        private String title;

        private String content;
        private String progressType;
        private Integer isImportant;
    }

    @PostMapping("/admin/{id}/progress")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<VolunteerActivityProgress> addProgress(Authentication authentication,
                                                         @PathVariable Long id,
                                                         @Valid @RequestBody AddProgressRequest request) {
        Long userId = Long.parseLong(authentication.getPrincipal().toString());
        VolunteerActivityProgress progress = volunteerActivityService.addProgress(
            id, userId, request.getTitle(), request.getContent(),
            request.getProgressType(), request.getIsImportant()
        );
        return Result.success("进度添加成功", progress);
    }

    @GetMapping("/{id}/progress")
    public Result<List<VolunteerActivityProgress>> getActivityProgress(@PathVariable Long id) {
        List<VolunteerActivityProgress> progressList = volunteerActivityService.getActivityProgress(id);
        return Result.success(progressList);
    }

    @DeleteMapping("/admin/progress/{progressId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> deleteProgress(Authentication authentication, @PathVariable Long progressId) {
        Long userId = Long.parseLong(authentication.getPrincipal().toString());
        volunteerActivityService.deleteProgress(progressId, userId);
        return Result.success("进度删除成功");
    }

    // ==================== 统计功能 ====================

    @GetMapping("/admin/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> getStatistics() {
        long totalActivities = volunteerActivityService.countByStatus(null);
        long recruitingActivities = volunteerActivityService.countByStatus(1);
        long inProgressActivities = volunteerActivityService.countByStatus(2);
        long endedActivities = volunteerActivityService.countByStatus(3);
        long pendingReports = volunteerActivityService.countReportsByStatus(0);

        return Result.success(new Object() {
            public final long total = totalActivities;
            public final long recruiting = recruitingActivities;
            public final long inProgress = inProgressActivities;
            public final long ended = endedActivities;
            public final long pendingReportsCount = pendingReports;
        });
    }
}
