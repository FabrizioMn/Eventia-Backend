package com.backend.eventia.config;

import com.backend.eventia.security.JwtAuthenticationFilter;
import com.backend.eventia.service.impl.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) {

        return httpSecurity
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(http -> {
                    //Configurar endpoints publicos
                    http.requestMatchers("/auth/login", "/auth/register").permitAll();
                    http.requestMatchers("/h2-console/**").permitAll();
                    http.requestMatchers("/error").permitAll();

                    // El catálogo de eventos es publico
                    http.requestMatchers(HttpMethod.GET, "/api/events/organizer").hasAnyRole("ADMIN", "ORGANIZER");
                    http.requestMatchers(HttpMethod.GET, "/api/events", "/api/events/*").permitAll();
                    http.requestMatchers(HttpMethod.GET, "/api/events/*/ticket-types").permitAll();

                    // Órdenes y tickets del usuario autenticado
                    http.requestMatchers(HttpMethod.POST, "/api/orders").authenticated();
                    http.requestMatchers(HttpMethod.GET, "/api/tickets/my").authenticated();

                    // Escritura de eventos
                    http.requestMatchers(HttpMethod.POST, "/api/events")
                            .hasAnyRole("ADMIN", "ORGANIZER");

                    http.requestMatchers(HttpMethod.PUT, "/api/events/*")
                            .hasAnyRole("ADMIN", "ORGANIZER");

                    http.requestMatchers(HttpMethod.PATCH, "/api/events/*/status", "/api/events/*/active")
                            .hasAnyRole("ADMIN", "ORGANIZER");

                    // Ticket types: lectura pública para eventos publicados, escritura solo ADMIN/ORGANIZER
                    http.requestMatchers(HttpMethod.POST, "/api/events/*/ticket-types")
                            .hasAnyRole("ADMIN", "ORGANIZER");
                    http.requestMatchers(HttpMethod.PUT, "/api/ticket-types/*")
                            .hasAnyRole("ADMIN", "ORGANIZER");
                    http.requestMatchers(HttpMethod.PATCH, "/api/ticket-types/*/status")
                            .hasAnyRole("ADMIN", "ORGANIZER");
                    http.requestMatchers(HttpMethod.DELETE, "/api/ticket-types/*")
                            .hasAnyRole("ADMIN", "ORGANIZER");

                    // Categorias: lectura publica, escritura solo ADMIN
                    http.requestMatchers(HttpMethod.GET, "/api/categories/admin").hasRole("ADMIN");
                    http.requestMatchers(HttpMethod.GET, "/api/categories", "/api/categories/*").permitAll();
                    http.requestMatchers(HttpMethod.POST, "/api/categories").hasRole("ADMIN");
                    http.requestMatchers(HttpMethod.PUT, "/api/categories/*").hasRole("ADMIN");
                    http.requestMatchers(HttpMethod.PATCH, "/api/categories/*/active").hasRole("ADMIN");

                    // Paneles de control
                    http.requestMatchers("/api/admin/**").hasRole("ADMIN");
                    http.requestMatchers("/api/organizer/**").hasAnyRole("ADMIN", "ORGANIZER");

                    //Configurar endpoints restantes no especificados
                    http.anyRequest().authenticated();
                })
                .headers(h -> h.frameOptions(f -> f.sameOrigin()))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .build();
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
