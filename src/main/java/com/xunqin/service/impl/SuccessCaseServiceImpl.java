package com.xunqin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xunqin.common.exception.BusinessException;
import com.xunqin.entity.SuccessCase;
import com.xunqin.mapper.SuccessCaseMapper;
import com.xunqin.service.SuccessCaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

@Service
public class SuccessCaseServiceImpl implements SuccessCaseService {

    @Autowired
    private SuccessCaseMapper successCaseMapper;
    
    @Autowired
    private com.xunqin.service.UserService userService;

    @Override
    @Transactional
    public SuccessCase createSuccessCase(Long userId, String title, String content, Long missingPersonId,
                                       String reunionTime, String reunionLocation, String photos,
                                       String videos, String tags, boolean isAdmin) {
        SuccessCase successCase = new SuccessCase();
        successCase.setSeekerId(userId);
        successCase.setTitle(title);
        successCase.setStory(content);
        successCase.setMissingPersonId(missingPersonId);
        successCase.setReunionDate(reunionTime);
        successCase.setReunionLocation(reunionLocation);
        successCase.setPhotos(photos);
        successCase.setVideos(videos);
        // successCase.setTags(tags);
        successCase.setViewCount(0);
        // 管理员发布的案例自动通过审核，其他用户发布的案例需要审核
        successCase.setStatus(isAdmin ? 1 : 0); // 0: 待审核, 1: 已通过, 2: 已拒绝
        successCase.setCreateTime(LocalDateTime.now());

        successCaseMapper.insert(successCase);
        return successCase;
    }

    @Override
    public Page<SuccessCase> getSuccessCases(String status, String tags, Integer pageNum, Integer pageSize) {
        Page<SuccessCase> page = new Page<>(pageNum, pageSize);
        QueryWrapper<SuccessCase> wrapper = new QueryWrapper<>();

        if (status != null) {
            try {
                wrapper.eq("status", Integer.parseInt(status));
            } catch (NumberFormatException e) {
                // 忽略无效的status值
            }
        } else {
            // 默认只显示已审核通过的
            wrapper.eq("status", 1);
        }

        // if (tags != null) {
        //     wrapper.like("tags", tags);
        // }

        wrapper.orderByDesc("create_time");
        return successCaseMapper.selectPage(page, wrapper);
    }

    @Override
    @Transactional
    public SuccessCase getSuccessCaseById(Long id) {
        SuccessCase successCase = successCaseMapper.selectById(id);
        if (successCase == null) {
            throw new BusinessException("案例不存在");
        }

        // 增加浏览次数
        successCase.setViewCount(successCase.getViewCount() + 1);
        successCaseMapper.updateById(successCase);

        return successCase;
    }

    @Override
    @Transactional
    public SuccessCase updateSuccessCase(Long id, Long userId, String title, String content, Long missingPersonId,
                                       String reunionTime, String reunionLocation, String photos,
                                       String videos, String tags) {
        SuccessCase successCase = successCaseMapper.selectById(id);
        if (successCase == null) {
            throw new BusinessException("案例不存在");
        }

        com.xunqin.entity.User user = userService.getUserById(userId);
        boolean isAdmin = user != null && "ADMIN".equals(user.getRole());

        if (!isAdmin && !successCase.getSeekerId().equals(userId)) {
            throw new BusinessException("无权修改他人的案例");
        }

        successCase.setTitle(title);
        successCase.setStory(content);
        successCase.setMissingPersonId(missingPersonId);
        successCase.setReunionDate(reunionTime);
        successCase.setReunionLocation(reunionLocation);
        if (photos != null) {
            successCase.setPhotos(photos);
        }
        successCase.setVideos(videos);
        // 管理员编辑后保持已通过状态，其他用户编辑后需要重新审核
        if (!isAdmin) {
            successCase.setStatus(0);
        }
        successCase.setUpdateTime(LocalDateTime.now());

        successCaseMapper.updateById(successCase);
        return successCase;
    }

    @Override
    @Transactional
    public void deleteSuccessCase(Long id, Long userId) {
        SuccessCase successCase = successCaseMapper.selectById(id);
        if (successCase == null) {
            throw new BusinessException("案例不存在");
        }

        // 检查权限：管理员可以删除任何案例，普通用户只能删除自己的
        com.xunqin.entity.User user = null;
        try {
            user = userService.getUserById(userId);
        } catch (Exception e) {
            throw new BusinessException("用户不存在");
        }
        
        if (!user.getRole().equals("ADMIN") && !successCase.getSeekerId().equals(userId)) {
            throw new BusinessException("无权删除他人的案例");
        }

        // 删除关联的图片文件
        String photos = successCase.getPhotos();
        if (photos != null && !photos.isEmpty()) {
            deletePhotos(photos);
        }

        // 使用物理删除
        successCaseMapper.physicalDeleteById(id);
    }

    /**
     * 删除图片文件
     */
    private void deletePhotos(String photos) {
        String[] photoUrls = photos.split(",");
        for (String photoUrl : photoUrls) {
            if (photoUrl != null && !photoUrl.isEmpty()) {
                try {
                    // 从URL中提取文件路径
                    String filePath = photoUrl;
                    if (photoUrl.startsWith("/uploads/")) {
                        filePath = System.getProperty("user.dir") + photoUrl;
                    }
                    
                    Path path = Paths.get(filePath);
                    if (Files.exists(path)) {
                        Files.delete(path);
                        System.out.println("删除图片文件: " + filePath);
                    }
                } catch (IOException e) {
                    System.err.println("删除图片文件失败: " + photoUrl + ", 错误: " + e.getMessage());
                }
            }
        }
    }

    @Override
    @Transactional
    public void approveSuccessCase(Long id, String approvalRemark) {
        SuccessCase successCase = successCaseMapper.selectById(id);
        if (successCase == null) {
            throw new BusinessException("案例不存在");
        }

        successCase.setStatus(1);
        successCase.setApprovalRemark(approvalRemark);
        successCase.setUpdateTime(LocalDateTime.now());

        successCaseMapper.updateById(successCase);
    }

    @Override
    @Transactional
    public void rejectSuccessCase(Long id, String rejectionRemark) {
        SuccessCase successCase = successCaseMapper.selectById(id);
        if (successCase == null) {
            throw new BusinessException("案例不存在");
        }

        successCase.setStatus(2);
        successCase.setApprovalRemark(rejectionRemark);
        successCase.setUpdateTime(LocalDateTime.now());

        successCaseMapper.updateById(successCase);
    }

    @Override
    public List<SuccessCase> getHotSuccessCases(Integer limit) {
        Page<SuccessCase> page = new Page<>(1, limit);
        QueryWrapper<SuccessCase> wrapper = new QueryWrapper<>();

        wrapper.eq("status", 1)
               .orderByDesc("view_count")
               .orderByDesc("create_time");

        return successCaseMapper.selectPage(page, wrapper).getRecords();
    }

    @Override
    public Page<SuccessCase> getMySuccessCases(Long userId, String status, Integer pageNum, Integer pageSize) {
        Page<SuccessCase> page = new Page<>(pageNum, pageSize);
        QueryWrapper<SuccessCase> wrapper = new QueryWrapper<>();

        wrapper.eq("seeker_id", userId);
        if (status != null) {
            try {
                wrapper.eq("status", Integer.parseInt(status));
            } catch (NumberFormatException e) {
                // 忽略无效的status值
            }
        }

        wrapper.orderByDesc("create_time");
        return successCaseMapper.selectPage(page, wrapper);
    }

    @Override
    public String uploadPhotos(MultipartFile[] photos) {
        if (photos == null || photos.length == 0) {
            return null;
        }

        StringBuilder photosUrls = new StringBuilder();
        String uploadDir = System.getProperty("user.dir") + "/uploads/success-cases/";

        // 确保上传目录存在
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            try {
                Files.createDirectories(uploadPath);
            } catch (IOException e) {
                throw new RuntimeException("创建上传目录失败", e);
            }
        }

        for (MultipartFile photo : photos) {
            if (!photo.isEmpty()) {
                try {
                    // 生成唯一文件名
                    String originalFilename = photo.getOriginalFilename();
                    String fileExtension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf('.')) : ".jpg";
                    String uniqueFilename = UUID.randomUUID().toString() + fileExtension;

                    // 保存文件
                    Path filePath = uploadPath.resolve(uniqueFilename);
                    Files.copy(photo.getInputStream(), filePath);

                    // 构建文件URL
                    String fileUrl = "/uploads/success-cases/" + uniqueFilename;
                    if (photosUrls.length() > 0) {
                        photosUrls.append(",");
                    }
                    photosUrls.append(fileUrl);
                } catch (IOException e) {
                    throw new RuntimeException("文件上传失败", e);
                }
            }
        }

        return photosUrls.toString();
    }
}
