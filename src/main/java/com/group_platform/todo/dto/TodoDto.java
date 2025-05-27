package com.group_platform.todo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.group_platform.todo.entity.Todo;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

public class TodoDto {
    @Getter
    @Setter
    @NoArgsConstructor
    public static class createRequest {
        @NotBlank(message = "제목 필수 입력")
        private String title;
        private String description;
        private LocalDate due_date;
        private boolean allMembers;
        private List<Long> memberIds;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class updateRequest {
        private String title;
        private String description;
        private LocalDate due_date;
        private Boolean allMembers;
        private List<Long> memberIds;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class changeAssigneeRequest {
        private Boolean allMembers;
        private List<Long> memberIds;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    //보내줄 때 기한지남 필드 필요하다(현재는 코드상에서 계산, Spring Batch처리보다 성능적 이점이 낫다)
    public static class Response {
        private Long id;
        private String title;
        private String description;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        private LocalDate due_date;
        private Todo.TodoStatus status;
        private boolean allMembers;
        private List<String> assignedMemberNicknames;
        private boolean overdue;    // true : 기한지남, 값 안넣으면 false
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class ResponseAssignedMy {
        private String title;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        private LocalDate due_date;
        private Todo.TodoStatus status;
        private boolean overdue;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    // name을 가져오기 위한 연관관계 쿼리용 dto
    public static class TodoUserNicknameDto {
        private Long todoId;
        private String nickname;
    }
}
