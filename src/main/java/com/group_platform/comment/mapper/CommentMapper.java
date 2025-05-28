package com.group_platform.comment.mapper;

import com.group_platform.comment.dto.CommentDto;
import com.group_platform.comment.entity.Comment;
import com.group_platform.user.mapper.UserMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

//usermapper주입
@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface CommentMapper {
    Comment createRequestToComment(CommentDto.CreateRequest createRequest);
    Comment updateRequestToComment(CommentDto.UpdateRequest updateRequest);

    @Mapping(target = "parent_id",  expression = "java(setParentIdIfExist(comment))")
    @Mapping(target = "writer", source = "user")
    CommentDto.Response commentToResponse(Comment comment);
    List<CommentDto.Response> commentsToResponse(List<Comment> comments);


    @Mapping(target = "replyCount", expression = "java(setReplyCount(isCommentHaveReplyDto))")
    @Mapping(target = "writer", source = "user")
    CommentDto.ResponseCommentList commentToResponseList(Comment comment, CommentDto.IsCommentHaveReplyDto isCommentHaveReplyDto);
    List<CommentDto.ResponseCommentList> commentsToResponseList(List<Comment> comments,List<CommentDto.IsCommentHaveReplyDto> isCommentHaveReplyDto);

    default Long setParentIdIfExist(Comment comment) {
        if (comment.getParent() == null) {
            return null;
        }
        return comment.getParent().getId();
    }

    default int setReplyCount(CommentDto.IsCommentHaveReplyDto isCommentHaveReplyDto) {
        return isCommentHaveReplyDto.getReplyCount().intValue();
    }
}
