package com.group_platform.post.bookmark;

import com.group_platform.post.entity.Post;
import com.group_platform.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "post_bookmarks")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class PostBookmark {

    @Embedded
    private PostBookmarkId id;

    @CreationTimestamp
    private Long createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", insertable = false, updatable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;
}
