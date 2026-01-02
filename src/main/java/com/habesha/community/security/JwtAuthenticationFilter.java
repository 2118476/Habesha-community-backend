package com.habesha.community.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Spring Security filter that intercepts incoming requests and
 * validates any Bearer token present in the Authorization header.
 * When a valid token is found the corresponding user is loaded
 * and placed into the security context.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    // Inject the UserRepository so we can update the lastActiveAt
    // timestamp on each authenticated request.  This allows the
    // platform to track online users and recent activity.
    private final com.habesha.community.repository.UserRepository userRepository;
    private final com.habesha.community.repository.UserSessionRepository sessionRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;
        // Check if header is present and starts with Bearer
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        jwt = authHeader.substring(7);
        try {
            username = jwtService.extractUsername(jwt);
        } catch (Exception e) {
            // Invalid token
            filterChain.doFilter(request, response);
            return;
        }
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
            if (jwtService.isTokenValid(jwt, userDetails)) {
                
                // Validate session exists and is not expired
                var sessionOpt = sessionRepository.findByToken(jwt);
                if (sessionOpt.isEmpty() || sessionOpt.get().isExpired()) {
                    // Session revoked or expired - reject the token
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\":\"SESSION_REVOKED_OR_EXPIRED\"}");
                    return;
                }
                
                // Check if user account is frozen
                com.habesha.community.model.User user = (com.habesha.community.model.User) userDetails;
                if (Boolean.TRUE.equals(user.getFrozen())) {
                    // Allow only reactivation endpoint for frozen accounts
                    String requestURI = request.getRequestURI();
                    if (!"/api/users/me/reactivate".equals(requestURI) && 
                        !"/api/auth/logout".equals(requestURI) &&
                        !"/api/auth/me".equals(requestURI)) {
                        response.setStatus(423); // HTTP 423 LOCKED
                        response.setContentType("application/json");
                        response.getWriter().write("{\"error\":\"ACCOUNT_FROZEN\",\"message\":\"Account is frozen. Reactivate to continue.\"}");
                        return;
                    }
                }
                
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
                
                // Update the lastActiveAt timestamp and touch the session
                try {
                    user.setLastActiveAt(java.time.LocalDateTime.now());
                    userRepository.save(user);
                    
                    // Update session lastSeen
                    var session = sessionOpt.get();
                    session.setLastSeen(java.time.LocalDateTime.now());
                    session.setIp(getClientIp(request));
                    session.setUserAgent(getUserAgent(request));
                    sessionRepository.save(session);
                } catch (Exception ignore) {
                    // Not our custom user type, ignore updating last active timestamp
                }
            }
        }
        filterChain.doFilter(request, response);
    }
    
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String getUserAgent(HttpServletRequest request) {
        String ua = request.getHeader("User-Agent");
        return ua != null && ua.length() > 500 ? ua.substring(0, 500) : ua;
    }
}