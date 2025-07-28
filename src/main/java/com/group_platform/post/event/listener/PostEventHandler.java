package com.group_platform.post.event.listener;

import com.group_platform.post.entity.Post;
import com.group_platform.post.entity.PostDocument;
import com.group_platform.post.event.PostCreatedEvent;
import com.group_platform.post.event.PostDeletedEvent;
import com.group_platform.post.event.PostUpdatedEvent;
import com.group_platform.post.event.log.ElasticSyncFailLog;
import com.group_platform.post.event.log.ElasticSyncFailLogRepository;
import com.group_platform.post.repository.PostRepository;
import com.group_platform.post.repository.elasticsearch.PostSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@EnableRetry    //@Retryable 어노테이션을 사용한 메서드에 대해 재시도 기능을 활성화
@Slf4j
//저장,수정,삭제시 비동기적으로 elasticsearch에서 처리한다
public class PostEventHandler {
    private final PostSearchRepository postSearchRepository;
    private final PostRepository postRepository;
    private final ElasticSyncFailLogRepository elasticSyncFailLogRepository;
    private final RedissonClient redissonClient;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Retryable(
//            value= {Exception.class}, => default이므로 필요없다
            maxAttempts = 3,
            backoff = @Backoff(delay = 500) //재시도시 500ms(0.5초) 대기
    )
    public void handlePostCreate(PostCreatedEvent event) {
        RLock lock = redissonClient.getLock("es-post-lock-" + event.post().getId());

        boolean acquired = false;
        try {
            // 락을 최대 3초 동안 기다리고, 5초 동안 유지
            acquired = lock.tryLock(3, 5, TimeUnit.SECONDS);
            if (acquired) {
                postSearchRepository.save(PostDocument.from(event.post()));
                log.info("Elasticsearch 저장 동기화 완료 : Created post id={}", event.post().getId());
            } else {
                log.warn("Elasticsearch 저장 락 획득 실패, 건너뜀 : post id={}", event.post().getId());
            }

        } catch (Exception e) {
            throw new RuntimeException("Elasticsearch 동기화 실패", e);
        } finally {
            if (acquired && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
        //        log.error("ElasticSearch save failed : {}", e.getMessage());
    }

    @Recover
    public void recoverPostCreate(Exception e, PostCreatedEvent event) {
        log.error("Elastcsearch 저장 실패 : post id={}, message={}", event.post().getId(), e.getMessage());
        saveFailLog(event.post().getId(), ElasticSyncFailLog.OperationType.CREATE, e.getMessage());
    }


    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Retryable(
            maxAttempts = 3,
            backoff = @Backoff(delay = 500)
    )
    public void handlePostUpdate(PostUpdatedEvent event) {
        postSearchRepository.save(PostDocument.from(event.post()));
        log.info("Elasticsearch 업데이트 동기화 완료 : Updated post id={}", event.post().getId());
    }

    @Recover
    public void recoverPostUpdate(Exception e, PostUpdatedEvent event) {
        log.error("Elastcsearch 업데이트 실패 : post id={}, message={}", event.post().getId(), e.getMessage());
        saveFailLog(event.post().getId(), ElasticSyncFailLog.OperationType.UPDATE, e.getMessage());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Retryable(
            maxAttempts = 3,
            backoff = @Backoff(delay = 500)
    )
    public void handlePostDelete(PostDeletedEvent event) {
        postSearchRepository.deleteById(event.postId().toString());
        log.info("Elasticsearch 삭제 동기화 완료 : deleted post id={}", event.postId());
    }

    @Recover
    public void recoverPostDelete(Exception e, PostDeletedEvent event) {
        log.error("Elastcsearch 삭제 실패 : post id={}, message={}", event.postId(), e.getMessage());
        saveFailLog(event.postId(), ElasticSyncFailLog.OperationType.DELETE, e.getMessage());
    }

    //재도시조차 실패 시 실패 테이블에 로그 저장
    private void saveFailLog(Long postId, ElasticSyncFailLog.OperationType operationType, String errorMessage) {
        ElasticSyncFailLog failLog = new ElasticSyncFailLog();
        failLog.setPostId(postId);
        failLog.setOperationType(operationType);
        failLog.setErrorMessage(errorMessage);
        failLog.setRetryCount(3); // 재시도 3번 후 실패 기록
        elasticSyncFailLogRepository.save(failLog);
    }
}
