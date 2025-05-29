package com.group_platform.comment.controller;

import com.group_platform.comment.dto.CommentDto;
import com.group_platform.comment.service.CommentService;
import com.group_platform.response.ResponseDto;
import com.group_platform.security.dto.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
public class CommentController {
    private final CommentService commentService;
    //댓글 작성
    @PostMapping("/posts/{post-id}/comments")
    public ResponseEntity<?> createComment(@AuthenticationPrincipal CustomUserDetails userDetails,
                                           @PathVariable("post-id") Long postId,
                                           @RequestBody @Valid CommentDto.CreateRequest createRequest) {
        CommentDto.Response response = commentService.createComment(userDetails.getId(), postId, createRequest);
        return ResponseEntity.ok(new ResponseDto.SingleResponseDto<>(response));
    }

    //댓글 수정
    @PutMapping("/comments/{comment-id}")
    public ResponseEntity<?> updateComment(@AuthenticationPrincipal CustomUserDetails userDetails,
                                           @PathVariable("comment-id") Long commentId,
                                           @RequestBody @Valid CommentDto.UpdateRequest updateRequest) {
        updateRequest.setId(commentId);
        CommentDto.Response response = commentService.updateComment(userDetails.getId(),updateRequest);
        return ResponseEntity.ok(new ResponseDto.SingleResponseDto<>(response));
    }

    //댓글 삭제
    @DeleteMapping("/comments/{comment-id}")
    public ResponseEntity<?> deleteComment(@AuthenticationPrincipal CustomUserDetails userDetails,
                                           @PathVariable("comment-id") Long commentId) {
        commentService.deleteComment(userDetails.getId(), commentId);
        return ResponseEntity.noContent().build();
    }

    //댓글 보기(페이징)
    @GetMapping("/posts/{post-id}/comments")
    public ResponseEntity<?> getComments(@PathVariable("post-id") Long postId,
                                         @PageableDefault(value = 10) Pageable pageable) {
        Page<CommentDto.ResponseCommentList> responseCommentLists = commentService.getAllComments(postId, pageable);
        return ResponseEntity.ok(new ResponseDto.MultipleResponseDto<>(responseCommentLists.getContent(), responseCommentLists));
    }

    //대댓글 불러오기
    @GetMapping("/comments/{comment-id}/replies")
    public ResponseEntity<?> getReplies(@PathVariable("comment-id") Long commentId) {
        List<CommentDto.Response> replies = commentService.getReplies(commentId);
        return ResponseEntity.ok(new ResponseDto.SingleResponseDto<>(replies));
    }
}
