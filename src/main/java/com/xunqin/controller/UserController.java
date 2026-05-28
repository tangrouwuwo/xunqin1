package com.xunqin.controller;

import com.xunqin.common.result.Result;
import com.xunqin.entity.User;
import com.xunqin.service.UserService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Data
    public static class UpdateProfileRequest {
        private String nickname;
        private String avatar;
        private String email;
        private String phone;
    }

    @Data
    public static class ChangePasswordRequest {
        @NotBlank(message = "原密码不能为空")
        private String oldPassword;

        @NotBlank(message = "新密码不能为空")
        private String newPassword;
    }

    @GetMapping("/info")
    @PreAuthorize("isAuthenticated()")
    public Result<User> getUserInfo(Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        log.info("获取用户信息, userId: {}", userId);
        User user = userService.getUserById(userId);
        return Result.success(user);
    }

    @PutMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public Result<?> updateProfile(Authentication authentication,
                                 @Valid @RequestBody UpdateProfileRequest request) {
        Long userId = getUserIdFromAuthentication(authentication);
        log.info("更新个人资料, userId: {}, nickname: {}, email: {}, phone: {}", 
                userId, request.getNickname(), request.getEmail(), request.getPhone());
        
        try {
            userService.updateProfile(userId, request.getNickname(), request.getAvatar(),
                    request.getEmail(), request.getPhone());
            log.info("个人资料更新成功, userId: {}", userId);
            return Result.success("更新成功");
        } catch (Exception e) {
            log.error("个人资料更新失败, userId: {}, error: {}", userId, e.getMessage(), e);
            throw e;
        }
    }

    @PutMapping("/password")
    @PreAuthorize("isAuthenticated()")
    public Result<?> changePassword(Authentication authentication,
                                  @Valid @RequestBody ChangePasswordRequest request) {
        Long userId = getUserIdFromAuthentication(authentication);
        log.info("修改密码, userId: {}", userId);
        
        try {
            userService.changePassword(userId, request.getOldPassword(), request.getNewPassword());
            log.info("密码修改成功, userId: {}", userId);
            return Result.success("密码修改成功");
        } catch (Exception e) {
            log.error("密码修改失败, userId: {}, error: {}", userId, e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping("/avatar")
    @PreAuthorize("isAuthenticated()")
    public Result<String> uploadAvatar(Authentication authentication,
                                      @RequestParam("file") MultipartFile file) {
        Long userId = getUserIdFromAuthentication(authentication);
        log.info("上传头像, userId: {}, 文件名: {}", userId, file.getOriginalFilename());
        
        if (file.isEmpty()) {
            return Result.error("请选择要上传的文件");
        }
        
        // 检查文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return Result.error("只能上传图片文件");
        }
        
        // 检查文件大小（最大5MB）
        if (file.getSize() > 5 * 1024 * 1024) {
            return Result.error("文件大小不能超过5MB");
        }
        
        try {
            // 创建上传目录
            String uploadDir = "uploads/avatars";
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null ? 
                    originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
            String newFilename = UUID.randomUUID().toString() + extension;
            
            // 保存文件
            Path filePath = uploadPath.resolve(newFilename);
            Files.copy(file.getInputStream(), filePath);
            
            // 构建访问URL
            String avatarUrl = "/uploads/avatars/" + newFilename;
            
            // 更新用户头像
            userService.updateAvatar(userId, avatarUrl);
            
            log.info("头像上传成功, userId: {}, avatarUrl: {}", userId, avatarUrl);
            return Result.success("上传成功", avatarUrl);
            
        } catch (IOException e) {
            log.error("头像上传失败, userId: {}, error: {}", userId, e.getMessage(), e);
            return Result.error("上传失败: " + e.getMessage());
        }
    }

    private Long getUserIdFromAuthentication(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof Long) {
            return (Long) principal;
        } else if (principal instanceof String) {
            try {
                return Long.parseLong((String) principal);
            } catch (NumberFormatException e) {
                log.error("无法解析用户ID: {}", principal, e);
                throw new RuntimeException("无效的用户ID");
            }
        } else {
            log.error("未知的principal类型: {}", principal.getClass().getName());
            throw new RuntimeException("无效的用户ID");
        }
    }
}
