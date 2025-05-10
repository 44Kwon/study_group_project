package com.group_platform.post.entity;

import lombok.Getter;

@Getter
public enum PostType {
    NOTICE("공지"),
    GENERAL("일반"),
    RECRUITMENT("모집"),
    INTRODUCTION("자기소개"),
    INFO("정보 공유"),
    FIXED("상단 고정");

    private String description;

    PostType(String description) {
        this.description = description;
    }
}
