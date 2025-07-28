package com.group_platform.webhook;

import com.group_platform.post.repository.elasticsearch.PostSearchRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class SlackWebhookServiceTest {
    @MockitoBean
    private PostSearchRepository postSearchRepository;
    @MockitoBean
    private RedissonClient redissonClient;

    @Autowired
    private SlackWebhookService slackWebhookService;

    @DisplayName("슬랙 웹훅 테스트")
    @Test
    void sendMessage() {
        //when
        slackWebhookService.sendMessage("테스트 메시지");
        //then

    }
}