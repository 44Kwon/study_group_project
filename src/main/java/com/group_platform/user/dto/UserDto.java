package com.group_platform.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

//Lombok은 어노테이션을 붙인 클래스에만 적용된다. 상위클래스에 붙여도 내부클래스에 적용되지 않는다
public class UserDto {

    @Getter
    @Setter
    @NoArgsConstructor
    //@JsonIgnoreProperties(ignoreUnknown = false) => 허용되지 않은 값이 추가로 들어오면 튕겨내기
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

        @Email(message = "이메일 형식이 올바르지 않습니다.")
        private String email;

        @Size(min = 2, max=10, message = "닉네임은 2~10자 이어야 합니다")
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
    public static class UpdateResponse {
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

    @Getter
    @AllArgsConstructor
    @Setter
    public static class UserProfileDto {
        private Long id;
        private String nickname;
//        private String profileImageUrl;
    }
}
