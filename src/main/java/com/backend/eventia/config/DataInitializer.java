package com.backend.eventia.config;

import com.backend.eventia.model.Category;
import com.backend.eventia.model.User;
import com.backend.eventia.model.enums.UserRole;
import com.backend.eventia.repository.CategoryRepository;
import com.backend.eventia.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner initData() {
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

            if (userRepository.findByEmail("organizador@eventia.com").isEmpty()) {
                userRepository.save(User.builder()
                        .firstName("Carlos")
                        .lastName("Organizador")
                        .dni("20000002")
                        .email("organizador@eventia.com")
                        .cellphone("987654321")
                        .password(passwordEncoder.encode("organizer123"))
                        .role(UserRole.ORGANIZER)
                        .build());
            }

            if (categoryRepository.count() == 0) {
                List<String[]> initialCategories = List.of(
                        new String[] { "Conciertos", "Presentaciones musicales en vivo, festivales y recitales." },
                        new String[] { "Festivales", "Grandes encuentros artísticos, gastronómicos y temáticos." },
                        new String[] { "Teatro & Artes", "Obras teatrales, comedia, artes escénicas y musicales." },
                        new String[] { "Tecnología & Startups",
                                "Conferencias, hackathons, foros tecnológicos y networking." },
                        new String[] { "Gastronomía & Ferias",
                                "Ferias culinarias representativas y catas gastronómicas." });

                for (String[] cat : initialCategories) {
                    categoryRepository.save(Category.builder()
                            .name(cat[0])
                            .description(cat[1])
                            .active(true)
                            .build());
                }
            }
        };
    }
}
