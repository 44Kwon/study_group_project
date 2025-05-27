package com.group_platform.todo.repository;

import com.group_platform.sutdygroup.entity.StudyGroup;
import com.group_platform.sutdygroup.repository.StudyGroupRepository;
import com.group_platform.todo.entity.Todo;
import com.group_platform.todo.entity.TodoUser;
import com.group_platform.user.entity.User;
import com.group_platform.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class TodoRepositoryTest {

    @Autowired
    private TodoRepository todoRepository;

    @Autowired
    private TodoUserRepository todoUserRepository;

    @Autowired
    private StudyGroupRepository studyGroupRepository;
    @Autowired
    private UserRepository userRepository;

    @DisplayName("나에게 할당된 Todo들 가져오기")
    @Test
    void findMyAssignedTodo() {
        //given
        User user = User.builder()
                .username("username")
                .nickname("nickname")
                .password("password")
                .build();

        StudyGroup studyGroup = StudyGroup.builder()
                .name("name")
                .description("description")
                .build();

        Todo todo1 = Todo.builder()
                .title("title1")
                .allMembers(true)
                .studyGroup(studyGroup)
                .build();

        Todo todo2 = Todo.builder()
                .title("title2")
                .allMembers(false)
                .studyGroup(studyGroup)
                .build();

        Todo todo3 = Todo.builder()
                .title("title3")
                .allMembers(false)
                .studyGroup(studyGroup)
                .build();

        TodoUser todoUser1 = TodoUser.builder()
                .todo(todo1)
                .user(user)
                .build();
        TodoUser todoUser2 = TodoUser.builder()
                .todo(todo2)
                .build();
        TodoUser todoUser3 = TodoUser.builder()
                .todo(todo3)
                .user(user)
                .build();

        User savedUser = userRepository.save(user);
        StudyGroup savedGroup = studyGroupRepository.save(studyGroup);
        todoRepository.saveAll(List.of(todo1,todo2,todo3));
        todoUserRepository.saveAll(List.of(todoUser1,todoUser2,todoUser3));

        //when
        List<Todo> myAssignedTodo = todoRepository.findMyAssignedTodo(savedGroup.getId(), savedUser.getId());


        //then
        assertThat(myAssignedTodo).hasSize(2)
                .extracting("title")
                .containsExactlyInAnyOrder("title1", "title3");

    }
}