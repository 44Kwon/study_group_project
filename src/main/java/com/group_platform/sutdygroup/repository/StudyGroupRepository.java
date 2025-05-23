package com.group_platform.sutdygroup.repository;

import com.group_platform.category.entity.CategoryType;
import com.group_platform.sutdygroup.entity.StudyGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudyGroupRepository extends JpaRepository<StudyGroup, Long> {
    Page<StudyGroup> findByTypeAndStatus(CategoryType type, StudyGroup.GroupStatus status, Pageable pageable);

    Page<StudyGroup> findByStatus(StudyGroup.GroupStatus status, Pageable pageable);
}
