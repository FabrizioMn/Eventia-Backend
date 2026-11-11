package com.backend.eventia.service.impl;

import com.backend.eventia.dto.category.CategoryResponseDto;
import com.backend.eventia.exception.ResourceNotFoundException;
import com.backend.eventia.model.Category;
import com.backend.eventia.repository.CategoryRepository;
import com.backend.eventia.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponseDto> findAllActive() {
        List<Category> categories = categoryRepository.findByActiveTrue(Sort.by("name"));
        log.debug("[Category] findAllActive -> {} categorías activas", categories.size());
        return categories.stream().map(this::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponseDto findById(Long id) {
        log.debug("[Category] Buscando categoria id={}", id);
        return toDto(getCategoryOrThrow(id));
    }

    private Category getCategoryOrThrow(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("[Category] No se encontro la categoria id={}", id);
                    return new ResourceNotFoundException("No se encontró la categoría con el id: " + id);
                });
    }

    private CategoryResponseDto toDto(Category category) {
        return new CategoryResponseDto(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.isActive(),
                category.getCreatedAt(),
                category.getUpdatedAt());
    }
}
