package com.group_platform.post.repository;

import com.group_platform.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long>, CustomPostRepository {

    @Modifying
    @Query("UPDATE Post p SET p.comment_count = p.comment_count + 1 WHERE p.id = :postId")
    int incrementCommentCount(Long postId);

    //혹시나 음수 방지
    @Modifying
    @Query("UPDATE Post p SET p.comment_count = CASE WHEN p.comment_count > 0 THEN p.comment_count - 1 ELSE 0 END WHERE p.id = :postId")
    int decrementCommentCount(Long postId);

    @Modifying
    @Query("UPDATE Post p SET p.viewCount = p.viewCount + 1 WHERE p.id = :postId")
    int incrementViewCount(Long postId);
}
