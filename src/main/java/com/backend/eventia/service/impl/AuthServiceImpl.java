package com.backend.eventia.service.impl;

import com.backend.eventia.dto.auth.AuthResponseDto;
import com.backend.eventia.dto.auth.AuthUserResponseDto;
import com.backend.eventia.dto.auth.AuthUserSummaryDto;
import com.backend.eventia.dto.auth.LoginRequestDto;
import com.backend.eventia.dto.auth.ProfileUpdateResponseDto;
import com.backend.eventia.dto.auth.RegisterRequestDto;
import com.backend.eventia.dto.auth.UpdateProfileRequestDto;
import com.backend.eventia.exception.DuplicateResourceException;
import com.backend.eventia.exception.ResourceNotFoundException;
import com.backend.eventia.model.User;
import com.backend.eventia.model.enums.UserRole;
import com.backend.eventia.repository.UserRepository;
import com.backend.eventia.security.JwtUtils;
import com.backend.eventia.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;


    @Override
    public AuthResponseDto login(LoginRequestDto request) {

        String email = request.email().trim().toLowerCase();
        log.info("[Auth] Recibida petición de LOGIN para: {}", email);

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.password())
        );

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontro el usuario autenticado con el correo: " + email));

        String token = jwtUtils.generateToken(user);

        // Nunca registrar el token: equivale a publicar una sesión válida.
        log.info("[JWT] Token generado para {} | role={} | userId={} | expira={}",
                email, user.getRole().name(), user.getId(),
                java.time.Instant.now().plusSeconds(jwtUtils.getExpirationInSeconds()));
        log.info("[Auth] LOGIN exitoso para: {}", email);
        return buildAuthResponse(token, user);
    }

    @Override
    @Transactional
    public AuthResponseDto register(RegisterRequestDto request) {

        String email = request.email().trim().toLowerCase();
        log.info("[Auth] Recibida petición de REGISTRO para: {}", email);

        if (userRepository.existsByEmail(email)) {
            log.warn("[Auth] REGISTRO rechazado: el email ya existe -> {}", email);
            throw new DuplicateResourceException("El email ya esta registrado");
        }

        if (userRepository.existsByDni(request.dni())) {
            log.warn("[Auth] REGISTRO rechazado: el DNI ya existe -> {}", request.dni());
            throw new DuplicateResourceException("El DNI ya esta registrado");
        }

        User user = User.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .dni(request.dni())
                .email(email)
                .password(passwordEncoder.encode(request.password()))
                .role(UserRole.USER)
                .build();
        userRepository.save(user);

        String token = jwtUtils.generateToken(user);
        log.info("[Auth] REGISTRO exitoso para: {} (id={})", email, user.getId());
        return buildAuthResponse(token, user);
    }

    private AuthResponseDto buildAuthResponse(String token, User user) {
        AuthUserSummaryDto userDto = new AuthUserSummaryDto(
                user.getId(),
                user.getFirstName() + " " + user.getLastName(),
                user.getEmail(),
                user.getRole().name()
        );

        return new AuthResponseDto(
                token,
                "Bearer",
                jwtUtils.getExpirationInSeconds(),
                userDto
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AuthUserResponseDto getCurrentUser(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontro el usuario autenticado con el correo: " + email));

        log.debug("[Auth] Perfil del usuario autenticado consultado: id={} email={}", user.getId(), user.getEmail());

        return new AuthUserResponseDto(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getRole().name()
        );
    }

    @Override
    @Transactional
    public ProfileUpdateResponseDto updateProfile(String email, UpdateProfileRequestDto request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontro el usuario autenticado con el correo: " + email));

        if (request.newPassword() != null && !request.newPassword().isBlank()) {
            if (request.currentPassword() == null || !passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
                throw new IllegalArgumentException("La contraseña actual es incorrecta");
            }
            if (request.newPassword().length() < 6) {
                throw new IllegalArgumentException("La nueva contraseña debe tener al menos 6 caracteres");
            }
            user.setPassword(passwordEncoder.encode(request.newPassword()));
        }

        if (request.email() != null && !request.email().isBlank()) {
            String newEmail = request.email().trim().toLowerCase();
            if (!newEmail.equals(user.getEmail()) && userRepository.existsByEmail(newEmail)) {
                throw new DuplicateResourceException("El email ya esta registrado");
            }
            user.setEmail(newEmail);
        }
        if (request.firstName() != null && !request.firstName().isBlank()) {
            user.setFirstName(request.firstName().trim());
        }
        if (request.lastName() != null && !request.lastName().isBlank()) {
            user.setLastName(request.lastName().trim());
        }
        if (request.cellphone() != null && !request.cellphone().isBlank()) {
            user.setCellphone(request.cellphone().trim());
        }

        userRepository.save(user);
        log.info("[Auth] Perfil actualizado para: {}", user.getEmail());
        return new ProfileUpdateResponseDto(jwtUtils.generateToken(user), getCurrentUser(user.getEmail()));
    }
}
