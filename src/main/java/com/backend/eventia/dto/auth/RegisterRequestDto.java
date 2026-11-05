package com.backend.eventia.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequestDto(
        @NotBlank(message = "El nombre es obligatorio")
        String firstName,
        @NotBlank(message = "El apellido es obligatorio")
        String lastName,
        @NotBlank(message = "El DNI es obligatorio")
        @Pattern(regexp = "\\d{8}", message = "El DNI debe tener 8 dígitos")
        String dni,
        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email no tiene un formato valido")
        String email,
        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 6, max = 100, message = "La contraseña debe tener entre 6 y 100 caracteres")
        String password
) {
}
