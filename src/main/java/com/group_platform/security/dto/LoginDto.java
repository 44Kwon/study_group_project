package com.group_platform.security.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LoginDto {
    @NotBlank(message = "아이디를 입력해 주세요")
    private String username;
    @NotBlank(message = "비밀번호를 입력해 주세요")
    private String password;
}
