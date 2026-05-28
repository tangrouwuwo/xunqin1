package com.xunqin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xunqin.common.result.Result;
import com.xunqin.entity.Clue;
import com.xunqin.service.ClueService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

@RestController
@RequestMapping("/clues")
public class ClueController {

    @Autowired
    private ClueService clueService;

    @Data
    public static class CreateClueRequest {
        @javax.validation.constraints.NotNull(message = "寻亲信息ID不能为空")
        private Long missingPersonId;

        private Integer isAnonymous;

        @NotBlank(message = "线索内容不能为空")
        private String content;

        private String contactName;
        private String contactPhone;
        private String contactEmail;
    }

    @Data
    public static class HandleClueRequest {
        private Integer status;
        private String handleResult;
        private String handleRemark;
    }

    @Data
    public static class ApproveClueRequest {
        private String handleRemark;
    }

    @Data
    public static class RejectClueRequest {
        @NotBlank(message = "驳回原因不能为空")
        private String rejectReason;
    }

    @PostMapping
    public Result<Clue> createClue(Authentication authentication, @Valid @RequestBody CreateClueRequest request) {
        Long userId = authentication != null ? Long.parseLong(authentication.getPrincipal().toString()) : null;
        Clue clue = clueService.createClue(userId, request.getMissingPersonId(), request.getIsAnonymous(),
                request.getContent(), request.getContactName(), request.getContactPhone(), request.getContactEmail());
        return Result.success("线索提交成功，等待管理员审核", clue);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('VOLUNTEER', 'ADMIN')")
    public Result<Page<Clue>> getClues(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long missingPersonId,
            @RequestParam(required = false) String content,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<Clue> page = clueService.getClues(status, missingPersonId, content, pageNum, pageSize);
        return Result.success(page);
    }

    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public Result<Page<Clue>> getMyClues(Authentication authentication,
                                         @RequestParam(required = false) Integer status,
                                         @RequestParam(defaultValue = "1") Integer pageNum,
                                         @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = Long.parseLong(authentication.getPrincipal().toString());
        Page<Clue> page = clueService.getMyClues(userId, status, pageNum, pageSize);
        return Result.success(page);
    }

    @GetMapping("/{id}")
    public Result<Clue> getClueById(@PathVariable Long id) {
        Clue clue = clueService.getClueById(id);
        return Result.success(clue);
    }

    @PutMapping("/{id}/handle")
    @PreAuthorize("hasAnyRole('VOLUNTEER', 'ADMIN')")
    public Result<Clue> handleClue(Authentication authentication, @PathVariable Long id,
                                 @Valid @RequestBody HandleClueRequest request) {
        Long userId = Long.parseLong(authentication.getPrincipal().toString());
        Clue clue = clueService.handleClue(id, userId, request.getStatus(), request.getHandleResult(), request.getHandleRemark());
        return Result.success("线索处理成功", clue);
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<Clue> updateClue(Authentication authentication, @PathVariable Long id,
                                 @Valid @RequestBody CreateClueRequest request) {
        Long userId = Long.parseLong(authentication.getPrincipal().toString());
        Clue clue = clueService.updateClue(id, userId, request.getIsAnonymous(), request.getContent(),
                                         request.getContactName(), request.getContactPhone(), request.getContactEmail());
        return Result.success("线索修改成功", clue);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> deleteClue(Authentication authentication, @PathVariable Long id) {
        Long userId = Long.parseLong(authentication.getPrincipal().toString());
        clueService.deleteClue(id, userId);
        return Result.success();
    }

    @GetMapping("/seeker")
    @PreAuthorize("hasRole('SEEKER')")
    public Result<Page<Clue>> getSeekerClues(Authentication authentication,
                                            @RequestParam(required = false) Integer status,
                                            @RequestParam(defaultValue = "1") Integer pageNum,
                                            @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = Long.parseLong(authentication.getPrincipal().toString());
        Page<Clue> page = clueService.getSeekerClues(userId, status, pageNum, pageSize);
        return Result.success(page);
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Clue> approveClue(Authentication authentication, @PathVariable Long id,
                                   @Valid @RequestBody(required = false) ApproveClueRequest request) {
        Long adminId = Long.parseLong(authentication.getPrincipal().toString());
        String remark = request != null ? request.getHandleRemark() : null;
        Clue clue = clueService.approveClue(id, adminId, remark);
        return Result.success("线索审核通过", clue);
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Clue> rejectClue(Authentication authentication, @PathVariable Long id,
                                  @Valid @RequestBody RejectClueRequest request) {
        Long adminId = Long.parseLong(authentication.getPrincipal().toString());
        Clue clue = clueService.rejectClue(id, adminId, request.getRejectReason());
        return Result.success("线索已驳回", clue);
    }

    @PutMapping("/{id}/admin-update")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Clue> adminUpdateClue(@PathVariable Long id, @Valid @RequestBody CreateClueRequest request) {
        Clue clue = clueService.adminUpdateClue(id, request.getIsAnonymous(), request.getContent(),
                                              request.getContactName(), request.getContactPhone(), request.getContactEmail());
        return Result.success("线索修改成功", clue);
    }

    @DeleteMapping("/{id}/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> adminDeleteClue(@PathVariable Long id) {
        clueService.adminDeleteClue(id);
        return Result.success("线索删除成功");
    }
}
