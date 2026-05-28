package com.xunqin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xunqin.common.result.Result;
import com.xunqin.entity.Task;
import com.xunqin.entity.TaskLog;
import com.xunqin.service.TaskService;
import com.xunqin.vo.TaskVO;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @Data
    public static class CreateTaskRequest {
        @NotBlank(message = "任务标题不能为空")
        private String title;

        @NotBlank(message = "任务描述不能为空")
        private String description;

        private String type;
        private Integer priority;
        private String location;
        private String requiredSkills;
        private LocalDateTime deadline;
    }

    @Data
    public static class UpdateTaskProgressRequest {
        private String logContent;
        private String attachments;
    }

    @Data
    public static class CompleteTaskRequest {
        private String result;
    }

    @Data
    public static class ReviewTaskRequest {
        private boolean approved;
        private String reviewRemark;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SEEKER', 'ADMIN')")
    public Result<Task> createTask(Authentication authentication, @Valid @RequestBody CreateTaskRequest request) {
        Long userId = Long.parseLong(authentication.getPrincipal().toString());
        String role = authentication.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
        Task task = taskService.createTask(userId, role, request.getTitle(), request.getDescription(),
                request.getType(), request.getPriority(), request.getLocation(),
                request.getRequiredSkills(), request.getDeadline());
        String message = "ADMIN".equals(role) ? "任务创建成功" : "任务已提交，等待管理员审核";
        return Result.success(message, task);
    }

    @GetMapping
    public Result<Page<TaskVO>> getTasks(Authentication authentication,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String location,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = null;
        String role = null;
        if (authentication != null) {
            userId = Long.parseLong(authentication.getPrincipal().toString());
            role = authentication.getAuthorities().iterator().next().getAuthority();
        }
        Page<TaskVO> page = taskService.getTasks(userId, role, status, type, location, pageNum, pageSize);
        return Result.success(page);
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('VOLUNTEER')")
    public Result<Page<TaskVO>> getMyTasks(Authentication authentication,
                                         @RequestParam(required = false) Integer status,
                                         @RequestParam(defaultValue = "1") Integer pageNum,
                                         @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = Long.parseLong(authentication.getPrincipal().toString());
        Page<TaskVO> page = taskService.getMyTasks(userId, status, pageNum, pageSize);
        return Result.success(page);
    }

    @GetMapping("/my-published")
    @PreAuthorize("hasAnyRole('ADMIN', 'SEEKER')")
    public Result<Page<TaskVO>> getMyPublishedTasks(Authentication authentication,
                                                   @RequestParam(required = false) Integer status,
                                                   @RequestParam(required = false) String type,
                                                   @RequestParam(required = false) String title,
                                                   @RequestParam(defaultValue = "1") Integer pageNum,
                                                   @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = Long.parseLong(authentication.getPrincipal().toString());
        String role = authentication.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
        Page<TaskVO> page = taskService.getMyPublishedTasks(userId, role, status, type, title, pageNum, pageSize);
        return Result.success(page);
    }

    @GetMapping("/{id}")
    public Result<TaskVO> getTaskById(@PathVariable Long id) {
        TaskVO task = taskService.getTaskById(id);
        return Result.success(task);
    }

    @PostMapping("/{id}/claim")
    @PreAuthorize("hasRole('VOLUNTEER')")
    public Result<?> claimTask(Authentication authentication, @PathVariable Long id) {
        Long userId = Long.parseLong(authentication.getPrincipal().toString());
        taskService.claimTask(id, userId);
        return Result.success("任务认领成功");
    }

    @PutMapping("/{id}/progress")
    @PreAuthorize("hasRole('VOLUNTEER')")
    public Result<?> updateTaskProgress(Authentication authentication, @PathVariable Long id,
                                      @Valid @RequestBody UpdateTaskProgressRequest request) {
        Long userId = Long.parseLong(authentication.getPrincipal().toString());
        taskService.updateTaskProgress(id, userId, request.getLogContent(), request.getAttachments());
        return Result.success("进度更新成功");
    }

    @PutMapping("/{id}/complete")
    @PreAuthorize("hasRole('VOLUNTEER')")
    public Result<?> completeTask(Authentication authentication, @PathVariable Long id,
                                @Valid @RequestBody CompleteTaskRequest request) {
        Long userId = Long.parseLong(authentication.getPrincipal().toString());
        taskService.completeTask(id, userId, request.getResult());
        return Result.success("任务完成成功");
    }

    @PutMapping("/{id}/review")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> reviewTask(Authentication authentication, @PathVariable Long id,
                               @Valid @RequestBody ReviewTaskRequest request) {
        Long userId = Long.parseLong(authentication.getPrincipal().toString());
        taskService.reviewTask(id, userId, request.isApproved(), request.getReviewRemark());
        String message = request.isApproved() ? "任务审核通过" : "任务已拒绝";
        return Result.success(message);
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'SEEKER')")
    public Result<?> cancelTask(Authentication authentication, @PathVariable Long id) {
        Long userId = Long.parseLong(authentication.getPrincipal().toString());
        taskService.cancelTask(id, userId);
        return Result.success("任务取消成功");
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SEEKER')")
    public Result<Task> updateTask(Authentication authentication, @PathVariable Long id,
                                   @Valid @RequestBody CreateTaskRequest request) {
        Long userId = Long.parseLong(authentication.getPrincipal().toString());
        Task task = taskService.updateTask(id, userId, request.getTitle(), request.getDescription(),
                request.getType(), request.getPriority(), request.getLocation(),
                request.getRequiredSkills(), request.getDeadline());
        return Result.success("任务更新成功", task);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SEEKER')")
    public Result<?> deleteTask(Authentication authentication, @PathVariable Long id) {
        Long userId = Long.parseLong(authentication.getPrincipal().toString());
        taskService.deleteTask(id, userId);
        return Result.success("任务删除成功");
    }

    @GetMapping("/{id}/logs")
    public Result<List<TaskLog>> getTaskLogs(@PathVariable Long id) {
        List<TaskLog> logs = taskService.getTaskLogs(id);
        return Result.success(logs);
    }
}
