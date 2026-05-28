package com.xunqin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xunqin.common.result.Result;
import com.xunqin.entity.CommunityPost;
import com.xunqin.service.CommunityService;
import com.xunqin.vo.CommentVO;
import com.xunqin.vo.PostVO;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

@RestController
@RequestMapping("/community")
public class CommunityController {

    @Autowired
    private CommunityService communityService;

    @Data
    public static class CreatePostRequest {
        @NotBlank(message = "帖子标题不能为空")
        private String title;

        @NotBlank(message = "帖子内容不能为空")
        private String content;

        private String category;
        private String tags;
    }

    @Data
    public static class CreateCommentRequest {
        @NotBlank(message = "评论内容不能为空")
        private String content;

        private Long parentId;
    }

    @PostMapping("/posts")
    @PreAuthorize("isAuthenticated()")
    public Result<CommunityPost> createPost(Authentication authentication, @Valid @RequestBody CreatePostRequest request) {
        Long userId = getUserIdFromAuthentication(authentication);
        CommunityPost post = communityService.createPost(userId, request.getTitle(), request.getContent(),
                request.getCategory(), request.getTags());
        return Result.success("发布成功", post);
    }

    @GetMapping("/posts")
    public Result<Page<PostVO>> getPosts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String orderBy,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<PostVO> page = communityService.getPosts(category, orderBy, pageNum, pageSize);
        return Result.success(page);
    }

    @GetMapping("/posts/{id}")
    public Result<PostVO> getPostById(@PathVariable Long id) {
        PostVO post = communityService.getPostById(id);
        return Result.success(post);
    }

    @PutMapping("/posts/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<CommunityPost> updatePost(Authentication authentication, @PathVariable Long id,
                                          @Valid @RequestBody CreatePostRequest request) {
        Long userId = getUserIdFromAuthentication(authentication);
        CommunityPost post = communityService.updatePost(id, userId, request.getTitle(), request.getContent(),
                request.getCategory(), request.getTags());
        return Result.success("更新成功", post);
    }

    @DeleteMapping("/posts/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<?> deletePost(Authentication authentication, @PathVariable Long id) {
        Long userId = getUserIdFromAuthentication(authentication);
        communityService.deletePost(id, userId);
        return Result.success("删除成功");
    }

    @PostMapping("/posts/{id}/like")
    @PreAuthorize("isAuthenticated()")
    public Result<?> likePost(Authentication authentication, @PathVariable Long id) {
        Long userId = getUserIdFromAuthentication(authentication);
        communityService.likePost(id, userId);
        return Result.success("点赞成功");
    }

    @DeleteMapping("/posts/{id}/like")
    @PreAuthorize("isAuthenticated()")
    public Result<?> unlikePost(Authentication authentication, @PathVariable Long id) {
        Long userId = getUserIdFromAuthentication(authentication);
        communityService.unlikePost(id, userId);
        return Result.success("取消点赞成功");
    }

    @PostMapping("/posts/{postId}/comments")
    @PreAuthorize("isAuthenticated()")
    public Result<?> createComment(Authentication authentication, @PathVariable Long postId,
                                 @Valid @RequestBody CreateCommentRequest request) {
        Long userId = getUserIdFromAuthentication(authentication);
        communityService.createComment(postId, userId, request.getContent(), request.getParentId());
        return Result.success("评论成功");
    }

    @GetMapping("/posts/{postId}/comments")
    public Result<Page<CommentVO>> getComments(@PathVariable Long postId,
                                @RequestParam(required = false) String orderBy,
                                @RequestParam(defaultValue = "1") Integer pageNum,
                                @RequestParam(defaultValue = "20") Integer pageSize) {
        return Result.success(communityService.getComments(postId, orderBy, pageNum, pageSize));
    }

    @PostMapping("/comments/{id}/like")
    @PreAuthorize("isAuthenticated()")
    public Result<?> likeComment(Authentication authentication, @PathVariable Long id) {
        Long userId = getUserIdFromAuthentication(authentication);
        communityService.likeComment(id, userId);
        return Result.success("点赞成功");
    }

    @DeleteMapping("/comments/{id}/like")
    @PreAuthorize("isAuthenticated()")
    public Result<?> unlikeComment(Authentication authentication, @PathVariable Long id) {
        Long userId = getUserIdFromAuthentication(authentication);
        communityService.unlikeComment(id, userId);
        return Result.success("取消点赞成功");
    }

    @DeleteMapping("/comments/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<?> deleteComment(Authentication authentication, @PathVariable Long id) {
        Long userId = getUserIdFromAuthentication(authentication);
        communityService.deleteComment(id, userId);
        return Result.success("删除成功");
    }

    @GetMapping("/posts/hot")
    public Result<?> getHotPosts(@RequestParam(defaultValue = "10") Integer limit) {
        return Result.success(communityService.getHotPosts(limit));
    }

    @GetMapping("/my/posts")
    @PreAuthorize("isAuthenticated()")
    public Result<Page<CommunityPost>> getMyPosts(Authentication authentication,
                                                @RequestParam(defaultValue = "1") Integer pageNum,
                                                @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = getUserIdFromAuthentication(authentication);
        Page<CommunityPost> page = communityService.getMyPosts(userId, pageNum, pageSize);
        return Result.success(page);
    }

    @GetMapping("/my/likes/posts")
    @PreAuthorize("isAuthenticated()")
    public Result<?> getLikedPostIds(Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        return Result.success(communityService.getLikedPostIds(userId));
    }

    @GetMapping("/my/likes/comments")
    @PreAuthorize("isAuthenticated()")
    public Result<?> getLikedCommentIds(Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        return Result.success(communityService.getLikedCommentIds(userId));
    }

    private Long getUserIdFromAuthentication(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof Long) {
            return (Long) principal;
        } else if (principal instanceof String) {
            try {
                return Long.parseLong((String) principal);
            } catch (NumberFormatException e) {
                throw new RuntimeException("无效的用户ID");
            }
        } else {
            throw new RuntimeException("无效的用户ID");
        }
    }
}
