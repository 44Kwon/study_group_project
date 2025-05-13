package com.group_platform.todo.entity;


import com.group_platform.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "todo_users")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TodoUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private boolean isCompleted;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "todo_id")
    private Todo todo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}
