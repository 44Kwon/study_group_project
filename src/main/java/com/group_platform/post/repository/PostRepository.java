package com.group_platform.post.repository;

import com.group_platform.post.entity.Post;
import com.group_platform.post.repository.querydsl.CustomPostRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

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
    @Query("UPDATE Post p SET p.likeCount = p.likeCount + 1 WHERE p.id = :postId")
    int incrementLikeCount(Long postId);

    //혹시나 음수 방지
    @Modifying
    @Query("UPDATE Post p SET p.likeCount = CASE WHEN p.likeCount > 0 THEN p.likeCount - 1 ELSE 0 END WHERE p.id = :postId")
    int decrementLikeCount(Long postId);

    @Modifying
    @Query("UPDATE Post p SET p.viewCount = p.viewCount + 1 WHERE p.id = :postId")
    int incrementViewCount(Long postId);

    long countByIsPinnedIsTrueAndStudyGroup_Id(Long studyGroupId);

    Optional<Post> findByIdAndStudyGroupId(Long postId, Long groupId);
}
