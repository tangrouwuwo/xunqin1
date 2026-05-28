package com.xunqin.controller;

import com.xunqin.common.result.Result;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/")
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "forward:/index.html";
    }

    @GetMapping("/api")
    public String api() {
        return "forward:/index.html";
    }

    @GetMapping("/health")
    public Result<?> health() {
        Map<String, Object> data = new HashMap<>();
        data.put("status", "ok");
        data.put("message", "寻亲网站服务运行正常");
        data.put("timestamp", System.currentTimeMillis());
        return Result.success(data);
    }

    @GetMapping("/info")
    public Result<?> info() {
        Map<String, Object> data = new HashMap<>();
        data.put("name", "寻亲网站");
        data.put("version", "1.0.0");
        data.put("description", "公共福利寻亲平台 - 帮助失散家庭团聚");
        data.put("features", new String[]{
            "用户注册与登录",
            "寻亲信息发布",
            "线索提交",
            "任务管理",
            "社区交流",
            "成功案例"
        });
        return Result.success(data);
    }
}
