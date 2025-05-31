package com.group_platform.post.repository.elasticsearch;

import com.group_platform.post.entity.PostDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomPostSearchRepository {
    Page<Long> getSearchCommonPosts(String keyword, Pageable pageable);
    Page<Long> geSearchGroupPosts(Long groupId, String keyword, Pageable pageable);
}
