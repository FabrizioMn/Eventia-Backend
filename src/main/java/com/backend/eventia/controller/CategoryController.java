package com.backend.eventia.controller;

import com.backend.eventia.dto.category.CategoryResponseDto;
import com.backend.eventia.exception.ResourceNotFoundException;
import com.backend.eventia.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
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

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponseDto> findById(@PathVariable Long id) {
        log.debug("[CategoryController] GET /api/categories/{} - Consultando categoría por ID", id);

        CategoryResponseDto category = categoryService.findById(id);
        if (!category.active()) {
            throw new ResourceNotFoundException("No se encontró la categoría con el id: " + id);
        }

        return ResponseEntity.ok(category);
    }
}
