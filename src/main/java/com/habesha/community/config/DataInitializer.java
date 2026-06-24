package com.habesha.community.config;

import com.habesha.community.model.Role;
import com.habesha.community.model.User;
import com.habesha.community.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        // Seed by email (safer than count()>0)
        seed("admin@habesha.com", "Admin", Role.ADMIN, "London", "+447000000001", "password");
        seed("provider@habesha.com", "Service Provider", Role.SERVICE_PROVIDER, "London", "+447000000002", "password");
        seed("user@habesha.com", "Regular User", Role.USER, "Addis Ababa", "+251911000001", "password");
    }

    private void seed(String email, String name, Role role, String city, String phone, String rawPassword) {
        // These are internal/system accounts that can't receive a verification
        // email at their demo addresses, so they must be pre-verified to sign in.
        var existing = userRepository.findByEmail(email);
        if (existing.isPresent()) {
            // Repair already-seeded accounts that were created before this change.
            User u = existing.get();
            if (!Boolean.TRUE.equals(u.getEmailVerified())) {
                u.setEmailVerified(true);
                userRepository.save(u);
            }
            return;
        }

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
                .emailVerified(true) // pre-verified system account (no real inbox)
                .build();

        userRepository.save(u);
    }
}
