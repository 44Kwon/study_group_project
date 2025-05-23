package com.group_platform.sutdygroup.mapper;

import com.group_platform.sutdygroup.dto.StudyGroupDto;
import com.group_platform.sutdygroup.dto.StudyGroupResponseDto;
import com.group_platform.sutdygroup.entity.StudyGroup;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface StudyGroupMapper {
    @Mapping(target = "type",source = "categoryType")   //@Mapping 연습용으로 써봄
    StudyGroup createRequestToStudyGroup(StudyGroupDto.CreateRequest createRequest);
    StudyGroup updateRequestToStudyGroup(StudyGroupDto.UpdateRequest updateRequest);
    StudyGroupResponseDto studyGroupToStudyGroupResponseDto(StudyGroup studyGroup);
    StudyGroupDto.ResponseList studyGroupToStudyGroupResponseListDto(StudyGroup studyGroup);
}
