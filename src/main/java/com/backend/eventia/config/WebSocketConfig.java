package com.backend.eventia.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Habilita el broker en memoria para los canales de suscripción en tiempo real
        registry.enableSimpleBroker("/topic", "/queue");
        // Prefijo para los mensajes cliente -> servidor
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Endpoint STOMP con SockJS
        registry.addEndpoint("/ws-eventia")
                .setAllowedOriginPatterns("http://localhost:*", "http://127.0.0.1:*")
                .withSockJS();

        // Endpoint STOMP nativo sin SockJS (para librerías modernas como @stomp/stompjs)
        registry.addEndpoint("/ws-eventia")
                .setAllowedOriginPatterns("http://localhost:*", "http://127.0.0.1:*");
    }
}

