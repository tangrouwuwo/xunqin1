package com.xunqin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xunqin.entity.SuccessCase;

import java.util.List;

public interface SuccessCaseService {

    /**
     * 创建成功案例
     */
    SuccessCase createSuccessCase(Long userId, String title, String content, Long missingPersonId,
                                 String reunionTime, String reunionLocation, String photos,
                                 String videos, String tags, boolean isAdmin);

    /**
     * 获取成功案例列表
     */
    Page<SuccessCase> getSuccessCases(String status, String tags, Integer pageNum, Integer pageSize);

    /**
     * 获取成功案例详情
     */
    SuccessCase getSuccessCaseById(Long id);

    /**
     * 更新成功案例
     */
    SuccessCase updateSuccessCase(Long id, Long userId, String title, String content, Long missingPersonId,
                                 String reunionTime, String reunionLocation, String photos,
                                 String videos, String tags);

    /**
     * 删除成功案例
     */
    void deleteSuccessCase(Long id, Long userId);

    /**
     * 审核通过成功案例
     */
    void approveSuccessCase(Long id, String approvalRemark);

    /**
     * 审核拒绝成功案例
     */
    void rejectSuccessCase(Long id, String rejectionRemark);

    /**
     * 获取热门成功案例
     */
    List<SuccessCase> getHotSuccessCases(Integer limit);

    /**
     * 获取我的成功案例
     */
    Page<SuccessCase> getMySuccessCases(Long userId, String status, Integer pageNum, Integer pageSize);

    /**
     * 上传照片
     */
    String uploadPhotos(org.springframework.web.multipart.MultipartFile[] photos);
}
