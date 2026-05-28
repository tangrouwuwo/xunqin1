package com.xunqin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xunqin.common.exception.BusinessException;
import com.xunqin.entity.User;
import com.xunqin.entity.VolunteerApplication;
import com.xunqin.mapper.UserMapper;
import com.xunqin.mapper.VolunteerApplicationMapper;
import com.xunqin.service.NotificationService;
import com.xunqin.service.UserService;
import com.xunqin.service.VolunteerApplicationService;
import com.xunqin.vo.VolunteerApplicationVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class VolunteerApplicationServiceImpl implements VolunteerApplicationService {

    @Autowired
    private VolunteerApplicationMapper applicationMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public VolunteerApplication submitApplication(VolunteerApplication application) {
        log.info("提交志愿者申请, username: {}, phone: {}", application.getUsername(), application.getPhone());

        QueryWrapper<VolunteerApplication> usernameWrapper = new QueryWrapper<>();
        usernameWrapper.eq("username", application.getUsername());
        if (applicationMapper.selectCount(usernameWrapper) > 0) {
            throw new BusinessException("用户名已存在");
        }

        QueryWrapper<VolunteerApplication> phoneWrapper = new QueryWrapper<>();
        phoneWrapper.eq("phone", application.getPhone());
        List<VolunteerApplication> existingApplications = applicationMapper.selectList(phoneWrapper);
        for (VolunteerApplication existing : existingApplications) {
            if (existing.getStatus() == 0) {
                throw new BusinessException("该手机号已有待审核的申请");
            }
            if (existing.getStatus() == 1) {
                throw new BusinessException("该手机号已通过审核，请直接登录");
            }
        }

        QueryWrapper<User> userPhoneWrapper = new QueryWrapper<>();
        userPhoneWrapper.eq("phone", application.getPhone()).eq("role", "VOLUNTEER");
        if (userMapper.selectCount(userPhoneWrapper) > 0) {
            throw new BusinessException("该手机号已注册为志愿者，请直接登录");
        }

        application.setPassword(passwordEncoder.encode(application.getPassword()));
        application.setStatus(0);
        applicationMapper.insert(application);

        log.info("志愿者申请提交成功, id: {}", application.getId());

        // 发送通知给所有管理员
        sendNotificationToAdmins(application);

        return application;
    }

    private void sendNotificationToAdmins(VolunteerApplication application) {
        try {
            List<User> admins = userService.getAdmins();
            String title = "新的志愿者申请";
            String content = String.format("用户 %s (%s) 提交了志愿者申请，请及时审核。", 
                    application.getName(), application.getPhone());
            
            for (User admin : admins) {
                notificationService.sendAdminNotification(
                        admin.getId(),
                        title,
                        content,
                        "VOLUNTEER_APPLICATION",
                        application.getId(),
                        "VOLUNTEER_APPLICATION",
                        1 // 高优先级
                );
            }
            log.info("已向 {} 位管理员发送志愿者申请通知", admins.size());
        } catch (Exception e) {
            log.error("发送管理员通知失败: {}", e.getMessage(), e);
        }
    }

    @Override
    public VolunteerApplicationVO getApplicationById(Long id) {
        VolunteerApplication application = applicationMapper.selectById(id);
        if (application == null) {
            throw new BusinessException("申请不存在");
        }
        return convertToVO(application);
    }

    @Override
    public Page<VolunteerApplicationVO> getApplications(Integer status, String name, Integer pageNum, Integer pageSize) {
        Page<VolunteerApplication> page = new Page<>(pageNum, pageSize);
        QueryWrapper<VolunteerApplication> wrapper = new QueryWrapper<>();

        if (status != null) {
            wrapper.eq("status", status);
        }
        if (name != null && !name.trim().isEmpty()) {
            wrapper.like("name", name).or().like("username", name);
        }

        wrapper.orderByDesc("create_time");

        Page<VolunteerApplication> applicationPage = applicationMapper.selectPage(page, wrapper);

        Page<VolunteerApplicationVO> voPage = new Page<>();
        BeanUtils.copyProperties(applicationPage, voPage, "records");
        voPage.setRecords(applicationPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList()));

        return voPage;
    }

    @Override
    public void approveApplication(Long id, Long reviewerId, String reviewComment) {
        log.info("审核通过志愿者申请, id: {}, reviewerId: {}", id, reviewerId);

        try {
            VolunteerApplication application = applicationMapper.selectById(id);
            if (application == null) {
                log.error("申请不存在, id: {}", id);
                throw new BusinessException("申请不存在");
            }

            if (application.getStatus() != 0) {
                log.error("该申请已被审核, id: {}, status: {}", id, application.getStatus());
                throw new BusinessException("该申请已被审核");
            }

            log.info("准备注册用户, phone: {}, username: {}", application.getPhone(), application.getUsername());
            User user = userService.registerWithEncodedPassword(
                    application.getPhone(),
                    application.getPassword(),
                    "VOLUNTEER",
                    application.getUsername(),
                    application.getEmail()
            );
            log.info("用户注册成功, userId: {}", user.getId());

            application.setStatus(1);
            application.setReviewComment(reviewComment);
            application.setReviewerId(reviewerId);
            application.setReviewTime(LocalDateTime.now());
            application.setUserId(user.getId());
            int result = applicationMapper.updateById(application);
            log.info("申请状态更新结果: {}", result);

            log.info("志愿者申请审核通过, applicationId: {}, userId: {}", id, user.getId());
        } catch (Exception e) {
            log.error("审核失败: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public void rejectApplication(Long id, Long reviewerId, String reviewComment) {
        log.info("审核拒绝志愿者申请, id: {}, reviewerId: {}", id, reviewerId);

        VolunteerApplication application = applicationMapper.selectById(id);
        if (application == null) {
            throw new BusinessException("申请不存在");
        }

        if (application.getStatus() != 0) {
            throw new BusinessException("该申请已被审核");
        }

        application.setStatus(2);
        application.setReviewComment(reviewComment);
        application.setReviewerId(reviewerId);
        application.setReviewTime(LocalDateTime.now());
        applicationMapper.updateById(application);

        log.info("志愿者申请审核拒绝, applicationId: {}", id);
    }

    @Override
    public void deleteApplication(Long id) {
        log.info("删除志愿者申请, id: {}", id);

        VolunteerApplication application = applicationMapper.selectByIdIgnoreLogicDelete(id);
        if (application == null) {
            throw new BusinessException("申请不存在");
        }

        // 如果申请已经通过并创建了用户，需要同时删除用户记录
        if (application.getStatus() == 1 && application.getUserId() != null) {
            log.info("申请已通过，同时删除关联的用户记录, userId: {}", application.getUserId());
            userMapper.deleteByIdPhysical(application.getUserId());
        }

        int result = applicationMapper.physicalDeleteById(id);
        log.info("志愿者申请删除成功, id: {}, 影响行数: {}", id, result);
    }

    @Override
    public VolunteerApplicationVO getMyApplication(String phone) {
        QueryWrapper<VolunteerApplication> wrapper = new QueryWrapper<>();
        wrapper.eq("phone", phone);
        wrapper.orderByDesc("create_time");
        wrapper.last("LIMIT 1");

        VolunteerApplication application = applicationMapper.selectOne(wrapper);
        if (application == null) {
            return null;
        }

        return convertToVO(application);
    }

    private VolunteerApplicationVO convertToVO(VolunteerApplication application) {
        VolunteerApplicationVO vo = new VolunteerApplicationVO();
        BeanUtils.copyProperties(application, vo);

        if (application.getReviewerId() != null) {
            User reviewer = userMapper.selectById(application.getReviewerId());
            if (reviewer != null) {
                vo.setReviewerName(reviewer.getNickname() != null ? reviewer.getNickname() : reviewer.getUsername());
            }
        }

        return vo;
    }
}
