package com.group_platform.post.mapper;

import com.group_platform.comment.mapper.CommentMapper;
import com.group_platform.post.dto.PostDto;
import com.group_platform.post.dto.PostResponseDto;
import com.group_platform.post.dto.PostResponseListDto;
import com.group_platform.post.entity.Post;
import com.group_platform.user.mapper.UserMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface PostMapper {
    Post createCommonToPost(PostDto.CreateRequestForCommonPost createRequestForCommonPost);
    Post createGroupToPost(PostDto.CreateRequestForGroupPost createRequestForGroupPost);
    Post updateToPost(PostDto.UpdateRequest updateRequest);


    //댓글 맵핑은 서비스에서 처리
    @Mapping(target = "writer", source = "user")
    @Mapping(target = "studyGroupId", source = "studyGroup.id") //조심해야함. 다른데서는 NPE난다
    PostResponseDto PostToResponse(Post post);

    @Mapping(target = "writer", source = "user")
    PostResponseListDto postToResponseList(Post post);
    List<PostResponseListDto> postToResponseList(List<Post> posts);
}
