package com.group_platform.user.mapper;

import com.group_platform.user.dto.UserDto;
import com.group_platform.user.dto.UserMyProfileDto;
import com.group_platform.user.dto.UserResponseDto;
import com.group_platform.user.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    //@Mapping을 쓰는 경우 => dto와 엔티티의 필드명이 다를 때나 타입을 바꾸려 할 때

    User CreateRequestToUser(UserDto.CreateRequest createRequest);
    User UpdateRequestToUser(UserDto.UpdateRequest updateRequest);
    UserDto.UpdateResponse UserToUpdateResponse(User user);
    UserResponseDto UserToUserResponseDto(User user);


    //게시글,댓글 등에서 쓰는 사용자 정보
    UserDto.UserProfileDto userToUserProfileDto(User user);

    UserMyProfileDto userToUserMyProfileDto(User user);
}
