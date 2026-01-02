package com.habesha.community.service;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class FileStorageService {

    public Path ensureDir(Path dir) throws IOException {
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }
        return dir;
    }

    public String safeFilename(String original) {
        String clean = StringUtils.hasText(original) ? Paths.get(original).getFileName().toString() : "upload";
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS"));
        return ts + "_" + clean.replaceAll("[\\s]+", "_");
    }

    public Path saveStream(Path dir, String filename, InputStream in) throws IOException {
        ensureDir(dir);
        Path target = dir.resolve(filename);
        Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        return target.toAbsolutePath();
    }
}
