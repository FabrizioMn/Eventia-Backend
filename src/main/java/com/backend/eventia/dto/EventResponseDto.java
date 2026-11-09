package com.backend.eventia.dto;

import java.time.LocalDate;

// Lo mínimo que necesita el catálogo del frontend.
// Precio = el más barato de sus tipos de entrada.
// Porcentaje vendido = calculado con el stock de los tipos de entrada.
public record EventResponseDto(
        Long id,
        String title,
        String description,
        LocalDate date,
        String location,
        Integer capacity,
        String status,
        String category,
        Double price,
        Integer soldPct
) {
}
