package com.backend.eventia.controller;

import com.backend.eventia.dto.category.CategoryRequestDto;
import com.backend.eventia.dto.category.CategoryResponseDto;
import com.backend.eventia.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<CategoryResponseDto>> findAll(@RequestParam(required = false, defaultValue = "false") boolean includeInactive) {
        log.debug("[CategoryController] GET /api/categories - Listando categorías (includeInactive={})", includeInactive);

        List<CategoryResponseDto> categories = categoryService.findAll(includeInactive);

        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponseDto> findById(@PathVariable Long id) {
        log.debug("[CategoryController] GET /api/categories/{} - Consultando categoría por ID", id);

        CategoryResponseDto category = categoryService.findById(id);
        return ResponseEntity.ok(category);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponseDto> create(@RequestBody @Valid CategoryRequestDto request, Authentication authentication) {
        log.info("[CategoryController] POST /api/categories - Creando nueva categoría con nombre='{}' por usuario='{}'", request.name(), authentication.getName());

        CategoryResponseDto createdCategory = categoryService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCategory);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponseDto> update(@PathVariable Long id, @RequestBody @Valid CategoryRequestDto request, Authentication authentication) {
        log.info("[CategoryController] PUT /api/categories/{} - Actualizando categoría nombre='{}' por usuario='{}'", id, request.name(), authentication.getName());

        CategoryResponseDto updatedCategory = categoryService.update(id, request);
        return ResponseEntity.ok(updatedCategory);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateStatus(@PathVariable Long id, @RequestParam boolean active, Authentication authentication) {
        log.info("[CategoryController] PATCH /api/categories/{}/status - Cambiando estado a active={} por usuario='{}'", id, active, authentication.getName());

        categoryService.updateStatus(id, active);
        return ResponseEntity.noContent().build();
    }
}
