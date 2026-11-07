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
    public List<CategoryResponseDto> findAll(boolean includeInactive) {

        List<Category> categories = includeInactive
                ? categoryRepository.findAll(Sort.by("name"))
                : categoryRepository.findByActiveTrue(Sort.by("name"));

        List<CategoryResponseDto> response = categories.stream().map(this::toDto).toList();
        log.debug("[Category] findAll(includeInactive={}) -> {} categorías", includeInactive, categories.size());
        return response;
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
        log.info("[Category] Creando categoria '{}'", name);

        ensureNameIsAvailable(name, null);

        Category category = toEntity(name, request.description());
        categoryRepository.save(category);

        log.info("[Category] Categoria creada id={} nombre {}", category.getId(), category.getName());
        return toDto(category);
    }

    @Override
    @Transactional
    public CategoryResponseDto update(Long id, CategoryRequestDto request) {
        log.info("[Category] Actualizando categoría id={}", id);

        Category category = getCategoryOrThrow(id);
        String newName = normalizeName(request.name());

        ensureNameIsAvailable(newName, category);

        category.setName(newName);
        category.setDescription(request.description());
        categoryRepository.save(category);

        log.info("[Category] Categoría actualizada id={} nombre='{}'", category.getId(), category.getName());
        return toDto(category);
    }

    @Override
    @Transactional
    public void updateStatus(Long id, boolean active) {
        log.info("[Category] Cambiando estado de categoría id={} a active={}", id, active);
        Category category = getCategoryOrThrow(id);
        category.setActive(active);
        categoryRepository.save(category);
        log.info("[Category] Estado de categoría actualizado id={} active={}", category.getId(), category.isActive());
    }


    // --------
    // Helpers
    // --------

    private String normalizeName(String name) {
        String trimmed = name.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("El nombre de la categoria no puede estar vacio");
        }

        return trimmed.substring(0, 1).toUpperCase() + trimmed.substring(1);
    }

    private void ensureNameIsAvailable(String name, Category currentCategory) {
        if (currentCategory != null && currentCategory.getName().equalsIgnoreCase(name)) {
            return;
        }

        if (categoryRepository.existsByNameIgnoreCase(name)) {
            log.warn("[Category] Nombre rechazado: ya existe una categoría con el nombre '{}'", name);
            throw new DuplicateResourceException("Ya existe una categoría con el mismo nombre");
        }
    }

    private Category getCategoryOrThrow(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("[Category] No se encontro la categoria id={}", id);
                    return new ResourceNotFoundException("No se encontro la categoria con el id: " + id);
                });
    }

    private CategoryResponseDto toDto(Category category) {
        return new CategoryResponseDto(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.isActive());
    }

    private Category toEntity(String name, String description) {
        return Category.builder().name(name).description(description).build();
    }


}
