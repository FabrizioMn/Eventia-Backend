package com.backend.eventia.dto.tickettype;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record TicketTypeRequestDto(

        @NotBlank(message = "El nombre de la tarifa es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
        String name,

        @Size(max = 100, message = "La zona no puede superar los 100 caracteres")
        String zone,

        @NotNull(message = "El precio es obligatorio")
        @PositiveOrZero(message = "El precio debe ser mayor o igual a 0")
        Double price,

        @NotNull(message = "La capacidad es obligatoria")
        @Positive(message = "La capacidad debe ser mayor a 0")
        Integer totalCapacity,

        @Positive(message = "El límite de compra debe ser mayor a 0")
        Integer maxPerPurchase,

        Boolean isPresale,

        LocalDate saleStartDate,

        LocalDate saleEndDate
) {
}

