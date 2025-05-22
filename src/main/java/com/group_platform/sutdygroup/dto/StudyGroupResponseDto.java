package com.group_platform.sutdygroup.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Builder
public class StudyGroupResponseDto {
    private Long id;
    private String name;
    private String description;
    private int currentMembers;  // 현재 멤버 수
    private int maxMembers;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate createdAt;

    //내가 만든게 맞는지(조회한 사람이 리더가 맞는지) -> 그래야 변경,삭제가 가능하게 할 수있다
    private boolean isOwner;

    // 리더 정보가 필요하면 리더의 간단한 정보도 포함 가능
//    private String leader;
}
