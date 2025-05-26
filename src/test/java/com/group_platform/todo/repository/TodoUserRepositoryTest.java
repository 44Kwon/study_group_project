package com.group_platform.todo.repository;

import com.group_platform.todo.entity.Todo;
import com.group_platform.todo.entity.TodoUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class TodoUserRepositoryTest {

    @Autowired
    private TodoUserRepository todoUserRepository;
    @Autowired
    private TodoRepository todoRepository;

    @DisplayName("외래키로 벌크 삭제")
    @Test
    void deleteAllInBatchTodoId() {
        //given
        Todo todo = Todo.builder()
                .title("title")
                .build();

        ArrayList<TodoUser> todoUsers = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            TodoUser todoUser = TodoUser.builder()
                    .todo(todo)
                    .build();
            todoUsers.add(todoUser);
        }
        Todo savedTodo = todoRepository.save(todo);
        todoUserRepository.saveAll(todoUsers);

        //when
        todoUserRepository.deleteAllInBatchByTodoId(savedTodo.getId());
        //then
        List<TodoUser> all = todoUserRepository.findAll();

        assertThat(all.size()).isZero();
    }

}