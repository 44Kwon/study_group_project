package com.group_platform.comment.repository;

import com.group_platform.comment.dto.CommentDto;
import com.group_platform.comment.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long>, CustomCommentRepository {
    @Query("SELECT c FROM Comment c JOIN FETCH c.user u WHERE c.id = :commentId")
    Optional<Comment> findByCommentWithUser(Long commentId);


    //댓글페이징
    //Jpql에서는 offset, limit을 pageable 보고 알아서 처리해준다.
    @Query("SELECT c FROM Comment c JOIN FETCH c.user u WHERE c.post.id = :postId AND c.parent IS NULL  ORDER BY c.id DESC")
    Page<Comment> findAllByPostIdAndParentIdIsNull(Long postId, Pageable pageable);

    List<Comment> findAllByParentId(Long parentId);

    Optional<Comment> findByIdAndUserId(Long commentId, Long userId);

    long countAllByPostId(Long postId);

    //대댓글 있는지 확인하는 쿼리 -> exist나 count서브쿼리는 10번쳐야 한다.
    //SELECT parent_id, COUNT(*)
    //FROM comment
    //WHERE parent_id IN (:parentIds)
    //GROUP BY parent_id
//    @Query("SELECT new com.group_platform.comment.dto.CommentDto.IsCommentHaveReplyDto(c.parent.id, COUNT(c)) FROM Comment c WHERE c.parent.id IN :parentIds GROUP BY c.parent.id")
//    List<CommentDto.IsCommentHaveReplyDto> existsByRelies(List<Long> parentIds);
}
