package com.backend.eventia.rules;

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
import com.backend.eventia.security.JwtUtils;
import com.backend.eventia.service.UserService;
import com.backend.eventia.support.TestUserFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class DeactivateAccountTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private TicketTypeRepository ticketTypeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserService userService;

    @Test
    void desactivaCuentaConContrasenaCorrecta() throws Exception {
        User user = TestUserFactory.createUser(userRepository, passwordEncoder,
                "deactiva@test.com", "secret123", UserRole.USER, true, "73000001");
        String token = jwtUtils.generateToken(user);

        mockMvc.perform(patch("/api/users/me/deactivate")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"secret123\"}"))
                .andExpect(status().isNoContent());

        User updated = userRepository.findByEmail("deactiva@test.com").orElseThrow();
        assertThat(updated.isActive()).isFalse();
        assertThat(updated.getDeactivatedAt()).isNotNull();
    }

    @Test
    void rechazaDesactivacionConContrasenaIncorrecta() throws Exception {
        User user = TestUserFactory.createUser(userRepository, passwordEncoder,
                "mala-clave@test.com", "secret123", UserRole.USER, true, "73000002");
        String token = jwtUtils.generateToken(user);

        mockMvc.perform(patch("/api/users/me/deactivate")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"incorrecta\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("La contraseña es incorrecta"));

        User updated = userRepository.findByEmail("mala-clave@test.com").orElseThrow();
        assertThat(updated.isActive()).isTrue();
        assertThat(updated.getDeactivatedAt()).isNull();
    }

    @Test
    void tokenAnteriorDejaDeFuncionarTrasDesactivar() throws Exception {
        User user = TestUserFactory.createUser(userRepository, passwordEncoder,
                "token-viejo@test.com", "secret123", UserRole.USER, true, "73000003");
        String token = jwtUtils.generateToken(user);

        mockMvc.perform(patch("/api/users/me/deactivate")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"secret123\"}"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void organizadorConservaEventosPeroLosOcultaDelCatalogo() throws Exception {
        User organizer = TestUserFactory.createUser(userRepository, passwordEncoder,
                "organizador-deact@test.com", "secret123", UserRole.ORGANIZER, true, "73000004");
        String token = jwtUtils.generateToken(organizer);

        Category category = categoryRepository.save(
                Category.builder().name("Cat Deact").description("d").active(true).build());
        Event event = eventRepository.save(Event.builder()
                .title("Evento Publicado")
                .description("d")
                .date(LocalDate.now().plusDays(10))
                .startTime(LocalTime.of(18, 0))
                .endTime(LocalTime.of(20, 0))
                .location("Lima")
                .capacity(100)
                .status(EventStatus.PUBLISHED)
                .active(true)
                .category(category)
                .organizer(organizer)
                .build());
        TicketType ticketType = ticketTypeRepository.save(TicketType.builder()
                .name("General")
                .zone("General")
                .price(50.0)
                .totalCapacity(100)
                .availableCapacity(80)
                .maxPerPurchase(6)
                .isPresale(false)
                .active(true)
                .event(event)
                .build());

        mockMvc.perform(patch("/api/users/me/deactivate")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"secret123\"}"))
                .andExpect(status().isNoContent());

        Event updated = eventRepository.findById(event.getId()).orElseThrow();
        assertThat(updated.isActive()).isFalse();
        assertThat(updated.getStatus()).isEqualTo(EventStatus.PUBLISHED);

        TicketType unchanged = ticketTypeRepository.findById(ticketType.getId()).orElseThrow();
        assertThat(unchanged.isActive()).isTrue();
        assertThat(unchanged.getAvailableCapacity()).isEqualTo(80);
    }

    @Test
    void reactivarUsuarioLimpiaFechaDeDesactivacion() throws Exception {
        User user = TestUserFactory.createUser(userRepository, passwordEncoder,
                "reactivar@test.com", "secret123", UserRole.USER, true, "73000005");
        userService.deactivateAccount("reactivar@test.com", "secret123");

        User deactivated = userRepository.findByEmail("reactivar@test.com").orElseThrow();
        assertThat(deactivated.isActive()).isFalse();
        assertThat(deactivated.getDeactivatedAt()).isNotNull();

        User reactivated = userService.reactivateUser(deactivated);

        assertThat(reactivated.isActive()).isTrue();
        assertThat(reactivated.getDeactivatedAt()).isNull();
    }
}
