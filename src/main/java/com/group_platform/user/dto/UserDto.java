package com.group_platform.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

//Lombok은 어노테이션을 붙인 클래스에만 적용된다. 상위클래스에 붙여도 내부클래스에 적용되지 않는다
public class UserDto {

    @Getter
    @Setter
    @NoArgsConstructor
    public static class CreateRequest {

        @NotBlank(message = "아이디를 입력해주세요.")
        private String username;
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        private String email;
        @NotBlank(message = "비밀번호를 입력해주세요.")
        private String password;
        @NotBlank(message = "닉네임을 입력햐주세요.")
        private String nickname;
    }


    @Getter
    @Setter
    @NoArgsConstructor
    public static class UpdateRequest {

        private Long id;

        @Email(message = "이메일 형식이 올바르지 않습니다.")
        private String email;

        @NotBlank(message = "닉네임을 입력햐주세요.")
        private String nickname;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class UpdatePasswordRequest {
        @NotBlank(message = "현재 비밀번호를 입력해주세요")
        private String currentPw;

        @NotBlank(message = "새로운 비밀번호를 입력해주세요")
        private String newPw;
    }

    @Getter
    @AllArgsConstructor
    @Builder
    public static class updateResponse {
        private Long id;
        private String username;
        private String email;
        private String nickname;
        //가입날짜
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        private LocalDate createdAt;

        //수정날짜
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime updatedAt;
        //나중에
//        private String profileImageUrl;
    }
}
