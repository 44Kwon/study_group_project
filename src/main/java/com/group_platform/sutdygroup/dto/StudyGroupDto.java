package com.group_platform.sutdygroup.dto;

import com.group_platform.category.entity.CategoryType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class StudyGroupDto {
    @Getter
    @NoArgsConstructor
    @Setter
    public static class CreateRequest {
        @NotBlank(message = "그룹명을 입력해주세요")
        private String name;
        @NotBlank(message = "설명을 적어주세요")
        private String description;
        @Max(value = 10)
        private int maxMember;

        private CategoryType categoryType;
    }

    @Getter
    @NoArgsConstructor
    @Setter
    public static class UpdateRequest {
        private Long id;
        @Size(min = 2, max = 20, message = "그룹명은 2~20자여야 합니다.")
        private String name;
        @Size(min = 10, message = "설명은 10자 이상이어야 합니다")
        private String description;
        @Size(max = 10, message = "그룹은 10명이 최대입니다")
        private Integer maxMember;

        private CategoryType type;
    }

    @Getter
    @NoArgsConstructor
    @Setter
    //리스트용 response dto
    public static class ResponseList {
        private Long id;
        private String name;
        private String description;
        private int currentMember;
        private int maxMember;
        private CategoryType type;
    }
}
