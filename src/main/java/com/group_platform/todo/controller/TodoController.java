package com.group_platform.todo.controller;

import com.group_platform.response.ResponseDto;
import com.group_platform.security.dto.CustomUserDetails;
import com.group_platform.todo.dto.TodoDto;
import com.group_platform.todo.entity.Todo;
import com.group_platform.todo.service.TodoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/study-group/{study-group-id}/todos")
@Validated
@RequiredArgsConstructor
public class TodoController {

    private final TodoService todoService;

    @PostMapping
    public ResponseEntity<?> createTodo(@AuthenticationPrincipal CustomUserDetails userDetails,
                                        @PathVariable("study-group-id") Long groupId,
                                        @RequestBody @Valid TodoDto.createRequest createRequest) {
        TodoDto.Response todo = todoService.createTodo(userDetails.getId(), groupId, createRequest);
        return new ResponseEntity<>(new ResponseDto.SingleResponseDto<>(todo), HttpStatus.CREATED);
    }

    @PatchMapping("/{todo-id}")
    public ResponseEntity<?> updateTodo (@AuthenticationPrincipal CustomUserDetails userDetails,
                                         @PathVariable("study-group-id") Long groupId,
                                         @PathVariable("todo-id") Long todoId,
                                         @RequestBody @Valid TodoDto.updateRequest updateRequest) {
        TodoDto.Response todo = todoService.updateTodo(userDetails.getId(), groupId, todoId, updateRequest);
        return new ResponseEntity<>(new ResponseDto.SingleResponseDto<>(todo), HttpStatus.OK);
    }

    @GetMapping("/{todo-id}")
    public ResponseEntity<?> getTodo(@PathVariable("study-group-id") Long groupId,
                                     @PathVariable("todo-id") Long todoId) {
        TodoDto.Response todo = todoService.getTodo(groupId, todoId);
        return new ResponseEntity<>(new ResponseDto.SingleResponseDto<>(todo), HttpStatus.OK);
    }

    @GetMapping
    //전체 주는거
    public ResponseEntity<?> getAllTodo(@AuthenticationPrincipal CustomUserDetails userDetails,
                                        @PathVariable("study-group-id") Long groupId) {
        List<TodoDto.Response> allTodo = todoService.getAllTodo(userDetails.getId(), groupId);
        return new ResponseEntity<>(new ResponseDto.SingleResponseDto<>(allTodo), HttpStatus.OK);
    }

    @DeleteMapping("/{todo-id}")
    public ResponseEntity<?> deleteTodo(@AuthenticationPrincipal CustomUserDetails userDetails,
                                        @PathVariable("study-group-id") Long groupId,
                                        @PathVariable("todo-id") Long todoId) {
        todoService.deleteTodo(userDetails.getId(), groupId, todoId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{todo-id}/status")
    //상태(예정/진행중/완료) 변경
    public ResponseEntity<?> changeStatus(@AuthenticationPrincipal CustomUserDetails userDetails,
                                          @PathVariable("study-group-id") Long groupId,
                                          @PathVariable("todo-id") Long todoId,
                                          @RequestParam("status") Todo.TodoStatus status) {
        TodoDto.Response todo = todoService.changeStatus(userDetails.getId(), groupId, todoId, status);
        return new ResponseEntity<>(new ResponseDto.SingleResponseDto<>(todo), HttpStatus.OK);
    }

    @PatchMapping("/{todo-id}/assign")
    public ResponseEntity<?> changeAssign(@AuthenticationPrincipal CustomUserDetails userDetails,
                                          @PathVariable("study-group-id") Long groupId,
                                          @PathVariable("todo-id") Long todoId,
                                          @RequestBody @Valid TodoDto.changeAssigneeRequest changeAssigneeRequest) {
        TodoDto.Response todo = todoService.changeAssign(userDetails.getId(), groupId, todoId, changeAssigneeRequest);
        return new ResponseEntity<>(new ResponseDto.SingleResponseDto<>(todo), HttpStatus.OK);
    }

    @GetMapping("/assigned")
    //내가 할당된 목록 (완료된 것은 제외)
    public ResponseEntity<?> assignedMy(@AuthenticationPrincipal CustomUserDetails userDetails,
                                          @PathVariable("study-group-id") Long groupId) {
        List<TodoDto.ResponseAssignedMy> todos = todoService.assignedMy(userDetails.getId(), groupId);
        return new ResponseEntity<>(new ResponseDto.SingleResponseDto<>(todos), HttpStatus.OK);
    }
}
