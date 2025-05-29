package com.group_platform.post.repository;

import com.group_platform.post.dto.PostResponseListDto;
import com.group_platform.post.dto.PostSortType;
import com.group_platform.post.entity.Post;
import com.group_platform.post.entity.PostType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomPostRepository {
    Page<PostResponseListDto> getCommonPosts(Long userId, PostType type, PostSortType sort, Pageable pageable);
}
