package com.group_platform.todo.controller;

import com.group_platform.response.ResponseDto;
import com.group_platform.security.dto.CustomUserDetails;
import com.group_platform.todo.dto.TodoDto;
import com.group_platform.todo.entity.Todo;
import com.group_platform.todo.service.TodoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Todos", description = "그룹 내 할일 관리")
public class TodoController {

    private final TodoService todoService;

    @PostMapping
    @Operation(summary = "그룹 내 할일을 작성", description = "해당 그룹 인원이 할일을 작성하고, 할당인원을 지정한다")
    public ResponseEntity<?> createTodo(@AuthenticationPrincipal CustomUserDetails userDetails,
                                        @PathVariable("study-group-id") Long groupId,
                                        @RequestBody @Valid TodoDto.createRequest createRequest) {
        TodoDto.Response todo = todoService.createTodo(userDetails.getId(), groupId, createRequest);
        return new ResponseEntity<>(new ResponseDto.SingleResponseDto<>(todo), HttpStatus.CREATED);
    }

    @PatchMapping("/{todo-id}")
    @Operation(summary = "할일에 대해 업데이트", description = "할일에 대해 업데이트 한다")
    public ResponseEntity<?> updateTodo (@AuthenticationPrincipal CustomUserDetails userDetails,
                                         @PathVariable("study-group-id") Long groupId,
                                         @PathVariable("todo-id") Long todoId,
                                         @RequestBody @Valid TodoDto.updateRequest updateRequest) {
        TodoDto.Response todo = todoService.updateTodo(userDetails.getId(), groupId, todoId, updateRequest);
        return new ResponseEntity<>(new ResponseDto.SingleResponseDto<>(todo), HttpStatus.OK);
    }

    @GetMapping("/{todo-id}")
    @Operation(summary = "할일을 조회")
    public ResponseEntity<?> getTodo(@PathVariable("study-group-id") Long groupId,
                                     @PathVariable("todo-id") Long todoId) {
        TodoDto.Response todo = todoService.getTodo(groupId, todoId);
        return new ResponseEntity<>(new ResponseDto.SingleResponseDto<>(todo), HttpStatus.OK);
    }

    @GetMapping
    @Operation(summary = "그룹 내 할일을 전부 조회한다", description = "그룹 내 할일을 전부 조회해서 프론트에서 상태에 따라 표 형태로")
    //전체 주는거
    public ResponseEntity<?> getAllTodo(@AuthenticationPrincipal CustomUserDetails userDetails,
                                        @PathVariable("study-group-id") Long groupId) {
        List<TodoDto.Response> allTodo = todoService.getAllTodo(userDetails.getId(), groupId);
        return new ResponseEntity<>(new ResponseDto.SingleResponseDto<>(allTodo), HttpStatus.OK);
    }

    @DeleteMapping("/{todo-id}")
    @Operation(summary = "할일을 삭제")
    public ResponseEntity<?> deleteTodo(@AuthenticationPrincipal CustomUserDetails userDetails,
                                        @PathVariable("study-group-id") Long groupId,
                                        @PathVariable("todo-id") Long todoId) {
        todoService.deleteTodo(userDetails.getId(), groupId, todoId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{todo-id}/status")
    //상태(예정/진행중/완료) 변경
    @Operation(summary = "할일에 상태에 대해 변경", description = "상태(예정/진행중/완료)를 변경한다")
    public ResponseEntity<?> changeStatus(@AuthenticationPrincipal CustomUserDetails userDetails,
                                          @PathVariable("study-group-id") Long groupId,
                                          @PathVariable("todo-id") Long todoId,
                                          @RequestParam("status") Todo.TodoStatus status) {
        TodoDto.Response todo = todoService.changeStatus(userDetails.getId(), groupId, todoId, status);
        return new ResponseEntity<>(new ResponseDto.SingleResponseDto<>(todo), HttpStatus.OK);
    }

    @PatchMapping("/{todo-id}/assign")
    @Operation(summary = "할일에 할당 인원을 변경")
    public ResponseEntity<?> changeAssign(@AuthenticationPrincipal CustomUserDetails userDetails,
                                          @PathVariable("study-group-id") Long groupId,
                                          @PathVariable("todo-id") Long todoId,
                                          @RequestBody @Valid TodoDto.changeAssigneeRequest changeAssigneeRequest) {
        TodoDto.Response todo = todoService.changeAssign(userDetails.getId(), groupId, todoId, changeAssigneeRequest);
        return new ResponseEntity<>(new ResponseDto.SingleResponseDto<>(todo), HttpStatus.OK);
    }

    @GetMapping("/assigned")
    //내가 할당된 목록 (완료된 것은 제외)
    @Operation(summary = "내가 할당된 할일에 대한 목록조회")
    public ResponseEntity<?> assignedMy(@AuthenticationPrincipal CustomUserDetails userDetails,
                                          @PathVariable("study-group-id") Long groupId) {
        List<TodoDto.ResponseAssignedMy> todos = todoService.assignedMy(userDetails.getId(), groupId);
        return new ResponseEntity<>(new ResponseDto.SingleResponseDto<>(todos), HttpStatus.OK);
    }
}
