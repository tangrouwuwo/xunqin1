package com.xunqin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xunqin.entity.MissingPerson;

public interface MissingPersonService {

    /**
     * 搜索寻亲信息
     */
    Page<MissingPerson> searchMissingPersons(String name, String gender, String location, String province, String startDate, String endDate,
                                            Integer pageNum, Integer pageSize);

    /**
     * 根据ID获取寻亲信息
     */
    MissingPerson getMissingPersonById(Long id);

    /**
     * 获取热门寻亲信息
     */
    java.util.List<MissingPerson> getHotMissingPersons(Integer limit);

    /**
     * 创建寻亲信息
     */
    MissingPerson createMissingPerson(Long userId, MissingPerson missingPerson);

    /**
     * 创建寻亲信息（带图片上传）
     */
    MissingPerson createMissingPerson(Long userId, MissingPerson missingPerson, org.springframework.web.multipart.MultipartFile[] photos);

    /**
     * 更新寻亲信息
     */
    MissingPerson updateMissingPerson(Long id, Long userId, MissingPerson missingPerson);

    /**
     * 更新寻亲信息（带图片上传）
     */
    MissingPerson updateMissingPerson(Long id, Long userId, MissingPerson missingPerson, org.springframework.web.multipart.MultipartFile[] photos);

    /**
     * 删除寻亲信息
     */
    void deleteMissingPerson(Long id, Long userId);

    /**
     * 获取寻亲者的寻亲信息
     */
    Page<MissingPerson> getMissingPersonsBySeeker(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 审核寻亲信息（通过）
     */
    void approveMissingPerson(Long id, String approvalRemark);

    /**
     * 审核寻亲信息（拒绝）
     */
    void rejectMissingPerson(Long id, String rejectionRemark);

    /**
     * 获取寻亲者发布的所有寻亲信息的ID列表
     */
    java.util.List<Long> getMissingPersonIdsByUserId(Long userId);

    /**
     * 获取寻亲信息列表（管理员）
     */
    com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.xunqin.vo.MissingPersonVO> getMissingPersonsForAdmin(String status, String name, String username, Integer pageNum, Integer pageSize);

    /**
     * 更新寻亲信息状态
     */
    void updateMissingPersonStatus(Long id, String status);

    /**
     * 获取寻亲信息的变更记录
     */
    java.util.List<com.xunqin.entity.MissingPersonChangeLog> getChangeLogs(Long missingPersonId);
}
