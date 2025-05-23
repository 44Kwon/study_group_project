package com.group_platform.studymember.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.group_platform.studymember.entity.StudyMember;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

public class StudyMemberDto {
    @Getter
    @Setter
    @NoArgsConstructor
    public static class Response {
        private Long id;
        private String nickname;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        private LocalDate joinDate;
        private StudyMember.InGroupRole role;
        //나중에 프로필 이미지
    }
}
