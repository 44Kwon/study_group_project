package com.group_platform.todo.repository;

import com.group_platform.todo.dto.TodoDto;
import com.group_platform.todo.entity.TodoUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TodoUserRepository extends JpaRepository<TodoUser, Long>, CustomTodoUserRepository {
    void deleteAllInBatchByTodoId(Long todoId);
    @Query("SELECT tu FROM TodoUser tu JOIN FETCH tu.user WHERE tu.todo.id = :todoId")
    List<TodoUser> findByTodoUserWithUsername(Long todoId);

    // dto조회 시 Alias가 안먹힘...
    // 중첩클래스로 인해 인식을 못하는듯 함
//    @Query("SELECT new com.group_platform.todo.dto.TodoDto.TodoUserNicknameDto(tu.todo.id, u.nickname) " +
//            "FROM TodoUser tu JOIN tu.user u WHERE tu.todo.id IN :todoIds")
//    List<TodoDto.TodoUserNicknameDto> findNicknamesByTodoIds(@Param("todoIds") List<Long> todoIds);
}
