package com.group_platform.post.mapper;

import com.group_platform.post.bookmark.PostBookmark;
import com.group_platform.post.dto.PostDto;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PostBookmarkMapper {

    //NPE방지해서 afterMapping 사용? -> 그러나 fetchjoin으로 이미 null이 아님을 방지했음
    @Mapping(target = "postId", source = "postBookmark.post.id")
    @Mapping(target = "title", source = "postBookmark.post.title")
    @Mapping(target = "studyGroupId", ignore = true) // 기본 매핑에서는 생략
    PostDto.PostBookMarkResponse postBookmarkToPostBookmarkResponse(PostBookmark postBookmark);
    List<PostDto.PostBookMarkResponse> postBookmarksToPostBookmarkResponse(List<PostBookmark> postBookmarks);

    @AfterMapping
    //studyGroupId 넣어주기, NPE방지를 위해 따로 aftermapping 사용
    default void setStudyGroupId(PostBookmark postBookmark, @MappingTarget PostDto.PostBookMarkResponse response) {
        if (postBookmark.getPost() != null && postBookmark.getPost().getStudyGroup() != null) {
            response.setStudyGroupId(postBookmark.getPost().getStudyGroup().getId());
        }
    }
}
