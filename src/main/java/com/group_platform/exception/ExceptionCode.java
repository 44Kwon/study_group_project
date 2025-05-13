package com.group_platform.exception;

import lombok.Getter;

@Getter
public enum ExceptionCode {
    // 비즈니스 로직상 에러코드
    USER_NOT_FOUND(404, "유저가 존재하지 않습니다");


    private int status;
    private String message;

    ExceptionCode(int status, String message) {
        this.status = status;
        this.message = message;
    }
}
