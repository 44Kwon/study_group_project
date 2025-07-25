package com.group_platform.post.bookmark;

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
@Table(name = "post_bookmarks")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class PostBookmark {

    @EmbeddedId
    private PostBookmarkId id;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("postId") // 복합키에서 postId를 사용하여 연관 관계를 설정
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId") // 복합키에서 userId를 사용하여 연관 관계를 설정 (필수는 아님)
    @JoinColumn(name = "user_id")
    private User user;

    public void bookmarkPost(Post post, User user) {
        this.post = post;
        this.user = user;
        this.id = new PostBookmarkId(user.getId(), post.getId());
    }
}
