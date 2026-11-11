package com.backend.eventia.dto.user;

import jakarta.validation.constraints.NotBlank;

public record DeactivateAccountRequestDto(
        @NotBlank(message = "La contraseña es obligatoria")
        String password
) {
}
