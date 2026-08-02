package com.hypercell.event_ticketing_platform.Service;

import com.hypercell.event_ticketing_platform.DTO.AuthDtos.*;
import com.hypercell.event_ticketing_platform.Entity.UserEntity;
import com.hypercell.event_ticketing_platform.Enum.UserRole;
import com.hypercell.event_ticketing_platform.Exception.UserAlreadyExistsException;
import com.hypercell.event_ticketing_platform.Repository.UserRepository;
import com.hypercell.event_ticketing_platform.Security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {
        // 1. Proactively check if the email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("A user with this email already exists.");
        }

        // 2. Proactively check if the username already exists
        if (userRepository.existsByUsername(request.getName())) {
            throw new UserAlreadyExistsException("This username is already taken.");
        }

        var user = UserEntity.builder()
                .username(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.CUSTOMER) // Default role
                .build();
        userRepository.save(user);

        var userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build();

        var jwtToken = jwtService.generateToken(userDetails);
        return AuthResponse.builder().token(jwtToken).build();
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow();

        var userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRole() != null ? user.getRole().name() : "USER")
                .build();

        var jwtToken = jwtService.generateToken(userDetails);
        return AuthResponse.builder().token(jwtToken).build();
    }
}