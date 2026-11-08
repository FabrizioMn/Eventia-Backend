package com.backend.eventia.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.stream.Collectors;

public final class SecurityLogUtils {

    private SecurityLogUtils() {
    }

    public static String username(Authentication authentication) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return "anonimo";
        }
        return authentication.getName();
    }

    public static String roles(Authentication authentication) {
        if (authentication == null || authentication.getAuthorities() == null) {
            return "[]";
        }

        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .sorted()
                .collect(Collectors.joining(", ", "[", "]"));
    }

    public static String requestLine(HttpServletRequest request) {
        return request.getMethod() + " " + request.getRequestURI();
    }
}
