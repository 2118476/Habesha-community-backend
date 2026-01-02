package com.habesha.community.service;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiskStorageService {

    /** Root folder on disk (relative or absolute). Default: "uploads" (next to your jar). */
    @Value("${app.uploads.root:uploads}")
    private String uploadsRoot;

    /** Public URL prefix used by the FE to load the file. Keep it in sync with WebMvc resource handler. */
    @Value("${app.uploads.public-prefix:/uploads}")
    private String publicPrefix;

    /** Max allowed size in bytes (default 15MB if not configured). */
    @Value("${app.uploads.max-bytes:15728640}")
    private long maxBytes;

    /** Allowed extensions (lowercase, without dot). */
    private static final Set<String> ALLOWED_EXT = Set.of("jpg", "jpeg", "png", "webp", "gif");

    /** Allowed MIME types. */
    private static final Set<String> ALLOWED_MIME = Set.of(
            MediaType.IMAGE_JPEG_VALUE,
            MediaType.IMAGE_PNG_VALUE,
            "image/webp",
            MediaType.IMAGE_GIF_VALUE
    );

    /**
     * Stores an image for a classified ad and returns metadata.
     * The file is written to: {uploadsRoot}/ads/{adId}/{generatedFilename}
     * The public URL is: {publicPrefix}/ads/{adId}/{generatedFilename}
     */
    public StoredImage storeForAd(Long adId, MultipartFile file) throws Exception {
        if (adId == null) {
            throw new IllegalArgumentException("adId is required");
        }
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        if (file.getSize() > maxBytes) {
            throw new IllegalArgumentException("File is too large (max " + maxBytes + " bytes)");
        }

        // Validate content type
        String contentType = safeContentType(file.getContentType());
        if (!isAllowedMime(contentType)) {
            throw new IllegalArgumentException("Unsupported image type: " + contentType);
        }

        // Build a safe filename
        String original = sanitize(file.getOriginalFilename());
        String ext = extensionOf(original);
        if (!StringUtils.hasText(ext)) {
            // derive from MIME if no extension present
            ext = extFromMime(contentType);
        }
        ext = ext.toLowerCase();

        if (!isAllowedExt(ext)) {
            throw new IllegalArgumentException("Unsupported file extension: ." + ext);
        }

        String base = stripExt(original);
        if (!StringUtils.hasText(base)) base = "image";
        String random = UUID.randomUUID().toString().replace("-", "");
        String filename = base + "_" + random + "." + ext;

        // Resolve destination
        Path root = Paths.get(uploadsRoot).normalize().toAbsolutePath();
        Path targetDir = root.resolve(Paths.get("ads", String.valueOf(adId))).normalize();

        System.out.println("Upload root: " + root);
        System.out.println("Target directory: " + targetDir);

        // Ensure the targetDir remains inside root (defense in depth)
        if (!targetDir.startsWith(root)) {
            throw new SecurityException("Invalid upload path");
        }

        Files.createDirectories(targetDir);
        System.out.println("Created directories: " + targetDir);

        Path destTmp = targetDir.resolve(filename + ".part");
        Path dest = targetDir.resolve(filename);
        
        System.out.println("Destination file: " + dest);

        // Write atomically: copy -> move
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, destTmp, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception ex) {
            safeDelete(destTmp);
            throw ex;
        }

        try {
            Files.move(destTmp, dest, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            // Fallback if FS doesn't support atomic move
            Files.move(destTmp, dest, StandardCopyOption.REPLACE_EXISTING);
        }

        // Read image dimensions (best-effort)
        Integer w = null, h = null;
        try {
            BufferedImage img = ImageIO.read(dest.toFile());
            if (img != null) { w = img.getWidth(); h = img.getHeight(); }
        } catch (Exception dimEx) {
            log.debug("Could not read image dimensions for {}", dest, dimEx);
        }

        StoredImage out = new StoredImage();
        out.setUrl(buildPublicUrlForAd(adId, filename));
        out.setPath(dest.toString());
        out.setFilename(filename);
        out.setContentType(contentType);
        out.setSizeBytes(file.getSize());
        out.setWidth(w);
        out.setHeight(h);
        out.setSavedAt(LocalDateTime.now());
        return out;
    }

    /**
     * Stores an image for a HomeSwap post and returns metadata.
     * The file is written to: {uploadsRoot}/homeswap/{homeSwapId}/{generatedFilename}
     * The public URL is: {publicPrefix}/homeswap/{homeSwapId}/{generatedFilename}
     */
    public StoredImage storeForHomeSwap(Long homeSwapId, MultipartFile file) throws Exception {
        if (homeSwapId == null) {
            throw new IllegalArgumentException("homeSwapId is required");
        }
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        if (file.getSize() > maxBytes) {
            throw new IllegalArgumentException("File is too large (max " + maxBytes + " bytes)");
        }

        // Validate content type
        String contentType = safeContentType(file.getContentType());
        if (!isAllowedMime(contentType)) {
            throw new IllegalArgumentException("Unsupported image type: " + contentType);
        }

        // Build a safe filename
        String original = sanitize(file.getOriginalFilename());
        String ext = extensionOf(original);
        if (!StringUtils.hasText(ext)) {
            // derive from MIME if no extension present
            ext = extFromMime(contentType);
        }
        ext = ext.toLowerCase();

        if (!isAllowedExt(ext)) {
            throw new IllegalArgumentException("Unsupported file extension: ." + ext);
        }

        String base = stripExt(original);
        if (!StringUtils.hasText(base)) base = "image";
        String random = UUID.randomUUID().toString().replace("-", "");
        String filename = base + "_" + random + "." + ext;

        // Resolve destination
        Path root = Paths.get(uploadsRoot).normalize().toAbsolutePath();
        Path targetDir = root.resolve(Paths.get("homeswap", String.valueOf(homeSwapId))).normalize();

        // Ensure the targetDir remains inside root (defense in depth)
        if (!targetDir.startsWith(root)) {
            throw new SecurityException("Invalid upload path");
        }

        Files.createDirectories(targetDir);

        Path destTmp = targetDir.resolve(filename + ".part");
        Path dest = targetDir.resolve(filename);

        // Write atomically: copy -> move
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, destTmp, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception ex) {
            safeDelete(destTmp);
            throw ex;
        }

        try {
            Files.move(destTmp, dest, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            // Fallback if FS doesn't support atomic move
            Files.move(destTmp, dest, StandardCopyOption.REPLACE_EXISTING);
        }

        // Read image dimensions (best-effort)
        Integer w = null, h = null;
        try {
            BufferedImage img = ImageIO.read(dest.toFile());
            if (img != null) { w = img.getWidth(); h = img.getHeight(); }
        } catch (Exception dimEx) {
            log.debug("Could not read image dimensions for {}", dest, dimEx);
        }

        StoredImage out = new StoredImage();
        out.setUrl(buildPublicUrl(homeSwapId, filename));
        out.setPath(dest.toString());
        out.setFilename(filename);
        out.setContentType(contentType);
        out.setSizeBytes(file.getSize());
        out.setWidth(w);
        out.setHeight(h);
        out.setSavedAt(LocalDateTime.now());
        return out;
    }

    /** Best-effort deletion of an absolute path on disk. */
    public void deletePath(String absolutePath) {
        if (!StringUtils.hasText(absolutePath)) return;
        try {
            Files.deleteIfExists(Paths.get(absolutePath));
        } catch (Exception ex) {
            log.debug("deletePath ignored error for {}: {}", absolutePath, ex.getMessage());
        }
    }

    /* ---- helpers ---- */

    private String buildPublicUrl(Long homeSwapId, String filename) {
        String prefix = publicPrefix.endsWith("/") ? publicPrefix.substring(0, publicPrefix.length() - 1) : publicPrefix;
        return prefix + "/homeswap/" + homeSwapId + "/" + filename;
    }

    private String buildPublicUrlForAd(Long adId, String filename) {
        String prefix = publicPrefix.endsWith("/") ? publicPrefix.substring(0, publicPrefix.length() - 1) : publicPrefix;
        return prefix + "/ads/" + adId + "/" + filename;
    }

    private static String sanitize(String name) {
        if (!StringUtils.hasText(name)) return "file";
        // remove directories + normalize
        name = Paths.get(name).getFileName().toString();
        // keep alnum, dot, underscore, dash – replace everything else with underscore
        return name.replaceAll("[^A-Za-z0-9._-]", "_");
    }

    private static String extensionOf(String name) {
        if (!StringUtils.hasText(name)) return "";
        int i = name.lastIndexOf('.');
        return (i >= 0 && i < name.length() - 1) ? name.substring(i + 1) : "";
    }

    private static String stripExt(String name) {
        if (!StringUtils.hasText(name)) return "";
        int i = name.lastIndexOf('.');
        return (i > 0) ? name.substring(0, i) : name;
    }

    private static String extFromMime(String mime) {
        if (mime == null) return "bin";
        return switch (mime) {
            case MediaType.IMAGE_JPEG_VALUE -> "jpg";
            case MediaType.IMAGE_PNG_VALUE -> "png";
            case "image/webp" -> "webp";
            case MediaType.IMAGE_GIF_VALUE -> "gif";
            default -> "bin";
        };
    }

    private static boolean isAllowedExt(String ext) {
        return StringUtils.hasText(ext) && ALLOWED_EXT.contains(ext.toLowerCase());
    }

    private static boolean isAllowedMime(String mime) {
        return StringUtils.hasText(mime) && ALLOWED_MIME.contains(mime.toLowerCase());
    }

    private static String safeContentType(String ct) {
        if (!StringUtils.hasText(ct)) return MediaType.APPLICATION_OCTET_STREAM_VALUE;
        // strip parameters like "; charset=binary"
        int semi = ct.indexOf(';');
        return semi > 0 ? ct.substring(0, semi).trim() : ct.trim();
    }

    private static void safeDelete(Path p) {
        if (p == null) return;
        try { Files.deleteIfExists(p); } catch (Exception ignore) {}
    }

    @Data
    public static class StoredImage {
        private String url;
        private String path;
        private String filename;
        private String contentType;
        private long sizeBytes;
        private Integer width;
        private Integer height;
        private LocalDateTime savedAt;
    }
}
