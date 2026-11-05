package com.backend.eventia.controller;

import com.backend.eventia.dto.category.CategoryRequestDto;
import com.backend.eventia.dto.category.CategoryResponseDto;
import com.backend.eventia.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<CategoryResponseDto>> findAll(@RequestParam(required = false, defaultValue = "false") boolean includeInactive) {
        List<CategoryResponseDto> categories = includeInactive
                ? categoryService.findAll()
                : categoryService.findAllActiveTrue();

        return ResponseEntity.ok(categories);
    }


    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponseDto> findById(@PathVariable Long id) {
        CategoryResponseDto category = categoryService.findById(id);
        return ResponseEntity.ok(category);
    }

    @PostMapping
    public ResponseEntity<CategoryResponseDto> create(@RequestBody @Valid CategoryRequestDto request) {
        CategoryResponseDto createdCategory = categoryService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCategory);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponseDto> update(
            @PathVariable Long id,
            @RequestBody @Valid CategoryRequestDto request) {
        CategoryResponseDto updatedCategory = categoryService.update(id, request);
        return ResponseEntity.ok(updatedCategory);
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        categoryService.deactivate(id);
        return ResponseEntity.noContent().build();
    }


}
