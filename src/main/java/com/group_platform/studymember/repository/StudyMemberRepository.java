package com.group_platform.studymember.repository;

import com.group_platform.studymember.entity.StudyMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudyMemberRepository extends JpaRepository<StudyMember, Long> {
    Optional<StudyMember> findByUserIdAndStudyGroup_Id(Long userId, Long studyGroupId);
    Optional<StudyMember> findByUser_IdAndStudyGroup_IdAndStatus(Long userId, Long studyGroupId, StudyMember.ActiveStatus status);

    boolean existsByUser_IdAndStudyGroup_IdAndRoleAndStatus(Long userId, Long studyGroupId, StudyMember.InGroupRole role, StudyMember.ActiveStatus status);
    boolean existsByUser_IdAndStudyGroup_IdAndStatus(Long user_id, Long studyGroup_id, StudyMember.ActiveStatus status);
    long countByStudyGroup_IdAndStatus(Long studyGroupId, StudyMember.ActiveStatus status);

    Optional<StudyMember> findFirstByStudyGroup_IdAndStatusAndIdNotOrderByJoinDateAsc(Long studyGroupId, StudyMember.ActiveStatus status, Long id);

    @Query("SELECT sm FROM StudyMember sm JOIN FETCH sm.user WHERE sm.studyGroup.id = :studyGroupId AND sm.status = :status ORDER BY sm.joinDate ASC")
    List<StudyMember> findAllByStudyGroupIdAndStatusOrderByJoinDateAsc(Long studyGroupId, StudyMember.ActiveStatus status);

    @Query("SELECT sm FROM StudyMember sm JOIN FETCH sm.user JOIN FETCH sm.studyGroup WHERE sm.user.id = :userId AND sm.studyGroup.id = :groupId AND sm.status = :status")
    Optional<StudyMember> findStudyMemberWithUserAndGroup(Long userId, Long groupId, StudyMember.ActiveStatus status);

    boolean existsByUserIdAndStatusAndRole(Long userId, StudyMember.ActiveStatus status, StudyMember.InGroupRole role);
    //엔티티그래프 방법
//    @EntityGraph(attributePaths = {"user"})
//    List<StudyMember> findAllByStudyGroupIdAndStatusOrderByJoinDateAsc(Long studyGroupId, StudyMember.ActiveStatus status);
}
