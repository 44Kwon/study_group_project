package com.group_platform.sutdygroup.repository;

import com.group_platform.sutdygroup.dto.StudyGroupDto;
import com.group_platform.sutdygroup.entity.QStudyGroup;
import com.group_platform.sutdygroup.entity.StudyGroup;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class StudyGroupRepositoryImpl implements CustomStudyGroupRepository {
    private final JPAQueryFactory jpaQueryFactory;

    //config에서 Entitymanager주입받게 처리함(싱글톤으로 관리하기 위해서)
    public StudyGroupRepositoryImpl(JPAQueryFactory jpaQueryFactory) {
        this.jpaQueryFactory = jpaQueryFactory;
    }

    @Override
    public Page<StudyGroupDto.ResponseList> searchStudyGroups(String keyword, Pageable pageable) {
        QStudyGroup studyGroup = QStudyGroup.studyGroup;
        BooleanBuilder builder = new BooleanBuilder();  //동적 쿼리 생성 때문에 필요 (동적 where절)

        builder.and(studyGroup.status.eq(StudyGroup.GroupStatus.ACTIVE));

        if (keyword != null && !keyword.isEmpty()) {
            BooleanBuilder keyBuilder = new BooleanBuilder();
            keyBuilder.or(studyGroup.name.containsIgnoreCase(keyword));
            keyBuilder.or(studyGroup.description.containsIgnoreCase(keyword));

            builder.and(keyBuilder);
        }
        //builder가 비어있으면 전체조회!
        //단일 테이블 조회라서 distict안붙이고 or로 해도 중복데이터 안 생김
//        List<StudyGroup> results = jpaQueryFactory.selectFrom(studyGroup)
//                .where(builder)
//                .offset(pageable.getOffset())   //스킵할 데이터 수
//                .limit(pageable.getPageSize()) //한 페이지당 크기
//                .fetch();
        //fetchOne()은 결과가 0 또는 1개일 때 사용(없으면 null반환) - 2개 이상이면 예외
        //fetchFirst();  // 결과 중 첫 번째만 반환

        List<StudyGroupDto.ResponseList> results = jpaQueryFactory
                .select(Projections.constructor(StudyGroupDto.ResponseList.class,
                        studyGroup.id, studyGroup.name, studyGroup.description, studyGroup.currentMembers, studyGroup.maxMembers, studyGroup.type))
                .from(studyGroup)
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(studyGroup.createdAt.desc())
                .fetch();


        Long totalCount = jpaQueryFactory
                .select(studyGroup.count())
                .from(studyGroup)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(results, pageable, totalCount != null ? totalCount : 0L);
    }
}
