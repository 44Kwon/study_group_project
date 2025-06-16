package com.group_platform.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.group_platform.post.dto.PostDto;
import com.group_platform.response.PageInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Setter
//Mapstruct에서는 기본생성자 + setter로 값을 넣는다. builder가 있으면 builder가 우선인듯하다
//@Builder
//프로필 조회 관련 response
public class UserMyProfileDto {
    private Long id;
    private String username;
    private String email;
    private String nickname;
    //가입날짜
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate createdAt;

    //수정날짜
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    //찜관련
    //PostId와 postTitle이 필요
    List<PostDto.PostBookMarkResponse> bookmarks;

    //찜 페이지네이션 정보
    private PageInfo bookmarksPage;
}
