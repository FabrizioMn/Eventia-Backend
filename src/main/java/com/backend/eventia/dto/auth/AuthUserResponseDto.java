package com.backend.eventia.dto.auth;

// Perfil devuelto por GET /auth/me a partir del JWT.
public record AuthUserResponseDto(
        Long id,
        String firstName,
        String lastName,
        String email,
        String role
) {
}
