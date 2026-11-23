package com.backend.eventia.config;

import com.backend.eventia.security.WebSocketAuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketAuthInterceptor webSocketAuthInterceptor;

    @Value("${app.cors.allowed-origins}")
    private String[] allowedOriginPatterns;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Habilita el broker en memoria para los canales de suscripción en tiempo real
        registry.enableSimpleBroker("/topic", "/queue");
        // Prefijo para los mensajes cliente -> servidor
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(webSocketAuthInterceptor);
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Endpoint STOMP con SockJS
        registry.addEndpoint("/ws-eventia")
                .setAllowedOriginPatterns(allowedOriginPatterns)
                .withSockJS();

        // Endpoint STOMP nativo sin SockJS (para librerías modernas como @stomp/stompjs)
        registry.addEndpoint("/ws-eventia")
                .setAllowedOriginPatterns(allowedOriginPatterns);
    }
}

