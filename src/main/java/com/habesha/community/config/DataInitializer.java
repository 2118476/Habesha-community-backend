package com.habesha.community.config;

import com.habesha.community.model.Role;
import com.habesha.community.model.User;
import com.habesha.community.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Seed by email (safer than count()>0)
        seed("admin@habesha.com", "Admin", Role.ADMIN, "London", "+447000000001", "password");
        seed("provider@habesha.com", "Service Provider", Role.SERVICE_PROVIDER, "London", "+447000000002", "password");
        seed("user@habesha.com", "Regular User", Role.USER, "Addis Ababa", "+251911000001", "password");
    }

    private void seed(String email, String name, Role role, String city, String phone, String rawPassword) {
        if (userRepository.findByEmail(email).isPresent()) return;

        String username = email.substring(0, email.indexOf('@'));

        User u = User.builder()
                .name(name)
                .email(email)
                .username(username) // <-- important
                .password(passwordEncoder.encode(rawPassword))
                .role(role)
                .city(city)
                .phone(phone)
                .active(true)
                .build();

        userRepository.save(u);
    }
}
