package com.xunqin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xunqin.common.result.Result;
import com.xunqin.entity.VolunteerApplication;
import com.xunqin.service.VolunteerApplicationService;
import com.xunqin.vo.VolunteerApplicationVO;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@RestController
@RequestMapping("/volunteer-application")
public class VolunteerApplicationController {

    @Autowired
    private VolunteerApplicationService applicationService;

    @Data
    public static class SubmitApplicationRequest {
        @NotBlank(message = "姓名不能为空")
        private String name;

        @NotBlank(message = "用户名不能为空")
        @Size(min = 3, max = 50, message = "用户名长度必须在3-50个字符之间")
        private String username;

        @NotBlank(message = "密码不能为空")
        @Size(min = 6, message = "密码长度不能少于6个字符")
        private String password;

        @NotBlank(message = "手机号不能为空")
        private String phone;

        @Email(message = "邮箱格式不正确")
        private String email;

        private Integer age;

        private String gender;

        private String location;

        private String skills;

        private String availability;

        private String reason;

        private String experience;
    }

    @PostMapping("/submit")
    public Result<VolunteerApplication> submitApplication(@Valid @RequestBody SubmitApplicationRequest request) {
        VolunteerApplication application = new VolunteerApplication();
        application.setName(request.getName());
        application.setUsername(request.getUsername());
        application.setPassword(request.getPassword());
        application.setPhone(request.getPhone());
        application.setEmail(request.getEmail());
        application.setAge(request.getAge());
        application.setGender(request.getGender());
        application.setLocation(request.getLocation());
        application.setSkills(request.getSkills());
        application.setAvailability(request.getAvailability());
        application.setReason(request.getReason());
        application.setExperience(request.getExperience());

        VolunteerApplication savedApplication = applicationService.submitApplication(application);
        return Result.success("申请提交成功，请等待管理员审核", savedApplication);
    }

    @GetMapping("/my-application")
    public Result<VolunteerApplicationVO> getMyApplication(@RequestParam String phone) {
        VolunteerApplicationVO application = applicationService.getMyApplication(phone);
        if (application == null) {
            return Result.success("未找到申请记录", null);
        }
        return Result.success(application);
    }

    @GetMapping("/{id}")
    public Result<VolunteerApplicationVO> getApplicationById(@PathVariable Long id) {
        VolunteerApplicationVO application = applicationService.getApplicationById(id);
        return Result.success(application);
    }
}
