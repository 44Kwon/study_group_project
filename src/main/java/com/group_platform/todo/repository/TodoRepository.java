package com.group_platform.todo.repository;

import com.group_platform.todo.entity.Todo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TodoRepository extends JpaRepository<Todo,Long> {
    Optional<Todo> findByIdAndStudyGroupId(Long id, Long studyGroupId);
}
