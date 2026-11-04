package com.backend.eventia.dto;

public record RegisterRequestDto(
        String firstName,
        String lastName,
        String dni,
        String email,
        String password
) {
}
