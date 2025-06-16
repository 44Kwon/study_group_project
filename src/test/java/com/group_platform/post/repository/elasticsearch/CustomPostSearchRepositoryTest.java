//package com.group_platform.post.repository.elasticsearch;
//
//import com.group_platform.post.entity.PostDocument;
//import org.assertj.core.groups.Tuple;
//import org.junit.jupiter.api.*;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.context.DynamicPropertyRegistry;
//import org.springframework.test.context.DynamicPropertySource;
//import org.testcontainers.elasticsearch.ElasticsearchContainer;
//import org.testcontainers.junit.jupiter.Container;
//import org.testcontainers.junit.jupiter.Testcontainers;
//
//import java.util.List;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
///**
// * elasticsearch 테스트
// */
//@Testcontainers
////@SpringBootTest
//@DataJpaTest
//@ActiveProfiles("test")
//@TestInstance(TestInstance.Lifecycle.PER_CLASS) //원래 기본은 메서드마다 인스턴스 생성
//class CustomPostSearchRepositoryTest {
//
//    @Container
//    static ElasticsearchContainer elasticsearchContainer =
//            new ElasticsearchContainer("docker.elastic.co/elasticsearch/elasticsearch:8.15.5")
//                    .withEnv("xpack.security.enabled", "false") //보안끄기
////                    .withEnv("ELASTIC_PASSWORD", "1234")  // 비밀번호 설정
//                    .withEnv("discovery.type", "single-node");
//
//    @Autowired
//    private PostSearchRepository postSearchRepository;
//
//    @Autowired
//    private ElasticsearchOperations elasticsearchOperations;
//
//    @DynamicPropertySource
//    static void elasticsearchProperties(DynamicPropertyRegistry registry) {
//        // 계속 오류가 나서 추가함
//        if (!elasticsearchContainer.isRunning()) {
//            elasticsearchContainer.start(); // 컨테이너 먼저 실행
//        }
//        registry.add("spring.elasticsearch.uris", elasticsearchContainer::getHttpHostAddress);
////        registry.add("spring.elasticsearch.username", () -> "elastic");
////        registry.add("spring.elasticsearch.password", () -> "1234");
//    }
//    @BeforeEach
//    void cleanUp() {
//        postSearchRepository.deleteAll();
//    }
//
//    @DisplayName("공통 게시글 검색 테스트")
//    @Test
//    void getSearchCommonPosts() {
//        //given
//        PostDocument postDocument1 = new PostDocument("1", "안녕하세요 제목입니다", "내용입니다 어쩌라고요", null);
//        PostDocument postDocument2 = new PostDocument("2", "두번째에요 제목입니다", "내용입니다 어쩌라고요", null);
//        PostDocument postDocument3 = new PostDocument("3", "우러러러누누누 추로", "이게뭔가요 세번째", null);
//        PostDocument postDocument4 = new PostDocument("4", "라허허허허 카카카카", "내용입니다 어쩌라고요", "1");
//
//        postSearchRepository.saveAll(List.of(postDocument1, postDocument2, postDocument3, postDocument4));
//
//        //인덱스 refresh (바로 저장 조회하면 결과가 안나올 수 있음 refresh 필요하다)
//        elasticsearchOperations.indexOps(PostDocument.class).refresh();
//
//        //when
//        String keyword = "안녕";
//        Page<Long> searchCommonPosts1 = postSearchRepository.getSearchCommonPosts(keyword, PageRequest.of(0, 10));
//
//        keyword = "내용";
//        Page<Long> searchCommonPosts2 = postSearchRepository.getSearchCommonPosts(keyword, PageRequest.of(0, 10));
//
//        //then
//        List<Long> content1 = searchCommonPosts1.getContent();
//        List<Long> content2 = searchCommonPosts2.getContent();
//
//        assertThat(content1).hasSize(1)
//                .containsExactlyInAnyOrder(1L);
//
//        assertThat(content2).hasSize(2)
//                .containsExactlyInAnyOrder(1L, 2L);
//    }
//
//
//    @DisplayName("그룹 내 게시글 검색 테스트")
//    @Test
//    void getSearchGroupPosts() {
//        //given
//        PostDocument postDocument1 = new PostDocument("1", "안녕하세요 제목입니다", "내용입니다 어쩌라고요", "1");
//        PostDocument postDocument2 = new PostDocument("2", "두번째에요 제목입니다", "내용입니다 어쩌라고요", "2");
//        PostDocument postDocument3 = new PostDocument("3", "우러러러누누누 추로", "이게뭔가요 세번째", null);
//        PostDocument postDocument4 = new PostDocument("4", "라허허허허 카카카카", "내용입니다 어쩌라고요", "1");
//
//        postSearchRepository.saveAll(List.of(postDocument1, postDocument2, postDocument3, postDocument4));
//
//        //인덱스 refresh (바로 저장 조회하면 결과가 안나올 수 있음 refresh 필요하다)
//        elasticsearchOperations.indexOps(PostDocument.class).refresh();
//
//        //when
//        String keyword = "안녕";
//        Long groupId = 1L;
//        Page<Long> searchGroupPosts1 = postSearchRepository.getSearchGroupPosts(groupId, keyword, PageRequest.of(0, 10));
//
//        keyword = "내용";
//        Page<Long> searchGroupPosts2 = postSearchRepository.getSearchGroupPosts(groupId, keyword, PageRequest.of(0, 10));
//
//
//        //then
//        List<Long> content1 = searchGroupPosts1.getContent();
//        List<Long> content2 = searchGroupPosts2.getContent();
//
//        assertThat(content1).hasSize(1)
//                .containsExactlyInAnyOrder(1L);
//        assertThat(content2).hasSize(2)
//                .containsExactlyInAnyOrder(1L, 4L);
//    }
//
//}