package com.xunqin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xunqin.common.constant.StatusConstant;
import com.xunqin.common.exception.BusinessException;
import com.xunqin.entity.Clue;
import com.xunqin.entity.MissingPerson;
import com.xunqin.mapper.ClueMapper;
import com.xunqin.mapper.MissingPersonMapper;
import com.xunqin.service.ClueService;
import com.xunqin.service.MissingPersonService;
import com.xunqin.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ClueServiceImpl implements ClueService {

    @Autowired
    private ClueMapper clueMapper;

    @Autowired
    private MissingPersonMapper missingPersonMapper;

    @Autowired
    private MissingPersonService missingPersonService;

    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private com.xunqin.service.UserService userService;

    @Override
    @Transactional
    public Clue createClue(Long userId, Long missingPersonId, Integer isAnonymous, String content,
                          String contactName, String contactPhone, String contactEmail) {
        MissingPerson missingPerson = missingPersonMapper.selectById(missingPersonId);
        if (missingPerson == null) {
            throw new BusinessException("寻亲信息不存在");
        }

        Clue clue = new Clue();
        clue.setProviderId(userId);
        clue.setMissingPersonId(missingPersonId);
        clue.setIsAnonymous(isAnonymous != null ? isAnonymous : 0);
        clue.setContent(content);
        clue.setContactName(contactName);
        clue.setContactPhone(contactPhone);
        clue.setContactEmail(contactEmail);
        clue.setStatus(StatusConstant.CLUE_PENDING);
        clue.setCreateTime(LocalDateTime.now());

        clueMapper.insert(clue);

        // 通知管理员有新线索待审核
        List<com.xunqin.entity.User> admins = userService.getAdmins();
        for (com.xunqin.entity.User admin : admins) {
            String title = "新的线索待审核";
            String contentText = "用户提交了一条关于\"" + missingPerson.getName() + "\"的新线索，请尽快审核。";
            notificationService.sendAdminNotification(admin.getId(), title, contentText, "NEW_CLUE", clue.getId(), "CLUE", 1);
        }

        return clue;
    }

    @Override
    public Page<Clue> getClues(Integer status, Long missingPersonId, String content, Integer pageNum, Integer pageSize) {
        Page<Clue> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Clue> wrapper = new QueryWrapper<>();

        if (status != null) {
            wrapper.eq("status", status);
        }
        if (missingPersonId != null) {
            wrapper.eq("missing_person_id", missingPersonId);
        }
        if (content != null && !content.isEmpty()) {
            wrapper.like("content", content);
        }

        wrapper.orderByDesc("create_time");
        Page<Clue> resultPage = clueMapper.selectPage(page, wrapper);
        enrichClueNames(resultPage.getRecords());
        return resultPage;
    }

    private void enrichClueNames(List<Clue> clues) {
        for (Clue clue : clues) {
            if (clue.getMissingPersonId() != null) {
                MissingPerson mp = missingPersonMapper.selectById(clue.getMissingPersonId());
                if (mp != null) {
                    clue.setMissingPersonName(mp.getName());
                }
            }
            if (clue.getProviderId() != null) {
                com.xunqin.entity.User provider = userService.getUserById(clue.getProviderId());
                if (provider != null) {
                    clue.setSubmitterName(provider.getNickname() != null ? provider.getNickname() : provider.getUsername());
                }
            }
        }
    }

    @Override
    public Page<Clue> getMyClues(Long userId, Integer status, Integer pageNum, Integer pageSize) {
        Page<Clue> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Clue> wrapper = new QueryWrapper<>();

        wrapper.eq("provider_id", userId);
        if (status != null) {
            wrapper.eq("status", status);
        }

        wrapper.orderByDesc("create_time");
        Page<Clue> resultPage = clueMapper.selectPage(page, wrapper);
        enrichClueNames(resultPage.getRecords());
        return resultPage;
    }

    @Override
    public Clue getClueById(Long id) {
        Clue clue = clueMapper.selectById(id);
        if (clue == null) {
            throw new BusinessException("线索不存在");
        }
        enrichClueNames(java.util.Collections.singletonList(clue));
        return clue;
    }

    @Override
    @Transactional
    public Clue handleClue(Long id, Long handlerId, Integer status, String handleResult, String handleRemark) {
        Clue clue = clueMapper.selectById(id);
        if (clue == null) {
            throw new BusinessException("线索不存在");
        }

        clue.setStatus(status);
        clue.setHandlerId(handlerId);
        clue.setHandleResult(handleResult);
        clue.setHandleRemark(handleRemark);
        clue.setHandleTime(LocalDateTime.now());

        clueMapper.updateById(clue);
        return clue;
    }

    @Override
    @Transactional
    public Clue approveClue(Long id, Long adminId, String handleRemark) {
        Clue clue = clueMapper.selectById(id);
        if (clue == null) {
            throw new BusinessException("线索不存在");
        }
        if (clue.getStatus() != StatusConstant.CLUE_PENDING) {
            throw new BusinessException("该线索已被处理，不能重复审核");
        }

        clue.setStatus(StatusConstant.CLUE_VERIFIED);
        clue.setHandlerId(adminId);
        clue.setHandleResult("审核通过");
        clue.setHandleRemark(handleRemark);
        clue.setHandleTime(LocalDateTime.now());

        clueMapper.updateById(clue);

        // 获取关联的寻亲信息
        MissingPerson missingPerson = missingPersonMapper.selectById(clue.getMissingPersonId());

        // 通知线索提供者：审核通过
        if (clue.getProviderId() != null) {
            String title = "线索审核通过";
            String msg = "您提供的关于\"" + (missingPerson != null ? missingPerson.getName() : "寻亲信息") + "\"的线索已审核通过，感谢您的贡献！";
            notificationService.sendClueProviderNotification(clue.getProviderId(), title, msg, "CLUE_APPROVED", clue.getId(), "CLUE", clue.getId());
        }

        // 通知寻亲者：收到新线索（审核通过后才通知寻亲者）
        if (missingPerson != null && missingPerson.getSeekerId() != null) {
            String title = "收到新线索";
            String msg = "您发布的寻亲信息\"" + missingPerson.getName() + "\"收到了一条审核通过的线索，快去查看吧！";
            notificationService.sendSeekerNotification(missingPerson.getSeekerId(), title, msg, "NEW_CLUE", clue.getId(), "CLUE", clue.getProviderId());
        }

        return clue;
    }

    @Override
    @Transactional
    public Clue rejectClue(Long id, Long adminId, String rejectReason) {
        Clue clue = clueMapper.selectById(id);
        if (clue == null) {
            throw new BusinessException("线索不存在");
        }
        if (clue.getStatus() != StatusConstant.CLUE_PENDING) {
            throw new BusinessException("该线索已被处理，不能重复审核");
        }

        clue.setStatus(StatusConstant.CLUE_REJECTED);
        clue.setHandlerId(adminId);
        clue.setHandleResult("审核驳回");
        clue.setHandleRemark(rejectReason);
        clue.setHandleTime(LocalDateTime.now());

        clueMapper.updateById(clue);

        // 获取关联的寻亲信息
        MissingPerson missingPerson = missingPersonMapper.selectById(clue.getMissingPersonId());

        // 通知线索提供者：审核驳回
        if (clue.getProviderId() != null) {
            String title = "线索审核未通过";
            String msg = "您提供的关于\"" + (missingPerson != null ? missingPerson.getName() : "寻亲信息") + "\"的线索未通过审核";
            if (rejectReason != null && !rejectReason.isEmpty()) {
                msg += "，原因：" + rejectReason;
            }
            notificationService.sendClueProviderNotification(clue.getProviderId(), title, msg, "CLUE_REJECTED", clue.getId(), "CLUE", clue.getId());
        }

        return clue;
    }

    @Override
    public Page<Clue> getCluesByMissingPersonId(Long missingPersonId, Integer pageNum, Integer pageSize) {
        Page<Clue> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Clue> wrapper = new QueryWrapper<>();

        wrapper.eq("missing_person_id", missingPersonId);
        wrapper.orderByDesc("create_time");
        return clueMapper.selectPage(page, wrapper);
    }

    @Override
    public void assignClueToVolunteer(Long clueId, Long volunteerId) {
        Clue clue = clueMapper.selectById(clueId);
        if (clue == null) {
            throw new BusinessException("线索不存在");
        }
    }

    @Override
    @Transactional
    public Clue updateClue(Long id, Long userId, Integer isAnonymous, String content, 
                          String contactName, String contactPhone, String contactEmail) {
        Clue clue = clueMapper.selectById(id);
        if (clue == null) {
            throw new BusinessException("线索不存在");
        }

        if (!clue.getProviderId().equals(userId)) {
            throw new BusinessException("无权修改此线索");
        }

        clue.setIsAnonymous(isAnonymous != null ? isAnonymous : 0);
        clue.setContent(content);
        clue.setContactName(contactName);
        clue.setContactPhone(contactPhone);
        clue.setContactEmail(contactEmail);

        // 修改后重新置为待审核状态
        if (clue.getStatus() != StatusConstant.CLUE_PENDING) {
            clue.setStatus(StatusConstant.CLUE_PENDING);
            clue.setHandlerId(null);
            clue.setHandleResult(null);
            clue.setHandleRemark(null);
            clue.setHandleTime(null);
        }

        clueMapper.updateById(clue);

        return clue;
    }

    @Override
    @Transactional
    public Clue adminUpdateClue(Long id, Integer isAnonymous, String content,
                               String contactName, String contactPhone, String contactEmail) {
        Clue clue = clueMapper.selectById(id);
        if (clue == null) {
            throw new BusinessException("线索不存在");
        }

        clue.setIsAnonymous(isAnonymous != null ? isAnonymous : clue.getIsAnonymous());
        clue.setContent(content != null ? content : clue.getContent());
        clue.setContactName(contactName);
        clue.setContactPhone(contactPhone);
        clue.setContactEmail(contactEmail);

        clueMapper.updateById(clue);

        return clue;
    }

    @Override
    @Transactional
    public void deleteClue(Long id, Long userId) {
        Clue clue = clueMapper.selectById(id);
        if (clue == null) {
            throw new BusinessException("线索不存在");
        }

        if (!clue.getProviderId().equals(userId)) {
            throw new BusinessException("无权删除此线索");
        }

        clueMapper.physicalDeleteById(id);
    }

    @Override
    @Transactional
    public void adminDeleteClue(Long id) {
        Clue clue = clueMapper.selectById(id);
        if (clue == null) {
            throw new BusinessException("线索不存在");
        }
        clueMapper.physicalDeleteById(id);
    }

    @Override
    public Page<Clue> getSeekerClues(Long seekerId, Integer status, Integer pageNum, Integer pageSize) {
        Page<Clue> page = new Page<>(pageNum, pageSize);
        
        List<Long> missingPersonIds = missingPersonService.getMissingPersonIdsByUserId(seekerId);
        
        if (missingPersonIds == null || missingPersonIds.isEmpty()) {
            return page;
        }
        
        QueryWrapper<Clue> wrapper = new QueryWrapper<>();
        wrapper.in("missing_person_id", missingPersonIds);
        
        // 寻亲者只能看见已审核通过的线索（status=1）
        if (status != null) {
            wrapper.eq("status", status);
        } else {
            wrapper.eq("status", StatusConstant.CLUE_VERIFIED);
        }
        
        wrapper.orderByDesc("create_time");
        return clueMapper.selectPage(page, wrapper);
    }
}
