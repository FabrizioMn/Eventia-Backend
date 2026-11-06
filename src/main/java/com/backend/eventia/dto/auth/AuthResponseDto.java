package com.backend.eventia.dto.auth;

// Respuesta de login y registro.
// Antes solo devolvía el token; ahora también devuelve los datos básicos
// del usuario para que el frontend los guarde sin tener que adivinarlos.
public record AuthResponseDto(
        Long id,
        String firstName,
        String lastName,
        String email,
        String token
) {
}
