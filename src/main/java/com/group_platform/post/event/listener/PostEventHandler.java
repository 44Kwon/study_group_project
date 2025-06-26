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

@Service
@RequiredArgsConstructor
@EnableRetry    //@Retryable 어노테이션을 사용한 메서드에 대해 재시도 기능을 활성화
@Slf4j
//저장,수정,삭제시 비동기적으로 elasticsearch에서 처리한다
public class PostEventHandler {
    private final PostSearchRepository postSearchRepository;
    private final PostRepository postRepository;
    private final ElasticSyncFailLogRepository elasticSyncFailLogRepository;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Retryable(
//            value= {Exception.class}, => default이므로 필요없다
            maxAttempts = 3,
            backoff = @Backoff(delay = 500) //재시도시 500ms 대기
    )
    public void handlePostCreate(PostCreatedEvent event) {
        postSearchRepository.save(PostDocument.from(event.post()));
        log.info("Elasticsearch 저장 동기화 완료 : Created post id={}", event.post().getId());
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

    //실패 로그에서 1시간 마다 재시도 스케줄링
    @Scheduled(fixedDelay = 60 * 60 * 1000) // 1시간 주기
    public void retryFailedScheduling() {
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
                    log.error("[알림] Elasticsearch 지속적 실패로 인해 수동처리 필요 postId={}", failLog.getPostId());
                    // 관리자 알림 추가해야 함(메일, 웹훅)
                }
                elasticSyncFailLogRepository.save(failLog);
            }
        }
    }
}
