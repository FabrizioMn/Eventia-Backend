package com.backend.eventia.controller;

import com.backend.eventia.dto.admin.AdminCreateUserRequestDto;
import com.backend.eventia.dto.admin.AdminStatusUpdateRequestDto;
import com.backend.eventia.dto.admin.AdminUpdateUserRequestDto;
import com.backend.eventia.dto.admin.AdminUserResponseDto;
import com.backend.eventia.service.AdminUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public ResponseEntity<Page<AdminUserResponseDto>> getOrganizers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "7") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean active
    ) {
        log.debug("[AdminUserController] GET /api/admin/users - page={} size={}", page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(adminUserService.getOrganizers(search, active, pageable));
    }

    @PostMapping
    public ResponseEntity<AdminUserResponseDto> createOrganizer(
            @Valid @RequestBody AdminCreateUserRequestDto request
    ) {
        log.info("[AdminUserController] POST /api/admin/users - email='{}'", request.email());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(adminUserService.createOrganizer(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AdminUserResponseDto> updateOrganizer(
            @PathVariable Long id,
            @Valid @RequestBody AdminUpdateUserRequestDto request
    ) {
        log.info("[AdminUserController] PUT /api/admin/users/{} - email='{}'", id, request.email());
        return ResponseEntity.ok(adminUserService.updateOrganizer(id, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<AdminUserResponseDto> toggleOrganizerStatus(
            @PathVariable Long id,
            @Valid @RequestBody AdminStatusUpdateRequestDto request
    ) {
        log.info("[AdminUserController] PATCH /api/admin/users/{}/status - active={}", id, request.active());
        return ResponseEntity.ok(adminUserService.toggleOrganizerStatus(id, Boolean.TRUE.equals(request.active())));
    }
}
