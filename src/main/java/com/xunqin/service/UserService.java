package com.xunqin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xunqin.entity.User;

public interface UserService {

    /**
     * 用户登录
     */
    User login(String username, String password);

    /**
     * 用户注册
     */
    User register(String phone, String password, String role, String nickname, String email);

    /**
     * 用户注册（密码已加密）
     */
    User registerWithEncodedPassword(String phone, String encodedPassword, String role, String username, String email);

    /**
     * 根据ID获取用户
     */
    User getUserById(Long id);

    /**
     * 更新用户资料
     */
    void updateProfile(Long userId, String nickname, String avatar, String email, String phone);

    /**
     * 修改密码
     */
    void changePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 重置密码
     */
    void resetPassword(Long userId, String newPassword);

    /**
     * 获取用户列表（管理员）
     */
    Page<User> getUsers(String username, String role, Integer status, Integer pageNum, Integer pageSize);

    /**
     * 更新用户状态
     */
    void updateUserStatus(Long userId, Integer status);

    /**
     * 审核用户实名认证
     */
    void auditUserRealName(Long userId, Integer status, String auditRemark);

    /**
     * 删除用户
     */
    void deleteUser(Long userId);

    /**
     * 更新用户头像
     */
    void updateAvatar(Long userId, String avatarUrl);
    
    /**
     * 获取所有管理员
     */
    java.util.List<User> getAdmins();

    /**
     * 管理员更新用户资料（昵称、邮箱）
     */
    void updateUser(Long userId, String nickname, String email);

    /**
     * 管理员创建用户
     */
    User createUser(String username, String phone, String password, String role);
}
