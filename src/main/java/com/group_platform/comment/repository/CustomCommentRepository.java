package com.group_platform.comment.repository;

import com.group_platform.comment.dto.CommentDto;

import java.util.List;

public interface CustomCommentRepository {
    List<CommentDto.IsCommentHaveReplyDto> existsByRelies(List<Long> parentIds);
}
