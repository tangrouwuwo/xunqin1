package com.xunqin.vo;

import com.xunqin.entity.CommunityPost;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PostVO extends CommunityPost {

    private String username;

    private String nickname;

    private String avatar;

    private String role;

    private Boolean isLiked;

    public PostVO() {
    }

    public PostVO(CommunityPost post) {
        this.setId(post.getId());
        this.setUserId(post.getUserId());
        this.setType(post.getType());
        this.setTitle(post.getTitle());
        this.setContent(post.getContent());
        this.setImages(post.getImages());
        this.setTags(post.getTags());
        this.setViewCount(post.getViewCount());
        this.setLikeCount(post.getLikeCount());
        this.setCommentCount(post.getCommentCount());
        this.setStatus(post.getStatus());
        this.setCreateTime(post.getCreateTime());
        this.setUpdateTime(post.getUpdateTime());
    }
}
