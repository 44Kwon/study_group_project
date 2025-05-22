package com.group_platform.category.entity;

import lombok.Getter;

@Getter
public enum CategoryType {
    //향후 추가하기
    MUSIC("음악") , SPORT("스포츠"), ENTERTAINMENT("오락"),
    EDUCATION("교육"), TECHNOLOGY("기술");

    private String name;

    CategoryType(String name) {
        this.name = name;
    }
}
