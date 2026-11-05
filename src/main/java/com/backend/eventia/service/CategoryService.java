package com.backend.eventia.service;

import com.backend.eventia.dto.category.CategoryRequestDto;
import com.backend.eventia.dto.category.CategoryResponseDto;

import java.util.List;

public interface CategoryService {

    List<CategoryResponseDto> findAll();

    List<CategoryResponseDto> findAllActiveTrue();

    CategoryResponseDto findById(Long id);

    CategoryResponseDto create(CategoryRequestDto request);

    CategoryResponseDto update(Long id, CategoryRequestDto request);

    void deactivate(Long id);
}
