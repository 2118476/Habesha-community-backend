package com.habesha.community.service;

import com.habesha.community.dto.MessageRequest;
import com.habesha.community.dto.ThreadSummaryDto;
import com.habesha.community.model.Message;
import com.habesha.community.model.User;
import com.habesha.community.repository.MessageRepository;
import com.habesha.community.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    // OPTIONAL: if you don’t have Twilio wired, this won’t break the app.
    @Autowired(required = false)
    private TwilioService twilioService;

    /* ---------------- anti-abuse guardrails (tune as needed) -------------- */
    private static final int MAX_PER_MINUTE = 40;       // per sender
    private static final int MAX_PER_5S = 10;           // burst limit
    private static final int DEDUPE_WINDOW_SECONDS = 10;
    private static final int MAX_MESSAGE_LENGTH = 2000;

    private final Map<Long, Deque<Long>> perSenderWindow = new ConcurrentHashMap<>();
    private final Map<String, Long> dedupeCache = new ConcurrentHashMap<>();

    /* -------------------------------- helpers ----------------------------- */

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new IllegalStateException("No current user"));
    }

    private String avatarUrlFor(User u) {
        if (u == null || u.getId() == null) return null;
        // If you store absolute URLs on the User entity, prefer that:
        // if (u.getProfileImageUrl() != null && !u.getProfileImageUrl().isBlank()) return u.getProfileImageUrl();
        return "/users/" + u.getId() + "/profile-image";
    }

    private String normalizeContent(String s) {
        return s == null ? null : s.replaceAll("\\s+", " ").trim();
    }

    private void enforceRateLimits(long senderId) {
        long now = System.currentTimeMillis();
        Deque<Long> window = perSenderWindow.computeIfAbsent(senderId, k -> new ConcurrentLinkedDeque<>());

        long cutoff1m = now - 60_000L;
        while (true) {
            Long head = window.peekFirst();
            if (head == null || head >= cutoff1m) break;
            window.pollFirst();
        }
        long cutoff5s = now - 5_000L;
        int last5s = 0;
        for (Long t : window) if (t >= cutoff5s) last5s++;

        if (window.size() >= MAX_PER_MINUTE) {
            throw new IllegalStateException("Rate limit: too many messages this minute");
        }
        if (last5s >= MAX_PER_5S) {
            throw new IllegalStateException("Slow down: too many messages in a short burst");
        }
        window.addLast(now);
    }

    private void enforceDedupe(long senderId, long recipientId, String content) {
        if (content == null || content.isBlank()) return;
        long nowSec = System.currentTimeMillis() / 1000L;
        String key = senderId + ":" + recipientId + ":" + Integer.toHexString(content.hashCode());
        Long last = dedupeCache.get(key);
        if (last != null && (nowSec - last) < DEDUPE_WINDOW_SECONDS) {
            throw new IllegalStateException("Duplicate message detected; try again shortly");
        }
        dedupeCache.put(key, nowSec);
        if (dedupeCache.size() > 50_000) {
            dedupeCache.entrySet().removeIf(e -> (nowSec - e.getValue()) > (2L * DEDUPE_WINDOW_SECONDS));
        }
    }

    /* --------------------------------- API -------------------------------- */

    @Transactional
    public void sendMessage(MessageRequest request) {
        if (request == null || request.getRecipientId() == null)
            throw new IllegalArgumentException("Recipient is required");

        String content = normalizeContent(request.getContent());
        if (content == null || content.isBlank())
            throw new IllegalArgumentException("Message content is required");
        if (content.length() > MAX_MESSAGE_LENGTH)
            throw new IllegalArgumentException("Message too long (max " + MAX_MESSAGE_LENGTH + " chars)");

        User sender = getCurrentUser();
        if (sender.getId().equals(request.getRecipientId()))
            throw new IllegalArgumentException("You cannot message yourself");

        User recipient = userRepository.findById(request.getRecipientId())
                .orElseThrow(() -> new IllegalArgumentException("Recipient not found"));

        // ✅ OPEN DMs: no friendship checks.
        enforceRateLimits(sender.getId());
        enforceDedupe(sender.getId(), recipient.getId(), content);

        Message message = Message.builder()
                .sender(sender)
                .recipient(recipient)
                .content(content)
                .readByRecipient(false)
                .viaSms(Boolean.TRUE.equals(request.isViaSms()))
                .build();

        messageRepository.save(message);

        if (Boolean.TRUE.equals(request.isViaSms())
                && twilioService != null
                && recipient.getPhone() != null
                && !recipient.getPhone().isBlank()) {
            twilioService.sendSms(recipient.getPhone(), content);
        }
    }

    public List<Message> getConversation(Long otherUserId) {
        User current = getCurrentUser();
        User other = userRepository.findById(otherUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return messageRepository.findBySenderAndRecipientOrRecipientAndSenderOrderBySentAtAsc(
                current, other, current, other);
    }

    public long unreadCountForCurrentUser() {
        User me = getCurrentUser();
        return messageRepository.countByRecipient_IdAndReadByRecipientFalse(me.getId());
    }

    public List<MessageRepository.UnreadCountView> unreadSummaryForCurrentUser() {
        User me = getCurrentUser();
        return messageRepository.findUnreadCountsByRecipient(me.getId());
    }

    @Transactional
    public void markReadFromOther(Long otherUserId) {
        User me = getCurrentUser();
        messageRepository.markReadFromTo(otherUserId, me.getId());
    }

    public List<ThreadSummaryDto> getRecentThreads(int limit) {
        User current = getCurrentUser();
        Long currentId = current.getId();

        List<Message> messages = messageRepository.findRecentMessagesForUser(currentId);
        Map<Long, ThreadSummaryDto> summaries = new LinkedHashMap<>();

        for (Message m : messages) {
            if (summaries.size() >= Math.max(limit, 1)) break;

            User other = m.getSender().getId().equals(currentId) ? m.getRecipient() : m.getSender();
            Long otherId = other.getId();
            if (summaries.containsKey(otherId)) continue;

            String name = other.getName();
            if (name == null || name.isBlank()) {
                name = (other.getUsername() != null && !other.getUsername().isBlank())
                        ? other.getUsername() : other.getEmail();
            }

            ThreadSummaryDto dto = ThreadSummaryDto.builder()
                    .userId(otherId)
                    .userName(name)
                    .avatarUrl(avatarUrlFor(other))
                    .lastText(m.getContent())
                    .lastAt(m.getSentAt().atZone(ZoneId.systemDefault()).toInstant())
                    .unread(0L)
                    .build();

            summaries.put(otherId, dto);
        }

        List<MessageRepository.UnreadCountView> unread = messageRepository.findUnreadCountsByRecipient(currentId);
        Map<Long, Long> unreadMap = new HashMap<>();
        for (MessageRepository.UnreadCountView uv : unread) unreadMap.put(uv.getUserId(), uv.getCount());
        summaries.forEach((otherId, dto) -> {
            Long c = unreadMap.get(otherId);
            if (c != null) dto.setUnread(c);
        });

        return new ArrayList<>(summaries.values());
    }
}
