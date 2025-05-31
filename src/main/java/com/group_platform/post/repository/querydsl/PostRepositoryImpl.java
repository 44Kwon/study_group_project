package com.group_platform.post.repository.querydsl;

import com.group_platform.post.dto.PostResponseListDto;
import com.group_platform.post.dto.PostSortType;
import com.group_platform.post.dto.QPostResponseListDto;
import com.group_platform.post.entity.PostType;
import com.group_platform.post.entity.QPost;
import com.group_platform.user.entity.QUser;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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

        //responseDto에 isMine를 위해
        BooleanExpression isMine = (currentUserId != null) ?
                post.user.id.eq(currentUserId) :
                null;   //Expressions.FALSE : 이게 더 명확할 수 있음

        List<PostResponseListDto> response = jpaQueryFactory
                .select(new QPostResponseListDto
                        (post.id, post.title, post.viewCount, post.comment_count, null, post.postType, post.likeCount, post.createdAt,
                                user.id, user.nickname, isMine))
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

    @Override
    public Page<PostResponseListDto> getGroupNormalPosts(Long currentUserId, Long groupId, PostType type, PostSortType sort, Pageable pageable) {
        QPost post = QPost.post;
        QUser user = QUser.user;
        BooleanBuilder builder = new BooleanBuilder();

        builder.and(post.studyGroup.id.eq(groupId));
        builder.and(post.isPinned.isFalse().or(post.isPinned.isNull()));
        if(type != null) {
            builder.and(post.postType.eq(type));
        }

        //정렬
        OrderSpecifier<?> orderSpecifier;
        switch (sort) {
            case COMMENT_COUNT -> orderSpecifier = post.comment_count.desc();
            case VIEW_COUNT -> orderSpecifier = post.viewCount.desc();
            case LIKE_COUNT -> orderSpecifier = post.likeCount.desc();
            default -> orderSpecifier = post.createdAt.desc();
        }

        //responseDto에 isMine를 위해
        BooleanExpression isMine = post.user.id.eq(currentUserId);

        //게시글 목록
        List<PostResponseListDto> response = jpaQueryFactory
                .select(new QPostResponseListDto
                        (post.id, post.title, post.viewCount, post.comment_count, post.isPinned, post.postType, post.likeCount, post.createdAt,
                                user.id, user.nickname, isMine))
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

    @Override
    public List<PostResponseListDto> getGroupPinnedPosts(Long currentUserId, Long groupId) {
        QPost post = QPost.post;
        QUser user = QUser.user;
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(post.studyGroup.id.eq(groupId));
        builder.and(post.isPinned.isTrue());

        //responseDto에 isMine를 위해
        BooleanExpression isMine = post.user.id.eq(currentUserId);

        //고정글 처리
        // 상단고정 고려할 것
        // 1순위 : 공지사항 + 고정
        // 2순위 : 고정
        List<PostResponseListDto> responsePinned = jpaQueryFactory
                .select(new QPostResponseListDto
                        (post.id, post.title, post.viewCount, post.comment_count, post.isPinned, post.postType, post.likeCount, post.createdAt,
                                user.id, user.nickname, isMine))
                .from(post)
                .join(user)
                .where(builder)
                //정렬조건 (1순위 공지사항+고정)
                .orderBy(new CaseBuilder()
                                .when(post.postType.eq(PostType.NOTICE)).then(0)
                                .otherwise(1)
                                .asc(),
                        post.createdAt.desc())
                .fetch();

        return  responsePinned;
    }

    @Override
    public List<PostResponseListDto> getSearchPosts(Long currentUserId, List<Long> postIds, Pageable pageable) {
        QPost post = QPost.post;
        QUser user = QUser.user;

        //responseDto에 isMine를 위해
        BooleanExpression isMine = (currentUserId != null) ?
                post.user.id.eq(currentUserId) :
                null;   //Expressions.FALSE : 이게 더 명확할 수 있음

        //게시글 목록
        List<PostResponseListDto> response = jpaQueryFactory
                .select(new QPostResponseListDto
                        (post.id, post.title, post.viewCount, post.comment_count, post.isPinned, post.postType, post.likeCount, post.createdAt,
                                user.id, user.nickname, isMine))
                .from(post)
                .join(user)
                .where(post.id.in(postIds))
                .orderBy(post.createdAt.desc())
                .fetch();

        return response;
    }
}
