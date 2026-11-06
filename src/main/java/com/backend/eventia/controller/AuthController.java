package com.backend.eventia.controller;

import com.backend.eventia.dto.auth.AuthResponseDto;
import com.backend.eventia.dto.auth.LoginRequestDto;
import com.backend.eventia.dto.auth.RegisterRequestDto;
import com.backend.eventia.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDto> register(@Valid @RequestBody RegisterRequestDto request) {
        log.info("[AuthController] POST /auth/register recibido (email={})", request.email());
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginRequestDto request) {
        log.info("[AuthController] POST /auth/login recibido (email={})", request.email());
        return ResponseEntity.ok(authService.login(request));
    }

}
