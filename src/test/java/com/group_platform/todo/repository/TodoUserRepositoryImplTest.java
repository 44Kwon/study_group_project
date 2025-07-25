package com.group_platform.todo.repository;

import com.group_platform.config.QueryDslConfig;
import com.group_platform.todo.dto.TodoDto;
import com.group_platform.todo.entity.Todo;
import com.group_platform.user.entity.User;
import com.group_platform.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(QueryDslConfig.class)
@ActiveProfiles("test")
class TodoUserRepositoryImplTest {

    @Autowired
    private TodoUserRepositoryImpl todoUserRepositoryImpl;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TodoRepository todoRepository;

    @DisplayName("Todo에 할당된 유저들의 닉네임을 Todo ID로 조회한다")
    @Test
    void findNicknamesByTodoIds() {
        //given
        User user1 = User.builder()
                .username("username1")
                .nickname("nickname1")
                .password("password1")
                .build();
        User user2 = User.builder()
                .username("username2")
                .nickname("nickname2")
                .password("password2")
                .build();
        User user3 = User.builder()
                .username("username3")
                .nickname("nickname3")
                .password("password3")
                .build();
        userRepository.saveAll(List.of(user1, user2, user3));

        Todo todo1 = Todo.builder()
                .title("title1")
                .allMembers(true)
                .build();

        Todo todo2 = Todo.builder()
                .title("title2")
                .allMembers(false)
                .build();

        List<String> nicknames = new ArrayList<>();
        todo2.addAssignMembers(List.of(user1,user2), nicknames);

        //cascade를 이용한 저장
        List<Todo> todos = todoRepository.saveAll(List.of(todo1, todo2));
        //when
        List<TodoDto.TodoUserNicknameDto> nicknamesByTodoIds = todoUserRepositoryImpl.findNicknamesByTodoIds(todos.stream().map(Todo::getId).toList());

        Map<Long, List<String>> map = nicknamesByTodoIds.stream().collect(Collectors.groupingBy(
                TodoDto.TodoUserNicknameDto::getTodoId,
                Collectors.mapping(TodoDto.TodoUserNicknameDto::getNickname, Collectors.toList())
        ));

        //then
        assertThat(nicknamesByTodoIds).hasSize(2);

        assertThat(map).hasSize(1)
                .containsExactly(
                        Map.entry(todo2.getId(), List.of("nickname1", "nickname2"))
                );
    }
}