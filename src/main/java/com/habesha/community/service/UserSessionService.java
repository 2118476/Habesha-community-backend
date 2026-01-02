package com.habesha.community.service;

import com.habesha.community.dto.UserSessionDto;
import com.habesha.community.model.User;
import com.habesha.community.model.UserSession;
import com.habesha.community.repository.UserRepository;
import com.habesha.community.repository.UserSessionRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserSessionService {

    private final UserSessionRepository sessionRepo;
    private final UserRepository userRepo;
    private final HttpServletRequest request;

    private User getCurrentUserOrThrow() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) throw new IllegalStateException("Not authenticated");
        String email = auth.getName();
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found"));
    }

    /**
     * Get all active sessions for the current user
     */
    public List<UserSessionDto> getMyActiveSessions() {
        User me = getCurrentUserOrThrow();
        List<UserSession> sessions = sessionRepo.findByUser_IdOrderByLastSeenDesc(me.getId());
        
        // Get current session token from request (if available)
        String currentToken = extractTokenFromRequest();
        
        return sessions.stream()
                .filter(s -> !s.isExpired())
                .map(s -> toDto(s, currentToken))
                .collect(Collectors.toList());
    }

    /**
     * Sign out a specific session
     */
    public void signOutSession(Long sessionId) {
        User me = getCurrentUserOrThrow();
        UserSession session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));
        
        if (!session.getUser().getId().equals(me.getId())) {
            throw new IllegalStateException("Not your session");
        }
        
        sessionRepo.delete(session);
    }

    /**
     * Sign out all other sessions (keep current one)
     */
    public void signOutAllOtherSessions() {
        User me = getCurrentUserOrThrow();
        String currentToken = extractTokenFromRequest();
        
        if (currentToken != null) {
            Optional<UserSession> current = sessionRepo.findByToken(currentToken);
            if (current.isPresent()) {
                sessionRepo.deleteAllByUserIdExceptCurrent(me.getId(), current.get().getId());
                return;
            }
        }
        
        // If we can't identify current session, delete all
        sessionRepo.deleteAllByUserId(me.getId());
    }

    /**
     * Create or update a session (called during login/authentication)
     */
    public UserSession createOrUpdateSession(User user, String token) {
        Optional<UserSession> existing = sessionRepo.findByToken(token);
        
        if (existing.isPresent()) {
            UserSession session = existing.get();
            session.setLastSeen(LocalDateTime.now());
            session.setIp(getClientIp());
            session.setUserAgent(getUserAgent());
            return sessionRepo.save(session);
        }
        
        UserSession newSession = UserSession.builder()
                .user(user)
                .token(token)
                .device(detectDevice())
                .ip(getClientIp())
                .userAgent(getUserAgent())
                .expiresAt(LocalDateTime.now().plusDays(30))
                .build();
        
        return sessionRepo.save(newSession);
    }

    /**
     * Clean up expired sessions (runs daily)
     */
    @Scheduled(cron = "0 0 2 * * *")  // 2 AM daily
    public void cleanupExpiredSessions() {
        sessionRepo.deleteExpiredSessions(LocalDateTime.now());
    }

    // Helper methods
    
    private UserSessionDto toDto(UserSession session, String currentToken) {
        boolean isCurrent = currentToken != null && currentToken.equals(session.getToken());
        
        return UserSessionDto.builder()
                .id(session.getId())
                .device(session.getDevice())
                .ip(session.getIp())
                .createdAt(session.getCreatedAt())
                .lastSeen(session.getLastSeen())
                .expiresAt(session.getExpiresAt())
                .current(isCurrent)
                .build();
    }

    private String extractTokenFromRequest() {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    private String getClientIp() {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String getUserAgent() {
        String ua = request.getHeader("User-Agent");
        return ua != null && ua.length() > 500 ? ua.substring(0, 500) : ua;
    }

    private String detectDevice() {
        String ua = getUserAgent();
        if (ua == null) return "Unknown";
        
        ua = ua.toLowerCase();
        if (ua.contains("mobile") || ua.contains("android") || ua.contains("iphone")) {
            if (ua.contains("android")) return "Android";
            if (ua.contains("iphone") || ua.contains("ipad")) return "iOS";
            return "Mobile";
        }
        
        if (ua.contains("windows")) return "Windows";
        if (ua.contains("mac")) return "Mac";
        if (ua.contains("linux")) return "Linux";
        
        return "Desktop";
    }
}
