package com.group_platform.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Setter
//Mapstruct에서는 기본생성자 + setter로 값을 넣는다. builder가 있으면 builder가 우선인듯하다
//@Builder
//프로필 조회 관련 response
public class UserResponseDto {
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
}
