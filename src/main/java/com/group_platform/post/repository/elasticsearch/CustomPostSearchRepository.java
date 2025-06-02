package com.group_platform.post.repository.elasticsearch;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomPostSearchRepository {
    Page<Long> getSearchCommonPosts(String keyword, Pageable pageable);
    Page<Long> getSearchGroupPosts(Long groupId, String keyword, Pageable pageable);
}
