package com.xunqin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xunqin.common.result.Result;
import com.xunqin.entity.SuccessCase;
import com.xunqin.entity.User;
import com.xunqin.service.SuccessCaseService;
import com.xunqin.service.UserService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

@RestController
@RequestMapping("/success-cases")
public class SuccessCaseController {

    @Autowired
    private SuccessCaseService successCaseService;

    @Autowired
    private UserService userService;

    @Data
    public static class CreateSuccessCaseRequest {
        @NotBlank(message = "案例标题不能为空")
        private String title;

        @NotBlank(message = "案例内容不能为空")
        private String content;

        private Long missingPersonId;
        private String reunionTime;
        private String reunionLocation;
        private String photos;
        private String videos;
        private String tags;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SEEKER', 'ADMIN')")
    public Result<SuccessCase> createSuccessCase(Authentication authentication,
                                               @RequestParam("title") String title,
                                               @RequestParam("content") String content,
                                               @RequestParam(required = false) Long missingPersonId,
                                               @RequestParam(required = false) String reunionTime,
                                               @RequestParam(required = false) String reunionLocation,
                                               @RequestParam(required = false) MultipartFile[] photos,
                                               @RequestParam(required = false) String videos,
                                               @RequestParam(required = false) String tags) {
        Long userId = Long.parseLong(authentication.getPrincipal().toString());
        
        // 获取用户信息，判断是否为管理员
        User user = userService.getUserById(userId);
        boolean isAdmin = "ADMIN".equals(user.getRole());
        
        System.out.println("User ID: " + userId);
        System.out.println("User Role: " + user.getRole());
        System.out.println("Is Admin: " + isAdmin);
        
        // 处理文件上传
        String photosUrls = null;
        if (photos != null && photos.length > 0) {
            photosUrls = successCaseService.uploadPhotos(photos);
        }
        
        SuccessCase successCase = successCaseService.createSuccessCase(userId, title, content,
                missingPersonId, reunionTime, reunionLocation,
                photosUrls, videos, tags, isAdmin);
        
        if (isAdmin) {
            System.out.println("Admin case created, returning '发布成功'");
            return Result.success("发布成功", successCase);
        } else {
            System.out.println("Non-admin case created, returning '发布成功，等待审核'");
            return Result.success("发布成功，等待审核", successCase);
        }
    }

    @PostMapping("/upload")
    @PreAuthorize("hasAnyRole('SEEKER', 'ADMIN')")
    public Result<SuccessCase> createSuccessCaseWithFiles(Authentication authentication,
                                                       @RequestParam("title") String title,
                                                       @RequestParam("content") String content,
                                                       @RequestParam(required = false) Long missingPersonId,
                                                       @RequestParam(required = false) String reunionTime,
                                                       @RequestParam(required = false) String reunionLocation,
                                                       @RequestParam(required = false) MultipartFile[] photos,
                                                       @RequestParam(required = false) String videos,
                                                       @RequestParam(required = false) String tags) {
        Long userId = Long.parseLong(authentication.getPrincipal().toString());
        
        // 获取用户信息，判断是否为管理员
        User user = userService.getUserById(userId);
        boolean isAdmin = "ADMIN".equals(user.getRole());
        
        // 处理文件上传
        String photosUrls = successCaseService.uploadPhotos(photos);
        
        SuccessCase successCase = successCaseService.createSuccessCase(userId, title, content,
                missingPersonId, reunionTime, reunionLocation,
                photosUrls, videos, tags, isAdmin);
        
        if (isAdmin) {
            return Result.success("发布成功", successCase);
        } else {
            return Result.success("发布成功，等待审核", successCase);
        }
    }

    @GetMapping
    public Result<Page<SuccessCase>> getSuccessCases(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String tags,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<SuccessCase> page = successCaseService.getSuccessCases(status, tags, pageNum, pageSize);
        return Result.success(page);
    }

    @GetMapping("/{id}")
    public Result<SuccessCase> getSuccessCaseById(@PathVariable Long id) {
        SuccessCase successCase = successCaseService.getSuccessCaseById(id);
        return Result.success(successCase);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SEEKER', 'ADMIN')")
    public Result<SuccessCase> updateSuccessCase(Authentication authentication, @PathVariable Long id,
                                               @Valid @RequestBody CreateSuccessCaseRequest request) {
        Long userId = Long.parseLong(authentication.getPrincipal().toString());
        SuccessCase successCase = successCaseService.updateSuccessCase(id, userId, request.getTitle(), request.getContent(),
                request.getMissingPersonId(), request.getReunionTime(), request.getReunionLocation(),
                request.getPhotos(), request.getVideos(), request.getTags());
        return Result.success("更新成功", successCase);
    }

    @PutMapping("/{id}/upload")
    @PreAuthorize("hasAnyRole('SEEKER', 'ADMIN')")
    public Result<SuccessCase> updateSuccessCaseWithFiles(Authentication authentication, @PathVariable Long id,
                                                       @RequestParam("title") String title,
                                                       @RequestParam("content") String content,
                                                       @RequestParam(required = false) Long missingPersonId,
                                                       @RequestParam(required = false) String reunionTime,
                                                       @RequestParam(required = false) String reunionLocation,
                                                       @RequestParam(required = false) MultipartFile[] photos,
                                                       @RequestParam(required = false) String videos,
                                                       @RequestParam(required = false) String tags) {
        Long userId = Long.parseLong(authentication.getPrincipal().toString());

        String photosUrls = null;
        if (photos != null && photos.length > 0) {
            photosUrls = successCaseService.uploadPhotos(photos);
        }

        SuccessCase successCase = successCaseService.updateSuccessCase(id, userId, title, content,
                missingPersonId, reunionTime, reunionLocation,
                photosUrls, videos, tags);
        return Result.success("更新成功", successCase);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SEEKER', 'ADMIN')")
    public Result<?> deleteSuccessCase(Authentication authentication, @PathVariable Long id) {
        Long userId = Long.parseLong(authentication.getPrincipal().toString());
        successCaseService.deleteSuccessCase(id, userId);
        return Result.success("删除成功");
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> approveSuccessCase(@PathVariable Long id, @RequestParam String approvalRemark) {
        successCaseService.approveSuccessCase(id, approvalRemark);
        return Result.success("审核通过");
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> rejectSuccessCase(@PathVariable Long id, @RequestParam String rejectionRemark) {
        successCaseService.rejectSuccessCase(id, rejectionRemark);
        return Result.success("审核拒绝");
    }

    @GetMapping("/hot")
    public Result<?> getHotSuccessCases(@RequestParam(defaultValue = "10") Integer limit) {
        return Result.success(successCaseService.getHotSuccessCases(limit));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('SEEKER')")
    public Result<Page<SuccessCase>> getMySuccessCases(Authentication authentication,
                                                    @RequestParam(required = false) String status,
                                                    @RequestParam(defaultValue = "1") Integer pageNum,
                                                    @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = Long.parseLong(authentication.getPrincipal().toString());
        Page<SuccessCase> page = successCaseService.getMySuccessCases(userId, status, pageNum, pageSize);
        return Result.success(page);
    }
}
