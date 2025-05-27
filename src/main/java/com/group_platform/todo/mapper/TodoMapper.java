package com.group_platform.todo.mapper;

import com.group_platform.todo.dto.TodoDto;
import com.group_platform.todo.entity.Todo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDate;
import java.util.List;

@Mapper(componentModel = "spring")
public interface TodoMapper {
    Todo createRequestToTodo(TodoDto.createRequest createRequest);
    Todo updateRequestToTodo(TodoDto.updateRequest updateRequest);
    @Mapping(target = "overdue", expression = "java(isOverdue(todo))")
    TodoDto.Response todoToResponse(Todo todo);
    TodoDto.ResponseAssignedMy todoToResponseAssignedMy(Todo todo);
    @Mapping(target = "overdue", expression = "java(isOverdue(todo))")
    List<TodoDto.ResponseAssignedMy> todosToResponsesAssignedMy(List<Todo> todos);

    @Mapping(target = "overdue", expression = "java(isOverdue(todo))")
    List<TodoDto.Response> todosToResponse(List<Todo> todos);

    //overdue가 지났는지 보내주는 메서드(단일객체 보내줄때는 그냥 직접넣고있음 -> 변경완료)
    default boolean isOverdue(Todo todo) {
        return todo.getDue_date() != null
                && todo.getDue_date().isBefore(LocalDate.now())
                && !todo.getStatus().equals(Todo.TodoStatus.COMPLETED);
    }
}
