package com.group_platform.post.repository.querydsl;

import com.group_platform.post.dto.PostResponseListDto;
import com.group_platform.post.dto.PostSortType;
import com.group_platform.post.entity.PostType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CustomPostRepository {
    Page<PostResponseListDto> getCommonPosts(Long userId, PostType type, PostSortType sort, Pageable pageable);
    Page<PostResponseListDto> getGroupNormalPosts(Long userId, Long groupId, PostType type, PostSortType sort, Pageable pageable);
    List<PostResponseListDto> getGroupPinnedPosts(Long userId, Long groupId);

    List<PostResponseListDto> getSearchPosts(Long userId, List<Long> postIds, Pageable pageable);
}
