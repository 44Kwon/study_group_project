package com.group_platform.post.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.group_platform.post.entity.PostType;
import com.group_platform.user.dto.UserDto;
import com.querydsl.core.annotations.QueryProjection;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
//게시글 목록조회
public class PostResponseListDto {
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
    //내가 쓴건지
    private boolean isMine;

    //querydsl dto쿼리용
    @QueryProjection
    public PostResponseListDto(Long id, String title, int viewCount, int comment_count, boolean isPinned, PostType postType, int likes, LocalDateTime createdAt, Long writerId, String writerName, boolean isMine) {
        this.id = id;
        this.title = title;
        this.viewCount = viewCount;
        this.comment_count = comment_count;
        this.isPinned = isPinned;
        this.postType = postType;
        this.likes = likes;
        this.createdAt = createdAt;
        this.writer = new UserDto.UserProfileDto(writerId,writerName);
        this.isMine = isMine;
    }
}
