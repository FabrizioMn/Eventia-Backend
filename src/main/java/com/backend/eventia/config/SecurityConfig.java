package com.backend.eventia.config;

import com.backend.eventia.security.JwtAccessDeniedHandler;
import com.backend.eventia.security.JwtAuthenticationEntryPoint;
import com.backend.eventia.security.JwtAuthenticationFilter;
import com.backend.eventia.security.RequestLoggingFilter;
import com.backend.eventia.service.impl.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RequestLoggingFilter requestLoggingFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final Environment environment;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) {

        return httpSecurity
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(http -> {
                    // Endpoints públicos
                    http.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();
                    http.requestMatchers("/auth/login", "/auth/register").permitAll();
                    http.requestMatchers(HttpMethod.GET,
                                    "/events", "/events/**",
                                    "/categories", "/categories/**",
                                    "/ticket-types", "/ticket-types/**",
                                    "/ticket_types", "/ticket_types/**")
                            .permitAll();
                    if (isDevProfileActive()) {
                        http.requestMatchers("/h2-console/**").permitAll();
                    }
                    http.requestMatchers("/error").permitAll();

                    // Administración y dashboard del organizador
                    http.requestMatchers("/api/admin/**").hasRole("ADMIN");
                    http.requestMatchers("/api/organizer/**").hasRole("ORGANIZER");

                    // Escritura de categorías
                    http.requestMatchers(HttpMethod.POST, "/api/categories/**").hasRole("ADMIN");
                    http.requestMatchers(HttpMethod.PUT, "/api/categories/**").hasRole("ADMIN");
                    http.requestMatchers(HttpMethod.PATCH, "/api/categories/**").hasRole("ADMIN");
                    http.requestMatchers(HttpMethod.DELETE, "/api/categories/**").hasRole("ADMIN");

                    // Escritura de eventos
                    http.requestMatchers(HttpMethod.POST, "/api/events").hasAnyRole("ORGANIZER", "ADMIN");
                    http.requestMatchers(HttpMethod.PUT, "/api/events/**").hasAnyRole("ORGANIZER", "ADMIN");
                    http.requestMatchers(HttpMethod.PATCH, "/api/events/**").hasAnyRole("ORGANIZER", "ADMIN");
                    http.requestMatchers(HttpMethod.DELETE, "/api/events/**").hasAnyRole("ORGANIZER", "ADMIN");

                    // Escritura de tipos de entrada
                    http.requestMatchers(HttpMethod.POST,
                                    "/api/events/*/ticket-types", "/api/events/*/ticket-types/**")
                            .hasAnyRole("ORGANIZER", "ADMIN");
                    http.requestMatchers(HttpMethod.PUT, "/api/events/*/ticket-types/**")
                            .hasAnyRole("ORGANIZER", "ADMIN");
                    http.requestMatchers(HttpMethod.PATCH, "/api/events/*/ticket-types/**")
                            .hasAnyRole("ORGANIZER", "ADMIN");
                    http.requestMatchers(HttpMethod.DELETE, "/api/events/*/ticket-types/**")
                            .hasAnyRole("ORGANIZER", "ADMIN");
                    http.requestMatchers(HttpMethod.POST, "/api/ticket-types/**")
                            .hasAnyRole("ORGANIZER", "ADMIN");
                    http.requestMatchers(HttpMethod.PUT, "/api/ticket-types/**")
                            .hasAnyRole("ORGANIZER", "ADMIN");
                    http.requestMatchers(HttpMethod.PATCH, "/api/ticket-types/**")
                            .hasAnyRole("ORGANIZER", "ADMIN");
                    http.requestMatchers(HttpMethod.DELETE, "/api/ticket-types/**")
                            .hasAnyRole("ORGANIZER", "ADMIN");

                    // Cualquier otro endpoint requiere autenticación
                    http.anyRequest().authenticated();
                })
                .headers(h -> h.frameOptions(f -> f.sameOrigin()))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(requestLoggingFilter, JwtAuthenticationFilter.class)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(jwtAccessDeniedHandler))
                .build();
    }

    private boolean isDevProfileActive() {
        String[] activeProfiles = environment.getActiveProfiles();
        return Arrays.asList(activeProfiles).contains("dev")
                || (activeProfiles.length == 0
                && ("dev".equals(environment.getProperty("spring.profiles.default"))
                || Arrays.asList(environment.getDefaultProfiles()).contains("dev")));
    }


    @Bean
    public FilterRegistrationBean<RequestLoggingFilter> requestLoggingFilterRegistration(RequestLoggingFilter filter) {
        // Se ejecuta dentro de la cadena de seguridad, no como filtro adicional del contenedor.
        FilterRegistrationBean<RequestLoggingFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }


    @Bean
    public AuthenticationProvider authenticationProvider(UserDetailsServiceImpl userDetailsService) {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider(userDetailsService);
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        return daoAuthenticationProvider;
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
