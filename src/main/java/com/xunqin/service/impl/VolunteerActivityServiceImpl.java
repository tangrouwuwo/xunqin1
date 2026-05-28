package com.xunqin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xunqin.common.constant.VolunteerActivityConstant;
import com.xunqin.common.exception.BusinessException;
import com.xunqin.entity.*;
import com.xunqin.mapper.*;
import com.xunqin.service.VolunteerActivityService;
import com.xunqin.service.NotificationService;
import com.xunqin.vo.VolunteerActivityVO;
import com.xunqin.vo.VolunteerActivityParticipantVO;
import com.xunqin.vo.VolunteerActivityReportVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class VolunteerActivityServiceImpl implements VolunteerActivityService {

    @Autowired
    private VolunteerActivityMapper activityMapper;

    @Autowired
    private VolunteerActivityParticipantMapper participantMapper;

    @Autowired
    private VolunteerActivityReportMapper reportMapper;

    @Autowired
    private VolunteerActivityProgressMapper progressMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private com.xunqin.service.UserService userService;

    // ==================== 管理员功能 ====================

    @Override
    public String uploadCoverImage(MultipartFile coverImage) {
        if (coverImage == null || coverImage.isEmpty()) {
            return null;
        }

        String uploadDir = System.getProperty("user.dir") + "/uploads/volunteer-activities/";
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        try {
            String originalFilename = coverImage.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String newFilename = UUID.randomUUID().toString() + extension;
            File dest = new File(uploadDir + newFilename);
            coverImage.transferTo(dest);
            return "/uploads/volunteer-activities/" + newFilename;
        } catch (IOException e) {
            throw new BusinessException("封面图片上传失败: " + e.getMessage());
        }
    }
    
    @Override
    public String uploadPhotos(MultipartFile[] photos) {
        if (photos == null || photos.length == 0) {
            return null;
        }

        String uploadDir = System.getProperty("user.dir") + "/uploads/volunteer-activities/reports/";
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        List<String> photoUrls = new ArrayList<>();
        try {
            for (MultipartFile photo : photos) {
                if (!photo.isEmpty()) {
                    String originalFilename = photo.getOriginalFilename();
                    String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                    String newFilename = UUID.randomUUID().toString() + extension;
                    File dest = new File(uploadDir + newFilename);
                    photo.transferTo(dest);
                    photoUrls.add("/uploads/volunteer-activities/reports/" + newFilename);
                }
            }
            return String.join(",", photoUrls);
        } catch (IOException e) {
            throw new BusinessException("图片上传失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public VolunteerActivity createActivity(Long publisherId, String title, String type, String description,
                                           String content, String location, Double locationLat, Double locationLng,
                                           LocalDateTime startTime, LocalDateTime endTime, Integer maxParticipants,
                                           String requiredSkills, String contactName, String contactPhone,
                                           String contactEmail, String coverImage, String attachments) {
        validateActivityType(type);
        validateActivityTime(startTime, endTime);

        VolunteerActivity activity = new VolunteerActivity();
        activity.setTitle(title);
        activity.setType(type);
        activity.setDescription(description);
        activity.setContent(content);
        activity.setLocation(location);
        activity.setLocationLat(locationLat);
        activity.setLocationLng(locationLng);
        activity.setStartTime(startTime);
        activity.setEndTime(endTime);
        activity.setMaxParticipants(maxParticipants);
        activity.setCurrentParticipants(0);
        activity.setRequiredSkills(requiredSkills);
        activity.setContactName(contactName);
        activity.setContactPhone(contactPhone);
        activity.setContactEmail(contactEmail);
        activity.setCoverImage(coverImage);
        activity.setAttachments(attachments);
        activity.setPublisherId(publisherId);
        activity.setStatus(VolunteerActivityConstant.ACTIVITY_STATUS_DRAFT);
        activity.setViewCount(0);

        activityMapper.insert(activity);
        return activity;
    }

    @Override
    @Transactional
    public VolunteerActivity updateActivity(Long activityId, Long publisherId, String title, String type,
                                           String description, String content, String location, Double locationLat,
                                           Double locationLng, LocalDateTime startTime, LocalDateTime endTime,
                                           Integer maxParticipants, String requiredSkills, String contactName,
                                           String contactPhone, String contactEmail, String coverImage, String attachments) {
        VolunteerActivity activity = activityMapper.selectById(activityId);
        if (activity == null || activity.getIsDeleted() == 1) {
            throw new BusinessException("活动不存在");
        }
        if (!activity.getPublisherId().equals(publisherId)) {
            throw new BusinessException("无权修改此活动");
        }
        if (activity.getStatus() == VolunteerActivityConstant.ACTIVITY_STATUS_ENDED ||
            activity.getStatus() == VolunteerActivityConstant.ACTIVITY_STATUS_CANCELLED) {
            throw new BusinessException("已结束或已取消的活动不能修改");
        }

        if (type != null) {
            validateActivityType(type);
            activity.setType(type);
        }
        if (startTime != null && endTime != null) {
            validateActivityTime(startTime, endTime);
            activity.setStartTime(startTime);
            activity.setEndTime(endTime);
        }

        if (title != null) activity.setTitle(title);
        if (description != null) activity.setDescription(description);
        if (content != null) activity.setContent(content);
        if (location != null) activity.setLocation(location);
        if (locationLat != null) activity.setLocationLat(locationLat);
        if (locationLng != null) activity.setLocationLng(locationLng);
        if (maxParticipants != null) activity.setMaxParticipants(maxParticipants);
        if (requiredSkills != null) activity.setRequiredSkills(requiredSkills);
        if (contactName != null) activity.setContactName(contactName);
        if (contactPhone != null) activity.setContactPhone(contactPhone);
        if (contactEmail != null) activity.setContactEmail(contactEmail);
        if (coverImage != null) activity.setCoverImage(coverImage);
        if (attachments != null) activity.setAttachments(attachments);

        activityMapper.updateById(activity);
        return activity;
    }

    @Override
    @Transactional
    public void deleteActivity(Long activityId, Long publisherId) {
        VolunteerActivity activity = activityMapper.selectById(activityId);
        if (activity == null || activity.getIsDeleted() == 1) {
            throw new BusinessException("活动不存在");
        }

        // 删除关联的封面图片文件
        String coverImage = activity.getCoverImage();
        if (coverImage != null && !coverImage.isEmpty()) {
            deleteCoverImage(coverImage);
        }

        // 删除关联的参与记录（物理删除）
        participantMapper.deletePhysicalByActivityId(activityId);
        // 先查出参与记录ID以删除关联的报告
        QueryWrapper<VolunteerActivityReport> reportWrapper = new QueryWrapper<>();
        reportWrapper.apply("participant_id IN (SELECT id FROM volunteer_activity_participant WHERE activity_id = {0})", activityId);
        List<VolunteerActivityReport> reports = reportMapper.selectList(reportWrapper);
        for (VolunteerActivityReport report : reports) {
            reportMapper.physicalDeleteById(report.getId());
        }
        // 删除关联的进度记录（物理删除）
        progressMapper.deletePhysicalByActivityId(activityId);

        // 物理删除活动本身
        activityMapper.deleteByIdPhysical(activityId);
    }

    /**
     * 删除封面图片文件
     */
    private void deleteCoverImage(String coverImage) {
        try {
            // 从URL中提取文件路径
            String filePath = coverImage;
            if (coverImage.startsWith("/uploads/")) {
                filePath = System.getProperty("user.dir") + coverImage;
            }
            
            java.nio.file.Path path = java.nio.file.Paths.get(filePath);
            if (java.nio.file.Files.exists(path)) {
                java.nio.file.Files.delete(path);
                System.out.println("删除封面图片文件: " + filePath);
            }
        } catch (IOException e) {
            System.err.println("删除封面图片文件失败: " + coverImage + ", 错误: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void publishActivity(Long activityId, Long publisherId) {
        VolunteerActivity activity = activityMapper.selectById(activityId);
        if (activity == null || activity.getIsDeleted() == 1) {
            throw new BusinessException("活动不存在");
        }
        if (!activity.getPublisherId().equals(publisherId)) {
            throw new BusinessException("无权操作此活动");
        }
        if (activity.getStatus() != VolunteerActivityConstant.ACTIVITY_STATUS_DRAFT) {
            throw new BusinessException("只有草稿状态的活动可以发布");
        }

        activity.setStatus(VolunteerActivityConstant.ACTIVITY_STATUS_RECRUITING);
        activityMapper.updateById(activity);

        // 通知所有志愿者有新活动发布
        notifyAllVolunteers(activity);

        // 通知管理员活动已发布
        notifyAdminActivityPublished(activity);
    }

    @Override
    @Transactional
    public void cancelActivity(Long activityId, Long publisherId) {
        VolunteerActivity activity = activityMapper.selectById(activityId);
        if (activity == null || activity.getIsDeleted() == 1) {
            throw new BusinessException("活动不存在");
        }
        if (!activity.getPublisherId().equals(publisherId)) {
            throw new BusinessException("无权操作此活动");
        }
        if (activity.getStatus() == VolunteerActivityConstant.ACTIVITY_STATUS_ENDED ||
            activity.getStatus() == VolunteerActivityConstant.ACTIVITY_STATUS_CANCELLED) {
            throw new BusinessException("活动已结束或已取消");
        }

        activity.setStatus(VolunteerActivityConstant.ACTIVITY_STATUS_CANCELLED);
        activityMapper.updateById(activity);

        // 通知已报名的志愿者
        notifyParticipantsActivityCancelled(activity);
    }

    @Override
    @Transactional
    public void startActivity(Long activityId, Long publisherId) {
        VolunteerActivity activity = activityMapper.selectById(activityId);
        if (activity == null || activity.getIsDeleted() == 1) {
            throw new BusinessException("活动不存在");
        }
        if (!activity.getPublisherId().equals(publisherId)) {
            throw new BusinessException("无权操作此活动");
        }
        if (activity.getStatus() != VolunteerActivityConstant.ACTIVITY_STATUS_RECRUITING) {
            throw new BusinessException("只有招募中的活动可以开始");
        }

        activity.setStatus(VolunteerActivityConstant.ACTIVITY_STATUS_IN_PROGRESS);
        activityMapper.updateById(activity);

        // 通知已报名的志愿者活动已开始
        notifyParticipantsActivityStarted(activity);
    }

    @Override
    @Transactional
    public void endActivity(Long activityId, Long publisherId) {
        VolunteerActivity activity = activityMapper.selectById(activityId);
        if (activity == null || activity.getIsDeleted() == 1) {
            throw new BusinessException("活动不存在");
        }
        if (!activity.getPublisherId().equals(publisherId)) {
            throw new BusinessException("无权操作此活动");
        }
        if (activity.getStatus() != VolunteerActivityConstant.ACTIVITY_STATUS_IN_PROGRESS) {
            throw new BusinessException("只有进行中的活动可以结束");
        }

        activity.setStatus(VolunteerActivityConstant.ACTIVITY_STATUS_ENDED);
        activityMapper.updateById(activity);

        // 通知已报名的志愿者
        notifyParticipantsActivityEnded(activity);
    }

    @Override
    public Page<VolunteerActivityVO> getAdminActivities(Long publisherId, Integer status, String type,
                                                         Integer pageNum, Integer pageSize) {
        Page<VolunteerActivity> page = new Page<>(pageNum, pageSize);
        QueryWrapper<VolunteerActivity> wrapper = new QueryWrapper<>();
        wrapper.eq("publisher_id", publisherId);

        if (status != null) {
            wrapper.eq("status", status);
        }
        if (type != null) {
            wrapper.eq("type", type);
        }

        wrapper.orderByDesc("create_time");
        Page<VolunteerActivity> activityPage = activityMapper.selectPage(page, wrapper);

        return convertToActivityVOPage(activityPage, pageNum, pageSize);
    }

    @Override
    public VolunteerActivityVO getAdminActivityDetail(Long activityId, Long publisherId) {
        VolunteerActivity activity = activityMapper.selectById(activityId);
        if (activity == null || activity.getIsDeleted() == 1) {
            throw new BusinessException("活动不存在");
        }
        if (!activity.getPublisherId().equals(publisherId)) {
            throw new BusinessException("无权查看此活动");
        }

        return convertToActivityVO(activity, null);
    }

    // ==================== 志愿者功能 ====================

    @Override
    public Page<VolunteerActivityVO> getActivities(Long volunteerId, Integer status, String type,
                                                    String keyword, Integer pageNum, Integer pageSize) {
        Page<VolunteerActivity> page = new Page<>(pageNum, pageSize);
        QueryWrapper<VolunteerActivity> wrapper = new QueryWrapper<>();

        // 只显示已发布、进行中、已结束的活动
        wrapper.in("status", Arrays.asList(
            VolunteerActivityConstant.ACTIVITY_STATUS_RECRUITING,
            VolunteerActivityConstant.ACTIVITY_STATUS_IN_PROGRESS,
            VolunteerActivityConstant.ACTIVITY_STATUS_ENDED
        ));

        if (status != null) {
            wrapper.eq("status", status);
        }
        if (type != null) {
            wrapper.eq("type", type);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like("title", keyword).or().like("description", keyword));
        }

        wrapper.orderByDesc("create_time");
        Page<VolunteerActivity> activityPage = activityMapper.selectPage(page, wrapper);

        return convertToActivityVOPage(activityPage, pageNum, pageSize, volunteerId);
    }

    @Override
    public VolunteerActivityVO getActivityDetail(Long activityId, Long volunteerId) {
        VolunteerActivity activity = activityMapper.selectById(activityId);
        if (activity == null || activity.getIsDeleted() == 1) {
            throw new BusinessException("活动不存在");
        }

        // 增加浏览次数
        activityMapper.incrementViewCount(activityId);

        VolunteerActivityVO vo = convertToActivityVO(activity, volunteerId);

        // 检查当前志愿者是否已参与
        if (volunteerId != null) {
            VolunteerActivityParticipant participant = participantMapper
                .selectByActivityAndVolunteer(activityId, volunteerId);
            if (participant != null) {
                vo.setHasParticipated(true);
                vo.setParticipantStatus(participant.getStatus());
                vo.setParticipantId(participant.getId());
                vo.setCanReport(participant.getStatus() == VolunteerActivityConstant.PARTICIPANT_STATUS_APPROVED &&
                               participant.getIsReported() == 0);
                vo.setMyWorkHours(participant.getWorkHours());
            } else {
                vo.setHasParticipated(false);
                vo.setCanReport(false);
            }
        }

        return vo;
    }

    @Override
    @Transactional
    public void joinActivity(Long activityId, Long volunteerId, String applyReason) {
        VolunteerActivity activity = activityMapper.selectById(activityId);
        if (activity == null || activity.getIsDeleted() == 1) {
            throw new BusinessException("活动不存在");
        }
        if (activity.getStatus() != VolunteerActivityConstant.ACTIVITY_STATUS_RECRUITING) {
            throw new BusinessException("活动不在招募中，无法报名");
        }

        // 检查是否已报名
        VolunteerActivityParticipant existing = participantMapper
            .selectByActivityAndVolunteer(activityId, volunteerId);
        if (existing != null) {
            throw new BusinessException("您已报名此活动");
        }

        // 检查人数限制
        if (activity.getMaxParticipants() != null &&
            activity.getCurrentParticipants() >= activity.getMaxParticipants()) {
            throw new BusinessException("活动人数已满");
        }

        VolunteerActivityParticipant participant = new VolunteerActivityParticipant();
        participant.setActivityId(activityId);
        participant.setVolunteerId(volunteerId);
        participant.setStatus(VolunteerActivityConstant.PARTICIPANT_STATUS_PENDING);
        participant.setApplyReason(applyReason);
        participant.setIsReported(0);

        participantMapper.insert(participant);

        // 增加参与人数
        activityMapper.incrementParticipantCount(activityId);

        // 通知管理员
        notifyAdminNewParticipant(activity, volunteerId);
    }

    @Override
    @Transactional
    public void quitActivity(Long activityId, Long volunteerId) {
        VolunteerActivity activity = activityMapper.selectById(activityId);
        if (activity == null || activity.getIsDeleted() == 1) {
            throw new BusinessException("活动不存在");
        }

        VolunteerActivityParticipant participant = participantMapper
            .selectByActivityAndVolunteer(activityId, volunteerId);
        if (participant == null) {
            throw new BusinessException("您未报名此活动");
        }
        if (participant.getStatus() == VolunteerActivityConstant.PARTICIPANT_STATUS_QUIT) {
            throw new BusinessException("您已退出此活动");
        }

        participant.setStatus(VolunteerActivityConstant.PARTICIPANT_STATUS_QUIT);
        participantMapper.updateById(participant);

        // 减少参与人数
        activityMapper.decrementParticipantCount(activityId);
    }

    @Override
    public Page<VolunteerActivityVO> getMyActivities(Long volunteerId, Integer status, Integer pageNum, Integer pageSize) {
        Page<VolunteerActivityParticipant> page = new Page<>(pageNum, pageSize);
        QueryWrapper<VolunteerActivityParticipant> wrapper = new QueryWrapper<>();
        wrapper.eq("volunteer_id", volunteerId);
        wrapper.ne("status", VolunteerActivityConstant.PARTICIPANT_STATUS_QUIT);

        if (status != null) {
            wrapper.eq("status", status);
        }

        wrapper.orderByDesc("create_time");
        Page<VolunteerActivityParticipant> participantPage = participantMapper.selectPage(page, wrapper);

        List<VolunteerActivityVO> voList = participantPage.getRecords().stream().map(participant -> {
            VolunteerActivity activity = activityMapper.selectById(participant.getActivityId());
            if (activity == null || activity.getIsDeleted() == 1) {
                return null;
            }
            VolunteerActivityVO vo = convertToActivityVO(activity, volunteerId);
            vo.setParticipantStatus(participant.getStatus());
            vo.setParticipantId(participant.getId());
            vo.setCanReport(participant.getStatus() == VolunteerActivityConstant.PARTICIPANT_STATUS_APPROVED &&
                           participant.getIsReported() == 0);
            vo.setMyWorkHours(participant.getWorkHours());
            return vo;
        }).filter(vo -> vo != null).collect(Collectors.toList());

        Page<VolunteerActivityVO> voPage = new Page<>(pageNum, pageSize);
        voPage.setRecords(voList);
        voPage.setTotal(participantPage.getTotal());
        return voPage;
    }

    @Override
    @Transactional
    public void volunteerCheckin(Long activityId, Long volunteerId) {
        VolunteerActivityParticipant participant = participantMapper
            .selectByActivityAndVolunteer(activityId, volunteerId);
        if (participant == null) {
            throw new BusinessException("您未参与此活动");
        }
        if (participant.getStatus() != VolunteerActivityConstant.PARTICIPANT_STATUS_APPROVED) {
            throw new BusinessException("您的参与申请尚未通过审核，无法签到");
        }

        VolunteerActivity activity = activityMapper.selectById(activityId);
        if (activity == null || activity.getIsDeleted() == 1) {
            throw new BusinessException("活动不存在");
        }
        if (activity.getStatus() != VolunteerActivityConstant.ACTIVITY_STATUS_IN_PROGRESS) {
            throw new BusinessException("活动未在进行中，无法签到");
        }

        participant.setCheckInTime(LocalDateTime.now());
        participantMapper.updateById(participant);

        notifyAdminCheckIn(activity, volunteerId);
    }

    // ==================== 参与管理功能 ====================

    @Override
    public Page<VolunteerActivityParticipantVO> getActivityParticipants(Long activityId, Long adminId,
                                                                         Integer status, Integer pageNum, Integer pageSize) {
        VolunteerActivity activity = activityMapper.selectById(activityId);
        if (activity == null || activity.getIsDeleted() == 1) {
            throw new BusinessException("活动不存在");
        }
        if (!activity.getPublisherId().equals(adminId)) {
            throw new BusinessException("无权查看此活动的参与者");
        }

        Page<VolunteerActivityParticipant> page = new Page<>(pageNum, pageSize);
        QueryWrapper<VolunteerActivityParticipant> wrapper = new QueryWrapper<>();
        wrapper.eq("activity_id", activityId);

        if (status != null) {
            wrapper.eq("status", status);
        }

        wrapper.orderByDesc("create_time");
        Page<VolunteerActivityParticipant> participantPage = participantMapper.selectPage(page, wrapper);

        List<VolunteerActivityParticipantVO> voList = participantPage.getRecords().stream().map(participant -> {
            User volunteer = userMapper.selectById(participant.getVolunteerId());
            String volunteerName = volunteer != null ? volunteer.getNickname() : "未知";
            String volunteerAvatar = volunteer != null ? volunteer.getAvatar() : null;

            VolunteerActivityParticipantVO vo = new VolunteerActivityParticipantVO();
            vo.setId(participant.getId());
            vo.setActivityId(participant.getActivityId());
            vo.setVolunteerId(participant.getVolunteerId());
            vo.setStatus(participant.getStatus());
            vo.setApplyReason(participant.getApplyReason());
            vo.setRejectReason(participant.getRejectReason());
            vo.setCheckInTime(participant.getCheckInTime());
            vo.setCheckOutTime(participant.getCheckOutTime());
            vo.setWorkHours(participant.getWorkHours());
            vo.setAdminRemark(participant.getAdminRemark());
            vo.setIsReported(participant.getIsReported());
            vo.setCreateTime(participant.getCreateTime());
            vo.setUpdateTime(participant.getUpdateTime());
            vo.setVolunteerName(volunteerName);
            vo.setVolunteerAvatar(volunteerAvatar);
            vo.setActivityTitle(activity.getTitle());
            vo.setActivityType(activity.getType());
            vo.setVolunteerPhone(volunteer != null ? volunteer.getPhone() : null);
            vo.setVolunteerEmail(volunteer != null ? volunteer.getEmail() : null);
            vo.setHasReport(participant.getIsReported() == 1);

            return vo;
        }).collect(Collectors.toList());

        Page<VolunteerActivityParticipantVO> voPage = new Page<>(pageNum, pageSize);
        voPage.setRecords(voList);
        voPage.setTotal(participantPage.getTotal());
        return voPage;
    }

    @Override
    @Transactional
    public void approveParticipant(Long participantId, Long adminId, String adminRemark) {
        VolunteerActivityParticipant participant = participantMapper.selectById(participantId);
        if (participant == null || participant.getIsDeleted() == 1) {
            throw new BusinessException("参与者记录不存在");
        }

        VolunteerActivity activity = activityMapper.selectById(participant.getActivityId());
        if (activity == null || activity.getIsDeleted() == 1) {
            throw new BusinessException("活动不存在");
        }
        if (!activity.getPublisherId().equals(adminId)) {
            throw new BusinessException("无权操作");
        }

        participant.setStatus(VolunteerActivityConstant.PARTICIPANT_STATUS_APPROVED);
        participant.setAdminRemark(adminRemark);
        participantMapper.updateById(participant);

        // 通知志愿者
        notifyVolunteerApplicationApproved(activity, participant.getVolunteerId());
    }

    @Override
    @Transactional
    public void rejectParticipant(Long participantId, Long adminId, String rejectReason) {
        VolunteerActivityParticipant participant = participantMapper.selectById(participantId);
        if (participant == null || participant.getIsDeleted() == 1) {
            throw new BusinessException("参与者记录不存在");
        }

        VolunteerActivity activity = activityMapper.selectById(participant.getActivityId());
        if (activity == null || activity.getIsDeleted() == 1) {
            throw new BusinessException("活动不存在");
        }
        if (!activity.getPublisherId().equals(adminId)) {
            throw new BusinessException("无权操作");
        }

        participant.setStatus(VolunteerActivityConstant.PARTICIPANT_STATUS_REJECTED);
        participant.setRejectReason(rejectReason);
        participantMapper.updateById(participant);

        // 减少参与人数
        activityMapper.decrementParticipantCount(activity.getId());

        // 通知志愿者
        notifyVolunteerApplicationRejected(activity, participant.getVolunteerId(), rejectReason);
    }

    @Override
    @Transactional
    public void checkInParticipant(Long participantId, Long adminId) {
        VolunteerActivityParticipant participant = participantMapper.selectById(participantId);
        if (participant == null || participant.getIsDeleted() == 1) {
            throw new BusinessException("参与者记录不存在");
        }

        VolunteerActivity activity = activityMapper.selectById(participant.getActivityId());
        if (activity == null || activity.getIsDeleted() == 1) {
            throw new BusinessException("活动不存在");
        }
        if (!activity.getPublisherId().equals(adminId)) {
            throw new BusinessException("无权操作");
        }

        participant.setCheckInTime(LocalDateTime.now());
        participantMapper.updateById(participant);

        // 通知管理员签到成功
        notifyAdminCheckIn(activity, participant.getVolunteerId());
    }

    @Override
    @Transactional
    public void checkOutParticipant(Long participantId, Long adminId, Double workHours) {
        VolunteerActivityParticipant participant = participantMapper.selectById(participantId);
        if (participant == null || participant.getIsDeleted() == 1) {
            throw new BusinessException("参与者记录不存在");
        }

        VolunteerActivity activity = activityMapper.selectById(participant.getActivityId());
        if (activity == null || activity.getIsDeleted() == 1) {
            throw new BusinessException("活动不存在");
        }
        if (!activity.getPublisherId().equals(adminId)) {
            throw new BusinessException("无权操作");
        }

        participant.setCheckOutTime(LocalDateTime.now());
        participant.setWorkHours(java.math.BigDecimal.valueOf(workHours));
        participantMapper.updateById(participant);
    }

    // ==================== 报告功能 ====================

    @Override
    @Transactional
    public void submitReport(Long activityId, Long volunteerId, String title, String content,
                            String workContent, String workResults, String problemsEncountered,
                            String suggestions, String photos, String attachments) {
        VolunteerActivity activity = activityMapper.selectById(activityId);
        if (activity == null || activity.getIsDeleted() == 1) {
            throw new BusinessException("活动不存在");
        }

        VolunteerActivityParticipant participant = participantMapper
            .selectByActivityAndVolunteer(activityId, volunteerId);
        if (participant == null) {
            throw new BusinessException("您未参与此活动");
        }
        if (participant.getStatus() != VolunteerActivityConstant.PARTICIPANT_STATUS_APPROVED) {
            throw new BusinessException("您的参与申请尚未通过审核");
        }
        if (participant.getIsReported() == 1) {
            throw new BusinessException("您已提交过报告");
        }

        VolunteerActivityReport report = new VolunteerActivityReport();
        report.setActivityId(activityId);
        report.setParticipantId(participant.getId());
        report.setVolunteerId(volunteerId);
        report.setTitle(title);
        report.setContent(content);
        report.setWorkContent(workContent);
        report.setWorkResults(workResults);
        report.setProblemsEncountered(problemsEncountered);
        report.setSuggestions(suggestions);
        report.setPhotos(photos);
        report.setAttachments(attachments);
        report.setStatus(VolunteerActivityConstant.REPORT_STATUS_PENDING);

        reportMapper.insert(report);

        // 标记参与者已提交报告
        participantMapper.markAsReported(participant.getId());

        // 通知管理员
        notifyAdminNewReport(activity, volunteerId);
    }

    @Override
    public Page<VolunteerActivityReportVO> getActivityReports(Long activityId, Long adminId,
                                                               Integer status, Integer pageNum, Integer pageSize) {
        VolunteerActivity activity = activityMapper.selectById(activityId);
        if (activity == null || activity.getIsDeleted() == 1) {
            throw new BusinessException("活动不存在");
        }
        // 所有管理员都可以查看报告
        // 移除了只允许活动发布者查看的限制

        Page<VolunteerActivityReport> page = new Page<>(pageNum, pageSize);
        QueryWrapper<VolunteerActivityReport> wrapper = new QueryWrapper<>();
        wrapper.eq("activity_id", activityId);

        if (status != null) {
            wrapper.eq("status", status);
        }

        wrapper.orderByDesc("create_time");
        Page<VolunteerActivityReport> reportPage = reportMapper.selectPage(page, wrapper);

        List<VolunteerActivityReportVO> voList = reportPage.getRecords().stream().map(report -> {
            User volunteer = userMapper.selectById(report.getVolunteerId());
            String volunteerName = volunteer != null ? volunteer.getNickname() : "未知";
            String volunteerAvatar = volunteer != null ? volunteer.getAvatar() : null;

            User reviewer = report.getReviewerId() != null ? userMapper.selectById(report.getReviewerId()) : null;
            String reviewerName = reviewer != null ? reviewer.getNickname() : null;

            VolunteerActivityReportVO vo = new VolunteerActivityReportVO();
            vo.setId(report.getId());
            vo.setActivityId(report.getActivityId());
            vo.setParticipantId(report.getParticipantId());
            vo.setVolunteerId(report.getVolunteerId());
            vo.setTitle(report.getTitle());
            vo.setContent(report.getContent());
            vo.setWorkContent(report.getWorkContent());
            vo.setWorkResults(report.getWorkResults());
            vo.setProblemsEncountered(report.getProblemsEncountered());
            vo.setSuggestions(report.getSuggestions());
            vo.setPhotos(report.getPhotos());
            vo.setAttachments(report.getAttachments());
            vo.setStatus(report.getStatus());
            vo.setReviewRemark(report.getReviewRemark());
            vo.setReviewTime(report.getReviewTime());
            vo.setReviewerId(report.getReviewerId());
            vo.setCreateTime(report.getCreateTime());
            vo.setUpdateTime(report.getUpdateTime());
            vo.setVolunteerName(volunteerName);
            vo.setVolunteerAvatar(volunteerAvatar);
            vo.setActivityTitle(activity.getTitle());
            vo.setActivityType(activity.getType());
            vo.setReviewerName(reviewerName);

            return vo;
        }).collect(Collectors.toList());

        Page<VolunteerActivityReportVO> voPage = new Page<>(pageNum, pageSize);
        voPage.setRecords(voList);
        voPage.setTotal(reportPage.getTotal());
        return voPage;
    }

    @Override
    @Transactional
    public void approveReport(Long reportId, Long adminId, String reviewRemark) {
        VolunteerActivityReport report = reportMapper.selectById(reportId);
        if (report == null || report.getIsDeleted() == 1) {
            throw new BusinessException("报告不存在");
        }

        VolunteerActivity activity = activityMapper.selectById(report.getActivityId());
        if (activity == null || activity.getIsDeleted() == 1) {
            throw new BusinessException("活动不存在");
        }
        // 所有管理员都可以审核报告
        // 移除了只允许活动发布者操作的限制

        report.setStatus(VolunteerActivityConstant.REPORT_STATUS_APPROVED);
        report.setReviewRemark(reviewRemark);
        report.setReviewTime(LocalDateTime.now());
        report.setReviewerId(adminId);
        reportMapper.updateById(report);

        // 通知志愿者
        notifyVolunteerReportApproved(activity, report.getVolunteerId());
    }

    @Override
    @Transactional
    public void rejectReport(Long reportId, Long adminId, String reviewRemark) {
        VolunteerActivityReport report = reportMapper.selectById(reportId);
        if (report == null || report.getIsDeleted() == 1) {
            throw new BusinessException("报告不存在");
        }

        VolunteerActivity activity = activityMapper.selectById(report.getActivityId());
        if (activity == null || activity.getIsDeleted() == 1) {
            throw new BusinessException("活动不存在");
        }
        // 所有管理员都可以驳回报告
        // 移除了只允许活动发布者操作的限制

        report.setStatus(VolunteerActivityConstant.REPORT_STATUS_REJECTED);
        report.setReviewRemark(reviewRemark);
        report.setReviewTime(LocalDateTime.now());
        report.setReviewerId(adminId);
        reportMapper.updateById(report);

        // 重置参与者的报告状态，允许重新提交
        VolunteerActivityParticipant participant = participantMapper.selectById(report.getParticipantId());
        if (participant != null) {
            participant.setIsReported(0);
            participantMapper.updateById(participant);
        }

        // 通知志愿者
        notifyVolunteerReportRejected(activity, report.getVolunteerId(), reviewRemark);
    }

    @Override
    public VolunteerActivityReportVO getMyReport(Long activityId, Long volunteerId) {
        VolunteerActivityParticipant participant = participantMapper
            .selectByActivityAndVolunteer(activityId, volunteerId);
        if (participant == null) {
            throw new BusinessException("您未参与此活动");
        }

        VolunteerActivityReport report = reportMapper.selectByParticipantId(participant.getId());
        if (report == null) {
            return null;
        }

        VolunteerActivity activity = activityMapper.selectById(activityId);
        User volunteer = userMapper.selectById(volunteerId);
        User reviewer = report.getReviewerId() != null ? userMapper.selectById(report.getReviewerId()) : null;

        VolunteerActivityReportVO vo = new VolunteerActivityReportVO();
        vo.setId(report.getId());
        vo.setActivityId(report.getActivityId());
        vo.setParticipantId(report.getParticipantId());
        vo.setVolunteerId(report.getVolunteerId());
        vo.setTitle(report.getTitle());
        vo.setContent(report.getContent());
        vo.setWorkContent(report.getWorkContent());
        vo.setWorkResults(report.getWorkResults());
        vo.setProblemsEncountered(report.getProblemsEncountered());
        vo.setSuggestions(report.getSuggestions());
        vo.setPhotos(report.getPhotos());
        vo.setAttachments(report.getAttachments());
        vo.setStatus(report.getStatus());
        vo.setReviewRemark(report.getReviewRemark());
        vo.setReviewTime(report.getReviewTime());
        vo.setReviewerId(report.getReviewerId());
        vo.setCreateTime(report.getCreateTime());
        vo.setUpdateTime(report.getUpdateTime());
        vo.setVolunteerName(volunteer != null ? volunteer.getNickname() : "未知");
        vo.setVolunteerAvatar(volunteer != null ? volunteer.getAvatar() : null);
        vo.setActivityTitle(activity != null ? activity.getTitle() : null);
        vo.setActivityType(activity != null ? activity.getType() : null);
        vo.setReviewerName(reviewer != null ? reviewer.getNickname() : null);

        return vo;
    }
    
    @Override
    public VolunteerActivityReportVO getReportDetail(Long reportId, Long adminId) {
        // 直接根据报告ID查询报告
        VolunteerActivityReport report = reportMapper.selectById(reportId);
        if (report == null || report.getIsDeleted() == 1) {
            throw new BusinessException("报告不存在");
        }

        // 获取相关信息
        VolunteerActivity activity = activityMapper.selectById(report.getActivityId());
        User volunteer = userMapper.selectById(report.getVolunteerId());
        User reviewer = report.getReviewerId() != null ? userMapper.selectById(report.getReviewerId()) : null;

        // 构建VO对象
        VolunteerActivityReportVO vo = new VolunteerActivityReportVO();
        vo.setId(report.getId());
        vo.setActivityId(report.getActivityId());
        vo.setParticipantId(report.getParticipantId());
        vo.setVolunteerId(report.getVolunteerId());
        vo.setTitle(report.getTitle());
        vo.setContent(report.getContent());
        vo.setWorkContent(report.getWorkContent());
        vo.setWorkResults(report.getWorkResults());
        vo.setProblemsEncountered(report.getProblemsEncountered());
        vo.setSuggestions(report.getSuggestions());
        vo.setPhotos(report.getPhotos());
        vo.setAttachments(report.getAttachments());
        vo.setStatus(report.getStatus());
        vo.setReviewRemark(report.getReviewRemark());
        vo.setReviewTime(report.getReviewTime());
        vo.setReviewerId(report.getReviewerId());
        vo.setCreateTime(report.getCreateTime());
        vo.setUpdateTime(report.getUpdateTime());
        vo.setVolunteerName(volunteer != null ? volunteer.getNickname() : "未知");
        vo.setVolunteerAvatar(volunteer != null ? volunteer.getAvatar() : null);
        vo.setActivityTitle(activity != null ? activity.getTitle() : null);
        vo.setActivityType(activity != null ? activity.getType() : null);
        vo.setReviewerName(reviewer != null ? reviewer.getNickname() : null);

        return vo;
    }

    // ==================== 进度功能 ====================

    @Override
    @Transactional
    public VolunteerActivityProgress addProgress(Long activityId, Long publisherId, String title,
                                                  String content, String progressType, Integer isImportant) {
        VolunteerActivity activity = activityMapper.selectById(activityId);
        if (activity == null || activity.getIsDeleted() == 1) {
            throw new BusinessException("活动不存在");
        }
        if (!activity.getPublisherId().equals(publisherId)) {
            throw new BusinessException("无权操作");
        }

        VolunteerActivityProgress progress = new VolunteerActivityProgress();
        progress.setActivityId(activityId);
        progress.setTitle(title);
        progress.setContent(content);
        progress.setProgressType(progressType);
        progress.setPublisherId(publisherId);
        progress.setIsImportant(isImportant != null ? isImportant : 0);

        progressMapper.insert(progress);

        // 通知所有参与者
        notifyParticipantsNewProgress(activity, progress);

        return progress;
    }

    @Override
    public List<VolunteerActivityProgress> getActivityProgress(Long activityId) {
        return progressMapper.selectByActivityId(activityId);
    }

    @Override
    @Transactional
    public void deleteProgress(Long progressId, Long publisherId) {
        VolunteerActivityProgress progress = progressMapper.selectById(progressId);
        if (progress == null || progress.getIsDeleted() == 1) {
            throw new BusinessException("进度记录不存在");
        }

        VolunteerActivity activity = activityMapper.selectById(progress.getActivityId());
        if (activity == null || activity.getIsDeleted() == 1) {
            throw new BusinessException("活动不存在");
        }
        if (!activity.getPublisherId().equals(publisherId)) {
            throw new BusinessException("无权操作");
        }

        progressMapper.deleteById(progressId);
    }

    // ==================== 统计功能 ====================

    @Override
    public long countByStatus(Integer status) {
        QueryWrapper<VolunteerActivity> wrapper = new QueryWrapper<>();
        wrapper.eq("is_deleted", 0);
        if (status != null) {
            wrapper.eq("status", status);
        }
        return activityMapper.selectCount(wrapper);
    }

    @Override
    public long countParticipantsByActivity(Long activityId) {
        return participantMapper.countApprovedParticipants(activityId);
    }

    @Override
    public long countReportsByStatus(Integer status) {
        QueryWrapper<VolunteerActivityReport> wrapper = new QueryWrapper<>();
        wrapper.eq("is_deleted", 0);
        if (status != null) {
            wrapper.eq("status", status);
        }
        return reportMapper.selectCount(wrapper);
    }

    // ==================== 私有方法 ====================

    private void validateActivityType(String type) {
        if (!Arrays.asList(VolunteerActivityConstant.ALL_ACTIVITY_TYPES).contains(type)) {
            throw new BusinessException("无效的活动类型");
        }
    }

    private void validateActivityTime(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            throw new BusinessException("活动开始时间和结束时间不能为空");
        }
        if (endTime.isBefore(startTime)) {
            throw new BusinessException("结束时间不能早于开始时间");
        }
        if (startTime.isBefore(LocalDateTime.now())) {
            throw new BusinessException("开始时间不能早于当前时间");
        }
    }

    private Page<VolunteerActivityVO> convertToActivityVOPage(Page<VolunteerActivity> activityPage,
                                                               Integer pageNum, Integer pageSize) {
        return convertToActivityVOPage(activityPage, pageNum, pageSize, null);
    }

    private Page<VolunteerActivityVO> convertToActivityVOPage(Page<VolunteerActivity> activityPage,
                                                               Integer pageNum, Integer pageSize, Long volunteerId) {
        List<VolunteerActivityVO> voList = activityPage.getRecords().stream()
            .map(activity -> convertToActivityVO(activity, volunteerId))
            .collect(Collectors.toList());

        Page<VolunteerActivityVO> voPage = new Page<>(pageNum, pageSize);
        voPage.setRecords(voList);
        voPage.setTotal(activityPage.getTotal());
        voPage.setPages(activityPage.getPages());
        voPage.setCurrent(activityPage.getCurrent());
        voPage.setSize(activityPage.getSize());
        return voPage;
    }

    private VolunteerActivityVO convertToActivityVO(VolunteerActivity activity, Long volunteerId) {
        User publisher = userMapper.selectById(activity.getPublisherId());
        String publisherName = publisher != null ? publisher.getNickname() : "未知";
        String publisherAvatar = publisher != null ? publisher.getAvatar() : null;

        VolunteerActivityVO vo = new VolunteerActivityVO(activity, publisherName, publisherAvatar);
        vo.setTypeName(getActivityTypeName(activity.getType()));
        vo.setStatusName(getActivityStatusName(activity.getStatus()));
        vo.setParticipantCount(participantMapper.countApprovedParticipants(activity.getId()));

        if (volunteerId != null) {
            VolunteerActivityParticipant participant = participantMapper
                .selectByActivityAndVolunteer(activity.getId(), volunteerId);
            if (participant != null) {
                vo.setHasParticipated(true);
                vo.setParticipantStatus(participant.getStatus());
                vo.setParticipantId(participant.getId());
                vo.setCanReport(participant.getStatus() == VolunteerActivityConstant.PARTICIPANT_STATUS_APPROVED &&
                               participant.getIsReported() == 0);
                vo.setMyWorkHours(participant.getWorkHours());
            }
        }

        return vo;
    }

    private String getActivityTypeName(String type) {
        switch (type) {
            case VolunteerActivityConstant.ACTIVITY_TYPE_INFO_VERIFY:
                return "信息核实";
            case VolunteerActivityConstant.ACTIVITY_TYPE_CLUE_INVESTIGATE:
                return "线索调查";
            case VolunteerActivityConstant.ACTIVITY_TYPE_VOLUNTEER_RECRUIT:
                return "志愿者招募";
            case VolunteerActivityConstant.ACTIVITY_TYPE_COMMUNITY_PROMOTION:
                return "社区宣传";
            default:
                return "未知";
        }
    }

    private String getActivityStatusName(Integer status) {
        switch (status) {
            case VolunteerActivityConstant.ACTIVITY_STATUS_DRAFT:
                return "草稿";
            case VolunteerActivityConstant.ACTIVITY_STATUS_RECRUITING:
                return "招募中";
            case VolunteerActivityConstant.ACTIVITY_STATUS_IN_PROGRESS:
                return "进行中";
            case VolunteerActivityConstant.ACTIVITY_STATUS_ENDED:
                return "已结束";
            case VolunteerActivityConstant.ACTIVITY_STATUS_CANCELLED:
                return "已取消";
            default:
                return "未知";
        }
    }

    // ==================== 自动结束活动定时任务 ====================

    /**
     * 每分钟检查一次，自动结束已到结束时间的活动
     */
    @Scheduled(cron = "0 * * * * ?")
    public void autoTerminateActivities() {
        LocalDateTime now = LocalDateTime.now();
        QueryWrapper<VolunteerActivity> wrapper = new QueryWrapper<>();
        wrapper.eq("status", VolunteerActivityConstant.ACTIVITY_STATUS_IN_PROGRESS)
               .le("end_time", now);

        List<VolunteerActivity> activities = activityMapper.selectList(wrapper);
        for (VolunteerActivity activity : activities) {
            activity.setStatus(VolunteerActivityConstant.ACTIVITY_STATUS_ENDED);
            activityMapper.updateById(activity);
            
            // 通知所有参与者活动已结束
            notifyParticipantsActivityEnded(activity);
        }
    }

    // ==================== 通知方法 ====================

    private void notifyAllVolunteers(VolunteerActivity activity) {
        // 通知所有志愿者有新活动发布
        List<com.xunqin.entity.User> volunteers = userMapper.selectList(
            new QueryWrapper<com.xunqin.entity.User>().eq("role", "VOLUNTEER")
        );
        for (com.xunqin.entity.User volunteer : volunteers) {
            String title = "新活动发布";
            String content = "新活动\"" + activity.getTitle() + "\"已发布，快来报名参加吧！";
            notificationService.sendVolunteerNotification(volunteer.getId(), title, content,
                    "NEW_ACTIVITY", activity.getId(), "VOLUNTEER_ACTIVITY", activity.getId());
        }
    }

    private void notifyAdminActivityPublished(VolunteerActivity activity) {
        // 通知所有管理员活动已发布
        List<com.xunqin.entity.User> admins = userService.getAdmins();
        for (com.xunqin.entity.User admin : admins) {
            String title = "活动已发布";
            String content = "活动\"" + activity.getTitle() + "\"已成功发布，现在处于招募中状态。";
            notificationService.sendAdminNotification(admin.getId(), title, content,
                    "ACTIVITY_PUBLISHED", activity.getId(), "VOLUNTEER_ACTIVITY", 1);
        }
    }

    private void notifyAdminNewParticipant(VolunteerActivity activity, Long volunteerId) {
        // 通知所有管理员
        List<com.xunqin.entity.User> admins = userService.getAdmins();
        com.xunqin.entity.User volunteer = userMapper.selectById(volunteerId);
        String volunteerName = volunteer != null ? volunteer.getNickname() : "未知用户";
        
        for (com.xunqin.entity.User admin : admins) {
            String title = "新的活动报名";
            String content = "用户" + volunteerName + "报名了活动\"" + activity.getTitle() + "\"，需要审核。";
            notificationService.sendAdminNotification(admin.getId(), title, content, "VOLUNTEER_ACTIVITY_APPLICATION", activity.getId(), "VOLUNTEER_ACTIVITY", 1);
        }
    }

    private void notifyAdminCheckIn(VolunteerActivity activity, Long volunteerId) {
        // 通知所有管理员有志愿者签到
        List<com.xunqin.entity.User> admins = userService.getAdmins();
        com.xunqin.entity.User volunteer = userMapper.selectById(volunteerId);
        String volunteerName = volunteer != null ? volunteer.getNickname() : "未知用户";
        
        for (com.xunqin.entity.User admin : admins) {
            String title = "志愿者签到通知";
            String content = "用户" + volunteerName + "已在活动\"" + activity.getTitle() + "\"中签到。";
            notificationService.sendAdminNotification(admin.getId(), title, content,
                    "VOLUNTEER_CHECKIN", activity.getId(), "VOLUNTEER_ACTIVITY", 1);
        }
    }

    private void notifyVolunteerApplicationApproved(VolunteerActivity activity, Long volunteerId) {
        com.xunqin.entity.User volunteer = userMapper.selectById(volunteerId);
        if (volunteer != null) {
            String title = "报名审核通过";
            String content = "您在活动\"" + activity.getTitle() + "\"的报名申请已通过审核，请按时参加活动。";
            notificationService.sendVolunteerNotification(volunteerId, title, content,
                    "APPLICATION_APPROVED", activity.getId(), "VOLUNTEER_ACTIVITY", activity.getId());
        }
    }

    private void notifyVolunteerApplicationRejected(VolunteerActivity activity, Long volunteerId, String reason) {
        com.xunqin.entity.User volunteer = userMapper.selectById(volunteerId);
        if (volunteer != null) {
            String title = "报名审核未通过";
            String content = "您在活动\"" + activity.getTitle() + "\"的报名申请未通过审核。原因：" + reason;
            notificationService.sendVolunteerNotification(volunteerId, title, content,
                    "APPLICATION_REJECTED", activity.getId(), "VOLUNTEER_ACTIVITY", activity.getId());
        }
    }

    private void notifyParticipantsActivityCancelled(VolunteerActivity activity) {
        // 查询所有已审核通过的参与者
        QueryWrapper<VolunteerActivityParticipant> wrapper = new QueryWrapper<>();
        wrapper.eq("activity_id", activity.getId())
               .eq("status", VolunteerActivityConstant.PARTICIPANT_STATUS_APPROVED);
        List<VolunteerActivityParticipant> participants = participantMapper.selectList(wrapper);
        
        for (VolunteerActivityParticipant participant : participants) {
            String title = "活动已取消";
            String content = "您报名的活动\"" + activity.getTitle() + "\"已被管理员取消。";
            notificationService.sendVolunteerNotification(participant.getVolunteerId(), title, content,
                    "ACTIVITY_CANCELLED", activity.getId(), "VOLUNTEER_ACTIVITY", activity.getId());
        }
    }

    private void notifyParticipantsActivityStarted(VolunteerActivity activity) {
        // 查询所有已审核通过的参与者
        QueryWrapper<VolunteerActivityParticipant> wrapper = new QueryWrapper<>();
        wrapper.eq("activity_id", activity.getId())
               .eq("status", VolunteerActivityConstant.PARTICIPANT_STATUS_APPROVED);
        List<VolunteerActivityParticipant> participants = participantMapper.selectList(wrapper);
        
        for (VolunteerActivityParticipant participant : participants) {
            String title = "活动已开始";
            String content = "您报名的活动\"" + activity.getTitle() + "\"已经开始，请按时参加。";
            notificationService.sendVolunteerNotification(participant.getVolunteerId(), title, content,
                    "ACTIVITY_STARTED", activity.getId(), "VOLUNTEER_ACTIVITY", activity.getId());
        }
    }

    private void notifyParticipantsActivityEnded(VolunteerActivity activity) {
        // 查询所有已审核通过的参与者
        QueryWrapper<VolunteerActivityParticipant> wrapper = new QueryWrapper<>();
        wrapper.eq("activity_id", activity.getId())
               .eq("status", VolunteerActivityConstant.PARTICIPANT_STATUS_APPROVED);
        List<VolunteerActivityParticipant> participants = participantMapper.selectList(wrapper);
        
        for (VolunteerActivityParticipant participant : participants) {
            String title = "活动已结束";
            String content = "您参与的活动\"" + activity.getTitle() + "\"已结束，请提交活动报告。";
            notificationService.sendVolunteerNotification(participant.getVolunteerId(), title, content,
                    "ACTIVITY_ENDED", activity.getId(), "VOLUNTEER_ACTIVITY", activity.getId());
        }
    }

    private void notifyAdminNewReport(VolunteerActivity activity, Long volunteerId) {
        // 通知所有管理员
        List<com.xunqin.entity.User> admins = userService.getAdmins();
        com.xunqin.entity.User volunteer = userMapper.selectById(volunteerId);
        String volunteerName = volunteer != null ? volunteer.getNickname() : "未知用户";
        
        for (com.xunqin.entity.User admin : admins) {
            String title = "新的活动报告";
            String content = "用户" + volunteerName + "提交了活动\"" + activity.getTitle() + "\"的报告，需要审核。";
            notificationService.sendAdminNotification(admin.getId(), title, content, "VOLUNTEER_ACTIVITY_REPORT", activity.getId(), "VOLUNTEER_ACTIVITY", 1);
        }
    }

    private void notifyVolunteerReportApproved(VolunteerActivity activity, Long volunteerId) {
        com.xunqin.entity.User volunteer = userMapper.selectById(volunteerId);
        if (volunteer != null) {
            String title = "报告审核通过";
            String content = "您在活动\"" + activity.getTitle() + "\"中提交的报告已审核通过，感谢您的参与和贡献！";
            notificationService.sendVolunteerNotification(volunteerId, title, content,
                    "REPORT_APPROVED", activity.getId(), "VOLUNTEER_ACTIVITY", activity.getId());
        }
    }

    private void notifyVolunteerReportRejected(VolunteerActivity activity, Long volunteerId, String reason) {
        com.xunqin.entity.User volunteer = userMapper.selectById(volunteerId);
        if (volunteer != null) {
            String title = "报告审核未通过";
            String content = "您在活动\"" + activity.getTitle() + "\"中提交的报告未通过审核。原因：" + reason + "，请修改后重新提交。";
            notificationService.sendVolunteerNotification(volunteerId, title, content,
                    "REPORT_REJECTED", activity.getId(), "VOLUNTEER_ACTIVITY", activity.getId());
        }
    }

    private void notifyParticipantsNewProgress(VolunteerActivity activity, VolunteerActivityProgress progress) {
        // 查询所有已审核通过的参与者
        QueryWrapper<VolunteerActivityParticipant> wrapper = new QueryWrapper<>();
        wrapper.eq("activity_id", activity.getId())
               .eq("status", VolunteerActivityConstant.PARTICIPANT_STATUS_APPROVED);
        List<VolunteerActivityParticipant> participants = participantMapper.selectList(wrapper);
        
        for (VolunteerActivityParticipant participant : participants) {
            String title = "活动新进度：" + progress.getTitle();
            String content = progress.getContent();
            notificationService.sendVolunteerNotification(participant.getVolunteerId(), title, content,
                    "ACTIVITY_PROGRESS", activity.getId(), "VOLUNTEER_ACTIVITY", activity.getId());
        }
    }
}
