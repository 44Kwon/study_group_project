package com.group_platform.sutdygroup.repository;

import com.group_platform.sutdygroup.dto.StudyGroupDto;
import com.group_platform.sutdygroup.entity.StudyGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CustomStudyGroupRepository {
    Page<StudyGroupDto.ResponseList> searchStudyGroups(String keyword, Pageable pageable);
}
