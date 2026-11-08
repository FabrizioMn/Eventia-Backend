package com.backend.eventia.controller;

import com.backend.eventia.dto.CategoryResponseDto;
import com.backend.eventia.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryRepository categoryRepository;

    @GetMapping
    public ResponseEntity<List<CategoryResponseDto>> list() {
        List<CategoryResponseDto> categories = categoryRepository.findAll().stream()
                .map(c -> new CategoryResponseDto(
                        c.getId() != null ? String.valueOf(c.getId()) : null,
                        c.getName(),
                        c.getDescription(),
                        c.isActive(),
                        c.getCreatedAt(),
                        c.getUpdatedAt()
                ))
                .toList();
        return ResponseEntity.ok(categories);
    }
}
