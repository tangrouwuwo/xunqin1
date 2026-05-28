package com.xunqin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xunqin.common.exception.BusinessException;
import com.xunqin.entity.User;
import com.xunqin.mapper.UserMapper;
import com.xunqin.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public User login(String username, String password) {
        log.info("用户登录, username: {}", username);
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        // 支持用户名或手机号登录
        wrapper.eq("username", username).or().eq("phone", username);
        User user = userMapper.selectOne(wrapper);

        if (user == null) {
            log.warn("登录失败, 用户不存在: {}", username);
            throw new BusinessException("用户名/手机号或密码错误");
        }

        if (user.getStatus() != 1) {
            log.warn("登录失败, 账号已被禁用: {}", username);
            throw new BusinessException("账号已被禁用");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.warn("登录失败, 密码错误: {}", username);
            throw new BusinessException("用户名/手机号或密码错误");
        }

        log.info("用户登录成功, username: {}, userId: {}", username, user.getId());
        return user;
    }

    @Override
    @Transactional
    public User register(String phone, String password, String role, String username, String email) {
        log.info("用户注册, phone: {}, role: {}, username: {}", phone, role, username);
        
        // 检查手机号长度是否为11位
        if (phone == null || phone.length() != 11) {
            log.warn("注册失败, 手机号长度必须为11位: {}", phone);
            throw new BusinessException("手机号长度必须为11位");
        }
        
        // 验证用户名是否为空
        if (username == null || username.trim().equals("")) {
            log.warn("注册失败, 用户名不能为空");
            throw new BusinessException("用户名不能为空");
        }

        // 检查用户名是否已存在
        QueryWrapper<User> usernameWrapper = new QueryWrapper<>();
        usernameWrapper.eq("username", username);
        if (userMapper.selectCount(usernameWrapper) > 0) {
            log.warn("注册失败, 用户名已存在: {}", username);
            throw new BusinessException("用户名已存在");
        }

        // 验证角色，禁止注册管理员
        if (!role.equals("SEEKER") && !role.equals("VOLUNTEER") && !role.equals("CLUE_PROVIDER")) {
            log.warn("注册失败, 角色类型错误: {}", role);
            throw new BusinessException("角色类型错误");
        }

        // 检查手机号和角色组合是否已存在
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("phone", phone).eq("role", role);
        if (userMapper.selectCount(wrapper) > 0) {
            log.warn("注册失败, 该手机号已注册过此角色: {} - {}", phone, role);
            throw new BusinessException("该手机号已注册过此角色");
        }

        User user = new User();
        user.setUsername(username); // 将用户名作为username
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        user.setNickname(username); // 将用户名作为nickname
        user.setEmail(email);
        user.setPhone(phone);
        user.setStatus(1); // 默认为启用
        user.setCreateTime(LocalDateTime.now());

        userMapper.insert(user);
        log.info("用户注册成功, phone: {}, role: {}, username: {}, userId: {}", phone, role, username, user.getId());
        return user;
    }

    @Override
    @Transactional
    public User registerWithEncodedPassword(String phone, String encodedPassword, String role, String username, String email) {
        log.info("用户注册(密码已加密), phone: {}, role: {}, username: {}", phone, role, username);

        // 检查手机号长度是否为11位
        if (phone == null || phone.length() != 11) {
            log.warn("注册失败, 手机号长度必须为11位: {}", phone);
            throw new BusinessException("手机号长度必须为11位");
        }

        // 验证用户名是否为空
        if (username == null || username.trim().equals("")) {
            log.warn("注册失败, 用户名不能为空");
            throw new BusinessException("用户名不能为空");
        }

        // 检查用户名是否已存在
        QueryWrapper<User> usernameWrapper = new QueryWrapper<>();
        usernameWrapper.eq("username", username);
        if (userMapper.selectCount(usernameWrapper) > 0) {
            log.warn("注册失败, 用户名已存在: {}", username);
            throw new BusinessException("用户名已存在");
        }

        // 验证角色，禁止注册管理员
        if (!role.equals("SEEKER") && !role.equals("VOLUNTEER") && !role.equals("CLUE_PROVIDER")) {
            log.warn("注册失败, 角色类型错误: {}", role);
            throw new BusinessException("角色类型错误");
        }

        // 检查手机号和角色组合是否已存在
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("phone", phone).eq("role", role);
        if (userMapper.selectCount(wrapper) > 0) {
            log.warn("注册失败, 该手机号已注册过此角色: {} - {}", phone, role);
            throw new BusinessException("该手机号已注册过此角色");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(encodedPassword); // 密码已加密，直接使用
        user.setRole(role);
        user.setNickname(username);
        user.setEmail(email);
        user.setPhone(phone);
        user.setStatus(1);
        user.setCreateTime(LocalDateTime.now());

        userMapper.insert(user);
        log.info("用户注册成功(密码已加密), phone: {}, role: {}, username: {}, userId: {}", phone, role, username, user.getId());
        return user;
    }

    @Override
    public User getUserById(Long id) {
        log.debug("获取用户信息, userId: {}", id);
        User user = userMapper.selectById(id);
        if (user == null) {
            log.warn("用户不存在, userId: {}", id);
            throw new BusinessException("用户不存在");
        }
        return user;
    }

    @Override
    @Transactional
    public void updateProfile(Long userId, String nickname, String avatar, String email, String phone) {
        log.info("更新个人资料, userId: {}, nickname: {}, email: {}, phone: {}", userId, nickname, email, phone);
        
        User user = userMapper.selectById(userId);
        if (user == null) {
            log.error("更新个人资料失败, 用户不存在, userId: {}", userId);
            throw new BusinessException("用户不存在");
        }

        user.setNickname(nickname);
        user.setAvatar(avatar);
        user.setEmail(email);
        user.setPhone(phone);

        int result = userMapper.updateById(user);
        if (result > 0) {
            log.info("个人资料更新成功, userId: {}", userId);
        } else {
            log.error("个人资料更新失败, userId: {}", userId);
            throw new BusinessException("更新失败");
        }
    }

    @Override
    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        log.info("修改密码, userId: {}", userId);
        
        User user = userMapper.selectById(userId);
        if (user == null) {
            log.error("修改密码失败, 用户不存在, userId: {}", userId);
            throw new BusinessException("用户不存在");
        }

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            log.warn("修改密码失败, 原密码错误, userId: {}", userId);
            throw new BusinessException("原密码错误");
        }

        user.setPassword(passwordEncoder.encode(newPassword));

        int result = userMapper.updateById(user);
        if (result > 0) {
            log.info("密码修改成功, userId: {}", userId);
        } else {
            log.error("密码修改失败, userId: {}", userId);
            throw new BusinessException("修改失败");
        }
    }

    @Override
    @Transactional
    public void resetPassword(Long userId, String newPassword) {
        log.info("重置密码, userId: {}", userId);
        
        User user = userMapper.selectById(userId);
        if (user == null) {
            log.error("重置密码失败, 用户不存在, userId: {}", userId);
            throw new BusinessException("用户不存在");
        }

        user.setPassword(passwordEncoder.encode(newPassword));

        int result = userMapper.updateById(user);
        if (result > 0) {
            log.info("密码重置成功, userId: {}", userId);
        } else {
            log.error("密码重置失败, userId: {}", userId);
            throw new BusinessException("重置失败");
        }
    }

    @Override
    public Page<User> getUsers(String username, String role, Integer status, Integer pageNum, Integer pageSize) {
        log.debug("获取用户列表, username: {}, role: {}, status: {}", username, role, status);
        Page<User> page = new Page<>(pageNum, pageSize);
        QueryWrapper<User> wrapper = new QueryWrapper<>();

        if (username != null && !username.isEmpty()) {
            wrapper.like("username", username).or().like("nickname", username);
        }
        if (role != null && !role.isEmpty()) {
            wrapper.eq("role", role);
        }
        if (status != null) {
            wrapper.eq("status", status);
        }

        wrapper.orderByDesc("create_time");
        return userMapper.selectPage(page, wrapper);
    }

    @Override
    @Transactional
    public void updateUserStatus(Long userId, Integer status) {
        log.info("更新用户状态, userId: {}, status: {}", userId, status);
        
        User user = userMapper.selectById(userId);
        if (user == null) {
            log.error("更新用户状态失败, 用户不存在, userId: {}", userId);
            throw new BusinessException("用户不存在");
        }

        user.setStatus(status);

        int result = userMapper.updateById(user);
        if (result > 0) {
            log.info("用户状态更新成功, userId: {}, status: {}", userId, status);
        } else {
            log.error("用户状态更新失败, userId: {}", userId);
            throw new BusinessException("更新失败");
        }
    }

    @Override
    @Transactional
    public void auditUserRealName(Long userId, Integer status, String auditRemark) {
        log.info("审核用户实名认证, userId: {}, status: {}", userId, status);
        // 这里可以实现实名认证审核逻辑
        // 例如，更新用户的实名认证状态
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        log.info("删除用户, userId: {}", userId);

        User user = userMapper.selectById(userId);
        if (user == null) {
            log.error("删除用户失败, 用户不存在, userId: {}", userId);
            throw new BusinessException("用户不存在");
        }

        // 防止删除管理员账号（可选的安全措施）
        if ("ADMIN".equals(user.getRole()) && user.getUsername().equals("admin")) {
            log.warn("尝试删除系统管理员账号, userId: {}", userId);
            throw new BusinessException("不能删除系统管理员账号");
        }

        // 使用物理删除（直接执行SQL删除语句，绕过@TableLogic）
        int result = userMapper.deleteByIdPhysical(userId);
        if (result > 0) {
            log.info("用户删除成功, userId: {}", userId);
        } else {
            log.error("用户删除失败, userId: {}", userId);
            throw new BusinessException("删除失败");
        }
    }

    @Override
    @Transactional
    public void updateUser(Long userId, String nickname, String email) {
        log.info("管理员更新用户资料, userId: {}, nickname: {}, email: {}", userId, nickname, email);

        User user = userMapper.selectById(userId);
        if (user == null) {
            log.error("更新用户失败, 用户不存在, userId: {}", userId);
            throw new BusinessException("用户不存在");
        }

        if (nickname != null) user.setNickname(nickname);
        if (email != null) user.setEmail(email);

        int result = userMapper.updateById(user);
        if (result > 0) {
            log.info("用户资料更新成功, userId: {}", userId);
        } else {
            log.error("用户资料更新失败, userId: {}", userId);
            throw new BusinessException("更新失败");
        }
    }

    @Override
    @Transactional
    public User createUser(String username, String phone, String password, String role) {
        log.info("管理员创建用户, username: {}, phone: {}, role: {}", username, phone, role);

        // 检查用户名是否已存在
        QueryWrapper<User> usernameWrapper = new QueryWrapper<>();
        usernameWrapper.eq("username", username);
        if (userMapper.selectCount(usernameWrapper) > 0) {
            log.warn("创建用户失败, 用户名已存在: {}", username);
            throw new BusinessException("用户名已存在");
        }

        // 检查手机号是否已被使用（全局唯一）
        if (phone != null && !phone.isEmpty()) {
            QueryWrapper<User> phoneWrapper = new QueryWrapper<>();
            phoneWrapper.eq("phone", phone);
            if (userMapper.selectCount(phoneWrapper) > 0) {
                log.warn("创建用户失败, 手机号已被使用: {}", phone);
                throw new BusinessException("手机号已被使用");
            }
        }

        // 验证角色
        if (!role.equals("SEEKER") && !role.equals("VOLUNTEER") && !role.equals("CLUE_PROVIDER") && !role.equals("ADMIN")) {
            log.warn("创建用户失败, 角色类型错误: {}", role);
            throw new BusinessException("角色类型错误");
        }

        User user = new User();
        user.setUsername(username);
        user.setNickname(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setPhone(phone);
        user.setRole(role);
        user.setStatus(1);
        user.setCreateTime(LocalDateTime.now());

        userMapper.insert(user);
        log.info("管理员创建用户成功, userId: {}, username: {}, role: {}", user.getId(), username, role);
        return user;
    }

    @Override
    @Transactional
    public void updateAvatar(Long userId, String avatarUrl) {
        log.info("更新用户头像, userId: {}, avatarUrl: {}", userId, avatarUrl);
        
        User user = userMapper.selectById(userId);
        if (user == null) {
            log.error("更新头像失败, 用户不存在, userId: {}", userId);
            throw new BusinessException("用户不存在");
        }
        
        user.setAvatar(avatarUrl);
        int result = userMapper.updateById(user);
        
        if (result > 0) {
            log.info("头像更新成功, userId: {}", userId);
        } else {
            log.error("头像更新失败, userId: {}", userId);
            throw new BusinessException("头像更新失败");
        }
    }
    
    @Override
    public java.util.List<User> getAdmins() {
        log.debug("获取所有管理员");
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("role", "ADMIN");
        return userMapper.selectList(wrapper);
    }
}
