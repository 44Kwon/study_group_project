package com.group_platform.todo.repository;

import com.group_platform.todo.entity.Todo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TodoRepository extends JpaRepository<Todo,Long> {
    Optional<Todo> findByIdAndStudyGroupId(Long id, Long studyGroupId);

    List<Todo> findAllByStudyGroupId(Long groupId);

    //내가 할당된 할일과 전체에게 할당된 할일 가져오기
    //allmembers가 true인 것은 연관테이블 값이 없이니 left join!
    @Query("SELECT DISTINCT td FROM Todo td LEFT JOIN td.todoUsers tdu WHERE (td.studyGroup.id = :groupId) AND (td.allMembers = true OR tdu.user.id = :userId)")
    List<Todo> findMyAssignedTodo(Long groupId, Long userId);
}
