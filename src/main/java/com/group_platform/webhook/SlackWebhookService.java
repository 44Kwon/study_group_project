package com.group_platform.webhook;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class SlackWebhookService {
    private final RestTemplate restTemplate;
    @Value("${webhook.slack.url}")
    private String slackWebhookUrl;

    @Async
    public void sendMessage(String text) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");

        Map<String, Object> payload = new HashMap<>();
        payload.put("text", text);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload,headers);

        try {
            restTemplate.postForEntity(slackWebhookUrl, entity, String.class);
        } catch (Exception e) {
            // 로깅 및 예외 처리
            log.error("Slack webhook error: {}", e.getMessage());
        }
    }
}
