package com.backend.eventia.repository;

import com.backend.eventia.model.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    boolean existsByNameIgnoreCase(String name);

    List<Category> findByActiveTrue(Sort sort);

    // Consultas del modulo Admin

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    @Query("""
            SELECT c FROM Category c
            WHERE (:active IS NULL OR c.active = :active)
              AND (:search IS NULL OR :search = ''
                   OR LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(COALESCE(c.description, '')) LIKE LOWER(CONCAT('%', :search, '%')))
            """)
    Page<Category> searchCategories(
            @Param("search") String search,
            @Param("active") Boolean active,
            Pageable pageable
    );
}
