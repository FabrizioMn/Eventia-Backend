package com.backend.eventia.dto.event;

import com.backend.eventia.model.enums.EventStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;

public record EventRequestDto(

        @NotBlank(message = "El titulo es obligatorio")
        @Size(max = 150, message = "El titulo no puede superar los 150 caracteres")
        String title,

        @Size(max = 1000, message = "La descripcion no puede superar los 1000 caracteres")
        String description,

        @NotNull(message = "La fecha es obligatoria")
        LocalDate date,

        @NotNull(message = "La hora de inicio es obligatoria")
        LocalTime startTime,

        @NotNull(message = "La hora de fin es obligatoria")
        LocalTime endTime,

        @NotBlank(message = "La ubicacion es obligatoria")
        @Size(max = 255, message = "La ubicacion no puede superar los 255 caracteres")
        String location,

        @NotNull(message = "La capacidad es obligatoria")
        @Positive(message = "La capacidad debe ser mayor a 0")
        Integer capacity,

        @NotNull(message = "La categoria es obligatoria")
        @Positive(message = "La categoria debe ser valida")
        Long categoryId,

        @Size(max = 500, message = "La URL del banner no puede superar los 500 caracteres")
        String bannerUrl,

        EventStatus status
) {
    public EventRequestDto(String title, String description, LocalDate date, LocalTime startTime, LocalTime endTime,
                           String location, Integer capacity, Long categoryId, String bannerUrl) {
        this(title, description, date, startTime, endTime, location, capacity, categoryId, bannerUrl, null);
    }
}