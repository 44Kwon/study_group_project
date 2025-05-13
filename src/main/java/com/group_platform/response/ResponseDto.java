package com.group_platform.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

//데이터 반환 dto
public class ResponseDto<T> {
    @Getter
    @AllArgsConstructor
    public static class SingleResponseDto<T> {
        private T data;
    }

    @Getter
    //**MultipleResponseDto**는 여러 개의 객체를 반환할 때 사용
    public static class MultipleResponseDto<T> {
        private List<T> data;
        private PageInfo pageInfo;

        public MultipleResponseDto(List<T> data, Page page) {
            this.data = data;
            this.pageInfo = new PageInfo(page.getNumber() + 1, page.getSize(), page.getTotalElements(), page.getTotalPages());
        }
    }

    @Getter
    //**MultipleInfoResponseDto**는 하나의 객체 안에 여러 항목을 포함시켜 반환할 때 사용(ex: 카테고리 안에 게시글들)
    public static class MultipleInfoResponseDto<T> {
        private T data;
        private PageInfo pageInfo;

        public MultipleInfoResponseDto(T data, Page page) {
            this.data = data;
            this.pageInfo = new PageInfo(page.getNumber() + 1, page.getSize(), page.getTotalElements(), page.getTotalPages());
        }
    }
}
