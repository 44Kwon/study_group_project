package com.group_platform.post.like;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@NoArgsConstructor
@Getter
public class PostLikeId implements Serializable {
    private Long userId;
    private Long postId;

    public PostLikeId(Long userId, Long postId) {
        this.userId = userId;
        this.postId = postId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PostLikeId that = (PostLikeId) o;
        return Objects.equals(userId, that.userId) && Objects.equals(postId, that.postId);
    }
    @Override
    public int hashCode() {
        return Objects.hash(userId, postId);
//        return 31 * userId.hashCode() + postId.hashCode();
    }
}
