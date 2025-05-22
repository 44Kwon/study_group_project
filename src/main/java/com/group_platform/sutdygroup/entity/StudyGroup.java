package com.group_platform.sutdygroup.entity;

import com.group_platform.audit.BaseEntity;
import com.group_platform.category.entity.CategoryType;
import com.group_platform.studymember.entity.StudyMember;
import com.group_platform.todo.entity.Todo;
import com.group_platform.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "study_groups")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class StudyGroup extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 500)
    private String description;

    //처음 생성 시 1로
    //현재는 필드에서 관리. 향후 -> redis에서 캐싱처리 해볼 것(만약 값이 없다면 count쿼리로 가져와서 넣기)
    @Column(nullable = false)
    @Builder.Default
    private int currentMembers = 1;

    @Builder.Default
    private int maxMembers = 6;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private GroupStatus status = GroupStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    //전역 카테고리 타입
    private CategoryType type;

    public enum GroupStatus {
        ACTIVE, INACTIVE, CLOSED    //ACTIVE : 활성화, INACTIVE : 휴면, CLOSE : 삭제
    }

//    private String imageUrl; // 그룹의 이미지 URL 나중에 처리

    @OneToMany(mappedBy = "studyGroup", cascade = {CascadeType.PERSIST,CascadeType.REMOVE}, orphanRemoval = true)
    private List<StudyMember> studyMembers = new ArrayList<>();

    @OneToMany(mappedBy = "studyGroup",cascade = {CascadeType.PERSIST,CascadeType.REMOVE}, orphanRemoval = true)
    private List<Todo> todos = new ArrayList<>();

    //연관관계 메서드
    public void addStudyMember(User user, StudyMember.InGroupRole role) {
        StudyMember studyMember = StudyMember.createStudyMember(this, user, role);
        studyMembers.add(studyMember);
    }

    public void changeName(String name) {
        this.name = name;
    }

    public void changeDescription(String description) {
        this.description = description;
    }

    public void changeMaxMembers(int maxMembers) {
        this.maxMembers = maxMembers;
    }

    //그룹삭제 시 쓰는 연관관계 메서드
    public void changeStatusToDelete(StudyMember studyMember) {
        this.status = GroupStatus.CLOSED;
        studyMember.changeStatus(StudyMember.ActiveStatus.INACTIVE);
    }
}
