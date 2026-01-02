package com.habesha.community.config;

import com.habesha.community.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Spring Security configuration with JWT auth and CORS configured
 * for Netlify frontend + local development.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;
    
    @Value("${app.cors.allowed-origin-patterns}")
    private String allowedOriginPatterns;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(auth -> auth
                // ----- Settings & Contact & Block endpoints -----
                .requestMatchers("/api/users/me/settings/**").authenticated()
                .requestMatchers("/contact/**").authenticated()
                .requestMatchers("/api/users/me/blocks/**").authenticated()

                // ----- CORS preflight -----
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // ----- Public / unauthenticated endpoints -----
                .requestMatchers(
                        "/auth/**",
                        "/login",
                        "/swagger-ui.html",
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/actuator/**"
                ).permitAll()

                .requestMatchers(HttpMethod.GET, "/health").permitAll()
                .requestMatchers("/payments/webhook").permitAll()

                // Public profile image read; delete requires auth
                .requestMatchers(HttpMethod.GET, "/users/*/profile-image").permitAll()
                .requestMatchers(HttpMethod.DELETE, "/users/me/profile-image").authenticated()

                // ---------- PUBLIC READ-ONLY CONTENT ----------
                // People can browse ads, rentals, services, events, travel, etc.
                .requestMatchers(HttpMethod.GET,
                        "/travel/**", "/api/travel/**",
                        "/rentals/**", "/api/rentals/**",
                        "/homeswap/**", "/api/home-swap/**",
                        "/services/**", "/api/services/**",
                        "/events/**", "/api/events/**",
                        "/ads/**", "/api/ads/**",
                        "/uploads/**",
                        "/homeswap/photos/**",
                        "/rentals/photos/**",
                        "/users/*"
                ).permitAll()

                // Debug endpoints (temporary)
                .requestMatchers(HttpMethod.POST, "/ads/debug/**").permitAll()

                // allow HEAD for existence checks used by search (HEAD /api/ads/{id}, /api/travel/{id}, etc.)
                .requestMatchers(HttpMethod.HEAD,
                        "/travel/**", "/api/travel/**",
                        "/rentals/**", "/api/rentals/**",
                        "/homeswap/**", "/api/home-swap/**",
                        "/services/**", "/api/services/**",
                        "/events/**", "/api/events/**",
                        "/ads/**", "/api/ads/**"
                ).permitAll()

                // Feed (dashboard / public feed data)
                .requestMatchers(HttpMethod.GET,
                        "/feed",
                        "/feed/**",
                        "/api/feed",
                        "/api/feed/**"
                ).permitAll()

                // ---------- COMMENTS ----------
                // Everyone can view comments on an ad:
                .requestMatchers(HttpMethod.GET, "/api/ads/*/comments").permitAll()
                // But writing comments / replies / deleting comments must be authenticated:
                .requestMatchers(HttpMethod.POST, "/api/ads/**").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/ad-comments/**").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/ad-comments/**").authenticated()

                // ---------- PHOTOS / RENTALS UPLOAD ----------
                .requestMatchers(HttpMethod.POST,
                        "/rentals/*/photos",
                        "/api/rentals/*/photos",
                        "/ads/*/photos",
                        "/api/ads/*/photos"
                ).authenticated()
                .requestMatchers(HttpMethod.DELETE,
                        "/rentals/*/photos",
                        "/api/rentals/*/photos",
                        "/ads/*/photos",
                        "/api/ads/*/photos"
                ).authenticated()

                // ---------- HOMESWAP WRITE ----------
                .requestMatchers(HttpMethod.POST,
                        "/homeswap/**",
                        "/api/home-swap/**"
                ).authenticated()
                .requestMatchers(HttpMethod.PUT,
                        "/homeswap/**",
                        "/api/home-swap/**"
                ).authenticated()
                .requestMatchers(HttpMethod.DELETE,
                        "/homeswap/**",
                        "/api/home-swap/**"
                ).authenticated()

                // ---------- ADS / TRAVEL WRITE ----------
                // create / edit / delete ads and travel listings
                .requestMatchers(HttpMethod.POST,
                        "/ads/**", "/api/ads/**",
                        "/travel/**", "/api/travel/**"
                ).authenticated()
                .requestMatchers(HttpMethod.PUT,
                        "/ads/**", "/api/ads/**",
                        "/travel/**", "/api/travel/**"
                ).authenticated()
                .requestMatchers(HttpMethod.DELETE,
                        "/ads/**", "/api/ads/**",
                        "/travel/**", "/api/travel/**"
                ).authenticated()

                // ---------- ROLE-BASED ADMIN/MOD ----------
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/mod/**").hasAnyRole("ADMIN", "MODERATOR")

                // ----- Everything else must be authenticated -----
                .anyRequest().authenticated()
            );

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config
    ) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * CORS: Environment-driven configuration for allowed origins.
     * Supports comma-separated patterns from ALLOWED_ORIGIN_PATTERNS env var.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Split comma-separated patterns and convert to list
        List<String> originPatterns = Arrays.asList(allowedOriginPatterns.split(","));
        config.setAllowedOriginPatterns(originPatterns);
        
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
