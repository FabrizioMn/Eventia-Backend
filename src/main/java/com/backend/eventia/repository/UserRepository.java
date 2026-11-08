package com.backend.eventia.repository;

import com.backend.eventia.model.User;
import com.backend.eventia.model.enums.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByDni(String dni);

    boolean existsByEmail(String email);

    // Consultas del modulo Admin

    Optional<User> findByIdAndRole(Long id, UserRole role);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    boolean existsByDniAndIdNot(String dni, Long id);

    long countByRoleAndActiveTrue(UserRole role);

    long countByRoleAndActiveTrueAndCreatedAtBetween(UserRole role, LocalDateTime start, LocalDateTime end);

    @Query("""
            SELECT u FROM User u
            WHERE u.role = :role
              AND (:active IS NULL OR u.active = :active)
              AND (:search IS NULL OR :search = ''
                   OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(u.dni) LIKE LOWER(CONCAT('%', :search, '%')))
            """)
    Page<User> searchUsersByRole(
            @Param("role") UserRole role,
            @Param("search") String search,
            @Param("active") Boolean active,
            Pageable pageable
    );
}
