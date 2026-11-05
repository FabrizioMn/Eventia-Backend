package com.backend.eventia.service.impl;

import com.backend.eventia.dto.category.CategoryRequestDto;
import com.backend.eventia.dto.category.CategoryResponseDto;
import com.backend.eventia.exception.DuplicateResourceException;
import com.backend.eventia.exception.ResourceNotFoundException;
import com.backend.eventia.model.Category;
import com.backend.eventia.repository.CategoryRepository;
import com.backend.eventia.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public List<CategoryResponseDto> findAll() {

        return categoryRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public List<CategoryResponseDto> findAllActiveTrue() {

        return categoryRepository.findByActiveTrue().stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public CategoryResponseDto findById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la categoria con el id: " + id));

        return toDto(category);
    }

    @Override
    public CategoryResponseDto create(CategoryRequestDto request) {

        if (categoryRepository.existsByNameIgnoreCase(request.name())) {
            throw new DuplicateResourceException("Ya existe una categoria con el mismo nombre");
        }

        Category category = toEntity(request);
        categoryRepository.save(category);

        return toDto(category);
    }

    @Override
    public CategoryResponseDto update(Long id, CategoryRequestDto request) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la categoria con el id: " + id));

        String newName = request.name().trim().toLowerCase();

        if (!category.getName().equals(newName) && categoryRepository.existsByNameIgnoreCase(newName)) {
            throw new DuplicateResourceException("Ya existe otra categoría con el mismo nombre");
        }

        category.setName(newName);
        category.setDescription(request.description());
        categoryRepository.save(category);
        return toDto(category);
    }

    @Override
    public void deactivate(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontro la categoria con el id: " + id));

        category.setActive(false);
        categoryRepository.save(category);

    }


    // Metodo de apoyo

    private CategoryResponseDto toDto(Category category) {
        return new CategoryResponseDto(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.isActive()
        );
    }


    private Category toEntity(CategoryRequestDto request) {
        String name = request.name().trim().toLowerCase();

        return Category.builder()
                .name(name)
                .description(request.description())
                .build();
    }


}
