package com.backend.eventia.config;

import com.backend.eventia.model.Category;
import com.backend.eventia.model.Event;
import com.backend.eventia.model.TicketType;
import com.backend.eventia.model.User;
import com.backend.eventia.model.enums.EventStatus;
import com.backend.eventia.model.enums.UserRole;
import com.backend.eventia.repository.CategoryRepository;
import com.backend.eventia.repository.EventRepository;
import com.backend.eventia.repository.TicketTypeRepository;
import com.backend.eventia.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
@Conditional(DataInitializer.SeedEnabledCondition.class)
public class DataInitializer {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner initData() {
        return args -> {
            if (userRepository.findByEmail("admin@eventia.com").isEmpty()) {
                log.info("[DataInitializer] Creando usuario admin por defecto");
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

            seedCategories();

            if (eventRepository.count() == 0) {
                log.info("[DataInitializer] Creando eventos y tipos de entrada de ejemplo");
                seedCatalog();
            }
        };
    }

    private void seedCategories() {
        List<String[]> initialCategories = List.of(
                new String[] { "Conciertos", "Presentaciones musicales en vivo, festivales y recitales." },
                new String[] { "Festivales", "Grandes encuentros artísticos, gastronómicos y temáticos." },
                new String[] { "Teatro & Artes", "Obras teatrales, comedia, artes escénicas y musicales." },
                new String[] { "Tecnología & Startups",
                        "Conferencias, hackathons, foros tecnológicos y networking." },
                new String[] { "Gastronomía & Ferias",
                        "Ferias culinarias representativas y catas gastronómicas." });

        for (String[] category : initialCategories) {
            getOrCreateCategory(category[0], category[1]);
        }
    }

    private void seedCatalog() {
        User organizer = userRepository.findByEmail("organizador@eventia.com").orElseThrow();

        Category conciertos = getOrCreateCategory("Conciertos", "Presentaciones musicales en vivo, festivales y recitales.");
        Category festivales = getOrCreateCategory("Festivales", "Grandes encuentros artísticos, gastronómicos y temáticos.");
        Category teatro = getOrCreateCategory("Teatro & Artes", "Obras teatrales, comedia, artes escénicas y musicales.");
        Category gastronomia = getOrCreateCategory("Gastronomía & Ferias", "Ferias culinarias representativas y catas gastronómicas.");
        Category tecnologia = getOrCreateCategory("Tecnología & Startups", "Conferencias, hackathons, foros tecnológicos y networking.");

        saveEvent("Illapu: 50 Años de Memoria", "Concierto por los 50 años de Illapu",
                LocalDate.of(2026, 11, 22), "Estadio Nacional, Lima", 45000, conciertos, organizer, 120.0);
        saveEvent("Sunsets Sessions Costa Verde", "Festival al atardecer frente al mar",
                LocalDate.of(2026, 12, 6), "Arena 1, San Miguel", 12000, festivales, organizer, 85.0);
        saveEvent("La Ciudad y los Perros (Adaptación)", "Adaptación teatral de la novela",
                LocalDate.of(2026, 11, 28), "Teatro Peruano Japonés", 800, teatro, organizer, 60.0);
        saveEvent("Expo Sabor & Coctelería Andina", "Feria gastronómica con coctelería andina",
                LocalDate.of(2026, 12, 14), "Jockey Club del Perú", 5000, gastronomia, organizer, 45.0);
        saveEvent("Trap & Latin Flow Live Tour", "Gira de trap y latin flow",
                LocalDate.of(2027, 1, 18), "Arena 1, Costa Verde", 15000, conciertos, organizer, 140.0);
        saveEvent("Peru Tech Summit & Fintech 2025", "Cumbre de tecnología y fintech",
                LocalDate.of(2027, 1, 25), "Centro de Convenciones 27 de Enero", 3000, tecnologia, organizer, 210.0);

        log.info("[DataInitializer] Eventos de ejemplo creados correctamente");
    }

    private Category getOrCreateCategory(String name, String description) {
        return categoryRepository.findAll().stream()
                .filter(category -> category.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElseGet(() -> categoryRepository.save(Category.builder()
                        .name(name)
                        .description(description)
                        .active(true)
                        .build()));
    }

    private void saveEvent(String title, String description, LocalDate date, String location,
                           int capacity, Category category, User organizer, double price) {
        Event event = eventRepository.save(Event.builder()
                .title(title)
                .description(description)
                .date(date)
                .startTime(LocalTime.of(20, 0))
                .endTime(LocalTime.of(23, 0))
                .location(location)
                .capacity(capacity)
                .status(EventStatus.PUBLISHED)
                .active(true)
                .category(category)
                .organizer(organizer)
                .build());

        // Un tipo de entrada por evento, con algo de stock vendido (para el % vendido)
        ticketTypeRepository.save(TicketType.builder()
                .name("General")
                .price(price)
                .totalCapacity(capacity)
                .availableCapacity((int) (capacity * 0.3))
                .active(true)
                .event(event)
                .build());
    }

    public static class SeedEnabledCondition implements Condition {

        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            Environment environment = context.getEnvironment();
            String[] activeProfiles = environment.getActiveProfiles();

            boolean devProfileActive = Arrays.asList(activeProfiles).contains("dev")
                    || (activeProfiles.length == 0
                    && ("dev".equals(environment.getProperty("spring.profiles.default"))
                    || Arrays.asList(environment.getDefaultProfiles()).contains("dev")));

            return devProfileActive
                    || environment.getProperty("app.seed.enabled", Boolean.class, false);
        }
    }
}
