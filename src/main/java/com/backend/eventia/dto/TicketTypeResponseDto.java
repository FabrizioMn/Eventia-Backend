package com.backend.eventia.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TicketTypeResponseDto(
        String id,
        @JsonProperty("id_event") Long eventId,
        String name,
        Double price,
        Integer stock
) {
}
