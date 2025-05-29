package com.group_platform.post.repository;

import com.group_platform.post.dto.PostResponseListDto;
import com.group_platform.post.dto.PostSortType;
import com.group_platform.post.dto.QPostResponseListDto;
import com.group_platform.post.entity.Post;
import com.group_platform.post.entity.PostType;
import com.group_platform.post.entity.QPost;
import com.group_platform.user.entity.QUser;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PostRepositoryImpl implements CustomPostRepository {

    private final JPAQueryFactory jpaQueryFactory;

    public PostRepositoryImpl(JPAQueryFactory jpaQueryFactory) {
        this.jpaQueryFactory = jpaQueryFactory;
    }

    @Override
    //공통게시글 정렬,필터
    public Page<PostResponseListDto> getCommonPosts(Long currentUserId, PostType type, PostSortType sort, Pageable pageable) {
        QPost post = QPost.post;
        QUser user = QUser.user;
        BooleanBuilder builder = new BooleanBuilder();

        if(type != null) {
            builder.and(post.postType.eq(type));
        }
        builder.and(post.studyGroup.isNull());

        //정렬
        OrderSpecifier<?> orderSpecifier;
        switch (sort) {
            case COMMENT_COUNT -> orderSpecifier = post.comment_count.desc();
            case VIEW_COUNT -> orderSpecifier = post.viewCount.desc();
            case LIKE_COUNT -> orderSpecifier = post.likeCount.desc();
            default -> orderSpecifier = post.createdAt.desc();
        }

        List<PostResponseListDto> response = jpaQueryFactory.select(new QPostResponseListDto
                        (post.id, post.title, post.viewCount, post.comment_count, null, post.postType, post.likeCount, post.createdAt,
                                user.id, user.nickname, post.user.id.eq(currentUserId)))
                .from(post)
                .join(user)
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(orderSpecifier)
                .fetch();

        Long total = jpaQueryFactory.select(post.count())
                .from(post)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(response,pageable,total == null ? 0 : total);
    }
}
