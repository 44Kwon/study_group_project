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
    public static class UpdatePinnedResponse{
        private Long id;
        private boolean pinned;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateLikeOrFavouriteResponse{
        private Long id;
        private boolean status;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PostBookMarkResponse{
        private Long postId;
        private String title;
    }
}
