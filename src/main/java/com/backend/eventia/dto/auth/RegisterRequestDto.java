package com.backend.eventia.dto.auth;

import com.backend.eventia.model.enums.TipoDocumento;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequestDto(
        @NotBlank(message = "El nombre es obligatorio")
        String firstName,
        @NotBlank(message = "El apellido es obligatorio")
        String lastName,
        @NotNull(message = "El tipo de documento es obligatorio")
        TipoDocumento documentType,
        @NotBlank(message = "El número de documento es obligatorio")
        String dni,
        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no tiene un formato válido")
        String email,
        @NotBlank(message = "El celular es obligatorio")
        @Pattern(regexp = "9\\d{8}", message = "El celular debe tener 9 dígitos y empezar con 9")
        String cellphone,
        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, max = 100, message = "La contraseña debe tener entre 8 y 100 caracteres")
        String password,
        @NotBlank(message = "El rol es obligatorio")
        String role
) {

    @AssertTrue(message = "El documento no es válido: el DNI debe tener 8 dígitos y el CE entre 9 y 12 caracteres alfanuméricos")
    public boolean isDocumentoValido() {
        if (documentType == null || dni == null) {
            return true;
        }
        String documento = dni.trim();
        return switch (documentType) {
            case DNI -> documento.matches("\\d{8}");
            case CE -> documento.matches("[A-Za-z0-9]{9,12}");
        };
    }
}
