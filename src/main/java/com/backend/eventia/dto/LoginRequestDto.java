package com.backend.eventia.dto;

public record LoginRequestDto(
        String email,
        String password
) {
}
