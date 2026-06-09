package com.org.order_service.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        log.info("=== JWT Filter invoked for URI: {} ===", request.getRequestURI());

        final String authHeader = request.getHeader("Authorization");
        log.info("Authorization header present: {}", authHeader != null);

        final String jwt;
        final String username;

        // Проверяем, что заголовок есть и начинается с "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("No Bearer token found in request");
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);
        log.info("JWT token extracted (length: {})", jwt.length());

        try {
            username = jwtUtil.extractUsername(jwt);
            log.info("Username extracted from token: {}", username);

            // Если пользователь найден в токене и еще не аутентифицирован в контексте
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                log.info("Loading user details for username: {}", username);
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
                log.info("User loaded successfully: {}", userDetails.getUsername());

                if (jwtUtil.isTokenValid(jwt, userDetails)) {
                    log.info("Token is VALID, setting authentication in SecurityContext");
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.info("Authentication set successfully. Authorities: {}", userDetails.getAuthorities());
                } else {
                    log.warn("Token is INVALID for user: {}", username);
                }
            } else {
                if (username == null) {
                    log.warn("Username is null in token");
                }
                if (SecurityContextHolder.getContext().getAuthentication() != null) {
                    log.info("User already authenticated in SecurityContext");
                }
            }
        } catch (Exception e) {
            log.error("JWT authentication failed: {}", e.getMessage(), e);
        }

        log.info("=== JWT Filter completed, passing to next filter ===");
        filterChain.doFilter(request, response);

    }
}