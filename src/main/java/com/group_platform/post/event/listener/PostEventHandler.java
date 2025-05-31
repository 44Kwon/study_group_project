package com.group_platform.post.event.listener;

import com.group_platform.post.entity.PostDocument;
import com.group_platform.post.event.PostCreatedEvent;
import com.group_platform.post.event.PostDeletedEvent;
import com.group_platform.post.event.PostUpdatedEvent;
import com.group_platform.post.repository.elasticsearch.PostSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
//저장,수정,삭제시 비동기적으로 elasticsearch에서 처리한다
public class PostEventHandler {
    private final PostSearchRepository postSearchRepository;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(PostCreatedEvent event) {
        try {
            postSearchRepository.save(PostDocument.from(event.post()));
        } catch (Exception e) {
            log.error("ElasticSearch save failed : {}", e.getMessage());
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(PostUpdatedEvent event) {
        try {
            postSearchRepository.save(PostDocument.from(event.post()));
        } catch (Exception e) {
            log.error("ElasticSearch update failed : {}", e.getMessage());
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(PostDeletedEvent event) {
        try {
            postSearchRepository.deleteById(event.postId().toString());
        } catch (Exception e) {
            log.error("ElasticSearch delete failed : {}", e.getMessage());
        }
    }
}
