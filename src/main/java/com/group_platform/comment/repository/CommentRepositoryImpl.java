package com.group_platform.comment.repository;

import com.group_platform.comment.dto.CommentDto;
import com.group_platform.comment.entity.QComment;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CommentRepositoryImpl implements CustomCommentRepository {

    private final JPAQueryFactory jpaQueryFactory;

    public CommentRepositoryImpl(JPAQueryFactory jpaQueryFactory) {
        this.jpaQueryFactory = jpaQueryFactory;
    }

    @Override
    public List<CommentDto.IsCommentHaveReplyDto> existsByRelies(List<Long> parentIds) {
        QComment comment = QComment.comment;

        return jpaQueryFactory.select(Projections.constructor(CommentDto.IsCommentHaveReplyDto.class,
                        comment.parent.id, comment.count()))
                .from(comment)
                .where(comment.parent.id.in(parentIds))
                .groupBy(comment.parent.id)
                .orderBy(comment.parent.id.asc())
                .fetch();
    }
}
