package com.group_platform.exception;

import lombok.Getter;

@Getter
public enum ExceptionCode {
    //400 - 요청 자체가 잘못되었을 떄, 409 - 리소스 상태 때문에 요청을 수용못함
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
    PASSWORD_SAME_AS_OLD(400, "새로운 비밀번호는 현재 비밀번호와 달라야 합니다"),

    //스터디 그룹
    GROUP_NOT_EXIST(404, "그룹이 존재하지 않습니다"),
    INVALID_MAX_CAPACITY(400, "최대인원은 현재 인원 이상이어야 합니다"),
    MEMBER_NOT_FOUND(404, "그룹의 멤버가 아닙니다"),//403 forbidden?
    NO_PERMISSION(403, "해당 권한이 없습니다"),
    GROUP_DELETE_DINED_BY_EXISTING_MEMBERS(409, "다른 멤버가 있어서 그룹을 삭제할 수 없습니다"),
    GROUP_DISABLED(403, "비활성화된 그룹입니다"),
    GROUP_DELETED(410, "그룹이 삭제된 상태입니다"),
    GROUP_FULL(409, "현재 그룹인원이 꽉 찬 상태입니다"),
    ALREADY_A_MEMBER(409, "이미 그룹원입니다"),

    //투두 관련
    DUE_DATE_PAST(400, "마감일은 오늘 이전이면 안됩니다"),
    TODO_NOT_EXIST(404, "Todo가 존재하지 않습니다"),
    TODO_NOT_ASSIGNED(400, "할당 인원이 설정되지 않았습니다"),

    //게시글관련
    POST_NOT_EXIST(404, "게시글이 존재하지 않습니다"),
    INVALID_POST_TYPE(400, "해당 게시글 타입은 허용되지 않습니다"),
    INVALID_BOARD_ACCESS(403, "해당 게시판에서는 접근할 수 없는 게시글입니다"),
    PINNED_VALID_ONLY_LEADER(400, "고정 등록,취소는 오직 그룹의 리더만 가능합니다"),
    PINNED_NUM_IS_OVER(400, "고정글 갯수가 10개를 초과합니다"),
    KEYWORD_NOT_EXIST(400, "검색 결과가 없습니다"),
    KEYWORD_MISSING(400, "검색어를 입력해주세요"),

    //댓글관련
    COMMENT_NOT_EXIST(404, "댓글이 존재하지 않습니다"),
    REPLY_NOT_EXIST(404, "대댓글이 존재하지 않습니다"),


    //좋아요관련
    LIKE_AUTH_REQUIRED(401,"좋아요는 로그인이 필요한 기능입니다"),
    LIKE_DUPLICATED(409, "이미 좋아요를 하였습니다"),


    FAVORITE_AUTH_REQUIRED(401, "찜은 로그인이 필요한 기능입니다"),
    FAVORITE_DUPLICATED(409, "이미 찜을 하였습니다");


    private int status;
    private String message;

    ExceptionCode(int status, String message) {
        this.status = status;
        this.message = message;
    }
}
