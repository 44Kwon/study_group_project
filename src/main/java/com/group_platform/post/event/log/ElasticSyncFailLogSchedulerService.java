package com.group_platform.post.event.log;

import com.group_platform.post.entity.Post;
import com.group_platform.post.entity.PostDocument;
import com.group_platform.post.repository.PostRepository;
import com.group_platform.post.repository.elasticsearch.PostSearchRepository;
import com.group_platform.webhook.SlackWebhookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
@ConditionalOnProperty(name = "scheduler.enabled", havingValue = "true")
public class ElasticSyncFailLogSchedulerService {
    private final PostRepository postRepository;
    private final PostSearchRepository postSearchRepository;
    private final ElasticSyncFailLogRepository elasticSyncFailLogRepository;
    private final RedissonClient redissonClient;
    private final SlackWebhookService slackWebhookService;

    //실패 로그에서 1시간 마다 재시도 스케줄링
    @Scheduled(fixedDelay = 60 * 60 * 1000) // 1시간 주기
    public void retryFailedScheduling() {
        RLock lock = redissonClient.getLock("es-fail-sync-lock");
        boolean acquired = false;
        try {
            acquired = lock.tryLock(5, 10, TimeUnit.SECONDS);

            // 분산 환경에서 스케줄링 시 락 획득 실패는 필연적이 상황이므로 로그 레벨을 낮춘다
            if (!acquired) {
                log.info("Elasticsearch 실패 로그 재시도 스케줄링 락 획득 실패, 작업 건너뜀");
                return;
            }

            List<ElasticSyncFailLog> failedLogs = elasticSyncFailLogRepository.findAllByOrderByCreatedAtAsc();

            for (ElasticSyncFailLog failLog : failedLogs) {
                try {
                    Post post = postRepository.findById(failLog.getPostId())
                            .orElseThrow(() -> new IllegalArgumentException("Post not found id=" + failLog.getPostId()));

                    switch (failLog.getOperationType()) {
                        case CREATE:
                        case UPDATE:
                            postSearchRepository.save(PostDocument.from(post));
                            break;
                        case DELETE:
                            postSearchRepository.deleteById(failLog.getPostId().toString());
                            break;
                    }
                    elasticSyncFailLogRepository.delete(failLog); // 성공 시 삭제
                    log.info("Elasticsearch 실패 로그 재시도 스케줄링 success. post id={}", failLog.getPostId());

                } catch (Exception e) {
                    failLog.incrementRetryCount();
                    failLog.setErrorMessage(e.getMessage());
                    if (failLog.getRetryCount() >= 5) {
                        log.error("[알림] Elasticsearch 지속적 실패로 인해 수동처리 필요 id= {}, postId={}", failLog.getId(), failLog.getPostId());
                        // 관리자 알림 추가해야 함(메일, 웹훅)
                        slackWebhookService.sendMessage("[알림] Elasticsearch 동기화 지속적 실패로 인해 수동처리 필요 FailLogId = " + failLog.getId());
                    }
                    elasticSyncFailLogRepository.save(failLog);
                }
            }
        } catch(Exception e) {
            // redis나 db 오류 or 락과 관련된 오류
            log.error("Elasticsearch 동기화 실패 로그 재시도 스케줄링 중 오류 발생: {}", e.getMessage(), e);
            // 관리자 알람 추가(웹훅)
            slackWebhookService.sendMessage("[알림] Elasticsearch 동기화 스케줄링 중 실패. 로그 확인 필요");
        } finally {
            if (acquired && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
