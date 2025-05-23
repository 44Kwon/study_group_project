package com.group_platform.studymember.mapper;

import com.group_platform.studymember.dto.StudyMemberDto;
import com.group_platform.studymember.entity.StudyMember;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface StudyMemberMapper {
    @Mapping(source = "user.nickname", target = "nickname")
    List<StudyMemberDto.Response> StudyMembersToResponse(List<StudyMember> studyMembers);
}
