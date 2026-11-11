package com.backend.eventia.rules;

import com.backend.eventia.model.User;
import com.backend.eventia.model.enums.UserRole;
import com.backend.eventia.repository.UserRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class RegisterAuthTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void registroClienteExitoso() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson("DNI", "70111222", "cliente@test.com", "987654321", "CLIENTE")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.user.role").value("USER"))
                .andExpect(jsonPath("$.user.documentType").value("DNI"))
                .andExpect(jsonPath("$.user.cellphone").value("987654321"));

        User saved = userRepository.findByEmail("cliente@test.com").orElseThrow();
        assertThat(saved.getRole()).isEqualTo(UserRole.USER);
        assertThat(saved.getDocumentType().name()).isEqualTo("DNI");
    }

    @Test
    void registroOrganizadorExitoso() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson("DNI", "70222333", "organizador@test.com", "987654322", "ORGANIZADOR")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.user.role").value("ORGANIZER"));

        User saved = userRepository.findByEmail("organizador@test.com").orElseThrow();
        assertThat(saved.getRole()).isEqualTo(UserRole.ORGANIZER);
    }

    @Test
    void rolInvalidoAdminDevuelve400() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson("DNI", "70333444", "admin-intento@test.com", "987654323", "ADMIN")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("El rol debe ser CLIENTE u ORGANIZADOR"));
    }

    @Test
    void correoDuplicadoDevuelve409() throws Exception {
        TestUserFactory.createUser(userRepository, passwordEncoder,
                "existente@test.com", "secret123", UserRole.USER, true, "70444555");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson("DNI", "70555666", "existente@test.com", "987654324", "CLIENTE")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("El correo ya está registrado."));
    }

    @Test
    void correoDeCuentaInactivaIndicaReactivacion() throws Exception {
        TestUserFactory.createUser(userRepository, passwordEncoder,
                "inactivo@test.com", "secret123", UserRole.USER, false, "70666777");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson("DNI", "70777888", "inactivo@test.com", "987654325", "CLIENTE")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(
                        "El correo ya está registrado en una cuenta inactiva. Puedes reactivar tu cuenta."));
    }

    @Test
    void documentoDuplicadoDevuelve409() throws Exception {
        TestUserFactory.createUser(userRepository, passwordEncoder,
                "otro@test.com", "secret123", UserRole.USER, true, "70888999");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson("DNI", "70888999", "nuevo@test.com", "987654326", "CLIENTE")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("El número de documento ya está registrado."));
    }

    @Test
    void dniInvalidoDevuelve400() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson("DNI", "1234567", "dni-invalido@test.com", "987654327", "CLIENTE")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void ceValidoRegistraCorrectamente() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson("CE", "CE1234567", "ce@test.com", "987654328", "CLIENTE")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.user.documentType").value("CE"));

        User saved = userRepository.findByEmail("ce@test.com").orElseThrow();
        assertThat(saved.getDocumentType().name()).isEqualTo("CE");
        assertThat(saved.getDni()).isEqualTo("CE1234567");
    }

    private String registerJson(String documentType, String documento, String email,
                                String cellphone, String role) {
        return "{\"firstName\":\"Ana\",\"lastName\":\"Perez\",\"documentType\":\"" + documentType + "\","
                + "\"dni\":\"" + documento + "\",\"email\":\"" + email + "\","
                + "\"cellphone\":\"" + cellphone + "\",\"password\":\"secret123\","
                + "\"role\":\"" + role + "\"}";
    }
}
