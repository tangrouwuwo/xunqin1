package com.xunqin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xunqin.entity.CommunityPost;
import com.xunqin.entity.Comment;
import com.xunqin.vo.CommentVO;
import com.xunqin.vo.PostVO;

import java.util.List;

public interface CommunityService {

    CommunityPost createPost(Long userId, String title, String content, String category, String tags);

    Page<PostVO> getPosts(String category, String orderBy, Integer pageNum, Integer pageSize);

    PostVO getPostById(Long id);

    CommunityPost updatePost(Long id, Long userId, String title, String content, String category, String tags);

    void deletePost(Long id, Long userId);

    void likePost(Long postId, Long userId);

    void unlikePost(Long postId, Long userId);

    Comment createComment(Long postId, Long userId, String content, Long parentId);

    Page<CommentVO> getComments(Long postId, String orderBy, Integer pageNum, Integer pageSize);

    void likeComment(Long commentId, Long userId);

    void unlikeComment(Long commentId, Long userId);

    void deleteComment(Long id, Long userId);

    List<CommunityPost> getHotPosts(Integer limit);

    Page<CommunityPost> getMyPosts(Long userId, Integer pageNum, Integer pageSize);

    // 获取用户已点赞的帖子ID列表
    List<Long> getLikedPostIds(Long userId);

    // 获取用户已点赞的评论ID列表
    List<Long> getLikedCommentIds(Long userId);
}
