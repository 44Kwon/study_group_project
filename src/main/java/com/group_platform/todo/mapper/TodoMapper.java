package com.group_platform.todo.mapper;

import com.group_platform.todo.dto.TodoDto;
import com.group_platform.todo.entity.Todo;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TodoMapper {
    Todo createRequestToTodo(TodoDto.createRequest createRequest);
    Todo updateRequestToTodo(TodoDto.updateRequest updateRequest);
    TodoDto.Response TodoToResponse(Todo todo);
}
