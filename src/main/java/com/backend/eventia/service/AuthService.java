package com.backend.eventia.service;

import com.backend.eventia.dto.auth.AuthResponseDto;
import com.backend.eventia.dto.auth.LoginRequestDto;
import com.backend.eventia.dto.auth.RegisterRequestDto;

public interface AuthService {
    AuthResponseDto login(LoginRequestDto request);
    AuthResponseDto register(RegisterRequestDto request);
}
