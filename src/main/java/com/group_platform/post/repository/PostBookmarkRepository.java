package com.group_platform.post.repository;

import com.group_platform.post.bookmark.PostBookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostBookmarkRepository extends JpaRepository<PostBookmark, Long> {
    boolean existsByUserIdAndPostId(Long userId, Long postId);
    void deleteByUserIdAndPostId(Long userId, Long postId);
}
