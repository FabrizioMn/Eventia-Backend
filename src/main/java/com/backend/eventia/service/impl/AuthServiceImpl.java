package com.backend.eventia.service.impl;

import com.backend.eventia.dto.auth.AuthResponseDto;
import com.backend.eventia.dto.auth.LoginRequestDto;
import com.backend.eventia.dto.auth.RegisterRequestDto;
import com.backend.eventia.exception.DuplicateResourceException;
import com.backend.eventia.model.User;
import com.backend.eventia.model.enums.UserRole;
import com.backend.eventia.repository.UserRepository;
import com.backend.eventia.security.JwtUtils;
import com.backend.eventia.service.AuthService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.password())
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtUtils.generateToken(userDetails);
        return new AuthResponseDto(token);
    }

    @Override
    @Transactional
    public AuthResponseDto register(RegisterRequestDto request) {

        String email = request.email().trim().toLowerCase();

        // Verificar si el email ya existe
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("El email ya esta registrado");
        }

        // Verificar si el dni ya existe
        if (userRepository.existsByDni(request.dni())) {
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
        return new AuthResponseDto(token);
    }
}
