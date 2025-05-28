package com.group_platform.comment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.group_platform.user.dto.UserDto;
import com.querydsl.core.annotations.QueryProjection;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

public class CommentDto {
    @Getter
    @Setter
    @NoArgsConstructor
    public static class CreateRequest {
        @NotBlank(message = "내용을 입력해주세요")
        private String content;
        private Long parent_id; //대댓글시 댓글 id
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class UpdateRequest {
        private Long id;
        @Size(min = 5, max = 100, message = "문자 최소 및 최대 길이를 맞춰서 작성해주세요")
        private String content;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private String content;
        private UserDto.UserProfileDto writer;
        private Long parent_id;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
        private LocalDateTime updatedAt;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IsCommentHaveReplyDto {
        private Long id;
        //현재는 boolean 쿼리에서는 숫자로 오게 해놓음
        private Long replyCount;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResponseCommentList {
        private Long id;
        private String content;
        private UserDto.UserProfileDto writer;
        private int replyCount;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
        private LocalDateTime updatedAt;
    }
}
