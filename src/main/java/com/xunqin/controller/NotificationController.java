package com.xunqin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xunqin.common.result.Result;
import com.xunqin.entity.User;
import com.xunqin.service.NotificationService;
import com.xunqin.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserService userService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Result<Page<?>> getNotifications(Authentication authentication,
                                           @RequestParam(defaultValue = "1") Integer pageNum,
                                           @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = Long.parseLong(authentication.getPrincipal().toString());
        User user = userService.getUserById(userId);
        Page<?> page = notificationService.getNotificationsByRole(user.getRole(), userId, pageNum, pageSize);
        return Result.success(page);
    }

    @GetMapping("/unread-count")
    @PreAuthorize("isAuthenticated()")
    public Result<Long> getUnreadCount(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getPrincipal().toString());
        User user = userService.getUserById(userId);
        Long count = notificationService.getUnreadCountByRole(user.getRole(), userId);
        return Result.success(count);
    }

    @PutMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public Result<?> markAsRead(Authentication authentication, @PathVariable Long id) {
        Long userId = Long.parseLong(authentication.getPrincipal().toString());
        User user = userService.getUserById(userId);
        notificationService.markAsReadByRole(user.getRole(), id, userId);
        return Result.success("标记成功");
    }

    @PutMapping("/read-all")
    @PreAuthorize("isAuthenticated()")
    public Result<?> markAllAsRead(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getPrincipal().toString());
        User user = userService.getUserById(userId);
        notificationService.markAllAsReadByRole(user.getRole(), userId);
        return Result.success("标记成功");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<?> deleteNotification(Authentication authentication, @PathVariable Long id) {
        Long userId = Long.parseLong(authentication.getPrincipal().toString());
        User user = userService.getUserById(userId);
        notificationService.deleteNotificationByRole(user.getRole(), id, userId);
        return Result.success("删除成功");
    }
}
