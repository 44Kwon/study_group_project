package com.group_platform.post.like;

import com.group_platform.post.entity.Post;
import com.group_platform.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "post_likes")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class PostLike {
    @EmbeddedId
    private PostLikeId id;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)  //기본값이 Eager이기 때문에 Lazy로 변경
    @MapsId("postId")
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)  //기본값이 Eager이기 때문에 Lazy로 변경
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    public void likePost(Post post, User user) {
        this.post = post;
        this.user = user;
        this.id = new PostLikeId(user.getId(), post.getId());
    }
}
