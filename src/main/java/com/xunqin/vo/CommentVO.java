package com.xunqin.vo;

import com.xunqin.entity.Comment;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CommentVO extends Comment {

    private String username;

    private String nickname;

    private String avatar;

    private String role;

    private Boolean isLiked;

    public CommentVO() {
    }

    public CommentVO(Comment comment) {
        this.setId(comment.getId());
        this.setPostId(comment.getPostId());
        this.setUserId(comment.getUserId());
        this.setParentId(comment.getParentId());
        this.setContent(comment.getContent());
        this.setLikeCount(comment.getLikeCount());
        this.setCreateTime(comment.getCreateTime());
    }
}
