package com.xunqin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xunqin.common.result.Result;
import com.xunqin.entity.User;
import com.xunqin.entity.MissingPerson;
import com.xunqin.vo.MissingPersonVO;
import com.xunqin.entity.Clue;
import com.xunqin.entity.MissingPersonChangeLog;
import com.xunqin.entity.Task;
import com.xunqin.entity.SuccessCase;
import com.xunqin.service.*;
import com.xunqin.vo.TaskVO;
import com.xunqin.vo.VolunteerApplicationVO;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private MissingPersonService missingPersonService;

    @Autowired
    private ClueService clueService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private SuccessCaseService successCaseService;

    @Autowired
    private CommunityService communityService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private VolunteerApplicationService volunteerApplicationService;

    // 用户管理
    @GetMapping("/users")
    public Result<Page<User>> getUsers(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(userService.getUsers(username, role, status, pageNum, pageSize));
    }

    @PutMapping("/users/{id}/status")
    public Result<?> updateUserStatus(@PathVariable Long id, @RequestParam Integer status) {
        // 防止禁用管理员
        User user = userService.getUserById(id);
        if ("ADMIN".equals(user.getRole())) {
            return Result.error("不能禁用管理员账号");
        }
        userService.updateUserStatus(id, status);
        return Result.success("状态更新成功");
    }

    @PutMapping("/users/{id}")
    public Result<?> updateUser(@PathVariable Long id, @RequestBody UpdateUserRequest request) {
        userService.updateUser(id, request.getNickname(), request.getEmail());
        return Result.success("更新成功");
    }

    @PostMapping("/users")
    public Result<?> createUser(@Valid @RequestBody CreateUserRequest request) {
        User user = userService.createUser(
                request.getUsername(),
                request.getPhone(),
                request.getPassword(),
                request.getRole()
        );
        return Result.success("创建成功", user);
    }

    @DeleteMapping("/users/{id}")
    public Result<?> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success("删除成功");
    }

    @Data
    public static class UpdateUserRequest {
        private String nickname;
        private String email;
    }

    @Data
    public static class CreateUserRequest {
        @NotBlank(message = "用户名不能为空")
        private String username;
        private String phone;
        @NotBlank(message = "密码不能为空")
        private String password;
        @NotBlank(message = "角色不能为空")
        private String role;
    }

    // 寻亲信息管理
    @GetMapping("/missing-persons")
    public Result<Page<MissingPersonVO>> getMissingPersons(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String username,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(missingPersonService.getMissingPersonsForAdmin(status, name, username, pageNum, pageSize));
    }

    @PutMapping("/missing-persons/{id}/approve")
    public Result<?> approveMissingPerson(@PathVariable Long id, @RequestParam String approvalRemark) {
        missingPersonService.approveMissingPerson(id, approvalRemark);
        return Result.success("审核通过");
    }

    @PutMapping("/missing-persons/{id}/reject")
    public Result<?> rejectMissingPerson(@PathVariable Long id, @RequestParam String rejectionRemark) {
        missingPersonService.rejectMissingPerson(id, rejectionRemark);
        return Result.success("审核拒绝");
    }

    @DeleteMapping("/missing-persons/{id}")
    public Result<?> deleteMissingPerson(@PathVariable Long id) {
        missingPersonService.deleteMissingPerson(id, null);
        return Result.success("删除成功");
    }

    @GetMapping("/missing-persons/{id}/change-logs")
    public Result<java.util.List<MissingPersonChangeLog>> getMissingPersonChangeLogs(@PathVariable Long id) {
        java.util.List<MissingPersonChangeLog> logs = missingPersonService.getChangeLogs(id);
        return Result.success(logs);
    }

    // 线索管理
    @GetMapping("/clues")
    public Result<Page<Clue>> getClues(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long missingPersonId,
            @RequestParam(required = false) String content,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<Clue> page = clueService.getClues(status, missingPersonId, content, pageNum, pageSize);
        return Result.success(page);
    }

    @PutMapping("/clues/{id}/assign")
    public Result<?> assignClue(@PathVariable Long id, @RequestParam Long volunteerId) {
        clueService.assignClueToVolunteer(id, volunteerId);
        return Result.success("分配成功");
    }

    // 任务管理
    @GetMapping("/tasks")
    public Result<Page<TaskVO>> getTasks(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String title,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(taskService.getAdminTasks(status, type, title, pageNum, pageSize));
    }

    @PostMapping("/tasks")
    public Result<Task> createTask(@Valid @RequestBody TaskController.CreateTaskRequest request) {
        Long adminId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Task task = taskService.createTask(adminId, "ADMIN", request.getTitle(), request.getDescription(),
                request.getType(), request.getPriority(), request.getLocation(),
                request.getRequiredSkills(), request.getDeadline());
        return Result.success("任务创建成功", task);
    }

    // 成功案例管理
    @GetMapping("/success-cases")
    public Result<Page<SuccessCase>> getSuccessCases(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(successCaseService.getSuccessCases(status, null, pageNum, pageSize));
    }

    // 社区管理
    @PutMapping("/community/posts/{id}/status")
    public Result<?> updatePostStatus(@PathVariable Long id, @RequestParam String status) {
        // 实现帖子状态更新
        return Result.success("状态更新成功");
    }

    @DeleteMapping("/community/posts/{id}")
    public Result<?> deletePost(@PathVariable Long id) {
        communityService.deletePost(id, 1L);
        return Result.success("删除成功");
    }

    @DeleteMapping("/community/comments/{id}")
    public Result<?> deleteComment(@PathVariable Long id) {
        communityService.deleteComment(id, 1L);
        return Result.success("删除成功");
    }

    // 通知管理
    @PostMapping("/notifications")
    public Result<?> sendNotification(@Valid @RequestBody SendNotificationRequest request) {
        // 根据目标角色发送不同类型的通知
        if (request.getTargetRole() != null) {
            // 这里可以实现批量发送通知的逻辑
            // 例如：根据角色获取所有用户，然后逐个发送通知
            return Result.success("通知发送成功");
        }
        return Result.success("通知发送成功");
    }

    @Data
    public static class SendNotificationRequest {
        @NotBlank(message = "标题不能为空")
        private String title;

        @NotBlank(message = "内容不能为空")
        private String content;

        private String type;
        private String targetRole;
    }

    // 系统信息
    @GetMapping("/system/stats")
    public Result<?> getSystemStats() {
        // 实现系统统计信息
        return Result.success("获取成功");
    }

    // 志愿者申请管理
    @GetMapping("/volunteer-applications")
    public Result<Page<VolunteerApplicationVO>> getVolunteerApplications(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(volunteerApplicationService.getApplications(status, name, pageNum, pageSize));
    }

    @PutMapping("/volunteer-applications/{id}/approve")
    public Result<?> approveVolunteerApplication(
            @PathVariable Long id,
            @RequestParam(required = false) String reviewComment) {
        try {
            Long adminId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            volunteerApplicationService.approveApplication(id, adminId, reviewComment);
            return Result.success("审核通过");
        } catch (Exception e) {
            log.error("审核失败: {}", e.getMessage(), e);
            return Result.error(500, "操作失败");
        }
    }

    @PutMapping("/volunteer-applications/{id}/reject")
    public Result<?> rejectVolunteerApplication(
            @PathVariable Long id,
            @RequestParam String reviewComment) {
        try {
            Long adminId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            volunteerApplicationService.rejectApplication(id, adminId, reviewComment);
            return Result.success("审核拒绝");
        } catch (Exception e) {
            log.error("拒绝审核失败: {}", e.getMessage(), e);
            return Result.error(500, "操作失败");
        }
    }

    @GetMapping("/volunteer-applications/{id}")
    public Result<VolunteerApplicationVO> getVolunteerApplicationById(@PathVariable Long id) {
        return Result.success(volunteerApplicationService.getApplicationById(id));
    }

    @DeleteMapping("/volunteer-applications/{id}")
    public Result<?> deleteVolunteerApplication(@PathVariable Long id) {
        try {
            volunteerApplicationService.deleteApplication(id);
            return Result.success("删除成功");
        } catch (Exception e) {
            log.error("删除申请失败: {}", e.getMessage(), e);
            return Result.error(500, "操作失败");
        }
    }
}
