package com.habesha.community.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Initialises the Firebase Admin SDK when credentials are available, so the
 * app can send FCM push notifications. If no credentials are configured the
 * app still boots normally — push is simply disabled (see PushNotificationService).
 *
 * Provide credentials in EITHER of these ways:
 *  - FIREBASE_CREDENTIALS_JSON: the full service-account JSON as a string
 *    (easiest on Render — paste the JSON into an env var), OR
 *  - GOOGLE_APPLICATION_CREDENTIALS: a path to the service-account JSON file.
 */
@Configuration
@EnableAsync
public class FirebaseConfig {

    private static final Logger log = LoggerFactory.getLogger(FirebaseConfig.class);

    @Value("${firebase.credentials-json:}")
    private String credentialsJson;

    @PostConstruct
    public void init() {
        if (!FirebaseApp.getApps().isEmpty()) return; // already initialised

        try (InputStream creds = resolveCredentials()) {
            if (creds == null) {
                log.info("Firebase credentials not configured — push notifications disabled.");
                return;
            }
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(creds))
                    .build();
            FirebaseApp.initializeApp(options);
            log.info("Firebase initialised — push notifications enabled.");
        } catch (Exception e) {
            log.warn("Firebase initialisation failed — push notifications disabled: {}", e.getMessage());
        }
    }

    private InputStream resolveCredentials() throws Exception {
        if (credentialsJson != null && !credentialsJson.isBlank()) {
            return new ByteArrayInputStream(credentialsJson.getBytes(StandardCharsets.UTF_8));
        }
        String path = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
        if (path != null && !path.isBlank() && Files.exists(Path.of(path))) {
            return new FileInputStream(path);
        }
        return null;
    }
}
