package com.group_platform.post.entity;

import lombok.Getter;

@Getter
public enum PostType {
    NOTICE("공지"),   //공지사항은 리더만 쓸수있게 할지...
    GENERAL("일반"),
    RECRUITMENT("모집"),  //모집은 공통 게시판에서만(그룹 내 게시판에서는 X)
    INTRODUCTION("자기소개"),   //자기소개는 그룹 내 게시판에서만(공통 게시판에서는 X)
    INFO("정보 공유");

    private final String description;

    PostType(String description) {
        this.description = description;
    }
}
