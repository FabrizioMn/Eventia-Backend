package com.backend.eventia.dto.auth;

// Perfil del usuario autenticado. Se obtiene con GET /auth/me a partir
// del token (Authentication); no forma parte de la respuesta de login.
public record AuthUserResponseDto(
        Long id,
        String firstName,
        String lastName,
        String email,
        String role
) {
}
