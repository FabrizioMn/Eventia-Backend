package com.backend.eventia.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${app.cors.allowed-origins}")
    private String[] allowedOriginPatterns;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Habilita el broker en memoria para canales de difusión en tiempo real
        registry.enableSimpleBroker("/topic");
        // Prefijo para mensajes del cliente hacia el backend (si se requiere)
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Endpoint STOMP con SockJS (fallback para compatibilidad)
        registry.addEndpoint("/ws-eventia")
                .setAllowedOriginPatterns(allowedOriginPatterns)
                .withSockJS();

        // Endpoint STOMP nativo directo (para @stomp/stompjs y clientes modernos)
        registry.addEndpoint("/ws-eventia")
                .setAllowedOriginPatterns(allowedOriginPatterns);
    }
}
