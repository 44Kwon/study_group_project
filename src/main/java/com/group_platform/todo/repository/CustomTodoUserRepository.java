package com.group_platform.todo.repository;

import com.group_platform.todo.dto.TodoDto;

import java.util.List;

public interface CustomTodoUserRepository {
    List<TodoDto.TodoUserNicknameDto> findNicknamesByTodoIds(List<Long> todoIds);
}
