package com.group_platform.post.repository;

import com.group_platform.post.bookmark.PostBookmark;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PostBookmarkRepository extends JpaRepository<PostBookmark, Long> {
    boolean existsByUserIdAndPostId(Long userId, Long postId);
    void deleteByUserIdAndPostId(Long userId, Long postId);

    //북마크 게시글들 가져오기
    @Query("SELECT pb FROM PostBookmark pb JOIN FETCH pb.user WHERE pb.id.userId = :userId ORDER BY pb.createdAt DESC")
    Page<PostBookmark> findPostMyBookmark(Long userId,Pageable pageable);
}
