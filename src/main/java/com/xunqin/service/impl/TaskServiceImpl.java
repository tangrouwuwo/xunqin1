package com.xunqin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xunqin.common.constant.StatusConstant;
import com.xunqin.common.exception.BusinessException;
import com.xunqin.entity.Task;
import com.xunqin.entity.TaskLog;
import com.xunqin.entity.User;
import com.xunqin.mapper.TaskMapper;
import com.xunqin.mapper.TaskLogMapper;
import com.xunqin.mapper.UserMapper;
import com.xunqin.service.TaskService;
import com.xunqin.service.NotificationService;
import com.xunqin.vo.TaskVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskServiceImpl implements TaskService {

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private TaskLogMapper taskLogMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private NotificationService notificationService;

    @Override
    @Transactional
    public Task createTask(Long publisherId, String role, String title, String description, String type,
                          Integer priority, String location, String requiredSkills,
                          LocalDateTime deadline) {
        Task task = new Task();
        task.setPublisherId(publisherId);
        task.setTitle(title);
        task.setDescription(description);
        task.setType(type);
        task.setPriority(priority != null ? priority : 1);
        task.setLocation(location);
        task.setRequiredSkills(requiredSkills);
        task.setDeadline(deadline);
        task.setCreateTime(LocalDateTime.now());

        // 管理员发布直接通过，寻亲者发布需要审核
        if ("ADMIN".equals(role)) {
            task.setStatus(StatusConstant.TASK_PENDING);
        } else {
            task.setStatus(StatusConstant.TASK_PENDING_REVIEW);
        }

        taskMapper.insert(task);

        // 记录任务日志
        TaskLog log = new TaskLog();
        log.setTaskId(task.getId());
        log.setVolunteerId(publisherId);
        log.setContent("任务创建" + ("ADMIN".equals(role) ? "，状态：待认领" : "，状态：待审核"));
        log.setCreateTime(LocalDateTime.now());
        taskLogMapper.insert(log);

        // 如果是寻亲者创建任务，通知管理员审核
        if (!"ADMIN".equals(role)) {
            User publisher = userMapper.selectById(publisherId);
            // 查找所有管理员发送通知
            QueryWrapper<User> adminWrapper = new QueryWrapper<>();
            adminWrapper.eq("role", "ADMIN");
            List<User> admins = userMapper.selectList(adminWrapper);
            for (User admin : admins) {
                String notifTitle = "新任务待审核";
                String notifContent = "寻亲者" + (publisher != null ? publisher.getNickname() : "未知") + "发布了任务\"" + title + "\"，请审核。";
                notificationService.sendAdminNotification(admin.getId(), notifTitle, notifContent, "TASK_PENDING_REVIEW", task.getId(), "TASK", 1);
            }
        }

        return task;
    }

    @Override
    public Page<TaskVO> getTasks(Long userId, String role, Integer status, String type, String location,
                              Integer pageNum, Integer pageSize) {
        Page<Task> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Task> wrapper = new QueryWrapper<>();

        // 志愿者在任务大厅只能看到待认领的任务
        if (userId != null && "ROLE_VOLUNTEER".equals(role)) {
            wrapper.eq("status", StatusConstant.TASK_PENDING);
        } else if (userId != null && "ROLE_SEEKER".equals(role)) {
            // 寻亲者只能看到自己发布的任务
            wrapper.eq("publisher_id", userId);
        }

        if (status != null && !("ROLE_VOLUNTEER".equals(role))) {
            wrapper.eq("status", status);
        }
        if (type != null) {
            wrapper.eq("type", type);
        }
        if (location != null) {
            wrapper.like("location", location);
        }

        wrapper.orderByDesc("create_time");
        Page<Task> taskPage = taskMapper.selectPage(page, wrapper);

        // 转换为TaskVO
        List<TaskVO> taskVOList = taskPage.getRecords().stream().map(task -> {
            User publisher = userMapper.selectById(task.getPublisherId());
            String publisherName = publisher != null ? publisher.getNickname() : "未知";
            String publisherRole = publisher != null ? publisher.getRole() : "未知";
            User volunteer = task.getVolunteerId() != null ? userMapper.selectById(task.getVolunteerId()) : null;
            String volunteerName = volunteer != null ? volunteer.getNickname() : "未认领";
            return new TaskVO(task, publisherName, publisherRole, volunteerName);
        }).collect(Collectors.toList());

        Page<TaskVO> taskVOPage = new Page<>(pageNum, pageSize);
        taskVOPage.setRecords(taskVOList);
        taskVOPage.setTotal(taskPage.getTotal());
        taskVOPage.setPages(taskPage.getPages());
        taskVOPage.setCurrent(taskPage.getCurrent());
        taskVOPage.setSize(taskPage.getSize());

        return taskVOPage;
    }

    @Override
    public Page<TaskVO> getMyTasks(Long userId, Integer status, Integer pageNum, Integer pageSize) {
        Page<Task> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Task> wrapper = new QueryWrapper<>();

        wrapper.eq("volunteer_id", userId);
        if (status != null) {
            wrapper.eq("status", status);
        }

        wrapper.orderByDesc("update_time");
        Page<Task> taskPage = taskMapper.selectPage(page, wrapper);

        // 转换为TaskVO
        List<TaskVO> taskVOList = taskPage.getRecords().stream().map(task -> {
            User publisher = userMapper.selectById(task.getPublisherId());
            String publisherName = publisher != null ? publisher.getNickname() : "未知";
            String publisherRole = publisher != null ? publisher.getRole() : "未知";
            User volunteer = task.getVolunteerId() != null ? userMapper.selectById(task.getVolunteerId()) : null;
            String volunteerName = volunteer != null ? volunteer.getNickname() : "未认领";
            return new TaskVO(task, publisherName, publisherRole, volunteerName);
        }).collect(Collectors.toList());

        Page<TaskVO> taskVOPage = new Page<>(pageNum, pageSize);
        taskVOPage.setRecords(taskVOList);
        taskVOPage.setTotal(taskPage.getTotal());
        taskVOPage.setPages(taskPage.getPages());
        taskVOPage.setCurrent(taskPage.getCurrent());
        taskVOPage.setSize(taskPage.getSize());

        return taskVOPage;
    }

    @Override
    public TaskVO getTaskById(Long id) {
        Task task = taskMapper.selectById(id);
        if (task == null) {
            throw new BusinessException("任务不存在");
        }

        User publisher = userMapper.selectById(task.getPublisherId());
        String publisherName = publisher != null ? publisher.getNickname() : "未知";
        String publisherRole = publisher != null ? publisher.getRole() : "未知";
        User volunteer = task.getVolunteerId() != null ? userMapper.selectById(task.getVolunteerId()) : null;
        String volunteerName = volunteer != null ? volunteer.getNickname() : "未认领";

        return new TaskVO(task, publisherName, publisherRole, volunteerName);
    }

    @Override
    public Page<TaskVO> getMyPublishedTasks(Long userId, String role, Integer status, String type, String title, Integer pageNum, Integer pageSize) {
        Page<Task> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Task> wrapper = new QueryWrapper<>();

        wrapper.eq("publisher_id", userId);
        if (status != null) {
            wrapper.eq("status", status);
        }
        if (type != null && !type.isEmpty()) {
            wrapper.eq("type", type);
        }
        if (title != null && !title.isEmpty()) {
            wrapper.like("title", title);
        }

        wrapper.orderByDesc("create_time");
        Page<Task> taskPage = taskMapper.selectPage(page, wrapper);

        List<TaskVO> taskVOList = taskPage.getRecords().stream().map(task -> {
            User publisher = userMapper.selectById(task.getPublisherId());
            String publisherName = publisher != null ? publisher.getNickname() : "未知";
            String publisherRole = publisher != null ? publisher.getRole() : "未知";
            User volunteer = task.getVolunteerId() != null ? userMapper.selectById(task.getVolunteerId()) : null;
            String volunteerName = volunteer != null ? volunteer.getNickname() : "未认领";
            return new TaskVO(task, publisherName, publisherRole, volunteerName);
        }).collect(Collectors.toList());

        Page<TaskVO> taskVOPage = new Page<>(pageNum, pageSize);
        taskVOPage.setRecords(taskVOList);
        taskVOPage.setTotal(taskPage.getTotal());
        taskVOPage.setPages(taskPage.getPages());
        taskVOPage.setCurrent(taskPage.getCurrent());
        taskVOPage.setSize(taskPage.getSize());

        return taskVOPage;
    }

    @Override
    public Page<TaskVO> getAdminTasks(Integer status, String type, String title, Integer pageNum, Integer pageSize) {
        Page<Task> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Task> wrapper = new QueryWrapper<>();

        if (status != null) {
            wrapper.eq("status", status);
        }
        if (type != null && !type.isEmpty()) {
            wrapper.eq("type", type);
        }
        if (title != null && !title.isEmpty()) {
            wrapper.like("title", title);
        }

        wrapper.orderByDesc("create_time");
        Page<Task> taskPage = taskMapper.selectPage(page, wrapper);

        List<TaskVO> taskVOList = taskPage.getRecords().stream().map(task -> {
            User publisher = userMapper.selectById(task.getPublisherId());
            String publisherName = publisher != null ? publisher.getNickname() : "未知";
            String publisherRole = publisher != null ? publisher.getRole() : "未知";
            User volunteer = task.getVolunteerId() != null ? userMapper.selectById(task.getVolunteerId()) : null;
            String volunteerName = volunteer != null ? volunteer.getNickname() : "未认领";
            return new TaskVO(task, publisherName, publisherRole, volunteerName);
        }).collect(Collectors.toList());

        Page<TaskVO> taskVOPage = new Page<>(pageNum, pageSize);
        taskVOPage.setRecords(taskVOList);
        taskVOPage.setTotal(taskPage.getTotal());
        taskVOPage.setPages(taskPage.getPages());
        taskVOPage.setCurrent(taskPage.getCurrent());
        taskVOPage.setSize(taskPage.getSize());

        return taskVOPage;
    }

    @Override
    @Transactional
    public void reviewTask(Long id, Long reviewerId, boolean approved, String reviewRemark) {
        Task task = taskMapper.selectById(id);
        if (task == null) {
            throw new BusinessException("任务不存在");
        }

        if (task.getStatus() != StatusConstant.TASK_PENDING_REVIEW) {
            throw new BusinessException("该任务不在待审核状态");
        }

        User reviewer = userMapper.selectById(reviewerId);
        if (reviewer == null || !"ADMIN".equals(reviewer.getRole())) {
            throw new BusinessException("只有管理员可以审核任务");
        }

        Long publisherId = task.getPublisherId();
        User publisher = userMapper.selectById(publisherId);

        if (approved) {
            task.setStatus(StatusConstant.TASK_PENDING);
            task.setReviewRemark(null);
        } else {
            task.setStatus(StatusConstant.TASK_REJECTED);
            task.setReviewRemark(reviewRemark);
        }
        task.setUpdateTime(LocalDateTime.now());
        taskMapper.updateById(task);

        // 记录任务日志
        TaskLog log = new TaskLog();
        log.setTaskId(id);
        log.setVolunteerId(reviewerId);
        log.setContent(approved ? "审核通过，任务状态变更为待认领" : "审核拒绝，原因：" + reviewRemark);
        log.setCreateTime(LocalDateTime.now());
        taskLogMapper.insert(log);

        // 发送通知给寻亲者
        if (publisher != null) {
            String notifTitle = approved ? "任务审核通过" : "任务审核未通过";
            String notifContent;
            if (approved) {
                notifContent = "您发布的任务\"" + task.getTitle() + "\"已通过审核，现在可以在任务大厅被志愿者认领了。";
            } else {
                notifContent = "您发布的任务\"" + task.getTitle() + "\"未通过审核，原因：" + reviewRemark + "。请修改后重新提交。";
            }
            notificationService.sendSeekerNotification(publisherId, notifTitle, notifContent, "TASK_REVIEWED", id, "TASK", reviewerId);
        }
    }

    @Override
    @Transactional
    public void claimTask(Long id, Long volunteerId) {
        Task task = taskMapper.selectById(id);
        if (task == null) {
            throw new BusinessException("任务不存在");
        }

        if (task.getStatus() != 0) {
            throw new BusinessException("任务状态不允许认领");
        }

        Long publisherId = task.getPublisherId();
        User publisher = userMapper.selectById(publisherId);
        User volunteer = userMapper.selectById(volunteerId);

        task.setVolunteerId(volunteerId);
        task.setStatus(1); // 1-进行中
        task.setUpdateTime(LocalDateTime.now());

        taskMapper.updateById(task);

        // 记录任务日志
        TaskLog log = new TaskLog();
        log.setTaskId(id);
        log.setVolunteerId(volunteerId);
        log.setContent("志愿者认领任务");
        log.setCreateTime(LocalDateTime.now());
        taskLogMapper.insert(log);

        // 发送通知给任务发布者
        if (publisher != null) {
            String title = "任务被认领"; 
            String content = "您发布的任务\"" + task.getTitle() + "\"已被志愿者" + (volunteer != null ? volunteer.getNickname() : "未知") + "认领。";
            String publisherRole = publisher.getRole();
            if ("ADMIN".equals(publisherRole)) {
                notificationService.sendAdminNotification(publisherId, title, content, "TASK_CLAIMED", id, "TASK", 1);
            } else {
                // 默认发送寻亲者通知，确保即使role为null也能发送
                notificationService.sendSeekerNotification(publisherId, title, content, "TASK_CLAIMED", id, "TASK", null);
            }
        }
    }

    @Override
    @Transactional
    public void updateTaskProgress(Long id, Long volunteerId, String logContent, String attachments) {
        Task task = taskMapper.selectById(id);
        if (task == null) {
            throw new BusinessException("任务不存在");
        }

        if (task.getStatus() != 1) {
            throw new BusinessException("任务状态不允许更新进度");
        }

        if (!volunteerId.equals(task.getVolunteerId())) {
            throw new BusinessException("您不是该任务的志愿者");
        }

        task.setUpdateTime(LocalDateTime.now());
        taskMapper.updateById(task);

        // 记录任务日志
        TaskLog log = new TaskLog();
        log.setTaskId(id);
        log.setVolunteerId(volunteerId);
        log.setContent(logContent);
        log.setAttachments(attachments);
        log.setCreateTime(LocalDateTime.now());
        taskLogMapper.insert(log);
    }

    @Override
    @Transactional
    public void completeTask(Long id, Long volunteerId, String result) {
        Task task = taskMapper.selectById(id);
        if (task == null) {
            throw new BusinessException("任务不存在");
        }

        if (task.getStatus() != 1) {
            throw new BusinessException("任务状态不允许完成");
        }

        if (!volunteerId.equals(task.getVolunteerId())) {
            throw new BusinessException("您不是该任务的志愿者");
        }

        Long publisherId = task.getPublisherId();
        User publisher = userMapper.selectById(publisherId);
        User volunteer = userMapper.selectById(volunteerId);

        task.setStatus(2); // 2-已完成
        task.setResult(result);
        task.setUpdateTime(LocalDateTime.now());

        taskMapper.updateById(task);

        // 记录任务日志
        TaskLog log = new TaskLog();
        log.setTaskId(id);
        log.setVolunteerId(volunteerId);
        log.setContent("任务完成：" + result);
        log.setCreateTime(LocalDateTime.now());
        taskLogMapper.insert(log);

        // 发送通知给任务发布者
        if (publisher != null) {
            String title = "任务已完成";
            String content = "您发布的任务\"" + task.getTitle() + "\"已被志愿者" + (volunteer != null ? volunteer.getNickname() : "未知") + "完成。\n完成结果：" + result;
            String publisherRole = publisher.getRole();
            if ("ADMIN".equals(publisherRole)) {
                notificationService.sendAdminNotification(publisherId, title, content, "TASK_COMPLETED", id, "TASK", 1);
            } else {
                notificationService.sendSeekerNotification(publisherId, title, content, "TASK_COMPLETED", id, "TASK", null);
            }
        }

        // 发送通知给志愿者确认提交成功
        if (volunteer != null) {
            notificationService.sendVolunteerNotification(volunteerId, "任务完成结果已提交",
                    "您已完成任务\"" + task.getTitle() + "\"，完成结果已提交给发布者。", "TASK_COMPLETED", id, "TASK", id);
        }
    }

    @Override
    @Transactional
    public void cancelTask(Long id, Long userId) {
        Task task = taskMapper.selectById(id);
        if (task == null) {
            throw new BusinessException("任务不存在");
        }

        if (task.getStatus() == 2) {
            throw new BusinessException("已完成的任务不能取消");
        }

        if (!userId.equals(task.getPublisherId())) {
            throw new BusinessException("您不是任务的发布者");
        }

        task.setStatus(3); // 3-已取消
        task.setUpdateTime(LocalDateTime.now());

        taskMapper.updateById(task);

        // 记录任务日志
        TaskLog log = new TaskLog();
        log.setTaskId(id);
        log.setVolunteerId(userId);
        log.setContent("任务被取消");
        log.setCreateTime(LocalDateTime.now());
        taskLogMapper.insert(log);
    }

    @Override
    @Transactional
    public Task updateTask(Long id, Long userId, String title, String description, String type,
                          Integer priority, String location, String requiredSkills,
                          LocalDateTime deadline) {
        Task task = taskMapper.selectById(id);
        if (task == null) {
            throw new BusinessException("任务不存在");
        }

        if (task.getStatus() != 0 && task.getStatus() != 1) {
            throw new BusinessException("只有待认领或进行中的任务才能修改");
        }

        if (!userId.equals(task.getPublisherId())) {
            throw new BusinessException("您不是任务的发布者，无权修改");
        }

        task.setTitle(title);
        task.setDescription(description);
        task.setType(type);
        task.setPriority(priority);
        task.setLocation(location);
        task.setRequiredSkills(requiredSkills);
        task.setDeadline(deadline);
        task.setUpdateTime(LocalDateTime.now());

        taskMapper.updateById(task);

        return task;
    }

    @Override
    @Transactional
    public void deleteTask(Long id, Long userId) {
        Task task = taskMapper.selectById(id);
        if (task == null) {
            throw new BusinessException("任务不存在");
        }

        // 检查用户角色
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 管理员可以删除任何任务，不需要检查状态和发布者
        if ("ADMIN".equals(user.getRole())) {
            taskMapper.physicalDeleteById(id);

            QueryWrapper<TaskLog> wrapper = new QueryWrapper<>();
            wrapper.eq("task_id", id);
            taskLogMapper.delete(wrapper);
            return;
        }

        // 寻亲者只能删除自己发布的任务，不管任务状态
        if ("SEEKER".equals(user.getRole())) {
            if (!userId.equals(task.getPublisherId())) {
                throw new BusinessException("您不是任务的发布者，无权删除");
            }
            taskMapper.physicalDeleteById(id);

            QueryWrapper<TaskLog> wrapper = new QueryWrapper<>();
            wrapper.eq("task_id", id);
            taskLogMapper.delete(wrapper);
            return;
        }

        // 其他角色（如志愿者）无权删除任务
        throw new BusinessException("您没有权限删除任务");
    }

    @Override
    public List<TaskLog> getTaskLogs(Long taskId) {
        QueryWrapper<TaskLog> wrapper = new QueryWrapper<>();
        wrapper.eq("task_id", taskId);
        wrapper.orderByDesc("create_time");
        return taskLogMapper.selectList(wrapper);
    }
}
