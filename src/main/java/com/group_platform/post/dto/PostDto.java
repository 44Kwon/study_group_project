package com.group_platform.post.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.group_platform.comment.dto.CommentDto;
import com.group_platform.post.entity.PostType;
import com.group_platform.response.PageInfo;
import com.group_platform.user.dto.UserDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

public class PostDto {
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    //그룹 내 게시판
    public static class CreateRequestForGroupPost {
        @NotBlank
        private String title;
        @NotBlank
        private String content;
        private PostType postType; //null이면 일반으로 두자
        private Boolean isPinned;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    //공통게시판
    public static class CreateRequestForCommonPost {
        @NotBlank
        private String title;
        @NotBlank
        private String content;
        private PostType postType; //null이면 일반으로 두자
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest{
        private Long id;
        @Size(min = 2, max = 30)
        private String title;
        @Size(min = 2, max = 50)
        private String content;
        private PostType postType;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    //게시글 상세조회
    public static class Response {
        private Long id;
        private String title;
        private String content;
        private int viewCount;
        private int comment_count;
        private boolean isPinned;
        private PostType postType;
        private int likes;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
        private LocalDateTime createdAt;
        //값이 있으면 해당 스터디그룹 소속글, 없으면 공통 게시판
        private Long studyGroupId;
        //게시글 작성자
        private UserDto.UserProfileDto writer;
        //댓글들
        private List<CommentDto.ResponseCommentList> comments;
        //댓글 페이징정보
        private PageInfo commentsPage;
    }

    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    //게시글 목록조회
    public static class ResponseList {
        private Long id;
        private String title;
        private int viewCount;
        private int comment_count;
        private boolean isPinned;
        private PostType postType;
        private int likes;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
        private LocalDateTime createdAt;
        //게시글 작성자
        private UserDto.UserProfileDto writer;
    }
}
