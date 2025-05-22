package com.group_platform.studymember.repository;

import com.group_platform.studymember.entity.StudyMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudyMemberRepository extends JpaRepository<StudyMember, Long> {
    Optional<StudyMember> findByUserIdAndStudyGroupId(Long userId, Long studyGroupId);
    boolean existsByUserIdAndStudyGroupIdAndRole(Long user_id, Long studyGroup_id, StudyMember.InGroupRole role);
    long countByStudyGroupIdAndStatus(Long studyGroupId, StudyMember.ActiveStatus status);
}
