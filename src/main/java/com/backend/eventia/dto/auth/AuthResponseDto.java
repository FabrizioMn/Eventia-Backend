package com.backend.eventia.dto.auth;

// Mantiene "token" para compatibilidad y agrega datos básicos del usuario autenticado.
public record AuthResponseDto(
        String token,
        String tokenType,
        long expiresIn,
        AuthUserSummaryDto user
) {
}
