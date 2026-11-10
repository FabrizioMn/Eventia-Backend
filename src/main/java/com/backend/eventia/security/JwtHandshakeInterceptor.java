package com.backend.eventia.security;

import com.backend.eventia.service.impl.UserDetailsServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtUtils jwtUtils;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes
    ) {
        HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest();

        // 1. Intentar obtener el token del query param ?token= o del header Authorization
        String token = servletRequest.getParameter("token");
        if (token == null || token.isBlank()) {
            String authHeader = servletRequest.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            }
        }

        if (token != null && !token.isBlank()) {
            try {
                String userEmail = jwtUtils.extractUsername(token);
                if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
                    if (jwtUtils.isTokenValid(token, userDetails)) {
                        UsernamePasswordAuthenticationToken authToken =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails, null, userDetails.getAuthorities()
                                );
                        SecurityContextHolder.getContext().setAuthentication(authToken);

                        // Guardar datos en los atributos de la sesión STOMP
                        attributes.put("user", userDetails);
                        attributes.put("authentication", authToken);

                        // Extraer y guardar el ID de usuario del token para autorización de topics
                        Long userId = jwtUtils.extractUserId(token);
                        if (userId != null) {
                            attributes.put("userId", userId);
                        }

                        log.debug("[WS] Handshake autenticado para usuario='{}' id={}", userEmail, userId);
                        return true;
                    }
                }
            } catch (Exception ex) {
                log.warn("[WS] Token inválido en handshake: {}", ex.getMessage());
            }
        }

        // Permitir el handshake incluso sin token (el autorización se valida en el canal de entrada)
        return true;
    }

    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception
    ) {
        // No-op: limpieza no necesaria por ser stateless
    }
}
