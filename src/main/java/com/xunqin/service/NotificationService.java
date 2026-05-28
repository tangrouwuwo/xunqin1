package com.xunqin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xunqin.entity.AdminNotification;
import com.xunqin.entity.ClueProviderNotification;
import com.xunqin.entity.SeekerNotification;
import com.xunqin.entity.VolunteerNotification;

public interface NotificationService {

    // 通用方法
    Page<?> getNotificationsByRole(String role, Long userId, Integer pageNum, Integer pageSize);

    Long getUnreadCountByRole(String role, Long userId);

    void markAsReadByRole(String role, Long id, Long userId);

    void markAllAsReadByRole(String role, Long userId);

    void deleteNotificationByRole(String role, Long id, Long userId);

    // 管理员通知
    void sendAdminNotification(Long userId, String title, String content, String type, Long targetId, String targetType, Integer priority);

    // 寻亲者通知
    void sendSeekerNotification(Long userId, String title, String content, String type, Long targetId, String targetType, Long relatedPersonId);

    // 志愿者通知
    void sendVolunteerNotification(Long userId, String title, String content, String type, Long targetId, String targetType, Long taskId);

    // 线索提供者通知
    void sendClueProviderNotification(Long userId, String title, String content, String type, Long targetId, String targetType, Long clueId);
}
