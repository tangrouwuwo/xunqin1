package com.xunqin.controller;

import com.xunqin.common.result.Result;
import com.xunqin.common.utils.JwtUtil;
import com.xunqin.entity.User;
import com.xunqin.service.UserService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Data
    public static class LoginRequest {
        @NotBlank(message = "用户名不能为空")
        private String username;

        @NotBlank(message = "密码不能为空")
        private String password;
    }

    @Data
    public static class RegisterRequest {
        @NotBlank(message = "手机号不能为空")
        private String phone;

        @NotBlank(message = "用户名不能为空")
        private String username;

        @NotBlank(message = "密码不能为空")
        private String password;

        @NotBlank(message = "角色不能为空")
        private String role;

        private String email;
    }

    @PostMapping("/login")
    public Result<?> login(@Valid @RequestBody LoginRequest request) {
        User user = userService.login(request.getUsername(), request.getPassword());
        
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("tokenType", "Bearer");
        data.put("user", user);
        
        return Result.success("登录成功", data);
    }

    @PostMapping("/register")
    public Result<?> register(@Valid @RequestBody RegisterRequest request) {
        User user = userService.register(
                request.getPhone(),
                request.getPassword(),
                request.getRole(),
                request.getUsername(),
                request.getEmail()
        );
        
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("tokenType", "Bearer");
        data.put("user", user);
        
        return Result.success("注册成功", data);
    }
}
