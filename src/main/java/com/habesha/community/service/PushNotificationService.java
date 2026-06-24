package com.habesha.community.service;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.Notification;
import com.habesha.community.model.DeviceToken;
import com.habesha.community.repository.DeviceTokenRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Sends Firebase Cloud Messaging push notifications to a user's registered
 * devices. Completely best-effort: if Firebase isn't configured, or sending
 * fails, it logs and moves on — it never disrupts the calling flow (e.g.
 * sending a chat message). Invalid/expired tokens are pruned automatically.
 */
@Service
@RequiredArgsConstructor
public class PushNotificationService {

    private static final Logger log = LoggerFactory.getLogger(PushNotificationService.class);

    private final DeviceTokenRepository tokenRepository;

    /** True once the Firebase Admin SDK has been initialised with credentials. */
    public boolean isEnabled() {
        return !FirebaseApp.getApps().isEmpty();
    }

    /**
     * Push a notification to every device registered to {@code userId}.
     * Runs asynchronously so it never adds latency to the request that triggered it.
     */
    @Async
    public void sendToUser(Long userId, String title, String body, Map<String, String> data) {
        if (!isEnabled() || userId == null) return;

        List<DeviceToken> tokens = tokenRepository.findByUser_Id(userId);
        if (tokens.isEmpty()) return;

        for (DeviceToken dt : tokens) {
            try {
                com.google.firebase.messaging.Message message =
                        com.google.firebase.messaging.Message.builder()
                                .setToken(dt.getToken())
                                .setNotification(Notification.builder()
                                        .setTitle(title)
                                        .setBody(body)
                                        .build())
                                .putAllData(data != null ? data : Map.of())
                                .build();
                FirebaseMessaging.getInstance().send(message);
            } catch (FirebaseMessagingException e) {
                MessagingErrorCode code = e.getMessagingErrorCode();
                if (code == MessagingErrorCode.UNREGISTERED || code == MessagingErrorCode.INVALID_ARGUMENT) {
                    // Token is dead — drop it so we stop trying.
                    try {
                        tokenRepository.delete(dt);
                    } catch (Exception ignore) {
                        /* ignore */
                    }
                } else {
                    log.debug("FCM send failed for token (code {}): {}", code, e.getMessage());
                }
            } catch (Exception e) {
                log.debug("FCM send error: {}", e.getMessage());
            }
        }
    }
}
