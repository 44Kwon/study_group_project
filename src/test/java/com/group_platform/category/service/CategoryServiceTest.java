package com.group_platform.category.service;

import com.group_platform.category.entity.CategoryType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class CategoryServiceTest {

    @DisplayName("전역카테고리 가져오기")
    @Test
    void getCategoryTypeList() {
        //given
        int length = CategoryType.values().length;


        //when
        int count = (int) Arrays.stream(CategoryType.values()).count();

        // then
        assertThat(length).isEqualTo(count);
    }

}