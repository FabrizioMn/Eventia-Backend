package com.backend.eventia.controller;

import com.backend.eventia.dto.category.CategoryRequestDto;
import com.backend.eventia.dto.category.CategoryResponseDto;
import com.backend.eventia.exception.ResourceNotFoundException;
import com.backend.eventia.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<CategoryResponseDto>> findAllActive() {
        log.debug("[CategoryController] GET /api/categories - Listando categorías activas");
        return ResponseEntity.ok(categoryService.findAllActive());
    }

    @GetMapping("/admin")
    public ResponseEntity<Page<CategoryResponseDto>> findAllForAdmin(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean active,
            @PageableDefault(size = 8, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        log.debug("[CategoryController] GET /api/categories/admin - page={} size={}",
                pageable.getPageNumber(), pageable.getPageSize());
        return ResponseEntity.ok(categoryService.search(search, active, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponseDto> findById(
            @PathVariable Long id,
            Authentication authentication
    ) {
        log.debug("[CategoryController] GET /api/categories/{} - Consultando categoría por ID", id);

        CategoryResponseDto category = categoryService.findById(id);
        if (!category.active() && !isAdmin(authentication)) {
            throw new ResourceNotFoundException("No se encontró la categoría con el id: " + id);
        }

        return ResponseEntity.ok(category);
    }

    @PostMapping
    public ResponseEntity<CategoryResponseDto> create(@Valid @RequestBody CategoryRequestDto request) {
        log.info("[CategoryController] POST /api/categories - nombre='{}'", request.name());
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponseDto> update(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequestDto request
    ) {
        log.info("[CategoryController] PUT /api/categories/{} - nombre='{}'", id, request.name());
        return ResponseEntity.ok(categoryService.update(id, request));
    }

    @PatchMapping("/{id}/active")
    public ResponseEntity<Void> updateActive(
            @PathVariable Long id,
            @RequestParam boolean active
    ) {
        log.info("[CategoryController] PATCH /api/categories/{}/active - active={}", id, active);
        categoryService.updateActive(id, active);
        return ResponseEntity.noContent().build();
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }
}
