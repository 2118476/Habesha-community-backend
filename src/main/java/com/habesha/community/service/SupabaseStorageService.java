package com.habesha.community.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;

/**
 * Uploads images to Supabase Storage (an S3-style object store) over its REST
 * API, so image bytes live on Supabase's CDN instead of bloating Postgres.
 *
 * <p>Configured via {@code supabase.url}, {@code supabase.service-key} and
 * {@code supabase.storage-bucket}. When the URL/key are not set, {@link #isEnabled()}
 * returns {@code false} and callers should fall back to their previous storage.
 *
 * <p>The target bucket is expected to be <b>public</b> so the URLs returned by
 * {@link #upload} are directly readable by browsers.
 */
@Slf4j
@Service
public class SupabaseStorageService {

    @Value("${supabase.url:}")
    private String supabaseUrl;

    @Value("${supabase.service-key:}")
    private String serviceKey;

    @Value("${supabase.storage-bucket:rental-photos}")
    private String bucket;

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    /** True when Supabase Storage is configured and should be used. */
    public boolean isEnabled() {
        return StringUtils.hasText(supabaseUrl) && StringUtils.hasText(serviceKey);
    }

    /**
     * Upload bytes and return the public URL of the stored object.
     *
     * @param folder      logical prefix, e.g. {@code "rental/123"}
     * @param filename    a human-ish filename (will be sanitised + made unique)
     * @param data        the file bytes
     * @param contentType MIME type, e.g. {@code "image/jpeg"}
     */
    public String upload(String folder, String filename, byte[] data, String contentType) {
        if (!isEnabled()) {
            throw new IllegalStateException("Supabase Storage is not configured");
        }
        String base = trimTrailingSlash(supabaseUrl);
        String objectPath = buildObjectPath(folder, filename);
        String uploadUrl = base + "/storage/v1/object/" + bucket + "/" + objectPath;

        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(uploadUrl))
                    .timeout(Duration.ofSeconds(30))
                    .header("Authorization", "Bearer " + serviceKey)
                    .header("Content-Type", StringUtils.hasText(contentType) ? contentType : "application/octet-stream")
                    .header("x-upsert", "true")
                    .header("cache-control", "max-age=31536000")
                    .POST(HttpRequest.BodyPublishers.ofByteArray(data))
                    .build();

            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (resp.statusCode() < 200 || resp.statusCode() >= 300) {
                throw new RuntimeException("Supabase upload failed (HTTP " + resp.statusCode() + "): " + resp.body());
            }
            // Public URL — readable directly by the browser when the bucket is public.
            return base + "/storage/v1/object/public/" + bucket + "/" + objectPath;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Supabase upload error: " + e.getMessage(), e);
        }
    }

    /** Best-effort delete of an object previously created via {@link #upload}. */
    public void deleteByPublicUrl(String publicUrl) {
        if (!isEnabled() || !StringUtils.hasText(publicUrl)) return;
        String marker = "/storage/v1/object/public/" + bucket + "/";
        int idx = publicUrl.indexOf(marker);
        if (idx < 0) return; // not one of our objects — nothing to do
        String objectPath = publicUrl.substring(idx + marker.length());
        String deleteUrl = trimTrailingSlash(supabaseUrl) + "/storage/v1/object/" + bucket + "/" + objectPath;
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(deleteUrl))
                    .timeout(Duration.ofSeconds(15))
                    .header("Authorization", "Bearer " + serviceKey)
                    .DELETE()
                    .build();
            http.send(req, HttpResponse.BodyHandlers.discarding());
        } catch (Exception e) {
            log.warn("Supabase delete failed for {}: {}", publicUrl, e.getMessage());
        }
    }

    private String buildObjectPath(String folder, String filename) {
        String f = (folder == null ? "" : folder.replaceAll("^/+", "").replaceAll("/+$", ""));
        String clean = StringUtils.hasText(filename) ? filename : "file";
        clean = clean.replaceAll("[^A-Za-z0-9._-]", "_");
        String name = UUID.randomUUID().toString().substring(0, 8) + "_" + clean;
        return f.isEmpty() ? name : f + "/" + name;
    }

    private String trimTrailingSlash(String s) {
        return s.endsWith("/") ? s.substring(0, s.length() - 1) : s;
    }
}
