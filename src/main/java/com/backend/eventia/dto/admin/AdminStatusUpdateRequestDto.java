package com.backend.eventia.dto.admin;

import jakarta.validation.constraints.NotNull;

public record AdminStatusUpdateRequestDto(
        @NotNull(message = "El estado activo es obligatorio")
        Boolean active
) {
}

