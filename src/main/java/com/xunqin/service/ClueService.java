package com.xunqin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xunqin.entity.Clue;

public interface ClueService {

    /**
     * 提交线索（提交后状态为待审核）
     */
    Clue createClue(Long userId, Long missingPersonId, Integer isAnonymous, String content,
                    String contactName, String contactPhone, String contactEmail);

    /**
     * 获取线索列表（管理员/志愿者）
     */
    Page<Clue> getClues(Integer status, Long missingPersonId, String content, Integer pageNum, Integer pageSize);

    /**
     * 获取我的线索（线索提供者）
     */
    Page<Clue> getMyClues(Long userId, Integer status, Integer pageNum, Integer pageSize);

    /**
     * 根据ID获取线索
     */
    Clue getClueById(Long id);

    /**
     * 处理线索（旧方法，保留兼容）
     */
    Clue handleClue(Long id, Long handlerId, Integer status, String handleResult, String handleRemark);

    /**
     * 管理员审核通过线索
     */
    Clue approveClue(Long id, Long adminId, String handleRemark);

    /**
     * 管理员驳回线索
     */
    Clue rejectClue(Long id, Long adminId, String rejectReason);

    /**
     * 获取寻亲信息的线索列表
     */
    Page<Clue> getCluesByMissingPersonId(Long missingPersonId, Integer pageNum, Integer pageSize);

    /**
     * 分配线索给志愿者
     */
    void assignClueToVolunteer(Long clueId, Long volunteerId);

    /**
     * 修改线索（提供者修改自己的线索，仅待审核状态可修改）
     */
    Clue updateClue(Long id, Long userId, Integer isAnonymous, String content, 
                   String contactName, String contactPhone, String contactEmail);

    /**
     * 管理员修改任何线索
     */
    Clue adminUpdateClue(Long id, Integer isAnonymous, String content,
                        String contactName, String contactPhone, String contactEmail);

    /**
     * 删除线索（提供者删除自己的线索，仅待审核状态可删除）
     */
    void deleteClue(Long id, Long userId);

    /**
     * 管理员删除任何线索
     */
    void adminDeleteClue(Long id);

    /**
     * 获取寻亲者相关的线索（审核通过后寻亲者可以看到）
     */
    Page<Clue> getSeekerClues(Long seekerId, Integer status, Integer pageNum, Integer pageSize);
}
