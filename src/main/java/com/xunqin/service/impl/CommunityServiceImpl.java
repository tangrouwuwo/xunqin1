package com.xunqin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xunqin.common.exception.BusinessException;
import com.xunqin.entity.*;
import com.xunqin.mapper.*;
import com.xunqin.service.CommunityService;
import com.xunqin.service.NotificationService;
import com.xunqin.vo.CommentVO;
import com.xunqin.vo.PostVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CommunityServiceImpl implements CommunityService {

    @Autowired
    private CommunityPostMapper communityPostMapper;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private LikeRecordMapper likeRecordMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private NotificationService notificationService;

    @Override
    @Transactional
    public CommunityPost createPost(Long userId, String title, String content, String category, String tags) {
        CommunityPost post = new CommunityPost();
        post.setUserId(userId);
        post.setType(category != null ? category : "DISCUSSION");
        post.setTitle(title);
        post.setContent(content);
        post.setImages(null);
        post.setTags(tags);
        post.setViewCount(0);
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setStatus(1); // 1: 已通过
        post.setCreateTime(LocalDateTime.now());

        communityPostMapper.insert(post);
        return post;
    }

    @Override
    public Page<PostVO> getPosts(String category, String orderBy, Integer pageNum, Integer pageSize) {
        Page<CommunityPost> page = new Page<>(pageNum, pageSize);
        QueryWrapper<CommunityPost> wrapper = new QueryWrapper<>();

        wrapper.eq("status", 1);
        if (category != null) {
            wrapper.eq("type", category);
        }

        // 排序
        if ("hot".equals(orderBy)) {
            wrapper.orderByDesc("like_count").orderByDesc("comment_count").orderByDesc("view_count");
        } else {
            wrapper.orderByDesc("create_time");
        }

        Page<CommunityPost> postPage = communityPostMapper.selectPage(page, wrapper);

        // 转换为PostVO
        Page<PostVO> voPage = new Page<>(pageNum, pageSize);
        voPage.setTotal(postPage.getTotal());
        voPage.setPages(postPage.getPages());
        voPage.setCurrent(postPage.getCurrent());
        voPage.setSize(postPage.getSize());

        List<PostVO> postVOList = postPage.getRecords().stream()
                .map(this::convertToPostVO)
                .collect(Collectors.toList());
        voPage.setRecords(postVOList);

        return voPage;
    }

    @Override
    @Transactional
    public PostVO getPostById(Long id) {
        CommunityPost post = communityPostMapper.selectById(id);
        if (post == null) {
            throw new BusinessException("帖子不存在");
        }

        // 增加浏览次数
        post.setViewCount(post.getViewCount() + 1);
        communityPostMapper.updateById(post);

        return convertToPostVO(post);
    }

    @Override
    @Transactional
    public CommunityPost updatePost(Long id, Long userId, String title, String content, String category, String tags) {
        CommunityPost post = communityPostMapper.selectById(id);
        if (post == null) {
            throw new BusinessException("帖子不存在");
        }

        if (!post.getUserId().equals(userId)) {
            throw new BusinessException("无权修改他人的帖子");
        }

        post.setType(category != null ? category : "DISCUSSION");
        post.setTitle(title);
        post.setContent(content);
        post.setTags(tags);
        post.setUpdateTime(LocalDateTime.now());

        communityPostMapper.updateById(post);
        return post;
    }

    @Override
    @Transactional
    public void deletePost(Long id, Long userId) {
        CommunityPost post = communityPostMapper.selectById(id);
        if (post == null) {
            throw new BusinessException("帖子不存在");
        }

        // 检查权限：只有帖子发布者或管理员可以删除
        User user = userMapper.selectById(userId);
        boolean isAdmin = user != null && "ADMIN".equals(user.getRole());
        if (!post.getUserId().equals(userId) && !isAdmin) {
            throw new BusinessException("无权删除该帖子");
        }

        communityPostMapper.deleteById(id);

        // 删除相关评论
        QueryWrapper<Comment> commentWrapper = new QueryWrapper<>();
        commentWrapper.eq("post_id", id);
        commentMapper.delete(commentWrapper);

        // 删除相关点赞
        QueryWrapper<LikeRecord> likeWrapper = new QueryWrapper<>();
        likeWrapper.eq("target_id", id).eq("target_type", "POST");
        likeRecordMapper.delete(likeWrapper);
    }

    @Override
    @Transactional
    public void likePost(Long postId, Long userId) {
        // 检查是否已点赞
        QueryWrapper<LikeRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
               .eq("target_id", postId)
               .eq("target_type", "POST");

        if (likeRecordMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("已经点赞过了");
        }

        // 记录点赞
        LikeRecord likeRecord = new LikeRecord();
        likeRecord.setUserId(userId);
        likeRecord.setTargetId(postId);
        likeRecord.setTargetType("POST");
        likeRecord.setCreateTime(LocalDateTime.now());
        likeRecordMapper.insert(likeRecord);

        // 更新帖子点赞数
        CommunityPost post = communityPostMapper.selectById(postId);
        if (post != null) {
            post.setLikeCount(post.getLikeCount() + 1);
            communityPostMapper.updateById(post);

            // 发送点赞通知给帖子作者
            if (!post.getUserId().equals(userId)) {
                User liker = userMapper.selectById(userId);
                User postAuthor = userMapper.selectById(post.getUserId());
                if (postAuthor != null) {
                    String title = "帖子收到点赞";
                    String contentText = (liker != null ? liker.getNickname() : "用户") + "赞了您的帖子";
                    String role = postAuthor.getRole();
                    try {
                        switch (role) {
                            case "ADMIN":
                                notificationService.sendAdminNotification(postAuthor.getId(), title, contentText, "POST_LIKED", postId, "POST", 1);
                                break;
                            case "SEEKER":
                                notificationService.sendSeekerNotification(postAuthor.getId(), title, contentText, "POST_LIKED", postId, "POST", null);
                                break;
                            case "VOLUNTEER":
                                notificationService.sendVolunteerNotification(postAuthor.getId(), title, contentText, "POST_LIKED", postId, "POST", null);
                                break;
                            case "CLUE_PROVIDER":
                                notificationService.sendClueProviderNotification(postAuthor.getId(), title, contentText, "POST_LIKED", postId, "POST", null);
                                break;
                            default:
                                notificationService.sendSeekerNotification(postAuthor.getId(), title, contentText, "POST_LIKED", postId, "POST", null);
                                break;
                        }
                    } catch (Exception e) {
                        log.error("发送帖子点赞通知失败: {}", e.getMessage());
                    }
                }
            }
        }
    }

    @Override
    @Transactional
    public void unlikePost(Long postId, Long userId) {
        QueryWrapper<LikeRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
               .eq("target_id", postId)
               .eq("target_type", "POST");

        if (likeRecordMapper.delete(wrapper) == 0) {
            throw new BusinessException("还没有点赞");
        }

        // 更新帖子点赞数
        CommunityPost post = communityPostMapper.selectById(postId);
        if (post != null && post.getLikeCount() > 0) {
            post.setLikeCount(post.getLikeCount() - 1);
            communityPostMapper.updateById(post);
        }
    }

    @Override
    @Transactional
    public Comment createComment(Long postId, Long userId, String content, Long parentId) {
        // 验证帖子是否存在
        CommunityPost post = communityPostMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException("帖子不存在");
        }

        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setContent(content);
        comment.setParentId(parentId);
        comment.setLikeCount(0);
        comment.setCreateTime(LocalDateTime.now());

        commentMapper.insert(comment);

        // 更新帖子评论数
        post.setCommentCount(post.getCommentCount() + 1);
        communityPostMapper.updateById(post);

        // 如果是回复评论，向被回复的评论作者发送通知
        if (parentId != null) {
            Comment parentComment = commentMapper.selectById(parentId);
            if (parentComment != null && !parentComment.getUserId().equals(userId)) {
                User replier = userMapper.selectById(userId);
                User parentCommentAuthor = userMapper.selectById(parentComment.getUserId());
                if (parentCommentAuthor != null) {
                    String title = "评论被回复"; 
                    String contentText = "您的评论被" + (replier != null ? replier.getNickname() : "用户") + "回复：\"" + content + "\"";
                    String role = parentCommentAuthor.getRole();
                    try {
                        switch (role) {
                            case "ADMIN":
                                notificationService.sendAdminNotification(parentCommentAuthor.getId(), title, contentText, "COMMENT_REPLY", comment.getId(), "COMMENT", 1);
                                break;
                            case "SEEKER":
                                notificationService.sendSeekerNotification(parentCommentAuthor.getId(), title, contentText, "COMMENT_REPLY", comment.getId(), "COMMENT", null);
                                break;
                            case "VOLUNTEER":
                                notificationService.sendVolunteerNotification(parentCommentAuthor.getId(), title, contentText, "COMMENT_REPLY", comment.getId(), "COMMENT", null);
                                break;
                            case "CLUE_PROVIDER":
                                notificationService.sendClueProviderNotification(parentCommentAuthor.getId(), title, contentText, "COMMENT_REPLY", comment.getId(), "COMMENT", null);
                                break;
                            default:
                                // 默认发送寻亲者通知，确保即使role为null也能发送
                                notificationService.sendSeekerNotification(parentCommentAuthor.getId(), title, contentText, "COMMENT_REPLY", comment.getId(), "COMMENT", null);
                                break;
                        }
                    } catch (Exception e) {
                        // 捕获通知发送异常，确保评论创建不受影响
                        log.error("发送评论回复通知失败: {}", e.getMessage());
                    }
                }
            }
        } else {
            // 如果是直接评论帖子，向帖子作者发送通知
            if (!post.getUserId().equals(userId)) {
                User commenter = userMapper.selectById(userId);
                User postAuthor = userMapper.selectById(post.getUserId());
                if (postAuthor != null) {
                    String title = "帖子有新评论"; 
                    String contentText = commenter != null ? commenter.getNickname() : "用户" + "评论了您的帖子：\"" + content + "\"";
                    String role = postAuthor.getRole();
                    try {
                        switch (role) {
                            case "ADMIN":
                                notificationService.sendAdminNotification(postAuthor.getId(), title, contentText, "POST_COMMENT", comment.getId(), "COMMENT", 1);
                                break;
                            case "SEEKER":
                                notificationService.sendSeekerNotification(postAuthor.getId(), title, contentText, "POST_COMMENT", comment.getId(), "COMMENT", null);
                                break;
                            case "VOLUNTEER":
                                notificationService.sendVolunteerNotification(postAuthor.getId(), title, contentText, "POST_COMMENT", comment.getId(), "COMMENT", null);
                                break;
                            case "CLUE_PROVIDER":
                                notificationService.sendClueProviderNotification(postAuthor.getId(), title, contentText, "POST_COMMENT", comment.getId(), "COMMENT", null);
                                break;
                            default:
                                // 默认发送寻亲者通知，确保即使role为null也能发送
                                notificationService.sendSeekerNotification(postAuthor.getId(), title, contentText, "POST_COMMENT", comment.getId(), "COMMENT", null);
                                break;
                        }
                    } catch (Exception e) {
                        // 捕获通知发送异常，确保评论创建不受影响
                        log.error("发送帖子评论通知失败: {}", e.getMessage());
                    }
                }
            }
        }

        return comment;
    }

    @Override
    public Page<CommentVO> getComments(Long postId, String orderBy, Integer pageNum, Integer pageSize) {
        Page<Comment> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Comment> wrapper = new QueryWrapper<>();

        wrapper.eq("post_id", postId);

        // 排序
        if ("hot".equals(orderBy)) {
            wrapper.orderByDesc("like_count").orderByDesc("create_time");
        } else {
            wrapper.orderByAsc("parent_id").orderByDesc("create_time");
        }

        Page<Comment> commentPage = commentMapper.selectPage(page, wrapper);

        // 转换为CommentVO
        Page<CommentVO> voPage = new Page<>(pageNum, pageSize);
        voPage.setTotal(commentPage.getTotal());
        voPage.setPages(commentPage.getPages());
        voPage.setCurrent(commentPage.getCurrent());
        voPage.setSize(commentPage.getSize());

        List<CommentVO> commentVOList = commentPage.getRecords().stream()
                .map(this::convertToCommentVO)
                .collect(Collectors.toList());
        voPage.setRecords(commentVOList);

        return voPage;
    }

    @Override
    @Transactional
    public void likeComment(Long commentId, Long userId) {
        // 检查是否已点赞
        QueryWrapper<LikeRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
               .eq("target_id", commentId)
               .eq("target_type", "COMMENT");

        if (likeRecordMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("已经点赞过了");
        }

        // 记录点赞
        LikeRecord likeRecord = new LikeRecord();
        likeRecord.setUserId(userId);
        likeRecord.setTargetId(commentId);
        likeRecord.setTargetType("COMMENT");
        likeRecord.setCreateTime(LocalDateTime.now());
        likeRecordMapper.insert(likeRecord);

        // 更新评论点赞数
        Comment comment = commentMapper.selectById(commentId);
        if (comment != null) {
            comment.setLikeCount(comment.getLikeCount() + 1);
            commentMapper.updateById(comment);

            // 发送点赞通知给评论作者
            if (!comment.getUserId().equals(userId)) {
                User liker = userMapper.selectById(userId);
                User commentAuthor = userMapper.selectById(comment.getUserId());
                if (commentAuthor != null) {
                    String title = "评论收到点赞";
                    String contentText = (liker != null ? liker.getNickname() : "用户") + "赞了您的评论";
                    String role = commentAuthor.getRole();
                    try {
                        switch (role) {
                            case "ADMIN":
                                notificationService.sendAdminNotification(commentAuthor.getId(), title, contentText, "COMMENT_LIKED", commentId, "COMMENT", 1);
                                break;
                            case "SEEKER":
                                notificationService.sendSeekerNotification(commentAuthor.getId(), title, contentText, "COMMENT_LIKED", commentId, "COMMENT", null);
                                break;
                            case "VOLUNTEER":
                                notificationService.sendVolunteerNotification(commentAuthor.getId(), title, contentText, "COMMENT_LIKED", commentId, "COMMENT", null);
                                break;
                            case "CLUE_PROVIDER":
                                notificationService.sendClueProviderNotification(commentAuthor.getId(), title, contentText, "COMMENT_LIKED", commentId, "COMMENT", null);
                                break;
                            default:
                                notificationService.sendSeekerNotification(commentAuthor.getId(), title, contentText, "COMMENT_LIKED", commentId, "COMMENT", null);
                                break;
                        }
                    } catch (Exception e) {
                        log.error("发送评论点赞通知失败: {}", e.getMessage());
                    }
                }
            }
        }
    }

    @Override
    @Transactional
    public void unlikeComment(Long commentId, Long userId) {
        QueryWrapper<LikeRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
               .eq("target_id", commentId)
               .eq("target_type", "COMMENT");

        if (likeRecordMapper.delete(wrapper) == 0) {
            throw new BusinessException("还没有点赞");
        }

        // 更新评论点赞数
        Comment comment = commentMapper.selectById(commentId);
        if (comment != null && comment.getLikeCount() > 0) {
            comment.setLikeCount(comment.getLikeCount() - 1);
            commentMapper.updateById(comment);
        }
    }

    @Override
    @Transactional
    public void deleteComment(Long id, Long userId) {
        Comment comment = commentMapper.selectById(id);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }

        // 检查权限：只有评论发布者或管理员可以删除
        User user = userMapper.selectById(userId);
        boolean isAdmin = user != null && "ADMIN".equals(user.getRole());
        if (!comment.getUserId().equals(userId) && !isAdmin) {
            throw new BusinessException("无权删除该评论");
        }

        commentMapper.deleteById(id);

        // 更新帖子评论数
        CommunityPost post = communityPostMapper.selectById(comment.getPostId());
        if (post != null && post.getCommentCount() > 0) {
            post.setCommentCount(post.getCommentCount() - 1);
            communityPostMapper.updateById(post);
        }

        // 删除相关点赞
        QueryWrapper<LikeRecord> likeWrapper = new QueryWrapper<>();
        likeWrapper.eq("target_id", id).eq("target_type", "COMMENT");
        likeRecordMapper.delete(likeWrapper);
    }

    @Override
    public List<CommunityPost> getHotPosts(Integer limit) {
        Page<CommunityPost> page = new Page<>(1, limit);
        QueryWrapper<CommunityPost> wrapper = new QueryWrapper<>();

        wrapper.eq("status", 1)
               .orderByDesc("like_count")
               .orderByDesc("comment_count")
               .orderByDesc("view_count");

        return communityPostMapper.selectPage(page, wrapper).getRecords();
    }

    @Override
    public Page<CommunityPost> getMyPosts(Long userId, Integer pageNum, Integer pageSize) {
        Page<CommunityPost> page = new Page<>(pageNum, pageSize);
        QueryWrapper<CommunityPost> wrapper = new QueryWrapper<>();

        wrapper.eq("user_id", userId)
               .orderByDesc("create_time");

        return communityPostMapper.selectPage(page, wrapper);
    }

    @Override
    public List<Long> getLikedPostIds(Long userId) {
        QueryWrapper<LikeRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
               .eq("target_type", "POST");
        
        return likeRecordMapper.selectList(wrapper).stream()
                .map(LikeRecord::getTargetId)
                .collect(Collectors.toList());
    }

    @Override
    public List<Long> getLikedCommentIds(Long userId) {
        QueryWrapper<LikeRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
               .eq("target_type", "COMMENT");
        
        return likeRecordMapper.selectList(wrapper).stream()
                .map(LikeRecord::getTargetId)
                .collect(Collectors.toList());
    }

    private PostVO convertToPostVO(CommunityPost post) {
        PostVO vo = new PostVO(post);
        User user = userMapper.selectById(post.getUserId());
        if (user != null) {
            vo.setUsername(user.getUsername());
            vo.setNickname(user.getNickname());
            vo.setAvatar(user.getAvatar());
            vo.setRole(user.getRole());
        }
        return vo;
    }

    private CommentVO convertToCommentVO(Comment comment) {
        CommentVO vo = new CommentVO(comment);
        User user = userMapper.selectById(comment.getUserId());
        if (user != null) {
            vo.setUsername(user.getUsername());
            vo.setNickname(user.getNickname());
            vo.setAvatar(user.getAvatar());
            vo.setRole(user.getRole());
        }
        return vo;
    }
}
