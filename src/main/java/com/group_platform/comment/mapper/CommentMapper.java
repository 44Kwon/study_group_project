package com.group_platform.comment.mapper;

import com.group_platform.comment.dto.CommentDto;
import com.group_platform.comment.entity.Comment;
import com.group_platform.user.mapper.UserMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

//usermapper주입
@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface CommentMapper {
    Comment createRequestToComment(CommentDto.CreateRequest createRequest);
    Comment updateRequestToComment(CommentDto.UpdateRequest updateRequest);

    @Mapping(target = "parent_id",  expression = "java(setParentIdIfExist(comment))")
    @Mapping(target = "writer", source = "user")
    CommentDto.Response commentToResponse(Comment comment);
    List<CommentDto.Response> commentsToResponse(List<Comment> comments);


//    @Mapping(target = "replyCount", expression = "java(setReplyCount(isCommentHaveReplyDto))")
    @Mapping(target ="replyCount", source="replyCount")
    @Mapping(target = "writer", source = "comment.user")
    @Mapping(target = "id", source = "comment.id")
    CommentDto.ResponseCommentList commentToResponseList(Comment comment, int replyCount);

    default List<CommentDto.ResponseCommentList> commentsToResponseList(List<Comment> comments,List<CommentDto.IsCommentHaveReplyDto> isCommentHaveReplyDtos) {
//        Map<Long, CommentDto.IsCommentHaveReplyDto> map = isCommentHaveReplyDtos.stream()
//                .collect(Collectors.toMap(CommentDto.IsCommentHaveReplyDto::getId, dto -> dto));
//        List<CommentDto.ResponseCommentList> result = comments.stream()
//                .map(comment -> {
//                    CommentDto.IsCommentHaveReplyDto dto = map.getOrDefault(
//                            comment.getId(), new CommentDto.IsCommentHaveReplyDto(comment.getId(), 0L)
//                    );
//                    return commentToResponseList(comment, dto);
//                })
//                .toList();

        Map<Long, Long> map = isCommentHaveReplyDtos.stream()
                .collect(Collectors.toMap(CommentDto.IsCommentHaveReplyDto::getId, CommentDto.IsCommentHaveReplyDto::getReplyCount));

        return comments.stream()
                .map((comment) -> {
                    Long replyCount = map.getOrDefault(comment.getId(), 0L);
                    return commentToResponseList(comment, replyCount.intValue());
                })
                .toList();
    }

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
