package com.group_platform.post.dto;

import com.group_platform.response.ResponseDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
//고정글 + 게시글 목록 보여주는 그룹 내 response 리스트 dto
public class PostListResponse {
    private List<PostResponseListDto> pinnedPosts;
    private ResponseDto.PageDto<PostResponseListDto> normalPosts;

    public PostListResponse(List<PostResponseListDto> pinnedPosts, Page<PostResponseListDto> normalPosts) {
        this.pinnedPosts = pinnedPosts;
        this.normalPosts = new ResponseDto.PageDto<>(normalPosts.getContent(), normalPosts);
    }
}
