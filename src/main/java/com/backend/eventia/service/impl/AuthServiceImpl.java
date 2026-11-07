package com.backend.eventia.service.impl;

import com.backend.eventia.dto.auth.AuthResponseDto;
import com.backend.eventia.dto.auth.AuthUserResponseDto;
import com.backend.eventia.dto.auth.LoginRequestDto;
import com.backend.eventia.dto.auth.RegisterRequestDto;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
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
    private final UserDetailsService userDetailsService;


    @Override
    public AuthResponseDto login(LoginRequestDto request) {

        String email = request.email().trim().toLowerCase();
        log.info("[Auth] Recibida petición de LOGIN para: {}", email);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.password())
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtUtils.generateToken(userDetails);

        log.info("[Auth] LOGIN exitoso para: {}", email);
        return new AuthResponseDto(token);
    }

    @Override
    @Transactional
    public AuthResponseDto register(RegisterRequestDto request) {

        String email = request.email().trim().toLowerCase();
        log.info("[Auth] Recibida petición de REGISTRO para: {}", email);

        // Verificar si el email ya existe
        if (userRepository.existsByEmail(email)) {
            log.warn("[Auth] REGISTRO rechazado: el email ya existe -> {}", email);
            throw new DuplicateResourceException("El email ya esta registrado");
        }

        // Verificar si el dni ya existe
        if (userRepository.existsByDni(request.dni())) {
            log.warn("[Auth] REGISTRO rechazado: el DNI ya existe -> {}", request.dni());
            throw new DuplicateResourceException("El DNI ya esta registrado");
        }

        // Creamos el usuario
        User user = User.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .dni(request.dni())
                .email(email)
                .password(passwordEncoder.encode(request.password()))
                .role(UserRole.USER)
                .build();
        // Guardamos el nuevo usuario en la base de datos
        userRepository.save(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtUtils.generateToken(userDetails);
        log.info("[Auth] REGISTRO exitoso para: {} (id={})", email, user.getId());
        return new AuthResponseDto(token);
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
}
