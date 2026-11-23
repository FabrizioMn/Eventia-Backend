package com.backend.eventia.dto.auth;

// El token se reenvía porque cambiar el email modifica el sujeto del JWT.
public record ProfileUpdateResponseDto(
        String token,
        AuthUserResponseDto user
) {
}
