package com.group_platform.post.event.log;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * elasticsearch의 동기화 실패 후 재시도조차 실패한다면 해당 로그테이블에 실패 데이터를 남긴다.
 * 이후 1시간마다 스케줄링(or 배치)을 통해 재시도를 처리하고 이 경우에도 실패한다면 수동으로 처리하도록 웹훅, 이메일 처리한다.
 */
@Entity
@Table(name = "elastic_fail_log")
@Getter
@Setter
@NoArgsConstructor
public class ElasticSyncFailLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long postId;

    @Enumerated(EnumType.STRING)
    private OperationType operationType;

    private String errorMessage;

    private int retryCount;

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime lastRetryAt;

    public void incrementRetryCount() {
        this.retryCount++;
        this.lastRetryAt = LocalDateTime.now();
    }

    public enum OperationType {
        CREATE, UPDATE, DELETE
    }
}
