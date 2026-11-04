package com.backend.eventia.service;

import com.backend.eventia.dto.AuthResponseDto;
import com.backend.eventia.dto.LoginRequestDto;
import com.backend.eventia.dto.RegisterRequestDto;

public interface AuthService {
    AuthResponseDto login(LoginRequestDto request);
    AuthResponseDto register(RegisterRequestDto request);
}
