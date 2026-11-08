package com.backend.eventia.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final String REQUEST_ID_MDC_KEY = "requestId";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String requestId = UUID.randomUUID().toString();
        MDC.put(REQUEST_ID_MDC_KEY, requestId);
        long startNanos = System.nanoTime();

        try {
            filterChain.doFilter(request, response);
        } finally {
            long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            log.info("[HTTP] {} {} -> {} | {} ms | user={} roles={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus(),
                    durationMs,
                    SecurityLogUtils.username(authentication),
                    SecurityLogUtils.roles(authentication));

            MDC.remove(REQUEST_ID_MDC_KEY);
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        return path.startsWith("/h2-console")
                || path.startsWith("/css/")
                || path.startsWith("/js/")
                || path.startsWith("/images/")
                || path.startsWith("/fonts/")
                || path.startsWith("/assets/")
                || path.startsWith("/static/")
                || path.startsWith("/webjars/")
                || path.equals("/favicon.ico");
    }
}
