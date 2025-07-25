package com.group_platform.post.repository.elasticsearch;

import com.group_platform.config.ElasticSearchTestConfig;
import com.group_platform.post.entity.PostDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// ElasticSearch Test
@Testcontainers //Testcontainers 라이브러리를 사용하겠다는 선언. 테스트 클래스에 붙임으로써 @Container로 선언한 컨테이너를 자동으로 관리
@SpringBootTest
@Import(ElasticSearchTestConfig.class)
@ActiveProfiles("test")
class PostSearchRepositoryImplTest {

    //@Container: 이 필드는 테스트 컨테이너로 관리됨을 선언
    //ElasticsearchContainer: ElasticSearch의 Docker 이미지를 기반으로 한 컨테이너 객체
    @Container
    static GenericContainer<?> elasticsearchContainer =
            new GenericContainer<>( // GenericContainer는 ImageFromDockerfile을 생성자로 받을 수 있습니다.
                    new ImageFromDockerfile()
                            .withFileFromClasspath("Dockerfile", "docker/Dockerfile")
            )
                    .withExposedPorts(9200, 9300) // Elasticsearch가 사용하는 포트 노출 (HTTP: 9200, Transport: 9300)
                    .withEnv("xpack.security.enabled", "false")
                    .withEnv("xpack.security.http.ssl.enabled", "false") // 8.x 버전 보안 관련
                    .withEnv("xpack.security.transport.ssl.enabled", "false") // 8.x 버전 보안 관련
                    .withEnv("discovery.type", "single-node") // 테스트용 단일 노드 설정
                    .withEnv("ES_JAVA_OPTS", "-Xms512m -Xmx512m") // JVM 메모리 설정 (선택 사항이지만 권장)
                    .withStartupTimeout(java.time.Duration.ofSeconds(240)) // 충분한 시작 타임아웃
                    .waitingFor(Wait.forHttp("/_cluster/health").forStatusCode(200)
                            .withStartupTimeout(java.time.Duration.ofSeconds(240))); // 헬스 체크 타임아웃도 늘림

    //@DynamicPropertySource: 테스트 실행 시점에 동적으로 애플리케이션 프로퍼티를 설정
    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        //설정 파일에 "spring.elasticsearch.uris" 프로퍼티 값을 동적으로 elasticsearchContainer.getHttpHostAddress() 호출 결과로 설정
        String httpHostAddress = String.format("%s:%d",
                elasticsearchContainer.getHost(),
                elasticsearchContainer.getMappedPort(9200));
        registry.add("spring.elasticsearch.uris", ()->httpHostAddress);
    }

    @Autowired
    private PostSearchRepository postSearchRepository;

    @Autowired
    private PostSearchRepositoryImpl postSearchRepositoryImpl;

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    @BeforeEach
    void cleanUp() {
        if (elasticsearchOperations.indexOps(PostDocument.class).exists()) {
            postSearchRepository.deleteAll(); // 인덱스가 있을 때만 데이터를 삭제합니다.
            elasticsearchOperations.indexOps(PostDocument.class).refresh();
        }
    }
    @DisplayName("공통 게시글 검색 테스트")
    @Test
    void getSearchCommonPosts() {
        //given
        PostDocument postDocument1 = new PostDocument("1", "안녕하세요 제목입니다", "내용입니다 어쩌라고요", null);
        PostDocument postDocument2 = new PostDocument("2", "두번째에요 제목입니다", "내용입니다 어쩌라고요", null);
        PostDocument postDocument3 = new PostDocument("3", "우러러러누누누 추로", "이게뭔가요 세번째", null);
        PostDocument postDocument4 = new PostDocument("4", "라허허허허 카카카카", "내용입니다 어쩌라고요", "1");

        postSearchRepository.saveAll(List.of(postDocument1, postDocument2, postDocument3, postDocument4));

        //인덱스 refresh (바로 저장 조회하면 결과가 안나올 수 있음 refresh 필요하다)
        elasticsearchOperations.indexOps(PostDocument.class).refresh();

        //when
        String keyword = "안녕";
        Page<Long> searchCommonPosts1 = postSearchRepository.getSearchCommonPosts(keyword, PageRequest.of(0, 10));

        keyword = "내용";
        Page<Long> searchCommonPosts2 = postSearchRepository.getSearchCommonPosts(keyword, PageRequest.of(0, 10));

        //then
        List<Long> content1 = searchCommonPosts1.getContent();
        List<Long> content2 = searchCommonPosts2.getContent();

        assertThat(content1).hasSize(1)
                .containsExactlyInAnyOrder(1L);

        assertThat(content2).hasSize(2)
                .containsExactlyInAnyOrder(1L, 2L);
    }


    @DisplayName("그룹 내 게시글 검색 테스트")
    @Test
    void getSearchGroupPosts() {
        //given
        PostDocument postDocument1 = new PostDocument("1", "안녕하세요 제목입니다", "내용입니다 어쩌라고요", "1");
        PostDocument postDocument2 = new PostDocument("2", "두번째에요 제목입니다", "내용입니다 어쩌라고요", "2");
        PostDocument postDocument3 = new PostDocument("3", "우러러러누누누 추로", "이게뭔가요 세번째", null);
        PostDocument postDocument4 = new PostDocument("4", "라허허허허 카카카카", "내용입니다 어쩌라고요", "1");

        postSearchRepository.saveAll(List.of(postDocument1, postDocument2, postDocument3, postDocument4));

        //인덱스 refresh (바로 저장 조회하면 결과가 안나올 수 있음 refresh 필요하다)
        elasticsearchOperations.indexOps(PostDocument.class).refresh();

        //when
        String keyword = "안녕";
        Long groupId = 1L;
        Page<Long> searchGroupPosts1 = postSearchRepository.getSearchGroupPosts(groupId, keyword, PageRequest.of(0, 10));

        keyword = "내용";
        Page<Long> searchGroupPosts2 = postSearchRepository.getSearchGroupPosts(groupId, keyword, PageRequest.of(0, 10));


        //then
        List<Long> content1 = searchGroupPosts1.getContent();
        List<Long> content2 = searchGroupPosts2.getContent();

        assertThat(content1).hasSize(1)
                .containsExactlyInAnyOrder(1L);
        assertThat(content2).hasSize(2)
                .containsExactlyInAnyOrder(1L, 4L);
    }

}