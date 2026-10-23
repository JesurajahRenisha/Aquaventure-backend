package com.example.aquaventure.service.storage;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class LocalFileStorageService implements FileStorageService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("pdf", "jpg", "jpeg", "png");
    private static final long MAX_SIZE_BYTES = 10L * 1024 * 1024;

    private final Path baseDir;

    public LocalFileStorageService(@Value("${app.storage.local.base-dir:./uploads}") String baseDir) {
        this.baseDir = Path.of(baseDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.baseDir);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not create storage base directory: " + this.baseDir, e);
        }
    }

    @Override
    public StoredFile store(MultipartFile file, String category, String ownerId) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File must not be empty");
        }
        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new IllegalArgumentException("File exceeds the maximum allowed size of 10MB");
        }

        String originalFilename = sanitize(file.getOriginalFilename());
        String extension = extensionOf(originalFilename);
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new IllegalArgumentException("Unsupported file type: " + extension);
        }

        String storedName = UUID.randomUUID() + "-" + originalFilename;
        Path relative = Path.of(category, ownerId, storedName);
        Path target = baseDir.resolve(relative).normalize();

        if (!target.startsWith(baseDir)) {
            throw new IllegalArgumentException("Invalid storage path");
        }

        try {
            Files.createDirectories(target.getParent());
            file.transferTo(target);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to store file", e);
        }

        return new StoredFile(relative.toString().replace('\\', '/'), originalFilename, file.getSize());
    }

    public List<StoredFile> storeAll(List<MultipartFile> files, String category, String ownerId) {
        return files.stream().map(f -> store(f, category, ownerId)).toList();
    }

    @Override
    public Resource load(String storedPath) {
        Path target = baseDir.resolve(storedPath).normalize();
        if (!target.startsWith(baseDir)) {
            throw new IllegalArgumentException("Invalid storage path");
        }
        return new FileSystemResource(target);
    }

    @Override
    public void delete(String storedPath) {
        Path target = baseDir.resolve(storedPath).normalize();
        if (!target.startsWith(baseDir)) {
            throw new IllegalArgumentException("Invalid storage path");
        }
        try {
            Files.deleteIfExists(target);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to delete file", e);
        }
    }

    private String sanitize(String filename) {
        if (filename == null || filename.isBlank()) {
            return "file";
        }
        String name = Path.of(filename).getFileName().toString();
        return name.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private String extensionOf(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot >= 0 && dot < filename.length() - 1 ? filename.substring(dot + 1) : "";
    }
}
