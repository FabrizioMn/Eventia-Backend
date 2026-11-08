package com.backend.eventia.dto;

import com.backend.eventia.model.enums.EventStatus;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;

public record EventRequestDto(
        @NotBlank(message = "El titulo es obligatorio")
        @Size(max = 200, message = "El titulo no debe superar 200 caracteres")
        String title,

        @NotBlank(message = "La descripcion es obligatoria")
        String description,

        @NotNull(message = "La fecha es obligatoria")
        @FutureOrPresent(message = "La fecha no puede ser anterior a hoy")
        LocalDate date,

        @NotNull(message = "La hora de inicio es obligatoria")
        LocalTime startTime,

        @NotNull(message = "La hora de fin es obligatoria")
        LocalTime endTime,

        @NotBlank(message = "La ubicacion es obligatoria")
        @Size(max = 255, message = "La ubicacion no debe superar 255 caracteres")
        String location,

        @Size(max = 120, message = "La ciudad no debe superar 120 caracteres")
        String city,

        @NotNull(message = "La capacidad es obligatoria")
        @Min(value = 1, message = "La capacidad debe ser mayor a 0")
        Integer capacity,

        @NotNull(message = "La categoria es obligatoria")
        Long categoryId,

        EventStatus status
) {
}
