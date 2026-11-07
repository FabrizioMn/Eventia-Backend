package com.backend.eventia.dto.auth;

// Solo token: el perfil y el rol se obtienen en GET /auth/me.
public record AuthResponseDto(
        String token
) {
}
