package com.backend.eventia.dto.auth;

// Campos editables del perfil. La contraseña actual se exige para cambiar la contraseña.
public record UpdateProfileRequestDto(
        String email,
        String firstName,
        String lastName,
        String cellphone,
        String currentPassword,
        String newPassword
) {
}
