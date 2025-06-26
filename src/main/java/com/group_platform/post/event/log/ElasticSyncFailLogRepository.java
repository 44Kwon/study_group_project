package com.group_platform.post.event.log;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ElasticSyncFailLogRepository extends JpaRepository<ElasticSyncFailLog, Long> {
    List<ElasticSyncFailLog> findAllByOrderByCreatedAtAsc();
}
