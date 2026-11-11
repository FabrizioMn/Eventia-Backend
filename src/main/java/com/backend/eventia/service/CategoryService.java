package com.backend.eventia.service;

import com.backend.eventia.dto.category.CategoryResponseDto;

import java.util.List;

public interface CategoryService {

    List<CategoryResponseDto> findAllActive();

    CategoryResponseDto findById(Long id);
}
