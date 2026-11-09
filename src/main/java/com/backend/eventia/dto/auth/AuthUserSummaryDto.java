package com.backend.eventia.dto.auth;

// Datos básicos del usuario incluidos en la respuesta de login/registro.
public record AuthUserSummaryDto(
        Long id,
        String name,
        String email,
        String role
) {
}
