package com.group_platform.todo.repository;

import com.group_platform.todo.entity.TodoUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TodoUserRepository extends JpaRepository<TodoUser, Long> {
    void deleteAllInBatchByTodoId(Long todoId);
    @Query("SELECT tu FROM TodoUser tu JOIN FETCH tu.user WHERE tu.todo.id = :todoId")
    List<TodoUser> findByTodoUserWithUsername(Long todoId);
}
