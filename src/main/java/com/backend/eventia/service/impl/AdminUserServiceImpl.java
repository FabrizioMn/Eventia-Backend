package com.backend.eventia.service.impl;

import com.backend.eventia.dto.admin.AdminCreateUserRequestDto;
import com.backend.eventia.dto.admin.AdminUpdateUserRequestDto;
import com.backend.eventia.dto.admin.AdminUserResponseDto;
import com.backend.eventia.exception.DuplicateResourceException;
import com.backend.eventia.exception.ResourceNotFoundException;
import com.backend.eventia.model.User;
import com.backend.eventia.model.enums.UserRole;
import com.backend.eventia.repository.UserRepository;
import com.backend.eventia.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public Page<AdminUserResponseDto> getOrganizers(String search, Boolean active, Pageable pageable) {
        String normalizedSearch = (search == null || search.isBlank()) ? null : search.trim();
        return userRepository.searchUsersByRole(UserRole.ORGANIZER, normalizedSearch, active, pageable)
                .map(user -> toDto(user));
    }

    @Override
    @Transactional
    public AdminUserResponseDto createOrganizer(AdminCreateUserRequestDto request) {
        String normalizedEmail = request.email().trim().toLowerCase();
        String normalizedDni = request.dni().trim();

        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new DuplicateResourceException("Ya existe un usuario registrado con el correo: " + normalizedEmail);
        }
        if (userRepository.existsByDni(normalizedDni)) {
            throw new DuplicateResourceException("Ya existe un usuario registrado con el DNI: " + normalizedDni);
        }

        String cellphone = request.cellphone();
        User organizer = Objects.requireNonNull(User.builder()
                .firstName(request.firstName().trim())
                .lastName(request.lastName().trim())
                .dni(normalizedDni)
                .email(normalizedEmail)
                .cellphone(cellphone != null ? cellphone.trim() : null)
                .password(passwordEncoder.encode(request.password()))
                .role(UserRole.ORGANIZER)
                .active(true)
                .build());

        userRepository.save(organizer);
        return toDto(organizer);
    }

    @Override
    @Transactional
    public AdminUserResponseDto updateOrganizer(Long id, AdminUpdateUserRequestDto request) {
        User organizer = findOrganizerOrThrow(id);

        String normalizedEmail = request.email().trim().toLowerCase();
        String normalizedDni = request.dni().trim();

        if (userRepository.existsByEmailIgnoreCaseAndIdNot(normalizedEmail, id)) {
            throw new DuplicateResourceException("Ya existe otro usuario con el correo: " + normalizedEmail);
        }
        if (userRepository.existsByDniAndIdNot(normalizedDni, id)) {
            throw new DuplicateResourceException("Ya existe otro usuario con el DNI: " + normalizedDni);
        }

        String cellphone = request.cellphone();
        organizer.setFirstName(request.firstName().trim());
        organizer.setLastName(request.lastName().trim());
        organizer.setDni(normalizedDni);
        organizer.setEmail(normalizedEmail);
        organizer.setCellphone(cellphone != null ? cellphone.trim() : null);

        userRepository.save(organizer);
        return toDto(organizer);
    }

    @Override
    @Transactional
    public AdminUserResponseDto toggleOrganizerStatus(Long id, boolean active) {
        User organizer = findOrganizerOrThrow(id);
        organizer.setActive(active);
        userRepository.save(organizer);
        return toDto(organizer);
    }

    private User findOrganizerOrThrow(Long id) {
        return userRepository.findByIdAndRole(Objects.requireNonNull(id), UserRole.ORGANIZER)
                .orElseThrow(() -> new ResourceNotFoundException("Organizador no encontrado con id: " + id));
    }

    private AdminUserResponseDto toDto(User user) {
        return new AdminUserResponseDto(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getDni(),
                user.getEmail(),
                user.getCellphone(),
                user.isActive(),
                user.getRole(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}

