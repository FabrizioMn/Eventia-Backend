package com.backend.eventia.service.impl;

import com.backend.eventia.dto.category.CategoryRequestDto;
import com.backend.eventia.dto.category.CategoryResponseDto;
import com.backend.eventia.exception.DuplicateResourceException;
import com.backend.eventia.exception.ResourceNotFoundException;
import com.backend.eventia.model.Category;
import com.backend.eventia.repository.CategoryRepository;
import com.backend.eventia.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    public Page<CategoryResponseDto> search(String search, Boolean active, Pageable pageable) {
        String normalizedSearch = (search == null || search.isBlank()) ? null : search.trim();
        log.debug("[Category] search(search='{}', active={}, page={}, size={})",
                normalizedSearch, active, pageable.getPageNumber(), pageable.getPageSize());

        return categoryRepository.searchCategories(normalizedSearch, active, pageable)
                .map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponseDto findById(Long id) {
        log.debug("[Category] Buscando categoria id={}", id);
        return toDto(getCategoryOrThrow(id));
    }

    @Override
    @Transactional
    public CategoryResponseDto create(CategoryRequestDto request) {
        String name = normalizeName(request.name());

        if (categoryRepository.existsByNameIgnoreCase(name)) {
            log.warn("[Category] Intento de crear categoria duplicada: '{}'", name);
            throw new DuplicateResourceException("Ya existe una categoría con el nombre: " + name);
        }

        Category category = Category.builder()
                .name(name)
                .description(normalizeDescription(request.description()))
                .build();

        categoryRepository.save(category);
        log.info("[Category] Categoria creada id={} nombre='{}'", category.getId(), category.getName());
        return toDto(category);
    }

    @Override
    @Transactional
    public CategoryResponseDto update(Long id, CategoryRequestDto request) {
        Category category = getCategoryOrThrow(id);
        String name = normalizeName(request.name());

        if (categoryRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            log.warn("[Category] Nombre duplicado al actualizar id={}: '{}'", id, name);
            throw new DuplicateResourceException("Ya existe otra categoría con el nombre: " + name);
        }

        category.setName(name);
        category.setDescription(normalizeDescription(request.description()));

        categoryRepository.save(category);
        log.info("[Category] Categoria actualizada id={} nombre='{}'", category.getId(), category.getName());
        return toDto(category);
    }

    @Override
    @Transactional
    public void updateActive(Long id, boolean active) {
        Category category = getCategoryOrThrow(id);
        category.setActive(active);
        categoryRepository.save(category);
        log.info("[Category] Estado actualizado id={} active={}", category.getId(), category.isActive());
    }

    private Category getCategoryOrThrow(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("[Category] No se encontro la categoria id={}", id);
                    return new ResourceNotFoundException("No se encontró la categoría con el id: " + id);
                });
    }

    private String normalizeName(String name) {
        String trimmed = name.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("El nombre de la categoría no puede estar vacío");
        }
        return trimmed.substring(0, 1).toUpperCase() + trimmed.substring(1);
    }

    private String normalizeDescription(String description) {
        return description == null ? null : description.trim();
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
