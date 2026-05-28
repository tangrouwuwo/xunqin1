package com.xunqin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xunqin.entity.VolunteerApplication;
import com.xunqin.vo.VolunteerApplicationVO;

public interface VolunteerApplicationService {

    VolunteerApplication submitApplication(VolunteerApplication application);

    VolunteerApplicationVO getApplicationById(Long id);

    Page<VolunteerApplicationVO> getApplications(Integer status, String name, Integer pageNum, Integer pageSize);

    void approveApplication(Long id, Long reviewerId, String reviewComment);

    void rejectApplication(Long id, Long reviewerId, String reviewComment);

    void deleteApplication(Long id);

    VolunteerApplicationVO getMyApplication(String phone);
}
