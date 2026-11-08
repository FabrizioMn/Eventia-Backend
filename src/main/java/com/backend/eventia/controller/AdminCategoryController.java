package com.backend.eventia.controller;

import com.backend.eventia.dto.admin.AdminCategoryRequestDto;
import com.backend.eventia.dto.admin.AdminCategoryResponseDto;
import com.backend.eventia.dto.admin.AdminStatusUpdateRequestDto;
import com.backend.eventia.service.AdminCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@RestController
@RequestMapping("/api/admin/categories")
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
        log.debug("[AdminCategoryController] GET /api/admin/categories - page={} size={}", page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(adminCategoryService.getCategories(search, active, pageable));
    }

    @PostMapping
    public ResponseEntity<AdminCategoryResponseDto> createCategory(
            @Valid @RequestBody AdminCategoryRequestDto request
    ) {
        log.info("[AdminCategoryController] POST /api/admin/categories - nombre='{}'", request.name());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(adminCategoryService.createCategory(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AdminCategoryResponseDto> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody AdminCategoryRequestDto request
    ) {
        log.info("[AdminCategoryController] PUT /api/admin/categories/{} - nombre='{}'", id, request.name());
        return ResponseEntity.ok(adminCategoryService.updateCategory(id, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<AdminCategoryResponseDto> toggleCategoryStatus(
            @PathVariable Long id,
            @Valid @RequestBody AdminStatusUpdateRequestDto request
    ) {
        log.info("[AdminCategoryController] PATCH /api/admin/categories/{}/status - active={}", id, request.active());
        return ResponseEntity.ok(adminCategoryService.toggleCategoryStatus(id, Boolean.TRUE.equals(request.active())));
    }
}
