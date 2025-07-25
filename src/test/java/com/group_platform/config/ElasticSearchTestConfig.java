package com.group_platform.config;


import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.group_platform.post.repository.elasticsearch.PostSearchRepositoryImpl;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

//elasticsearch 테스트를 위한 설정 클래스(실제 프러덕션 컨테이너가 아니라 테스트용 컨테이너를 사용하기 위함)
//이 설정을 사용 안하면 테스트 컨테이너 생성보다 빈 등록이 빨라서 오류가 남

@TestConfiguration  //test용 설정 클래스임을 나타냄
public class ElasticSearchTestConfig {
    @Bean
    public ElasticsearchClient elasticsearchClient(RestClient restClient) {
        return new ElasticsearchClient(new RestClientTransport(restClient, new JacksonJsonpMapper()));
    }

    @Bean
    public RestClient restClient(@Value("${spring.elasticsearch.uris}") String uri) {
        return RestClient.builder(HttpHost.create(uri)).build();
    }

    @Bean
    public PostSearchRepositoryImpl postSearchRepositoryImpl(ElasticsearchClient client) {
        return new PostSearchRepositoryImpl(client);
    }
}
