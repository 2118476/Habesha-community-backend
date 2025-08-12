package com.habesha.community.config;

import com.habesha.community.model.Role;
import com.habesha.community.model.User;
import com.habesha.community.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Inserts initial data into the database on application startup.  This
 * class creates a handful of demo users covering different roles.
 * In a production deployment you might remove this and instead
 * manage data via migrations.
 */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() > 0) return; // Only seed on fresh database
        User admin = User.builder()
                .name("Admin")
                .email("admin@habesha.com")
                .password(passwordEncoder.encode("password"))
                .role(Role.ADMIN)
                .city("London")
                .phone("+447000000001")
                .active(true)
                .build();
        User provider = User.builder()
                .name("Service Provider")
                .email("provider@habesha.com")
                .password(passwordEncoder.encode("password"))
                .role(Role.SERVICE_PROVIDER)
                .city("London")
                .phone("+447000000002")
                .active(true)
                .build();
        User user = User.builder()
                .name("Regular User")
                .email("user@habesha.com")
                .password(passwordEncoder.encode("password"))
                .role(Role.USER)
                .city("Addis Ababa")
                .phone("+251911000001")
                .active(true)
                .build();
        userRepository.save(admin);
        userRepository.save(provider);
        userRepository.save(user);
    }
}