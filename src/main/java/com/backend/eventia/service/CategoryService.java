package com.backend.eventia.service;

import com.backend.eventia.dto.category.CategoryRequestDto;
import com.backend.eventia.dto.category.CategoryResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CategoryService {

    List<CategoryResponseDto> findAllActive();

    Page<CategoryResponseDto> search(String search, Boolean active, Pageable pageable);

    CategoryResponseDto findById(Long id);

    CategoryResponseDto create(CategoryRequestDto request);

    CategoryResponseDto update(Long id, CategoryRequestDto request);

    void updateActive(Long id, boolean active);
}
