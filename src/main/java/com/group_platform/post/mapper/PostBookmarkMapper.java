package com.group_platform.post.mapper;

import com.group_platform.post.bookmark.PostBookmark;
import com.group_platform.post.dto.PostDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PostBookmarkMapper {

    @Mapping(target = "postId", source = "postBookmark.post.id")
    @Mapping(target = "title", source = "postBookmark.post.title")
    //NPE방지해서 afterMapping 사용? -> 그러나 fetchjoin으로 이미 null이 아님을 방지했음
    PostDto.PostBookMarkResponse postBookmarkToPostBookmarkResponse(PostBookmark postBookmark);
    List<PostDto.PostBookMarkResponse> postBookmarksToPostBookmarkResponse(List<PostBookmark> postBookmarks);
}
