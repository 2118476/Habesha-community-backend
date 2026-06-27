package com.habesha.community.service;

import com.habesha.community.dto.MessageRequest;
import com.habesha.community.model.Role;
import com.habesha.community.model.User;
import com.habesha.community.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Moderation actions for the Trust &amp; Safety console: remove a single piece
 * of content, suspend/reinstate a user (with a reason the user sees), and send
 * formal warnings. Every action is written to the audit log.
 */
@Service
@RequiredArgsConstructor
public class ModerationService {

    private final UserRepository userRepository;
    private final RentalRepository rentalRepository;
    private final ServiceOfferRepository serviceOfferRepository;
    private final EventRepository eventRepository;
    private final ClassifiedAdRepository classifiedAdRepository;
    private final TravelPostRepository travelPostRepository;
    private final HomeSwapRepository homeSwapRepository;
    private final ServiceReviewRepository serviceReviewRepository;
    private final MessageService messageService;
    private final AuditService auditService;

    private User currentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }

    /** Best-effort in-app notice (DM) to a user; never breaks the action. */
    private void notify(Long userId, String text) {
        try {
            MessageRequest msg = new MessageRequest();
            msg.setRecipientId(userId);
            msg.setContent(text);
            msg.setViaSms(false);
            messageService.sendMessage(msg);
        } catch (Exception ignore) {
            /* notice is best-effort */
        }
    }

    private boolean deleteIfExists(CrudRepository<?, Long> repo, Long id) {
        if (id == null || !repo.existsById(id)) return false;
        repo.deleteById(id);
        return true;
    }

    /** Remove one piece of content (admin/moderator only). */
    @Transactional
    public void takedown(String contentType, Long contentId, String reason) {
        User actor = currentUser();
        String type = contentType == null ? "" : contentType.trim().toUpperCase();
        boolean removed;
        switch (type) {
            case "RENTAL":   removed = deleteIfExists(rentalRepository, contentId); break;
            case "SERVICE":  removed = deleteIfExists(serviceOfferRepository, contentId); break;
            case "EVENT":    removed = deleteIfExists(eventRepository, contentId); break;
            case "AD":       removed = deleteIfExists(classifiedAdRepository, contentId); break;
            case "TRAVEL":   removed = deleteIfExists(travelPostRepository, contentId); break;
            case "HOMESWAP": removed = deleteIfExists(homeSwapRepository, contentId); break;
            case "REVIEW":   removed = deleteIfExists(serviceReviewRepository, contentId); break;
            default:
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot remove content type: " + contentType);
        }
        if (!removed) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, type + " #" + contentId + " not found");
        }
        auditService.record(actor, "CONTENT_REMOVED", type, contentId, reason);
    }

    /** Suspend (ban) a user with a reason — shown to them at login. */
    @Transactional
    public void suspend(Long userId, String reason) {
        User actor = currentUser();
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (u.getRole() == Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot suspend an admin");
        }
        String why = (reason == null || reason.isBlank())
                ? "Your account has been suspended for violating our community guidelines."
                : reason.trim();
        notify(userId, "⚠️ Your account has been suspended.\nReason: " + why);
        u.setActive(false);
        u.setSuspensionReason(why);
        userRepository.save(u);
        auditService.record(actor, "USER_SUSPENDED", "USER", userId, why);
    }

    /** Reinstate a suspended user. */
    @Transactional
    public void unsuspend(Long userId) {
        User actor = currentUser();
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        u.setActive(true);
        u.setSuspensionReason(null);
        userRepository.save(u);
        auditService.record(actor, "USER_UNSUSPENDED", "USER", userId, null);
        notify(userId, "✅ Your account has been reinstated. Welcome back.");
    }

    /** Send a formal warning to a user (they stay active). */
    @Transactional
    public void warn(Long userId, String message) {
        User actor = currentUser();
        userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        String text = (message == null || message.isBlank())
                ? "Please review our community guidelines."
                : message.trim();
        notify(userId, "⚠️ Warning from the UK Habesha team:\n" + text);
        auditService.record(actor, "USER_WARNED", "USER", userId, text);
    }
}
