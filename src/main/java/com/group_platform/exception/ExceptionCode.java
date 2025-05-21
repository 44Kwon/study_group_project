package com.group_platform.exception;

import lombok.Getter;

@Getter
public enum ExceptionCode {
    USER_NOT_FOUND(404, "유저가 존재하지 않습니다"),
    //회원 가입 시 중복 검증
    USER_EMAIL_DUPLICATED(409, "이미 등록된 이메일입니다"),
    USER_USERNAME_DUPLICATED(409, "이미 사용 중인 아이디입니다"),
    USER_NICKNAME_DUPLICATED(409, "이미 사용 중인 닉네임입니다"),

    USER_EMAIL_CANNOT_UPDATE(400, "이메일은 수정할 수 없습니다"),
    USER_INVALID_NICKNAME(400, "닉네임을 비워둘 수 없습니다"),

    SAME_EMAIL(400, "현재 사용 중인 이메일과 동일합니다."),
    SAME_NICKNAME(400, "현재 사용 중인 닉네임과 동일합니다."),

    //비밀번호 관련 오류
    PASSWORD_MISMATCH(400, "현재 비밀번호가 올바르지 않습니다"),
    PASSWORD_SAME_AS_OLD(400, "새로운 비밀번호는 현재 비밀번호와 달라야 합니다");





    private int status;
    private String message;

    ExceptionCode(int status, String message) {
        this.status = status;
        this.message = message;
    }
}
