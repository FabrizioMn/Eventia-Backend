package com.backend.eventia.controller;

import com.backend.eventia.dto.admin.AdminCategoryRequestDto;
import com.backend.eventia.dto.admin.AdminCategoryResponseDto;
import com.backend.eventia.dto.admin.AdminStatusUpdateRequestDto;
import com.backend.eventia.service.AdminCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final AdminCategoryService adminCategoryService;

    @GetMapping
    public ResponseEntity<Page<AdminCategoryResponseDto>> getCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean active
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(adminCategoryService.getCategories(search, active, pageable));
    }

    @PostMapping
    public ResponseEntity<AdminCategoryResponseDto> createCategory(
            @Valid @RequestBody AdminCategoryRequestDto request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(adminCategoryService.createCategory(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AdminCategoryResponseDto> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody AdminCategoryRequestDto request
    ) {
        return ResponseEntity.ok(adminCategoryService.updateCategory(id, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<AdminCategoryResponseDto> toggleCategoryStatus(
            @PathVariable Long id,
            @Valid @RequestBody AdminStatusUpdateRequestDto request
    ) {
        return ResponseEntity.ok(adminCategoryService.toggleCategoryStatus(id, Boolean.TRUE.equals(request.active())));
    }
}

