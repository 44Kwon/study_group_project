package com.group_platform.studymember.repository;

import com.group_platform.studymember.entity.StudyMember;
import com.group_platform.sutdygroup.entity.StudyGroup;
import com.group_platform.sutdygroup.repository.StudyGroupRepository;
import com.group_platform.user.entity.User;
import com.group_platform.user.repository.UserRepository;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS) // 필수: BeforeAll을 static 없이 사용하
class StudyMemberRepositoryTest {

    @Autowired
    private StudyMemberRepository studyMemberRepository;
    @Autowired
    private StudyGroupRepository studyGroupRepository;
    @Autowired
    private UserRepository userRepository;

    @BeforeAll
    void setUp() {
        User user1 = User.builder()
                .username("user1")
                .nickname("user1")
                .password("user1")
                .build();

        User user2 = User.builder()
                .username("user2")
                .nickname("user2")
                .password("user2")
                .build();

        User user3 = User.builder()
                .username("user3")
                .nickname("user3")
                .password("user3")
                .build();

        StudyGroup studyGroup1 = StudyGroup.builder()
                .name("studyGroup1")
                .description("studyGroup1")
                .build();

        StudyMember studyMember1 = StudyMember.builder()
                .role(StudyMember.InGroupRole.LEADER)
                .studyGroup(studyGroup1)
                .user(user1)
                .build();

        StudyMember studyMember2 = StudyMember.builder()
                .role(StudyMember.InGroupRole.MEMBER)
                .studyGroup(studyGroup1)
                .user(user2)
                .build();

        StudyMember studyMember3 = StudyMember.builder()
                .role(StudyMember.InGroupRole.MEMBER)
                .status(StudyMember.ActiveStatus.INACTIVE)
                .studyGroup(studyGroup1)
                .user(user3)
                .build();


        userRepository.saveAll(List.of(user1, user2, user3));
        studyGroupRepository.save(studyGroup1);
        studyMemberRepository.saveAll(List.of(studyMember1, studyMember2, studyMember3));
    }


    @Test
    @DisplayName("스터디 멤버 가져오기")
    void testFindByUserIdAndStudyGroupIdAndStatus() {
        //when
        Optional<StudyMember> result = studyMemberRepository.findByUser_IdAndStudyGroup_IdAndStatus(1L, 1L, StudyMember.ActiveStatus.ACTIVE);
        //then
        assertThat(result).isPresent();
        StudyMember studyMember = result.get();
        assertThat(studyMember.getRole()).isEqualTo(StudyMember.InGroupRole.LEADER);
    }

    @Test
    @DisplayName("스터디 멤버 존재하는지")
    void testExistsByUserIdAndStudyGroupIdAndRole() {
        //when
        boolean exists = studyMemberRepository.existsByUser_IdAndStudyGroup_IdAndRoleAndStatus(2L, 1L, StudyMember.InGroupRole.MEMBER, StudyMember.ActiveStatus.ACTIVE);
        //then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("스터디 멤버 숫자 일치확인")
    void testCountByStudyGroupIdAndStatus() {
        //when
        long count = studyMemberRepository.countByStudyGroup_IdAndStatus(1L, StudyMember.ActiveStatus.ACTIVE);

        //then
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("내가 아닌 오래된 스터디 멤버 가져오기")
    void testFindFirstByStudyGroupIdAndStatusOrderByJoinDateAsc() {
        //when
        Optional<StudyMember> studyMember = studyMemberRepository.findById(1L);
        Optional<StudyMember> result = studyMemberRepository.findFirstByStudyGroup_IdAndStatusAndIdNotOrderByJoinDateAsc
                (1L, StudyMember.ActiveStatus.ACTIVE, studyMember.get().getId());

        //then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("스터디 멤버를 유저정보와 함께 fetch join")
    void testFindAllByStudyGroupIdAndStatusOrderByJoinDateAsc() {
        //when
        List<StudyMember> members = studyMemberRepository.findAllByStudyGroupIdAndStatusOrderByJoinDateAsc(1L, StudyMember.ActiveStatus.ACTIVE);

        //then
        assertThat(members).hasSize(2)
                .extracting(StudyMember::getUser)
                .extracting(User::getUsername)
                .containsExactlyInAnyOrder("user1", "user2");
    }
}