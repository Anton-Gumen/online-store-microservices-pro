package com.org.order_service.service;

import com.org.order_service.dto.AuthResponse;
import com.org.order_service.dto.LoginRequest;
import com.org.order_service.dto.RegisterRequest;
import com.org.order_service.entity.Role;
import com.org.order_service.entity.User;
import com.org.order_service.repository.UserRepository;
import com.org.order_service.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    public AuthResponse register(RegisterRequest request) {

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .role(Role.ROLE_USER)
                .build();
        userRepository.save(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        return AuthResponse.builder()
                .accessToken(jwtUtil.generateToken(userDetails))
                .refreshToken(jwtUtil.generateRefreshToken(userDetails))
                .build();
    }

    public AuthResponse login(LoginRequest request) {

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(),
                request.getPassword()));

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        return AuthResponse.builder()
                .accessToken(jwtUtil.generateToken(userDetails))
                .refreshToken(jwtUtil.generateRefreshToken(userDetails))
                .build();
    }

    public AuthResponse refresh(String refreshToken) {

        String username = jwtUtil.extractUsername(refreshToken);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        if (jwtUtil.isTokenValid(refreshToken, userDetails)) {
            return AuthResponse.builder()
                    .accessToken(jwtUtil.generateToken(userDetails))
                    .refreshToken(jwtUtil.generateRefreshToken(userDetails))
                    .build();
        }
        throw new RuntimeException("Invalid refresh token");
    }


}
