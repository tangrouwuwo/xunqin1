package com.xunqin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xunqin.common.constant.RoleConstant;
import com.xunqin.entity.AdminNotification;
import com.xunqin.entity.ClueProviderNotification;
import com.xunqin.entity.SeekerNotification;
import com.xunqin.entity.VolunteerNotification;
import com.xunqin.mapper.AdminNotificationMapper;
import com.xunqin.mapper.ClueProviderNotificationMapper;
import com.xunqin.mapper.SeekerNotificationMapper;
import com.xunqin.mapper.VolunteerNotificationMapper;
import com.xunqin.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private AdminNotificationMapper adminNotificationMapper;

    @Autowired
    private SeekerNotificationMapper seekerNotificationMapper;

    @Autowired
    private VolunteerNotificationMapper volunteerNotificationMapper;

    @Autowired
    private ClueProviderNotificationMapper clueProviderNotificationMapper;

    @Override
    public Page<?> getNotificationsByRole(String role, Long userId, Integer pageNum, Integer pageSize) {
        
        switch (role) {
            case RoleConstant.ADMIN:
                Page<AdminNotification> adminPage = new Page<>(pageNum, pageSize);
                LambdaQueryWrapper<AdminNotification> adminWrapper = new LambdaQueryWrapper<>();
                adminWrapper.eq(AdminNotification::getUserId, userId);
                adminWrapper.orderByDesc(AdminNotification::getCreateTime);
                return adminNotificationMapper.selectPage(adminPage, adminWrapper);
            
            case RoleConstant.SEEKER:
                Page<SeekerNotification> seekerPage = new Page<>(pageNum, pageSize);
                LambdaQueryWrapper<SeekerNotification> seekerWrapper = new LambdaQueryWrapper<>();
                seekerWrapper.eq(SeekerNotification::getUserId, userId);
                seekerWrapper.orderByDesc(SeekerNotification::getCreateTime);
                return seekerNotificationMapper.selectPage(seekerPage, seekerWrapper);
            
            case RoleConstant.VOLUNTEER:
                Page<VolunteerNotification> volunteerPage = new Page<>(pageNum, pageSize);
                LambdaQueryWrapper<VolunteerNotification> volunteerWrapper = new LambdaQueryWrapper<>();
                volunteerWrapper.eq(VolunteerNotification::getUserId, userId);
                volunteerWrapper.orderByDesc(VolunteerNotification::getCreateTime);
                return volunteerNotificationMapper.selectPage(volunteerPage, volunteerWrapper);
            
            case RoleConstant.CLUE_PROVIDER:
                Page<ClueProviderNotification> cluePage = new Page<>(pageNum, pageSize);
                LambdaQueryWrapper<ClueProviderNotification> clueWrapper = new LambdaQueryWrapper<>();
                clueWrapper.eq(ClueProviderNotification::getUserId, userId);
                clueWrapper.orderByDesc(ClueProviderNotification::getCreateTime);
                return clueProviderNotificationMapper.selectPage(cluePage, clueWrapper);
            
            default:
                throw new IllegalArgumentException("Invalid role: " + role);
        }
    }

    @Override
    public Long getUnreadCountByRole(String role, Long userId) {
        switch (role) {
            case RoleConstant.ADMIN:
                LambdaQueryWrapper<AdminNotification> adminWrapper = new LambdaQueryWrapper<>();
                adminWrapper.eq(AdminNotification::getUserId, userId);
                adminWrapper.eq(AdminNotification::getIsRead, 0);
                return adminNotificationMapper.selectCount(adminWrapper);
            
            case RoleConstant.SEEKER:
                LambdaQueryWrapper<SeekerNotification> seekerWrapper = new LambdaQueryWrapper<>();
                seekerWrapper.eq(SeekerNotification::getUserId, userId);
                seekerWrapper.eq(SeekerNotification::getIsRead, 0);
                return seekerNotificationMapper.selectCount(seekerWrapper);
            
            case RoleConstant.VOLUNTEER:
                LambdaQueryWrapper<VolunteerNotification> volunteerWrapper = new LambdaQueryWrapper<>();
                volunteerWrapper.eq(VolunteerNotification::getUserId, userId);
                volunteerWrapper.eq(VolunteerNotification::getIsRead, 0);
                return volunteerNotificationMapper.selectCount(volunteerWrapper);
            
            case RoleConstant.CLUE_PROVIDER:
                LambdaQueryWrapper<ClueProviderNotification> clueWrapper = new LambdaQueryWrapper<>();
                clueWrapper.eq(ClueProviderNotification::getUserId, userId);
                clueWrapper.eq(ClueProviderNotification::getIsRead, 0);
                return clueProviderNotificationMapper.selectCount(clueWrapper);
            
            default:
                throw new IllegalArgumentException("Invalid role: " + role);
        }
    }

    @Override
    @Transactional
    public void markAsReadByRole(String role, Long id, Long userId) {
        switch (role) {
            case RoleConstant.ADMIN:
                AdminNotification adminNotification = adminNotificationMapper.selectById(id);
                if (adminNotification != null && adminNotification.getUserId().equals(userId)) {
                    adminNotification.setIsRead(1);
                    adminNotificationMapper.updateById(adminNotification);
                }
                break;
            
            case RoleConstant.SEEKER:
                SeekerNotification seekerNotification = seekerNotificationMapper.selectById(id);
                if (seekerNotification != null && seekerNotification.getUserId().equals(userId)) {
                    seekerNotification.setIsRead(1);
                    seekerNotificationMapper.updateById(seekerNotification);
                }
                break;
            
            case RoleConstant.VOLUNTEER:
                VolunteerNotification volunteerNotification = volunteerNotificationMapper.selectById(id);
                if (volunteerNotification != null && volunteerNotification.getUserId().equals(userId)) {
                    volunteerNotification.setIsRead(1);
                    volunteerNotificationMapper.updateById(volunteerNotification);
                }
                break;
            
            case RoleConstant.CLUE_PROVIDER:
                ClueProviderNotification clueNotification = clueProviderNotificationMapper.selectById(id);
                if (clueNotification != null && clueNotification.getUserId().equals(userId)) {
                    clueNotification.setIsRead(1);
                    clueProviderNotificationMapper.updateById(clueNotification);
                }
                break;
            
            default:
                throw new IllegalArgumentException("Invalid role: " + role);
        }
    }

    @Override
    @Transactional
    public void markAllAsReadByRole(String role, Long userId) {
        switch (role) {
            case RoleConstant.ADMIN:
                LambdaQueryWrapper<AdminNotification> adminWrapper = new LambdaQueryWrapper<>();
                adminWrapper.eq(AdminNotification::getUserId, userId);
                adminWrapper.eq(AdminNotification::getIsRead, 0);
                
                AdminNotification adminUpdate = new AdminNotification();
                adminUpdate.setIsRead(1);
                adminNotificationMapper.update(adminUpdate, adminWrapper);
                break;
            
            case RoleConstant.SEEKER:
                LambdaQueryWrapper<SeekerNotification> seekerWrapper = new LambdaQueryWrapper<>();
                seekerWrapper.eq(SeekerNotification::getUserId, userId);
                seekerWrapper.eq(SeekerNotification::getIsRead, 0);
                
                SeekerNotification seekerUpdate = new SeekerNotification();
                seekerUpdate.setIsRead(1);
                seekerNotificationMapper.update(seekerUpdate, seekerWrapper);
                break;
            
            case RoleConstant.VOLUNTEER:
                LambdaQueryWrapper<VolunteerNotification> volunteerWrapper = new LambdaQueryWrapper<>();
                volunteerWrapper.eq(VolunteerNotification::getUserId, userId);
                volunteerWrapper.eq(VolunteerNotification::getIsRead, 0);
                
                VolunteerNotification volunteerUpdate = new VolunteerNotification();
                volunteerUpdate.setIsRead(1);
                volunteerNotificationMapper.update(volunteerUpdate, volunteerWrapper);
                break;
            
            case RoleConstant.CLUE_PROVIDER:
                LambdaQueryWrapper<ClueProviderNotification> clueWrapper = new LambdaQueryWrapper<>();
                clueWrapper.eq(ClueProviderNotification::getUserId, userId);
                clueWrapper.eq(ClueProviderNotification::getIsRead, 0);
                
                ClueProviderNotification clueUpdate = new ClueProviderNotification();
                clueUpdate.setIsRead(1);
                clueProviderNotificationMapper.update(clueUpdate, clueWrapper);
                break;
            
            default:
                throw new IllegalArgumentException("Invalid role: " + role);
        }
    }

    @Override
    @Transactional
    public void deleteNotificationByRole(String role, Long id, Long userId) {
        int deleted;
        switch (role) {
            case RoleConstant.ADMIN:
                deleted = adminNotificationMapper.physicalDeleteByIdAndUserId(id, userId);
                break;
            case RoleConstant.SEEKER:
                deleted = seekerNotificationMapper.physicalDeleteByIdAndUserId(id, userId);
                break;
            case RoleConstant.VOLUNTEER:
                deleted = volunteerNotificationMapper.physicalDeleteByIdAndUserId(id, userId);
                break;
            case RoleConstant.CLUE_PROVIDER:
                deleted = clueProviderNotificationMapper.physicalDeleteByIdAndUserId(id, userId);
                break;
            default:
                throw new IllegalArgumentException("Invalid role: " + role);
        }
        if (deleted == 0) {
            throw new IllegalArgumentException("通知不存在或无权删除");
        }
        log.info("用户 {} 删除了通知 {}", userId, id);
    }

    @Override
    @Transactional
    public void sendAdminNotification(Long userId, String title, String content, String type, Long targetId, String targetType, Integer priority) {
        AdminNotification notification = new AdminNotification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(type);
        notification.setTargetId(targetId);
        notification.setTargetType(targetType);
        notification.setPriority(priority);
        notification.setIsRead(0);
        
        adminNotificationMapper.insert(notification);
        log.info("发送管理员通知给用户 {}：{}", userId, title);
    }

    @Override
    @Transactional
    public void sendSeekerNotification(Long userId, String title, String content, String type, Long targetId, String targetType, Long relatedPersonId) {
        SeekerNotification notification = new SeekerNotification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(type);
        notification.setTargetId(targetId);
        notification.setTargetType(targetType);
        notification.setRelatedPersonId(relatedPersonId);
        notification.setIsRead(0);
        
        seekerNotificationMapper.insert(notification);
        log.info("发送寻亲者通知给用户 {}：{}", userId, title);
    }

    @Override
    @Transactional
    public void sendVolunteerNotification(Long userId, String title, String content, String type, Long targetId, String targetType, Long taskId) {
        VolunteerNotification notification = new VolunteerNotification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(type);
        notification.setTargetId(targetId);
        notification.setTargetType(targetType);
        notification.setTaskId(taskId);
        notification.setIsRead(0);
        
        volunteerNotificationMapper.insert(notification);
        log.info("发送志愿者通知给用户 {}：{}", userId, title);
    }

    @Override
    @Transactional
    public void sendClueProviderNotification(Long userId, String title, String content, String type, Long targetId, String targetType, Long clueId) {
        ClueProviderNotification notification = new ClueProviderNotification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(type);
        notification.setTargetId(targetId);
        notification.setTargetType(targetType);
        notification.setClueId(clueId);
        notification.setIsRead(0);
        
        clueProviderNotificationMapper.insert(notification);
        log.info("发送线索提供者通知给用户 {}：{}", userId, title);
    }
}
