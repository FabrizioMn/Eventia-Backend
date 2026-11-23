package com.backend.eventia.service;

import com.backend.eventia.dto.auth.AuthResponseDto;
import com.backend.eventia.dto.auth.AuthUserResponseDto;
import com.backend.eventia.dto.auth.LoginRequestDto;
import com.backend.eventia.dto.auth.ProfileUpdateResponseDto;
import com.backend.eventia.dto.auth.RegisterRequestDto;
import com.backend.eventia.dto.auth.UpdateProfileRequestDto;

public interface AuthService {
    AuthResponseDto login(LoginRequestDto request);
    AuthResponseDto register(RegisterRequestDto request);
    AuthUserResponseDto getCurrentUser(String email);
    ProfileUpdateResponseDto updateProfile(String email, UpdateProfileRequestDto request);
}
