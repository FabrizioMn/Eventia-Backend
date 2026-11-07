package com.backend.eventia.dto.auth;

// Respuesta de login y registro: solo el token.
// La identidad y el perfil del usuario se consultan con GET /auth/me
// usando el JWT (Authentication). Así la autenticación no depende de
// enviar nombres, correo u otros datos personales en cada login.
public record AuthResponseDto(
        String token
) {
}
