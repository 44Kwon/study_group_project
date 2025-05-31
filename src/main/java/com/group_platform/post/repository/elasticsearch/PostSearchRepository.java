package com.group_platform.post.repository.elasticsearch;

import com.group_platform.post.entity.PostDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface PostSearchRepository extends ElasticsearchRepository<PostDocument, String>, CustomPostSearchRepository {
}
