package com.group_platform.post.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.group_platform.comment.dto.CommentDto;
import com.group_platform.post.entity.PostType;
import com.group_platform.response.PageInfo;
import com.group_platform.user.dto.UserDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
//게시글 상세조회
public class PostResponseDto {
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
    //내가쓴건지
    private boolean isMine;
    //좋아요 했는지
    private boolean isLiked;
    //찜 했는지
    private boolean isBookmarked;
    //댓글들
    private List<CommentDto.ResponseCommentList> comments;
    //댓글 페이징정보
    private PageInfo commentsPage;
}
