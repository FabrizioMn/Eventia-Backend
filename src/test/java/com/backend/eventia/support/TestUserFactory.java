package com.backend.eventia.support;

import com.backend.eventia.model.User;
import com.backend.eventia.model.enums.UserRole;
import com.backend.eventia.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

public final class TestUserFactory {

    private TestUserFactory() {
    }

    public static User createUser(UserRepository userRepository, PasswordEncoder passwordEncoder,
                                  String email, String rawPassword, UserRole role, boolean active) {
        return createUser(userRepository, passwordEncoder, email, rawPassword, role, active, dniFor(role, active));
    }

    public static User createUser(UserRepository userRepository, PasswordEncoder passwordEncoder,
                                  String email, String rawPassword, UserRole role, boolean active, String dni) {
        User user = User.builder()
                .firstName(role.name())
                .lastName("Test")
                .dni(dni)
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .role(role)
                .active(active)
                .build();
        return userRepository.save(user);
    }

    private static String dniFor(UserRole role, boolean active) {
        String suffix = active ? "1" : "0";
        return switch (role) {
            case ORGANIZER -> "9000001" + suffix;
            case USER -> "9000002" + suffix;
        };
    }
}
