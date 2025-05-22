package com.group_platform.category.service;

import com.group_platform.category.entity.CategoryType;
import com.group_platform.category.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public List<String> getCategoryTypeList() {
        return Arrays.stream(CategoryType.values())
                .map(CategoryType::getName)
                .collect(Collectors.toList());
    }
}
