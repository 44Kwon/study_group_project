package com.group_platform.todo.service;

import com.group_platform.exception.BusinessLogicException;
import com.group_platform.exception.ExceptionCode;
import com.group_platform.studymember.entity.StudyMember;
import com.group_platform.studymember.service.StudyMemberService;
import com.group_platform.sutdygroup.entity.StudyGroup;
import com.group_platform.sutdygroup.service.StudyGroupService;
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
import java.util.stream.Collectors;

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
    private final StudyGroupService studyGroupService;

    public TodoDto.Response createTodo(Long userId, Long groupId, TodoDto.createRequest createRequest) {
        Todo requestTodo = todoMapper.createRequestToTodo(createRequest);

        //굳이 유저에 대한 검증을 또 할 필요가 없다(필터에서 하는 중)
        //getReferenceById()는 연관관계용으로 id값만 가진 프록시객체를 생성한다
//        User referenceById = userRepository.getReferenceById(userId);

        //그룹검증(연관관계잇기 위해)
        StudyGroup studyGroup = studyGroupService.validateByGroupId(groupId);

        //그룹인원인지에 대한 검증
        studyMemberService.validateMemberWithUserId(userId, groupId, StudyMember.ActiveStatus.ACTIVE);

        //due_date는 오늘날짜이전이면 에러처리
        if(requestTodo.getDue_date() != null &&  requestTodo.getDue_date().isBefore(LocalDate.now())) {
            throw new BusinessLogicException(ExceptionCode.DUE_DATE_PAST);
        }

        //allmembers가 false인데, list값이 없을 때
        //에러처리 말고 그냥 all처리로 하자
        if(!requestTodo.isAllMembers() && createRequest.getMemberIds() == null) {
            requestTodo.setAllMembers(true);
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

        //그냥 단방향으로 처리함
        requestTodo.addTodoWithStudyGroup(studyGroup);

        Todo savedTodo = todoRepository.save(requestTodo);
        TodoDto.Response response = todoMapper.todoToResponse(savedTodo);
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
        if(updateTodo.getDue_date() != null &&  updateTodo.getDue_date().isBefore(LocalDate.now())) {
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
                        deleteAllAndNewInsert(updateRequest.getMemberIds(), todo, names);
                    }
                }, ()-> {
                    if (!todo.isAllMembers() && !updateRequest.getMemberIds().isEmpty()) {
                        //전부 삭제하고, 다시 넣는 로직
                        deleteAllAndNewInsert(updateRequest.getMemberIds(), todo, names);
                    }
                });

        TodoDto.Response response = todoMapper.todoToResponse(todo);
        response.setAssignedMemberNicknames(names);

        return response;
    }

    @Transactional(readOnly = true)
    public TodoDto.Response getTodo(Long groupId, Long todoId) {
        // todoId와 groupId로 있는지 검증
        Todo todo = validateTodo(groupId, todoId);
        return getResponse(todoId, todo);
    }

    @Transactional(readOnly = true)
    //필요시 Map으로 반환하고 현재는 List로 반환 -> 클라이언트쪽에서 그룹핑하도록
    public List<TodoDto.Response> getAllTodo(Long userId, Long groupId) {
        //그룹인원인지에 대한 검증
        studyMemberService.validateMemberWithUserId(userId, groupId, StudyMember.ActiveStatus.ACTIVE);

        List<Todo> todos = todoRepository.findAllByStudyGroup_Id(groupId);
        List<Long> allMembersFalseTodoIds = todos.stream()
                .filter((todo) -> !todo.isAllMembers())
                .map(Todo::getId)
                .toList();

        //allMembersFalseTodoIds가 값이 없을수도 있으니
        List<TodoDto.TodoUserNicknameDto> nicknamesByTodoIds = Collections.emptyList();
        //DB최대한 덜 타게 한다
        if(!allMembersFalseTodoIds.isEmpty()) {
            nicknamesByTodoIds = todoUserRepository.findNicknamesByTodoIds(allMembersFalseTodoIds);
        }

        Map<Long, List<String>> todoIdToNicknames = nicknamesByTodoIds.stream()
                .collect(Collectors.groupingBy(
                        TodoDto.TodoUserNicknameDto::getTodoId,
                        Collectors.mapping(TodoDto.TodoUserNicknameDto::getNickname, Collectors.toList())
                ));

        List<TodoDto.Response> responses = todoMapper.todosToResponse(todos);

        if (!todoIdToNicknames.isEmpty()) {
            responses.forEach((response) -> {
                response.setAssignedMemberNicknames(todoIdToNicknames.get(response.getId()));
            });
        }

        return responses;
    }

    public void deleteTodo(Long userId, Long groupId, Long todoId) {
        //그룹원인지
        studyMemberService.validateMemberWithUserId(userId, groupId, StudyMember.ActiveStatus.ACTIVE);
        todoRepository.deleteById(todoId);
    }

    //동시성문제 (낙관적 락 적용해보자)
    public TodoDto.Response changeStatus(Long userId, Long groupId, Long todoId, Todo.TodoStatus status) {
        //그룹인원인지에 대한 검증
        studyMemberService.validateMemberWithUserId(userId, groupId, StudyMember.ActiveStatus.ACTIVE);

        // todoId와 groupId로 있는지 검증
        Todo todo = validateTodo(groupId, todoId);
        todo.changeStatus(status);

        return getResponse(todoId, todo);
    }

    public TodoDto.Response changeAssign(Long userId, Long groupId, Long todoId, TodoDto.changeAssigneeRequest changeAssigneeRequest) {
        //그룹인원인지에 대한 검증
        studyMemberService.validateMemberWithUserId(userId, groupId, StudyMember.ActiveStatus.ACTIVE);

        // todoId와 groupId로 있는지 검증
        Todo todo = validateTodo(groupId, todoId);


        //allmembers이 false가 넘어온 상황에서 List가 안넘어왔을 때
        if(changeAssigneeRequest.getAllMembers() != null && !changeAssigneeRequest.getAllMembers() && changeAssigneeRequest.getMemberIds() == null) {
            throw new BusinessLogicException(ExceptionCode.TODO_NOT_ASSIGNED);
        }

        //기존 true일 때 memberIds가 넘어와도 처리되게 해야한다

        List<String> names = new ArrayList<>();

        Optional.ofNullable(changeAssigneeRequest.getAllMembers())
                .ifPresentOrElse((isAllMembers) -> {
                    if(isAllMembers) {
                        //속한 todo_user전부 삭제
                        todoUserRepository.deleteAllInBatchByTodoId(todo.getId());
                        todo.setAllMembers(true);
                    } else {
                        //전부 삭제하고, 다시 넣는 로직
                        deleteAllAndNewInsert(changeAssigneeRequest.getMemberIds(), todo, names);
                    }
                }, ()-> {
                    //위에 투두에 대해서 변경하는것과 다르게 처리한다
                    //여기서는 기존값이 true던 false건 list값이 넘어오면 변경처리한다
                    if (!changeAssigneeRequest.getMemberIds().isEmpty()) {
                        //전부 삭제하고, 다시 넣는 로직
                        deleteAllAndNewInsert(changeAssigneeRequest.getMemberIds(), todo, names);
                    }
                });

        TodoDto.Response response = todoMapper.todoToResponse(todo);
        response.setAssignedMemberNicknames(names);

//        //매퍼에서 처리로 인한 삭제
//        if(todo.getDue_date().isBefore(LocalDate.now())) {
//            response.setOverdue(true);
//        }

        return response;
    }

    @Transactional(readOnly = true)
    public List<TodoDto.ResponseAssignedMy> assignedMy(Long userId, Long groupId) {
        //그룹에서 모든 할일 데이터를 가져오기
        //할일 데이터에서 allmembers가 true인것과, todo_user쪽에서 내 userId와 해당 todoId와 맞는 값을 가져온다
        List<Todo> myAssignedTodos = todoRepository.findMyAssignedTodo(groupId, userId);
        //COMPLETED가 된 값은 필터링
        List<Todo> AssignedTodosWithOutCompleted = myAssignedTodos.stream()
                .filter((myAssignedTodo) -> myAssignedTodo.getStatus() != Todo.TodoStatus.COMPLETED)
                .toList();
        return todoMapper.todosToResponsesAssignedMy(AssignedTodosWithOutCompleted);
    }

    private Todo validateTodo(Long groupId, Long todoId) {
        return todoRepository.findByIdAndStudyGroup_Id(todoId, groupId).orElseThrow(() -> new BusinessLogicException(ExceptionCode.TODO_NOT_EXIST));
    }

    private void deleteAllAndNewInsert(List<Long> ids, Todo todo, List<String> names) {
        //전부 삭제하고, 다시 넣는 로직 들어가야함
            todoUserRepository.deleteAllInBatchByTodoId(todo.getId());
            List<User> assignedMembers = userRepository.findAllById(ids);
            if (!assignedMembers.isEmpty()) {
                todo.setAllMembers(false);

                List<TodoUser> todoUsers = todo.addAssignMembers(assignedMembers, names);
                //변경감지로 연관관계 casacade로 저장이 안먹힘. 그래서 쓰던 연관관계 메서드에 반환값 바꿈
                todoUserRepository.saveAll(todoUsers);
            } else {
                todo.setAllMembers(true);
            }
    }

    //단일 객체 내보낼 때 response dto로 변환하고 할당 인원들 넣어서 보내줌
    private TodoDto.Response getResponse(Long todoId, Todo todo) {
        TodoDto.Response response = todoMapper.todoToResponse(todo);

        if (!todo.isAllMembers()) {
            //todo_user전부를 가져오고 여기서 nickname가져올것
            List<TodoUser> byTodoUserWithUser = todoUserRepository.findByTodoUserWithUsername(todoId);
            //이름넣기
            response.setAssignedMemberNicknames(byTodoUserWithUser.stream()
                    .map((todoUser)-> todoUser.getUser().getNickname())
                    .toList()
            );
        }

        return response;
    }
}
