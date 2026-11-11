package com.backend.eventia.dto.user;

import com.backend.eventia.model.enums.TipoDocumento;

public record OrganizerCustomerResponseDto(
        Long id,
        String firstName,
        String lastName,
        String email,
        TipoDocumento documentType,
        String documento,
        String cellphone,
        boolean active,
        long ticketsPurchased
) {
}
