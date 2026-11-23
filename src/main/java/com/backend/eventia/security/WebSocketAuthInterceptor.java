package com.backend.eventia.security;

import com.backend.eventia.model.User;
import com.backend.eventia.model.enums.UserRole;
import com.backend.eventia.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.security.Principal;

/**
 * Autentica el handshake STOMP con el JWT y comprueba que cada organizador
 * solo se suscriba a su propio topic de dashboard.
 */
@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            return message;
        }

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String header = accessor.getFirstNativeHeader("Authorization");
            if (header == null || !header.startsWith("Bearer ")) {
                throw new AccessDeniedException("Se requiere un token Bearer para conectar");
            }

            String token = header.substring(7);
            String email = jwtUtils.extractUsername(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            if (!userDetails.isEnabled() || !jwtUtils.isTokenValid(token, userDetails)) {
                throw new AccessDeniedException("Token inválido o usuario desactivado");
            }

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            accessor.setUser(authentication);
            return message;
        }

        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            String destination = accessor.getDestination();
            Principal principal = accessor.getUser();
            if (destination == null || !destination.startsWith("/topic/organizers/")) {
                return message;
            }
            if (principal == null) {
                throw new AccessDeniedException("Suscripción sin autenticar");
            }

            String[] parts = destination.split("/");
            User user = userRepository.findByEmail(principal.getName())
                    .orElseThrow(() -> new AccessDeniedException("Usuario no encontrado"));
            boolean isAdmin = user.getRole() == UserRole.ADMIN;
            boolean isOwner = parts.length > 3 && parts[3].equals(String.valueOf(user.getId()));
            if (!isAdmin && !isOwner) {
                throw new AccessDeniedException("No puedes suscribirte al dashboard de otro organizador");
            }
        }

        return message;
    }
}
