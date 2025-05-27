package com.group_platform.todo.repository;

import com.group_platform.sutdygroup.dto.StudyGroupDto;
import com.group_platform.todo.dto.TodoDto;
import com.group_platform.todo.entity.QTodoUser;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

@Repository
public class TodoUserRepositoryImpl implements CustomTodoUserRepository{

    private final JPAQueryFactory jpaQueryFactory;

    public TodoUserRepositoryImpl(JPAQueryFactory jpaQueryFactory) {
        this.jpaQueryFactory = jpaQueryFactory;
    }

    @Override
    public List<TodoDto.TodoUserNicknameDto> findNicknamesByTodoIds(List<Long> todoIds) {
        if(todoIds.isEmpty()) {
            return Collections.emptyList();
        }
        QTodoUser todoUser = QTodoUser.todoUser;

        return jpaQueryFactory.select(Projections.constructor(TodoDto.TodoUserNicknameDto.class,
                        todoUser.todo.id, todoUser.user.nickname))
                .from(todoUser)
                .join(todoUser.user)
                .where(todoUser.todo.id.in(todoIds))
                .fetch();

    }
}
