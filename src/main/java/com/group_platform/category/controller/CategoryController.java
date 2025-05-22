package com.group_platform.category.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/category")
@RequiredArgsConstructor
public class CategoryController {


    @GetMapping
    //스터디그룹 등록 시 카테고리 선택을 위해
    public ResponseEntity<?> getCategory() {
        return null;
    }
}
