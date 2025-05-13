package com.group_platform.todo.entity;

import com.group_platform.audit.BaseEntity;
import com.group_platform.sutdygroup.entity.StudyGroup;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "todos")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Todo extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(length = 500)   //null 허용
    private String description;

    private LocalDate due_date;

    private boolean isAllMembers;   //담당자를 스터디구성원 전부로 할지

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TodoStatus status = TodoStatus.PLANNED;

    public enum TodoStatus {
        PLANNED,     // 예정
        IN_PROGRESS, // 진행중
        COMPLETED    // 완료
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_group_id")
    private StudyGroup studyGroup;

    @OneToMany(mappedBy = "todo",cascade = {CascadeType.PERSIST,CascadeType.REMOVE}, orphanRemoval = true)
    private List<TodoUser> todoUsers = new ArrayList<>();
}
