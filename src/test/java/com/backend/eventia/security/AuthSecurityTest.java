package com.backend.eventia.security;

import com.backend.eventia.model.User;
import com.backend.eventia.model.enums.UserRole;
import com.backend.eventia.repository.UserRepository;
import com.backend.eventia.support.TestUserFactory;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    @Test
    void loginCorrectoDevuelveTokenConClaims() throws Exception {
        User user = TestUserFactory.createUser(userRepository, passwordEncoder,
                "admin@test.com", "secret123", UserRole.ADMIN, true);

        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson("admin@test.com", "secret123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();

        String token = JsonPath.read(result.getResponse().getContentAsString(), "$.token");
        assertThat(jwtUtils.extractRole(token)).isEqualTo("ADMIN");
        assertThat(jwtUtils.extractUserId(token)).isEqualTo(user.getId());
    }

    @Test
    void loginConContrasenaIncorrectaDevuelve401() throws Exception {
        TestUserFactory.createUser(userRepository, passwordEncoder,
                "user@test.com", "secret123", UserRole.USER, true);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson("user@test.com", "badpass")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void peticionSinTokenAMeDevuelve401ConJson() throws Exception {
        mockMvc.perform(get("/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.path").value("/auth/me"));
    }

    @Test
    void tokenAlteradoDevuelve401() throws Exception {
        TestUserFactory.createUser(userRepository, passwordEncoder,
                "user2@test.com", "secret123", UserRole.USER, true);

        MvcResult login = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson("user2@test.com", "secret123")))
                .andExpect(status().isOk())
                .andReturn();

        String token = JsonPath.read(login.getResponse().getContentAsString(), "$.token");
        String altered = token.substring(0, token.length() - 2) + (token.endsWith("a") ? "b" : "a");

        mockMvc.perform(get("/auth/me").header("Authorization", "Bearer " + altered))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenDeUsuarioDesactivadoDevuelve401() throws Exception {
        User user = TestUserFactory.createUser(userRepository, passwordEncoder,
                "disabled@test.com", "secret123", UserRole.USER, true);

        MvcResult login = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson("disabled@test.com", "secret123")))
                .andExpect(status().isOk())
                .andReturn();

        String token = JsonPath.read(login.getResponse().getContentAsString(), "$.token");
        user.setActive(false);
        userRepository.saveAndFlush(user);

        mockMvc.perform(get("/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    private String loginJson(String email, String password) {
        return "{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}";
    }
}
