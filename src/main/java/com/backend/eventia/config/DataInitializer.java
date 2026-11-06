package com.backend.eventia.config;

import com.backend.eventia.model.Category;
import com.backend.eventia.model.Event;
import com.backend.eventia.model.TicketType;
import com.backend.eventia.model.User;
import com.backend.eventia.model.enums.UserRole;
import com.backend.eventia.repository.CategoryRepository;
import com.backend.eventia.repository.EventRepository;
import com.backend.eventia.repository.TicketTypeRepository;
import com.backend.eventia.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner initUsers() {
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
            // Datos de ejemplo para que el catálogo (GET /events) no se vea vacío.
            // Solo se crean si la tabla de eventos está vacía.
            if (eventRepository.count() == 0) {
                log.info("[DataInitializer] Creando categorías y eventos de ejemplo");
                seedCatalog();
            }
        };
    }

    private void seedCatalog() {
        User organizer = userRepository.findByEmail("admin@eventia.com").orElseThrow();

        Category conciertos = saveCategory("Conciertos", "Shows musicales en vivo");
        Category festivales = saveCategory("Festivales", "Festivales de varios días y artistas");
        Category teatro = saveCategory("Teatro & Artes", "Obras de teatro y espectáculos culturales");
        Category gastronomia = saveCategory("Gastronomía & Ferias", "Ferias gastronómicas y food trucks");
        Category tecnologia = saveCategory("Tecnología & Startups", "Conferencias y ferias de tecnología");

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

        log.info("[DataInitializer] Datos de ejemplo creados correctamente");
    }

    private Category saveCategory(String name, String description) {
        return categoryRepository.save(Category.builder()
                .name(name).description(description).active(true).build());
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
                .status("published")
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
}
