package com.group_platform.post.dto;

import org.springframework.data.domain.Sort;

//게시글 목록 조회시 필터링용 enum
public enum PostSortType {
    LATEST,        // 최신순
    COMMENT_COUNT, // 댓글 많은 순
    LIKE_COUNT,    // 좋아요 많은 순
    VIEW_COUNT;     // 조회수 많은 순

    public Sort toSort() {
        return switch (this) {
            case LATEST -> Sort.by(Sort.Direction.DESC, "createdAt");
            case COMMENT_COUNT -> Sort.by(Sort.Direction.DESC, "comment_count");
            case LIKE_COUNT -> Sort.by(Sort.Direction.DESC, "likeCount");
            case VIEW_COUNT -> Sort.by(Sort.Direction.DESC, "viewCount");
        };
    }
}
