package com.backend.eventia.config;

import com.backend.eventia.model.User;
import com.backend.eventia.model.enums.UserRole;
import com.backend.eventia.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner initUsers() {
        return args -> {
            if (userRepository.findByEmail("admin@eventia.com").isEmpty()) {
                userRepository.save(User.builder()
                        .firstName("Admin")
                        .lastName("Eventia")
                        .dni("10000001")
                        .email("admin@eventia.com")
                        .cellphone("123456789")
                        .password(passwordEncoder.encode("admin123"))
                        .role(UserRole.ADMIN)
                        .build());
            }
        };
    }
}
