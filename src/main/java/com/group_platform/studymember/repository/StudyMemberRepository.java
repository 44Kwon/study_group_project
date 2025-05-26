package com.group_platform.studymember.repository;

import com.group_platform.studymember.entity.StudyMember;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudyMemberRepository extends JpaRepository<StudyMember, Long> {
    Optional<StudyMember> findByUserIdAndStudyGroupId(Long userId, Long studyGroupId);
    Optional<StudyMember> findByUserIdAndStudyGroupIdAndStatus(Long userId, Long studyGroupId, StudyMember.ActiveStatus status);
    boolean existsByUserIdAndStudyGroupIdAndRole(Long user_id, Long studyGroup_id, StudyMember.InGroupRole role);
    boolean existsByUserIdAndStudyGroupIdAndStatus(Long user_id, Long studyGroup_id, StudyMember.ActiveStatus status);
    long countByStudyGroupIdAndStatus(Long studyGroupId, StudyMember.ActiveStatus status);

    Optional<StudyMember> findFirstByStudyGroupIdAndStatusAndIdNotOrderByJoinDateAsc(Long studyGroupId, StudyMember.ActiveStatus status, Long id);

    @Query("SELECT sm FROM StudyMember sm JOIN FETCH sm.user WHERE sm.studyGroup.id = :studyGroupId AND sm.status = :status ORDER BY sm.joinDate ASC")
    List<StudyMember> findAllByStudyGroupIdAndStatusOrderByJoinDateAsc(Long studyGroupId, StudyMember.ActiveStatus status);
    //엔티티그래프 방법
//    @EntityGraph(attributePaths = {"user"})
//    List<StudyMember> findAllByStudyGroupIdAndStatusOrderByJoinDateAsc(Long studyGroupId, StudyMember.ActiveStatus status);
}
