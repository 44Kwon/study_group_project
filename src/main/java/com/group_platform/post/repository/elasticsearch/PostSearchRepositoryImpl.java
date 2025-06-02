package com.group_platform.post.repository.elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.group_platform.exception.BusinessLogicException;
import com.group_platform.exception.ExceptionCode;
import com.group_platform.post.entity.PostDocument;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.List;

@Repository
@Slf4j
public class PostSearchRepositoryImpl implements CustomPostSearchRepository {
    private final ElasticsearchClient elasticsearchClient;

    public PostSearchRepositoryImpl(ElasticsearchClient elasticsearchClient) {
        this.elasticsearchClient = elasticsearchClient;
    }

    @Override
    public Page<Long> getSearchCommonPosts(String keyword, Pageable pageable) {
        try {
            //MatchQuery는 텍스트 분석이 적용된 필드에 적합한 검색 쿼리
            Query titleQuery = MatchQuery.of(m -> m.field("title").query(keyword))._toQuery();
            Query contentQuery = MatchQuery.of(m -> m.field("content").query(keyword))._toQuery();
            Query groupIdExistsQuery = ExistsQuery.of(e -> e.field("groupId"))._toQuery();

//            Query commonPostGroupIdQuery = TermQuery.of(t -> t.field("groupId").value(""))._toQuery();

            //BoolQuery에서 should는 OR 조건 역할
            BoolQuery boolQuery = BoolQuery.of(b -> b
                    .should(titleQuery)
                    .should(contentQuery)
                    .minimumShouldMatch("1")  // 제목이나 내용 중 하나는 반드시 포함
                     .mustNot(groupIdExistsQuery)
//                    .filter(commonPostGroupIdQuery) // groupId가 ""인 것들만
            );

            //search 요청 생성
            SearchRequest request = SearchRequest.of(s -> s
                    .index("posts")
                    .query(boolQuery._toQuery())
                    .from((int)pageable.getOffset())
                    .size(pageable.getPageSize())
//                    .sort(sort->sort.field(f->f.field("createdAt").order(SortOrder.Desc)))
            );

            //검색실행
            SearchResponse<PostDocument> response = elasticsearchClient.search(request, PostDocument.class);

            // 총 검색 결과 개수 (total hits)
            long totalHits = response.hits().total() != null ? response.hits().total().value() : 0;

            List<Long> content = response.hits().hits().stream()//현재 페이지에 해당하는 문서 배열(response.hits().hits())
                    .map((hit) -> {
                        if (hit.source() == null) {
                            throw new BusinessLogicException(ExceptionCode.KEYWORD_NOT_EXIST);
                        }
                        return Long.valueOf(hit.source().getId());
                    })
                    .toList();

            return new PageImpl<>(content, pageable, totalHits);
        } catch (Exception e) {
            //후처리필요
            log.error("검색 중 오류발생 : {}", e.getMessage());
            throw new RuntimeException("Elasticsearch 검색 실패", e);
        }
    }

    @Override
    public Page<Long> getSearchGroupPosts(Long groupId, String keyword, Pageable pageable) {
        try {
            //MatchQuery는 텍스트 분석이 적용된 필드에 적합한 검색 쿼리
            Query titleQuery = MatchQuery.of(m -> m.field("title").query(keyword))._toQuery();
            Query contentQuery = MatchQuery.of(m -> m.field("content").query(keyword))._toQuery();
            Query groupQuery = TermQuery.of(m -> m.field("groupId").value(groupId))._toQuery();

            //BoolQuery에서 should는 OR 조건 역할
            BoolQuery boolQuery = BoolQuery.of(b -> b
                    .should(titleQuery)
                    .should(contentQuery)
                    .minimumShouldMatch("1")
                    .must(groupQuery)
            );

            //search 요청 생성
            SearchRequest request = SearchRequest.of(s -> s
                    .index("posts")
                    .query(boolQuery._toQuery())
                    .from((int)pageable.getOffset())
                    .size(pageable.getPageSize())
//                    .sort(sort->sort.field(f->f.field("createdAt").order(SortOrder.Desc)))
            );

            //검색실행
            SearchResponse<PostDocument> response = elasticsearchClient.search(request, PostDocument.class);

            // 총 검색 결과 개수 (total hits)
            long totalHits = response.hits().total() != null ? response.hits().total().value() : 0;

            List<Long> content = response.hits().hits().stream()//현재 페이지에 해당하는 문서 배열(response.hits().hits())
                    .map((hit) -> {
                        if (hit.source() == null) {
                            throw new BusinessLogicException(ExceptionCode.KEYWORD_NOT_EXIST);
                        }
                        return Long.valueOf(hit.source().getId());
                    })
                    .toList();

            return new PageImpl<>(content, pageable, totalHits);
        } catch (Exception e) {
            //후처리필요
            log.error("검색 중 오류발생 : {}", e.getMessage());
            throw new RuntimeException("Elasticsearch 검색 실패", e);
        }
    }
}
