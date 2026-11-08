package com.backend.eventia.service;

import com.backend.eventia.dto.admin.AdminCategoryRequestDto;
import com.backend.eventia.dto.admin.AdminCategoryResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminCategoryService {

    Page<AdminCategoryResponseDto> getCategories(String search, Boolean active, Pageable pageable);

    AdminCategoryResponseDto createCategory(AdminCategoryRequestDto request);

    AdminCategoryResponseDto updateCategory(Long id, AdminCategoryRequestDto request);

    AdminCategoryResponseDto toggleCategoryStatus(Long id, boolean active);
}

