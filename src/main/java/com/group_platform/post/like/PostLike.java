package com.group_platform.post.like;

import com.group_platform.post.entity.Post;
import com.group_platform.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "post_likes")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class PostLike {
    @Embedded
    private PostLikeId id;

    @CreationTimestamp
    private Long createdAt;

    @ManyToOne(fetch = FetchType.LAZY)  //기본값이 Eager이기 때문에 Lazy로 변경
    @JoinColumn(name = "post_id", insertable = false, updatable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)  //기본값이 Eager이기 때문에 Lazy로 변경
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;
}
