package com.group_platform.studymember.mapper;

import com.group_platform.studymember.dto.StudyMemberDto;
import com.group_platform.studymember.entity.StudyMember;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface StudyMemberMapper {
    List<StudyMemberDto.Response> StudyMembersToResponse(List<StudyMember> studyMembers);
    //List 변환을 사용하기 위해서는 단일 맵핑 메서드 필수(특히나 이렇게 연관관계가 엮여있는 경우)!!!
    @Mapping(source = "user.nickname", target = "nickname")
    StudyMemberDto.Response StudyMemberToResponse(StudyMember studyMember);
}
