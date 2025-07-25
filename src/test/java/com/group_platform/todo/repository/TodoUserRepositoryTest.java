package com.group_platform.todo.repository;

import com.group_platform.config.QueryDslConfig;
import com.group_platform.todo.entity.Todo;
import com.group_platform.todo.entity.TodoUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

//@SpringBootTest
//@Transactional
@DataJpaTest
@Import(QueryDslConfig.class)
@ActiveProfiles("test")
class TodoUserRepositoryTest {

    @Autowired
    private TodoUserRepository todoUserRepository;
    @Autowired
    private TodoRepository todoRepository;

    @DisplayName("Todo에 할당된 유저들(TodoUser)에 대한 정보를 전부 삭제한다")
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
        long countByTodo = todoUserRepository.countByTodo(savedTodo);
        todoUserRepository.deleteAllInBatchByTodoId(savedTodo.getId());
        //then
        List<TodoUser> all = todoUserRepository.findAll();

        assertThat(countByTodo).isEqualTo(10);
        assertThat(all.size()).isZero();
    }
}