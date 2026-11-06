package com.backend.eventia.service;

import com.backend.eventia.dto.category.CategoryRequestDto;
import com.backend.eventia.dto.category.CategoryResponseDto;

import java.util.List;

public interface CategoryService {

    List<CategoryResponseDto> findAll(boolean includeInactive);

    CategoryResponseDto findById(Long id);

    CategoryResponseDto create(CategoryRequestDto request);

    CategoryResponseDto update(Long id, CategoryRequestDto request);

    void updateStatus(Long id, boolean active);
}
