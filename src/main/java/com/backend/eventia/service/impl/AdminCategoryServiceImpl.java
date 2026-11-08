package com.backend.eventia.service.impl;

import com.backend.eventia.dto.admin.AdminCategoryRequestDto;
import com.backend.eventia.dto.admin.AdminCategoryResponseDto;
import com.backend.eventia.exception.DuplicateResourceException;
import com.backend.eventia.exception.ResourceNotFoundException;
import com.backend.eventia.model.Category;
import com.backend.eventia.repository.CategoryRepository;
import com.backend.eventia.service.AdminCategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminCategoryServiceImpl implements AdminCategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<AdminCategoryResponseDto> getCategories(String search, Boolean active, Pageable pageable) {
        String normalizedSearch = (search == null || search.isBlank()) ? null : search.trim();
        return categoryRepository.searchCategories(normalizedSearch, active, pageable)
                .map(category -> toDto(category));
    }

    @Override
    @Transactional
    public AdminCategoryResponseDto createCategory(AdminCategoryRequestDto request) {
        String normalizedName = request.name().trim();
        if (categoryRepository.existsByNameIgnoreCase(normalizedName)) {
            log.warn("[AdminCategory] Intento de crear categoria duplicada: '{}'", normalizedName);
            throw new DuplicateResourceException("Ya existe una categoría con el nombre: " + normalizedName);
        }

        String description = request.description();
        Category category = Objects.requireNonNull(Category.builder()
                .name(normalizedName)
                .description(description != null ? description.trim() : null)
                .active(Boolean.TRUE.equals(request.active()))
                .build());

        categoryRepository.save(category);
        log.info("[AdminCategory] Categoria creada id={} nombre='{}'", category.getId(), category.getName());
        return toDto(category);
    }

    @Override
    @Transactional
    public AdminCategoryResponseDto updateCategory(Long id, AdminCategoryRequestDto request) {
        Category category = findCategoryOrThrow(id);

        String normalizedName = request.name().trim();
        if (categoryRepository.existsByNameIgnoreCaseAndIdNot(normalizedName, id)) {
            log.warn("[AdminCategory] Nombre duplicado al actualizar id={}: '{}'", id, normalizedName);
            throw new DuplicateResourceException("Ya existe otra categoría con el nombre: " + normalizedName);
        }

        String description = request.description();
        category.setName(normalizedName);
        category.setDescription(description != null ? description.trim() : null);
        category.setActive(Boolean.TRUE.equals(request.active()));

        categoryRepository.save(category);
        log.info("[AdminCategory] Categoria actualizada id={} nombre='{}'", category.getId(), category.getName());
        return toDto(category);
    }

    @Override
    @Transactional
    public AdminCategoryResponseDto toggleCategoryStatus(Long id, boolean active) {
        Category category = findCategoryOrThrow(id);
        category.setActive(active);
        categoryRepository.save(category);
        log.info("[AdminCategory] Estado actualizado id={} active={}", category.getId(), category.isActive());
        return toDto(category);
    }

    private Category findCategoryOrThrow(Long id) {
        return categoryRepository.findById(Objects.requireNonNull(id))
                .orElseThrow(() -> {
                    log.warn("[AdminCategory] Categoria no encontrada id={}", id);
                    return new ResourceNotFoundException("Categoría no encontrada con id: " + id);
                });
    }

    private AdminCategoryResponseDto toDto(Category category) {
        return new AdminCategoryResponseDto(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.isActive(),
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }
}
