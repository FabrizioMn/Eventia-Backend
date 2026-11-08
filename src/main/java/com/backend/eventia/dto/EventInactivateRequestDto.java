package com.backend.eventia.dto;

import com.backend.eventia.model.enums.InactivationReason;
import jakarta.validation.constraints.NotNull;

public record EventInactivateRequestDto(
        @NotNull(message = "El motivo de inactivacion es obligatorio")
        InactivationReason reason
) {
}
