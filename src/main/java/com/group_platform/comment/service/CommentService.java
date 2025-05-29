package com.group_platform.comment.service;

import com.group_platform.comment.dto.CommentDto;
import com.group_platform.comment.entity.Comment;
import com.group_platform.comment.mapper.CommentMapper;
import com.group_platform.comment.repository.CommentRepository;
import com.group_platform.exception.BusinessLogicException;
import com.group_platform.exception.ExceptionCode;
import com.group_platform.post.entity.Post;
import com.group_platform.post.repository.PostRepository;
import com.group_platform.user.entity.User;
import com.group_platform.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final UserService userService;
    private final PostRepository postRepository;
    private final CommentMapper commentMapper;

    public CommentDto.Response createComment(Long userId, Long postId, CommentDto.CreateRequest createRequest) {
        //유저검증(이미 인증된 유저임) 보다는 연관관계 잇기 위한 조회
        User user = userService.validateUserWithUserId(userId);

        //게시글검증
        Post post = postRepository.findById(postId).orElseThrow(()->
                new BusinessLogicException(ExceptionCode.POST_NOT_EXIST));

        Comment requestComment = commentMapper.createRequestToComment(createRequest);

        //대댓글일때
        if (createRequest.getParent_id() != null) {
            Comment comment = validateCommentWithCommentId(createRequest.getParent_id());
            requestComment.addReply(comment);
        }

        requestComment.setPostWithComment(user,post);

        Comment savedComment = commentRepository.save(requestComment);
        //댓글 숫자 증가
        postRepository.incrementCommentCount(post.getId());

        // parent id, user정보 맵핑관련 전부 매퍼에서 처리함
        return commentMapper.commentToResponse(savedComment);
    }

    public CommentDto.Response updateComment(Long userId, CommentDto.UpdateRequest updateRequest) {
        //유저검증(이미 인증된 유저임) 보다는 연관관계 잇기 위한 조회
        User user = userService.validateUserWithUserId(userId);

        //fetchjoin으로 한번에 가져옴(user까지)
        Comment comment = validateCommentWithCommentIdAndUser(updateRequest.getId());

        //같은 트랜잭션 내에서 동일한 엔티티는 동일 객체로 보장한다는 것을 알아두자 (==비교 가능) -> 영속성 컨텍스트 비교가 젤 빠름
        if (comment.getUser() != user) {
            throw new BusinessLogicException(ExceptionCode.NO_PERMISSION);
        }

        Optional.ofNullable(updateRequest.getContent())
                .ifPresent(comment::changeContent);

        return commentMapper.commentToResponse(comment);
    }

    public void deleteComment(Long userId, Long commentId) {
        //댓글이 있는지
        Comment comment = validateCommentWithCommentId(commentId);

        if (comment.getUser().getId().equals(userId)) {
            throw new BusinessLogicException(ExceptionCode.NO_PERMISSION);
        }

        commentRepository.delete(comment);
        //게시글에서 댓글수 감소
        postRepository.decrementCommentCount(comment.getPost().getId());
    }

    public Page<CommentDto.ResponseCommentList> getAllComments(Long postId, Pageable pageable) {
        // 대댓글이 아닌 comment들 가져와서 페이징처리(parent_id가 없는것들)
        Page<Comment> pagedComments = commentRepository.findAllByPostIdAndParentIdIsNull(postId,
                PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.ASC, "createdAt")));

        // 댓글당 대댓글 유무처리
        List<Long> commentIds = pagedComments.stream()
                .map(Comment::getId)
                .toList();

        //댓글에 대해서 대댓글 갯수가져옴
        List<CommentDto.IsCommentHaveReplyDto> isCommentHaveReplyDto = commentRepository.existsByRelies(commentIds);
        List<CommentDto.ResponseCommentList> responseCommentLists = commentMapper.commentsToResponseList(pagedComments.getContent(), isCommentHaveReplyDto);

        return new PageImpl<>(responseCommentLists, pageable, pagedComments.getTotalElements());
    }

    //대댓글 가져오기
    public List<CommentDto.Response> getReplies(Long commentId) {
        //parent id가 commnetId 인 것 다 가져오기
        List<Comment> allByParentId = commentRepository.findAllByParentId(commentId);
        if (allByParentId.isEmpty()) {
            throw new BusinessLogicException(ExceptionCode.REPLY_NOT_EXIST);
        }
        return commentMapper.commentsToResponse(allByParentId);
    }

    private Comment validateCommentWithCommentId(Long commentId) {
        return commentRepository.findById(commentId).orElseThrow(()-> new BusinessLogicException(ExceptionCode.COMMENT_NOT_EXIST));
    }

    private Comment validateCommentWithCommentIdAndUser(Long commentId) {
        return commentRepository.findByCommentWithUser(commentId).orElseThrow(()-> new BusinessLogicException(ExceptionCode.COMMENT_NOT_EXIST));
    }

    //내가 쓴 댓글인지 검증(현재는 무조건 가져와서 객체 비교나 id 비교 중 -> 오류 검증을 나누기 위해서)
//    private Comment ValidateCommentIsMy(Long commentId, Long userId) {
//        return commentRepository.findByIdAndUserId(commentId,userId).orElseThrow(()-> new BusinessLogicException(ExceptionCode.NO_PERMISSION));
//    }
}
