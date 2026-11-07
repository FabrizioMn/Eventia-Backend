package com.backend.eventia.service;

import com.backend.eventia.dto.admin.AdminCreateUserRequestDto;
import com.backend.eventia.dto.admin.AdminUpdateUserRequestDto;
import com.backend.eventia.dto.admin.AdminUserResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminUserService {

    Page<AdminUserResponseDto> getOrganizers(String search, Boolean active, Pageable pageable);

    AdminUserResponseDto createOrganizer(AdminCreateUserRequestDto request);

    AdminUserResponseDto updateOrganizer(Long id, AdminUpdateUserRequestDto request);

    AdminUserResponseDto toggleOrganizerStatus(Long id, boolean active);
}

