package com.group_platform.sutdygroup.repository;

import com.group_platform.category.entity.CategoryType;
import com.group_platform.sutdygroup.entity.StudyGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
//인터페이스는 중복상속 가능하다
public interface StudyGroupRepository extends JpaRepository<StudyGroup, Long>, CustomStudyGroupRepository {
    //페이징 쿼리는 기본적으로 두번친다는 것을 알아두자!!!(count를 매번 가져와야함 -> 캐싱처리해야하나?)
    Page<StudyGroup> findByTypeAndStatus(CategoryType type, StudyGroup.GroupStatus status, Pageable pageable);

    Page<StudyGroup> findByStatus(StudyGroup.GroupStatus status, Pageable pageable);
}
