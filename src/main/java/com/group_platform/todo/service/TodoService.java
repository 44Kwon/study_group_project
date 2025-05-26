package com.group_platform.todo.service;

import com.group_platform.exception.BusinessLogicException;
import com.group_platform.exception.ExceptionCode;
import com.group_platform.studymember.entity.StudyMember;
import com.group_platform.studymember.service.StudyMemberService;
import com.group_platform.todo.dto.TodoDto;
import com.group_platform.todo.entity.Todo;
import com.group_platform.todo.entity.TodoUser;
import com.group_platform.todo.mapper.TodoMapper;
import com.group_platform.todo.repository.TodoRepository;
import com.group_platform.todo.repository.TodoUserRepository;
import com.group_platform.user.entity.User;
import com.group_platform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

/**
 * 현재는 동시성 문제에 대해 Optimisic Lock으로 해결하고
 * 향후에 Websocket이나 SSE방식으로 실시간 동기화 구현 한번 해볼것
 */
@Service
@Transactional
@RequiredArgsConstructor
public class TodoService {

    private final TodoRepository todoRepository;
    private final TodoMapper todoMapper;
    private final StudyMemberService studyMemberService;
    private final UserRepository userRepository;
    private final TodoUserRepository todoUserRepository;

    public TodoDto.Response createTodo(Long userId, Long groupId, TodoDto.createRequest createRequest) {
        Todo requestTodo = todoMapper.createRequestToTodo(createRequest);

        //굳이 유저에 대한 검증을 또 할 필요가 없다(필터에서 하는 중)
        //getReferenceById()는 연관관계용으로 id값만 가진 프록시객체를 생성한다
//        User referenceById = userRepository.getReferenceById(userId);

        //그룹인원인지에 대한 검증
        studyMemberService.validateMemberWithUserId(userId, groupId, StudyMember.ActiveStatus.ACTIVE);

        //due_date는 오늘날짜이전이면 에러처리
        if(requestTodo.getDue_date() != null &&  LocalDate.now().isBefore(requestTodo.getDue_date())) {
            throw new BusinessLogicException(ExceptionCode.DUE_DATE_PAST);
        }

        List<String> names = new ArrayList<>();

        if(createRequest.getMemberIds() != null) {
            //list값이 넘어오면 false, 기본적으로 allmembers 필드는 True로 되어있음
            List<User> assignedMembers = userRepository.findAllById(createRequest.getMemberIds());
            if (!assignedMembers.isEmpty()) {
                requestTodo.setAllMembers(false);
                //필수로 양방향 연관관계 설정해야 함. (1쪽에서 casacade를 통한 저장을 하기 때문)
                requestTodo.addAssignMembers(assignedMembers, names);
            }
        }

        Todo savedTodo = todoRepository.save(requestTodo);
        TodoDto.Response response = todoMapper.TodoToResponse(savedTodo);
        //assignedMemberNicknames에 대한처리
        response.setAssignedMemberNicknames(names);
        return response;
    }

    public TodoDto.Response updateTodo(Long userId, Long groupId, Long todoId, TodoDto.updateRequest updateRequest) {
        //그룹인원인지에 대한 검증
        studyMemberService.validateMemberWithUserId(userId, groupId, StudyMember.ActiveStatus.ACTIVE);

        // todoId와 groupId로 있는지 검증
        Todo todo = validateTodo(groupId, todoId);
        Todo updateTodo = todoMapper.updateRequestToTodo(updateRequest);

        //due_date는 오늘날짜이전이면 에러처리
        if(updateTodo.getDue_date() != null &&  LocalDate.now().isBefore(updateTodo.getDue_date())) {
            throw new BusinessLogicException(ExceptionCode.DUE_DATE_PAST);
        }

        //allmembers이 false가 넘어온 상황에서 List가 안넘어왔을 때
        if(updateRequest.getAllMembers() != null && !updateRequest.getAllMembers() && updateRequest.getMemberIds() == null) {
            throw new BusinessLogicException(ExceptionCode.TODO_NOT_ASSIGNED);
        }

        
        List<String> names = new ArrayList<>();

        //할당인원에 변화가 있다면(값이 넘어온다면) 기존것 전부 삭제 후 다시 넣기
        //isAllMembers에 값에 따른 처리
        //기존 값이 false인데 할당인원 수정할때
        Optional.ofNullable(updateTodo.getTitle())
                .ifPresent(todo::changeTitle);
        Optional.ofNullable(updateTodo.getDescription())
                .ifPresent(todo::changeDescription);
        Optional.ofNullable(updateTodo.getDue_date())
                .ifPresent(todo::changeDueDate);
        Optional.ofNullable(updateRequest.getAllMembers())
                .ifPresentOrElse((isAllMembers) -> {
                    if(isAllMembers) {
                        //속한 todo_user전부 삭제
                        todoUserRepository.deleteAllInBatchByTodoId(todo.getId());
                        todo.setAllMembers(true);
                    } else {
                        //전부 삭제하고, 다시 넣는 로직
                        deleteAllAndNewInsert(updateRequest, todo, names);
                    }
                }, ()-> {
                    if (!todo.isAllMembers()) {
                        //전부 삭제하고, 다시 넣는 로직
                        deleteAllAndNewInsert(updateRequest, todo, names);
                    }
                });

        TodoDto.Response response = todoMapper.TodoToResponse(todo);
        response.setAssignedMemberNicknames(names);
        return response;
    }

    private void deleteAllAndNewInsert(TodoDto.updateRequest updateRequest, Todo todo, List<String> names) {
        //전부 삭제하고, 다시 넣는 로직 들어가야함
        todoUserRepository.deleteAllInBatchByTodoId(todo.getId());
        List<User> assignedMembers = userRepository.findAllById(updateRequest.getMemberIds());
        if (!assignedMembers.isEmpty()) {
            todo.setAllMembers(false);

            List<TodoUser> todoUsers = todo.addAssignMembers(assignedMembers, names);
            //변경감지로 연관관계 casacade로 저장이 안먹힘. 그래서 쓰던 연관관계 메서드에 반환값 바꿈
            todoUserRepository.saveAll(todoUsers);
        } else {
            todo.setAllMembers(true);
        }
    }

    @Transactional(readOnly = true)
    public TodoDto.Response getTodo(Long groupId, Long todoId) {
        // todoId와 groupId로 있는지 검증
        Todo todo = validateTodo(groupId, todoId);
        TodoDto.Response response = todoMapper.TodoToResponse(todo);

        if (!todo.isAllMembers()) {
            //todo_user전부를 가져오고 여기서 nickname가져올것
            List<TodoUser> byTodoUserWithUser = todoUserRepository.findByTodoUserWithUsername(todoId);
            //이름넣기
            response.setAssignedMemberNicknames(byTodoUserWithUser.stream()
                    .map((todoUser)-> todoUser.getUser().getNickname())
                    .toList()
            );
        }

        if(LocalDate.now().isBefore(todo.getDue_date())) {
            response.setOverdue(true);
        }

        return response;
    }

    @Transactional(readOnly = true)
    public List<TodoDto.Response> getAllTodo(Long id, Long groupId) {
        //어떻게 줘야할까?

        return null;
    }

    public void deleteTodo(Long userId, Long groupId, Long todoId) {
        //그룹원인지
        studyMemberService.validateMemberWithUserId(userId, groupId, StudyMember.ActiveStatus.ACTIVE);
        todoRepository.deleteById(todoId);
    }

    public TodoDto.Response changeStatus(Long id, Long groupId, Long todoId, Todo.TodoStatus status) {
        return null;
    }

    public TodoDto.Response changeAssign(Long id, Long groupId, Long todoId, TodoDto.changeAssigneeRequest changeAssigneeRequest) {
        return null;
    }

    @Transactional(readOnly = true)
    public List<TodoDto.Response> assignedMy(Long id, Long groupId) {
        return null;
    }

    private Todo validateTodo(Long groupId, Long todoId) {
        return todoRepository.findByIdAndStudyGroupId(todoId, groupId).orElseThrow(() -> new BusinessLogicException(ExceptionCode.TODO_NOT_EXIST));
    }
}
