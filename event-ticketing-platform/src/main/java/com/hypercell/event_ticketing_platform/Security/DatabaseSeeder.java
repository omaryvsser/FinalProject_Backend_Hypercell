package com.hypercell.event_ticketing_platform.Security;

import com.hypercell.event_ticketing_platform.Entity.UserEntity;
import com.hypercell.event_ticketing_platform.Enum.UserRole;
import com.hypercell.event_ticketing_platform.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class DatabaseSeeder {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner seedDatabase() {
        return args -> {
            // Check if an admin already exists to prevent duplicate creation on restarts
            if (!userRepository.existsByEmail("admin@hypercell.com")) {
                UserEntity admin = UserEntity.builder()
                        .username("admin_user")
                        .email("admin@hypercell.com")
                        .password(passwordEncoder.encode("admin123")) // Securely hash the password
                        .role(UserRole.ADMIN) // Assign the ADMIN enum
                        .build();

                userRepository.save(admin);
                System.out.println("✅ Default Admin user created successfully!");
            }
        };
    }
}