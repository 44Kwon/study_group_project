package com.group_platform.todo.entity;

import com.group_platform.audit.BaseEntity;
import com.group_platform.sutdygroup.entity.StudyGroup;
import com.group_platform.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

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

    @Setter
    @Builder.Default
    private boolean allMembers = true;   //담당자를 스터디구성원 전부로 할지

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TodoStatus status = TodoStatus.PLANNED;

    @Version
    private Long version;

    public enum TodoStatus {
        PLANNED,     // 예정
        IN_PROGRESS, // 진행중
        COMPLETED,    // 완료

    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_group_id")
    private StudyGroup studyGroup;

    @OneToMany(mappedBy = "todo",cascade = {CascadeType.PERSIST,CascadeType.REMOVE}, orphanRemoval = true)
    @Builder.Default
    private List<TodoUser> todoUsers = new ArrayList<>();

    //연관관계 메서드 (할당인원이 있을때 쓰는 것)
    public List<TodoUser> addAssignMembers(List<User> userList, List<String> names) {
        List<TodoUser> newTodoUsers = new ArrayList<>();
        for (User user : userList) {
            TodoUser todoUser = TodoUser.builder()
                    .user(user)
                    .todo(this)
                    .build();
            newTodoUsers.add(todoUser);
            names.add(user.getNickname());
        }
        this.todoUsers = newTodoUsers;
        return newTodoUsers;

        //만약 대용량 처리 저장시에는 JPA연관관계를 통한게 아닌 다이렉트로 저장하는게 좋다
//        List<TodoUser> todoUsers = userList.stream()
//                .map(user -> TodoUser.builder()
//                        .user(user)
//                        .todo(todo)
//                        .build())
//                .toList();
//
//        todoUserRepository.saveAll(todoUsers); (서비스에서처리)
    }

    public void changeTitle(String title) {
        this.title = title;
    }

    public void changeDescription(String description) {
        this.description = description;
    }

    public void changeDueDate(LocalDate dueDate) {
        this.due_date = dueDate;
    }

    public void changeStatus(TodoStatus todoStatus) {
        this.status = todoStatus;
    }
}
