package com.xunqin.controller;

import com.xunqin.common.result.Result;
import com.xunqin.service.AIService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/ai")
public class AIController {

    private final AIService aiService;

    public AIController(AIService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/search-by-region")
    public Result<?> searchByRegion(@RequestBody Map<String, String> request) {
        String region = request.get("region");
        if (region == null || region.isEmpty()) {
            return Result.error("地区不能为空");
        }
        return Result.success(aiService.searchByRegion(region));
    }

    @PostMapping("/ask")
    public Result<?> askQuestion(@RequestBody Map<String, String> request) {
        String question = request.get("question");
        String userIdStr = request.get("userId");
        String sessionId = request.get("sessionId");
        
        if (question == null || question.isEmpty()) {
            return Result.error("问题不能为空");
        }
        
        Long userId = null;
        if (userIdStr != null && !userIdStr.isEmpty()) {
            try {
                userId = Long.parseLong(userIdStr);
            } catch (NumberFormatException e) {
                // 忽略无效的userId
            }
        }
        
        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = "default";
        }
        
        String answer = aiService.askQuestion(userId, sessionId, question);
        return Result.success("操作成功", answer);
    }
}
