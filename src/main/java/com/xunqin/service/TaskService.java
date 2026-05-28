package com.xunqin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xunqin.entity.Task;
import com.xunqin.entity.TaskLog;
import com.xunqin.vo.TaskVO;

import java.time.LocalDateTime;
import java.util.List;

public interface TaskService {

    /**
     * 创建任务
     */
    Task createTask(Long publisherId, String role, String title, String description, String type,
                   Integer priority, String location, String requiredSkills,
                   LocalDateTime deadline);

    /**
     * 获取任务列表（任务大厅）
     */
    Page<TaskVO> getTasks(Long userId, String role, Integer status, String type, String location,
                       Integer pageNum, Integer pageSize);

    /**
     * 获取我的任务（志愿者认领的任务）
     */
    Page<TaskVO> getMyTasks(Long userId, Integer status, Integer pageNum, Integer pageSize);

    /**
     * 获取我发布的任务（寻亲者和管理员）
     */
    Page<TaskVO> getMyPublishedTasks(Long userId, String role, Integer status, String type, String title, Integer pageNum, Integer pageSize);

    /**
     * 管理员获取所有任务（按条件筛选）
     */
    Page<TaskVO> getAdminTasks(Integer status, String type, String title, Integer pageNum, Integer pageSize);

    /**
     * 根据ID获取任务
     */
    TaskVO getTaskById(Long id);

    /**
     * 认领任务
     */
    void claimTask(Long id, Long volunteerId);

    /**
     * 更新任务进度
     */
    void updateTaskProgress(Long id, Long volunteerId, String logContent, String attachments);

    /**
     * 完成任务
     */
    void completeTask(Long id, Long volunteerId, String result);

    /**
     * 审核任务（管理员审核寻亲者发布的任务）
     */
    void reviewTask(Long id, Long reviewerId, boolean approved, String reviewRemark);

    /**
     * 取消任务
     */
    void cancelTask(Long id, Long userId);

    /**
     * 更新任务
     */
    Task updateTask(Long id, Long userId, String title, String description, String type,
                   Integer priority, String location, String requiredSkills,
                   LocalDateTime deadline);

    /**
     * 删除任务
     */
    void deleteTask(Long id, Long userId);

    /**
     * 获取任务日志列表
     */
    List<TaskLog> getTaskLogs(Long taskId);
}
