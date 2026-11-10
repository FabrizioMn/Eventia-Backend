package com.backend.eventia.config;

import com.backend.eventia.security.JwtUtils;
import com.backend.eventia.service.impl.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.MessageHandlingException;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.List;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${app.cors.allowed-origins:http://localhost:5173,http://127.0.0.1:5173,https://eventia-frontend-eight.vercel.app}")
    private String[] allowedOriginPatterns;

    private final JwtUtils jwtUtils;
    private final UserDetailsServiceImpl userDetailsService;

    // ------------------------------------------------------------------
    // Configuración del broker y endpoints
    // ------------------------------------------------------------------
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-eventia")
                .setAllowedOriginPatterns(allowedOriginPatterns)
                .withSockJS();

        registry.addEndpoint("/ws-eventia")
                .setAllowedOriginPatterns(allowedOriginPatterns);
    }

    // ------------------------------------------------------------------
    // Canal de entrada: autenticación JWT en CONNECT y autorización de
    // suscripciones a topics de organizador
    // ------------------------------------------------------------------
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {

            private final AuthenticationTrustResolver trustResolver = new AuthenticationTrustResolverImpl();

            @Override
            public org.springframework.messaging.Message<?> preSend(
                    org.springframework.messaging.Message<?> message,
                    org.springframework.messaging.MessageChannel channel) {

                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                if (accessor == null) return message;

                // --- CONNECT: autenticar JWT y guardar en sesión ---
                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    String token = extractToken(accessor);
                    if (token != null && !token.isBlank()) {
                        try {
                            String userEmail = jwtUtils.extractUsername(token);
                            if (userEmail != null) {
                                UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
                                if (jwtUtils.isTokenValid(token, userDetails)) {
                                    UsernamePasswordAuthenticationToken authToken =
                                            new UsernamePasswordAuthenticationToken(
                                                    userDetails, null, userDetails.getAuthorities()
                                            );
                                    SecurityContextHolder.getContext().setAuthentication(authToken);

                                    accessor.getSessionAttributes().put("authentication", authToken);
                                    accessor.getSessionAttributes().put("user", userDetails);

                                    Long userId = jwtUtils.extractUserId(token);
                                    if (userId != null) {
                                        accessor.getSessionAttributes().put("userId", userId);
                                    }
                                }
                            }
                        } catch (Exception ex) {
                            logWs("Token JWT inválido en CONNECT: " + ex.getMessage());
                        }
                    }
                }

                // Transferir authentication del session al SecurityContextHolder para este hilo
                Authentication auth = (Authentication) accessor.getSessionAttributes().get("authentication");
                if (auth != null && trustResolver.isAuthenticated(auth)) {
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }

                // --- SUBSCRIBE: autorizar topics de organizador ---
                if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
                    String destination = accessor.getDestination();
                    if (destination != null && destination.startsWith("/topic/organizers/")) {
                        Authentication current = SecurityContextHolder.getContext().getAuthentication();
                        if (current == null || !trustResolver.isAuthenticated(current)) {
                            throw new MessageHandlingException(
                                    message, "No autenticado para suscribirse a " + destination);
                        }

                        String[] parts = destination.split("/");
                        if (parts.length >= 4) {
                            String topicOrganizerId = parts[3];
                            Long authenticatedUserId = (Long) accessor.getSessionAttributes().get("userId");
                            if (authenticatedUserId == null || !authenticatedUserId.toString().equals(topicOrganizerId)) {
                                throw new MessageHandlingException(
                                        message,
                                        "No autorizado para el dashboard del organizador " + topicOrganizerId);
                            }
                        }
                    }
                }

                return message;
            }

            @Override
            public void afterSendCompletion(
                    org.springframework.messaging.Message<?> message,
                    org.springframework.messaging.MessageChannel channel,
                    boolean sent,
                    Exception ex) {
                SecurityContextHolder.clearContext();
            }

            private String extractToken(StompHeaderAccessor accessor) {
                // 1. Header Authorization: Bearer <token>
                List<String> authHeader = accessor.getNativeHeader("Authorization");
                if (authHeader != null && !authHeader.isEmpty()) {
                    String value = authHeader.get(0);
                    if (value.startsWith("Bearer ")) {
                        return value.substring(7);
                    }
                }
                // 2. Header token (SockJS query params pueden llegar como native headers)
                List<String> tokenHeader = accessor.getNativeHeader("token");
                if (tokenHeader != null && !tokenHeader.isEmpty()) {
                    return tokenHeader.get(0);
                }
                return null;
            }

            private void logWs(String msg) {
                if (org.springframework.util.StringUtils.hasText(msg)) {
                    org.slf4j.LoggerFactory.getLogger(WebSocketConfig.class).warn("[WS] {}", msg);
                }
            }
        });
    }
}
