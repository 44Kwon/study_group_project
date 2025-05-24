package com.group_platform.studymember.entity;

import com.group_platform.sutdygroup.entity.StudyGroup;
import com.group_platform.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "study_members")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
//StudyGroup과 user의 중간테이블
public class StudyMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    //그룹 가입날
    private LocalDateTime joinDate;

    @LastModifiedDate
    private LocalDateTime lastModifiedDate;

    //상태(활성,비활성 => 탈퇴),
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ActiveStatus status = ActiveStatus.ACTIVE;

    //그룹에서의 역할(Leader, Members)
    //생성 시 수동으로 적절히 명시해서 집어넣어야 함
    @Enumerated(EnumType.STRING)
    private InGroupRole role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_group_id")
    private StudyGroup studyGroup;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public enum InGroupRole {
        LEADER, MEMBER;
    }

    public enum ActiveStatus{
        ACTIVE, INACTIVE;
    }

    public static StudyMember createStudyMember(StudyGroup studyGroup, User user, InGroupRole role){
        return StudyMember.builder()
                .studyGroup(studyGroup)
                .user(user)
                .role(role)
                .build();
    }

    //활성,비활성(그룹탈퇴) 상태변경
    public void changeStatus(ActiveStatus status){
        this.status = status;
    }

    public void changeRole(InGroupRole role){
        this.role = role;
    }

    public void giveLeaderRole(StudyMember newLeader){
        this.role = InGroupRole.MEMBER;
        newLeader.role = InGroupRole.LEADER;
    }
}
